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
 * Compatível com schema v2 (address e coordinates como objetos nested).
 *
 * @param context usado para acessar o SharedPreferences de persistência
 */
class FirestorePointsSource(private val context: Context) {

    private val db get() = Firebase.firestore

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // ── API pública ───────────────────────────────────────────────────────────

    suspend fun hasRemoteChanges(): Boolean {
        val remote = fetchRemoteTimestamp() ?: return false
        val hasChanges = remote != localTimestamp
        if (hasChanges) Log.d(TAG, "Mudança detectada no Firestore — invalidando cache geográfico")
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

    // ── Mapeamento Firestore → domínio (schema v2) ────────────────────────────

    @Suppress("UNCHECKED_CAST")
    private fun com.google.firebase.firestore.DocumentSnapshot.toRecyclingPoint(): RecyclingPoint {

        // address{} nested
        val addrObj      = get("address") as? Map<*, *>
        val street       = addrObj?.get("street")       as? String ?: ""
        val number       = addrObj?.get("number")       as? String ?: ""
        val neighborhood = addrObj?.get("neighborhood") as? String ?: ""
        val address = buildString {
            append(street)
            if (number.isNotEmpty()) append(", $number")
            if (neighborhood.isNotEmpty()) append(" — $neighborhood")
        }

        // coordinates{} nested
        val coordsObj = get("coordinates") as? Map<*, *>
        val latitude  = (coordsObj?.get("latitude")  as? Number)?.toDouble() ?: 0.0
        val longitude = (coordsObj?.get("longitude") as? Number)?.toDouble() ?: 0.0

        // schedule{} nested
        val scheduleObj      = get("schedule") as? Map<*, *>
        val scheduleWeekdays = scheduleObj?.get("weekdays") as? String ?: ""
        val scheduleSaturday = scheduleObj?.get("saturday") as? String ?: ""
        val scheduleSunday   = scheduleObj?.get("sunday")   as? String ?: ""

        // campos diretos
        val materials = (get("materials") as? List<*>)
            ?.filterIsInstance<String>() ?: emptyList()
        val benefits  = (get("benefits")  as? List<*>)
            ?.filterIsInstance<String>() ?: emptyList()

        val type = getString("type")
            ?.let { runCatching { PointType.valueOf(it) }.getOrDefault(PointType.PEV) }
            ?: PointType.PEV

        return RecyclingPoint(
            id               = id,
            name             = getString("name")             ?: "",
            subtitle         = getString("subtitle")         ?: "",
            address          = address,
            reference        = getString("reference")        ?: "",
            latitude         = latitude,
            longitude        = longitude,
            materials        = materials,
            type             = type,
            scheduleWeekdays = scheduleWeekdays,
            scheduleSaturday = scheduleSaturday,
            scheduleSunday   = scheduleSunday,
            benefitsProgram  = getString("benefitsProgram")  ?: "",
            benefits         = benefits,
        )
    }

    // ── Persistência last-known ───────────────────────────────────────────────

    private fun saveLastKnown(points: List<RecyclingPoint>, timestamp: String?) {
        try {
            val array = JSONArray()
            points.forEach { p ->
                array.put(JSONObject().apply {
                    put("id",               p.id)
                    put("name",             p.name)
                    put("subtitle",         p.subtitle)
                    put("address",          p.address)
                    put("reference",        p.reference)
                    put("lat",              p.latitude)
                    put("lng",              p.longitude)
                    put("type",             p.type.name)
                    put("materials",        JSONArray(p.materials))
                    put("scheduleWeekdays", p.scheduleWeekdays)
                    put("scheduleSaturday", p.scheduleSaturday)
                    put("scheduleSunday",   p.scheduleSunday)
                    put("benefitsProgram",  p.benefitsProgram)
                    put("benefits",         JSONArray(p.benefits))
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

                    val benefits = obj.optJSONArray("benefits")?.let { arr ->
                        (0 until arr.length()).map { arr.getString(it) }
                    } ?: emptyList()

                    val type = runCatching {
                        PointType.valueOf(obj.optString("type", PointType.PEV.name))
                    }.getOrDefault(PointType.PEV)

                    RecyclingPoint(
                        id               = obj.getString("id"),
                        name             = obj.getString("name"),
                        subtitle         = obj.optString("subtitle",         ""),
                        address          = obj.getString("address"),
                        reference        = obj.optString("reference",        ""),
                        latitude         = obj.getDouble("lat"),
                        longitude        = obj.getDouble("lng"),
                        materials        = materials,
                        type             = type,
                        scheduleWeekdays = obj.optString("scheduleWeekdays", ""),
                        scheduleSaturday = obj.optString("scheduleSaturday", ""),
                        scheduleSunday   = obj.optString("scheduleSunday",   ""),
                        benefitsProgram  = obj.optString("benefitsProgram",  ""),
                        benefits         = benefits,
                    )
                }.getOrNull()
            }
        } catch (_: Exception) {
            emptyList()
        }
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