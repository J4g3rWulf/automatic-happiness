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
 * 1. Tenta buscar a coleção [COLLECTION] no Firestore
 * 2. Se bem-sucedido → persiste resultado em SharedPreferences e retorna
 * 3. Se falhar ou vazio → tenta carregar o último fetch salvo localmente
 * 4. Se não houver nada salvo → retorna [RecyclingPointsData.ALL] como último recurso
 *
 * Isso garante que, em modo offline com cache expirado, o app sempre exibe
 * os dados mais recentes que o Firestore conseguiu entregar alguma vez.
 *
 * @param context usado para acessar o SharedPreferences de persistência
 */
class FirestorePointsSource(private val context: Context) {

    private val db = Firebase.firestore

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // ── API pública ───────────────────────────────────────────────────────────

    /**
     * Retorna os pontos de coleta, priorizando dados frescos do Firestore.
     * Garante fallback progressivo: Firestore → último fetch salvo → lista estática.
     */
    suspend fun getPoints(): List<RecyclingPoint> {
        return try {
            val snapshot = db.collection(COLLECTION).get().await()

            if (snapshot.isEmpty) {
                Log.w(TAG, "Coleção '$COLLECTION' vazia — tentando último fetch salvo")
                return loadLastKnownOrFallback()
            }

            val points = snapshot.documents.mapNotNull { doc ->
                runCatching { doc.toRecyclingPoint() }
                    .onFailure { Log.w(TAG, "Erro ao mapear documento ${doc.id}: $it") }
                    .getOrNull()
            }

            if (points.isEmpty()) {
                Log.w(TAG, "Nenhum documento mapeado — tentando último fetch salvo")
                return loadLastKnownOrFallback()
            }

            // Firestore respondeu com sucesso → persiste para uso offline futuro
            saveLastKnown(points)
            points

        } catch (e: Exception) {
            Log.e(TAG, "Firestore indisponível — tentando último fetch salvo", e)
            loadLastKnownOrFallback()
        }
    }

    // ── Persistência "last known good" ────────────────────────────────────────

    /**
     * Salva a lista de pontos no SharedPreferences como "último fetch bem-sucedido".
     * Chamado sempre que o Firestore retorna dados válidos.
     */
    private fun saveLastKnown(points: List<RecyclingPoint>) {
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
            prefs.edit { putString(KEY_LAST_KNOWN, array.toString()) }
            Log.d(TAG, "Último fetch salvo com ${points.size} pontos")
        } catch (e: Exception) {
            Log.w(TAG, "Falha ao salvar último fetch: $e")
        }
    }

    /**
     * Carrega o último fetch bem-sucedido do Firestore salvo localmente.
     * Se não houver nada salvo, retorna [RecyclingPointsData.ALL] como último recurso.
     */
    private fun loadLastKnownOrFallback(): List<RecyclingPoint> {
        val json = prefs.getString(KEY_LAST_KNOWN, null)
        if (json != null) {
            val points = parsePointsFromJson(json)
            if (points.isNotEmpty()) {
                Log.d(TAG, "Usando último fetch salvo (${points.size} pontos)")
                return points
            }
        }
        Log.w(TAG, "Nenhum fetch anterior salvo — usando lista estática")
        return RecyclingPointsData.ALL
    }

    private fun parsePointsFromJson(json: String): List<RecyclingPoint> {
        return try {
            val array = JSONArray(json)
            (0 until array.length()).mapNotNull { i ->
                runCatching {
                    val obj = array.getJSONObject(i)
                    @Suppress("UNCHECKED_CAST")
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
        const val COLLECTION    = "recycling_points"
        const val PREFS_NAME    = "firestore_points_cache"
        const val KEY_LAST_KNOWN = "last_known_points"
        const val TAG           = "FirestorePointsSource"
    }
}