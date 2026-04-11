package br.recycleapp.data.map

import android.util.Log
import br.recycleapp.domain.map.PointType
import br.recycleapp.domain.map.RecyclingPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

/**
 * Fonte de dados remota que busca pontos de coleta no Cloud Firestore.
 *
 * Fluxo:
 * 1. Tenta buscar a coleção [COLLECTION] no Firestore
 * 2. Mapeia cada documento para [RecyclingPoint]
 * 3. Se a coleção estiver vazia ou ocorrer qualquer erro,
 *    retorna [RecyclingPointsData.ALL] como fallback offline
 */
class FirestorePointsSource {

    private val db = Firebase.firestore

    /**
     * Retorna todos os pontos de coleta cadastrados no Firestore.
     * Em caso de falha, retorna a lista estática embutida no app.
     */
    suspend fun getPoints(): List<RecyclingPoint> {
        return try {
            val snapshot = db.collection(COLLECTION).get().await()

            if (snapshot.isEmpty) {
                Log.w(TAG, "Coleção '$COLLECTION' vazia — usando fallback estático")
                return RecyclingPointsData.ALL
            }

            snapshot.documents.mapNotNull { doc ->
                runCatching { doc.toRecyclingPoint() }
                    .onFailure { Log.w(TAG, "Erro ao mapear documento ${doc.id}: $it") }
                    .getOrNull()
            }.ifEmpty {
                Log.w(TAG, "Nenhum documento mapeado com sucesso — usando fallback estático")
                RecyclingPointsData.ALL
            }

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar Firestore — usando fallback estático", e)
            RecyclingPointsData.ALL
        }
    }

    // ── Mapeamento ────────────────────────────────────────────────────────────

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
            name      = getString("name")    ?: "",
            address   = getString("address") ?: "",
            latitude  = getDouble("latitude")  ?: 0.0,
            longitude = getDouble("longitude") ?: 0.0,
            materials = materials,
            type      = type
        )
    }

    // ── Constantes ────────────────────────────────────────────────────────────

    private companion object {
        const val COLLECTION = "recycling_points"
        const val TAG        = "FirestorePointsSource"
    }
}