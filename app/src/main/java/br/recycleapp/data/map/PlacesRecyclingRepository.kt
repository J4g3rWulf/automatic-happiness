package br.recycleapp.data.map

import android.content.Context
import androidx.core.content.edit
import br.recycleapp.domain.map.IRecyclingPointRepository
import br.recycleapp.domain.map.PointType
import br.recycleapp.domain.map.RecyclingPoint
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.math.*

/**
 * Implementação do repositório de pontos de coleta seletiva.
 *
 * Fluxo:
 * 1. Verifica se há cache válido (mesmo raio + menos de 7 dias)
 * 2. Se sim → retorna dados cacheados sem chamar a API
 * 3. Se não → chama Places API Nearby Search → cacheia resultado
 *    Se a API retornar vazio → usa lista estática de fallback
 *
 * Cache geográfico: se o usuário estiver dentro de [CACHE_RADIUS_KM] km
 * da última busca, os dados em cache são reutilizados.
 *
 * @param context usado para inicializar o Places SDK e acessar SharedPreferences
 * @param apiKey  chave da API do Google Maps
 */
class PlacesRecyclingRepository(
    private val context: Context,
    private val apiKey: String
) : IRecyclingPointRepository {

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val placesClient by lazy {
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(context, apiKey)
        }
        Places.createClient(context)
    }

    // ── API pública ───────────────────────────────────────────────────────────

    override suspend fun getNearbyPoints(
        latitude: Double,
        longitude: Double
    ): List<RecyclingPoint> {
        getCachedPoints(latitude, longitude)?.let { return it }

        val apiPoints = fetchFromApi(latitude, longitude)
        val result    = apiPoints.ifEmpty { STATIC_FALLBACK }

        cacheProvider(result, latitude, longitude)
        return result
    }

    // ── Cache geográfico ──────────────────────────────────────────────────────

    private fun getCachedPoints(latitude: Double, longitude: Double): List<RecyclingPoint>? {
        val cachedLat  = prefs.getFloat(KEY_LAT, Float.MIN_VALUE).toDouble()
        val cachedLng  = prefs.getFloat(KEY_LNG, Float.MIN_VALUE).toDouble()
        val timestamp  = prefs.getLong(KEY_TIMESTAMP, 0L)
        val cachedJson = prefs.getString(KEY_POINTS, null)

        if (cachedJson == null || cachedLat == Double.MIN_VALUE) return null

        val expired = System.currentTimeMillis() - timestamp > CACHE_DURATION_MS
        val farAway = distanceKm(latitude, longitude, cachedLat, cachedLng) > CACHE_RADIUS_KM

        if (expired || farAway) return null

        return parsePointsFromJson(cachedJson)
    }

    private fun cacheProvider(points: List<RecyclingPoint>, latitude: Double, longitude: Double) {
        prefs.edit {
            putFloat(KEY_LAT, latitude.toFloat())
            putFloat(KEY_LNG, longitude.toFloat())
            putLong(KEY_TIMESTAMP, System.currentTimeMillis())
            putString(KEY_POINTS, serializePointsToJson(points))
        }
    }

    // ── Places API ────────────────────────────────────────────────────────────

    private suspend fun fetchFromApi(
        latitude: Double,
        longitude: Double
    ): List<RecyclingPoint> = withContext(Dispatchers.IO) {
        try {
            val center = com.google.android.gms.maps.model.LatLng(latitude, longitude)
            val circle = CircularBounds.newInstance(center, SEARCH_RADIUS_METERS)

            val fields = listOf(
                Place.Field.ID,
                Place.Field.DISPLAY_NAME,
                Place.Field.FORMATTED_ADDRESS,
                Place.Field.LOCATION
            )

            val request = SearchNearbyRequest.builder(circle, fields)
                .setIncludedTypes(listOf("recycling_center"))
                .setMaxResultCount(20)
                .build()

            suspendCancellableCoroutine { continuation ->
                placesClient.searchNearby(request)
                    .addOnSuccessListener { response ->
                        val points = response.places.mapNotNull { place ->
                            val location = place.location ?: return@mapNotNull null
                            RecyclingPoint(
                                id        = place.id ?: "",
                                name      = place.displayName ?: "Ponto de coleta",
                                address   = place.formattedAddress ?: "",
                                latitude  = location.latitude,
                                longitude = location.longitude
                            )
                        }
                        continuation.resume(points)
                    }
                    .addOnFailureListener {
                        continuation.resume(emptyList())
                    }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    // ── Serialização ──────────────────────────────────────────────────────────

    private fun serializePointsToJson(points: List<RecyclingPoint>): String {
        val array = JSONArray()
        points.forEach { point ->
            array.put(JSONObject().apply {
                put("id", point.id)
                put("name", point.name)
                put("address", point.address)
                put("lat", point.latitude)
                put("lng", point.longitude)
                put("type", point.type.name)
                put("materials", JSONArray(point.materials))
            })
        }
        return array.toString()
    }

    private fun parsePointsFromJson(json: String): List<RecyclingPoint> {
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { i ->
                val obj       = array.getJSONObject(i)
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
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    // ── Distância geográfica ──────────────────────────────────────────────────

    /**
     * Calcula a distância em km entre dois pontos usando a fórmula de Haversine.
     */
    private fun distanceKm(
        lat1: Double, lng1: Double,
        lat2: Double, lng2: Double
    ): Double {
        val r    = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a    = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2).pow(2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    // ── Constantes ────────────────────────────────────────────────────────────

    companion object {
        private const val PREFS_NAME           = "recycling_points_cache"
        private const val KEY_LAT              = "cache_lat"
        private const val KEY_LNG              = "cache_lng"
        private const val KEY_TIMESTAMP        = "cache_timestamp"
        private const val KEY_POINTS           = "cache_points"
        private const val CACHE_RADIUS_KM      = 5.0
        private const val CACHE_DURATION_MS    = 7 * 24 * 60 * 60 * 1_000L
        private const val SEARCH_RADIUS_METERS = 10_000.0

        // ── Lista estática de fallback ────────────────────────────────────────
        // Usada quando a Places API não retorna resultados.
        // Coordenadas verificadas manualmente via Google Maps.
        val STATIC_FALLBACK = listOf(

            // ── PEVs da Comlurb (funcionam 24h) ──────────────────────────────
            // Recebem: papel, plástico, vidro e metal
            RecyclingPoint(
                id        = "pev_bangu",
                name      = "PEV Bangu",
                address   = "Rua Roque Barbosa, 348 - Bangu",
                latitude  = -22.85046155285499,
                longitude = -43.46413468350324,
                materials = listOf("Papel", "Plástico", "Vidro", "Metal"),
                type      = PointType.PEV
            ),
            RecyclingPoint(
                id        = "pev_madureira",
                name      = "PEV Madureira",
                address   = "Sob o Viaduto Prefeito Negrão de Lima - Madureira",
                latitude  = -22.87516362916978,
                longitude = -43.33496706988172,
                materials = listOf("Papel", "Plástico", "Vidro", "Metal"),
                type      = PointType.PEV
            ),
            RecyclingPoint(
                id        = "pev_tijuca",
                name      = "PEV Tijuca",
                address   = "Rua Dr. Renato Rocco, 400 - Tijuca",
                latitude  = -22.927124968864515,
                longitude = -43.229079230075605,
                materials = listOf("Papel", "Plástico", "Vidro", "Metal"),
                type      = PointType.PEV
            ),

            // ── Ecoponto de exemplo (próximo ao PEV Bangu) ───────────────────
            // ATENÇÃO: localização fictícia — substituir por coordenada real
            RecyclingPoint(
                id        = "ecoponto_bangu_exemplo",
                name      = "Ecoponto Bangu (exemplo)",
                address   = "Próximo ao PEV Bangu - Bangu",
                latitude  = -22.852,
                longitude = -43.466,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis"),
                type      = PointType.ECOPONTO
            )
        )
    }
}