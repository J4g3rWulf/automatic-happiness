package br.recycleapp.data.map

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import br.recycleapp.domain.map.PointType
import br.recycleapp.domain.map.RecyclingPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

/**
 * Fonte de dados remota que busca pontos de coleta no Cloud Firestore.
 *
 * Fluxo de resolução:
 * 1. Busca `last_updated` em metadata/recycling_points (1 leitura)
 * 2. Compara com o timestamp salvo localmente
 * 3. Se igual → retorna last-known salvo (zero leituras extras)
 * 4. Se diferente → busca coleção completa, salva last-known e timestamp
 * 5. Se qualquer erro → tenta last-known salvo → fallback estático
 *
 * @param context usado para acessar o SharedPreferences de persistência
 */
class FirestorePointsSource(private val context: Context) {

    private val db get() = Firebase.firestore

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // ── API pública ───────────────────────────────────────────────────────────

    /**
     * Verifica em 1 leitura se o Firestore tem dados mais recentes que o cache local.
     * Retorna true se o timestamp remoto for diferente do salvo localmente.
     */
    suspend fun hasRemoteChanges(): Boolean {
        val remote = fetchRemoteTimestamp() ?: return false
        val hasChanges = remote != localTimestamp
        if (hasChanges) {
            Log.d(TAG, "Mudança detectada no Firestore — invalidando cache geográfico")
        }
        return hasChanges
    }

    suspend fun getPoints(): List<RecyclingPoint> {
        return try {
            val remoteTimestamp = fetchRemoteTimestamp()

            if (remoteTimestamp != null && remoteTimestamp == localTimestamp) {
                Log.d(TAG, "Dados sem alteração (timestamp igual) — usando last-known")
                return loadLastKnownOrFallback()
            }

            val points = fetchAllPoints()

            if (points.isEmpty()) {
                Log.w(TAG, "Nenhum ponto retornado — usando last-known")
                return loadLastKnownOrFallback()
            }

            saveLastKnown(points, remoteTimestamp)
            points

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar Firestore — usando last-known", e)
            loadLastKnownOrFallback()
        }
    }

    // ── Timestamp remoto ──────────────────────────────────────────────────────

    private suspend fun fetchRemoteTimestamp(): String? {
        return try {
            val doc = db.collection(METADATA_COLLECTION)
                .document(METADATA_DOCUMENT)
                .get()
                .await()
            doc.getString(FIELD_LAST_UPDATED).also {
                Log.d(TAG, "Timestamp remoto: $it")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Falha ao buscar timestamp remoto: $e")
            null
        }
    }

    private val localTimestamp: String?
        get() = prefs.getString(KEY_TIMESTAMP, null)

    // ── Busca completa dos pontos ─────────────────────────────────────────────

    private suspend fun fetchAllPoints(): List<RecyclingPoint> {
        val snapshot = db.collection(POINTS_COLLECTION).get().await()

        if (snapshot.isEmpty) {
            Log.w(TAG, "Coleção '$POINTS_COLLECTION' vazia")
            return emptyList()
        }

        return snapshot.documents.mapNotNull { doc ->
            runCatching { doc.toRecyclingPoint() }
                .onFailure { Log.w(TAG, "Erro ao mapear ${doc.id}: $it") }
                .getOrNull()
        }
    }

    // ── Persistência last-known ───────────────────────────────────────────────

    private fun saveLastKnown(points: List<RecyclingPoint>, timestamp: String?) {
        try {
            val array = JSONArray()
            points.forEach { point ->
                array.put(JSONObject().apply {
                    put("id",        point.id)
                    put("name",      point.name)
                    put("address",   point.address)
                    put("lat",       point.latitude)
                    put("lng",       point.longitude)
                    put("type",      point.type.name)
                    put("materials", JSONArray(point.materials))
                })
            }
            prefs.edit {
                putString(KEY_LAST_KNOWN, array.toString())
                putString(KEY_TIMESTAMP, timestamp)
            }
            Log.d(TAG, "Last-known salvo: ${points.size} pontos, timestamp: $timestamp")
        } catch (e: Exception) {
            Log.w(TAG, "Falha ao salvar last-known: $e")
        }
    }

    private fun loadLastKnownOrFallback(): List<RecyclingPoint> {
        val json = prefs.getString(KEY_LAST_KNOWN, null)
        if (json != null) {
            val points = parsePointsFromJson(json)
            if (points.isNotEmpty()) {
                Log.d(TAG, "Usando last-known (${points.size} pontos)")
                return points
            }
        }
        Log.w(TAG, "Sem last-known — usando lista estática")
        return RecyclingPointsData.ALL
    }

    private fun parsePointsFromJson(json: String): List<RecyclingPoint> {
        return try {
            val array = JSONArray(json)
            (0 until array.length()).mapNotNull { i ->
                runCatching {
                    val obj = array.getJSONObject(i)
                    val materials = obj.optJSONArray("materials")?.let { arr ->
                        (0 until arr.length()).map { arr.getString(it) }
                    } ?: emptyList()
                    val type = runCatching {
                        PointType.valueOf(obj.optString("type", PointType.PEV.name))
                    }.getOrDefault(PointType.PEV)

                    RecyclingPoint(
                        id        = obj.getString("id"),
                        name      = obj.getString("name"),
                        address   = obj.getString("address"),
                        latitude  = obj.getDouble("lat"),
                        longitude = obj.getDouble("lng"),
                        materials = materials,
                        type      = type
                    )
                }.getOrNull()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    // ── Mapeamento Firestore → domínio ────────────────────────────────────────

    private fun com.google.firebase.firestore.DocumentSnapshot.toRecyclingPoint(): RecyclingPoint {
        @Suppress("UNCHECKED_CAST")
        val materials = (get("materials") as? List<*>)
            ?.filterIsInstance<String>()
            ?: emptyList()

        val type = getString("type")
            ?.let { runCatching { PointType.valueOf(it) }.getOrDefault(PointType.PEV) }
            ?: PointType.PEV

        return RecyclingPoint(
            id        = id,
            name      = getString("name")      ?: "",
            address   = getString("address")   ?: "",
            latitude  = getDouble("latitude")  ?: 0.0,
            longitude = getDouble("longitude") ?: 0.0,
            materials = materials,
            type      = type
        )
    }

    // ── Constantes ────────────────────────────────────────────────────────────

    private companion object {
        const val POINTS_COLLECTION   = "recycling_points"
        const val METADATA_COLLECTION = "metadata"
        const val METADATA_DOCUMENT   = "recycling_points"
        const val FIELD_LAST_UPDATED  = "last_updated"
        const val PREFS_NAME          = "firestore_points_cache"
        const val KEY_LAST_KNOWN      = "last_known_points"
        const val KEY_TIMESTAMP       = "last_updated_timestamp"
        const val TAG                 = "FirestorePointsSource"
    }
}