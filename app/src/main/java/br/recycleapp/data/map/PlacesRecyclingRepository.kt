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
        private const val PREFS_NAME           = "recycling_points_cache_v2"
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

            // ── PEVs da Comlurb (funcionam 24h) ──────────────────────────────────────
            // Recebem: papel, plástico, vidro e metal
            // Coordenadas verificadas manualmente
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

            // ── Ecopontos da Comlurb ─────────────────────────────────────────────────
            // Recebem: lixo domiciliar, entulho, bens inservíveis e galhadas
            // Fonte: Comlurb / Prefeitura do Rio de Janeiro
            RecyclingPoint(
                id        = "ecoponto_arara",
                name      = "Ecoponto Arará",
                address   = "Rua Aloysio Amancio, s/n, Benfica (Favela do Arará)",
                latitude  = -22.88947222,
                longitude = -43.24230556,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_barreira_do_vasco",
                name      = "Ecoponto Barreira do Vasco",
                address   = "Rua Bela, sn, Vasco da Gama (São Cristóvão)",
                latitude  = -22.88697222,
                longitude = -43.22652778,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_fogueteiro",
                name      = "Ecoponto Fogueteiro",
                address   = "Rua Barão de Petrópolis, 786, Santa Teresa",
                latitude  = -22.93238889,
                longitude = -43.20138889,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_jupara",
                name      = "Ecoponto Jupará",
                address   = "Rua Jupará, Mangueira",
                latitude  = -22.90194444,
                longitude = -43.23625,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_mangueira",
                name      = "Ecoponto Mangueira",
                address   = "Rua Visconde de Niterói, 800, Mangueira",
                latitude  = -22.90536111,
                longitude = -43.23988889,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_mineira",
                name      = "Ecoponto Mineira",
                address   = "Rua Van Erven, 135, Catumbi",
                latitude  = -22.91794444,
                longitude = -43.19730556,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_providencia",
                name      = "Ecoponto Providência",
                address   = "Rua Barão da Gamboa, 24, Gamboa (Santo Cristo)",
                latitude  = -22.89836111,
                longitude = -43.19780556,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_sao_carlos",
                name      = "Ecoponto São Carlos",
                address   = "Rua São Diniz, S/N, Estácio",
                latitude  = -22.91566667,
                longitude = -43.20197222,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_vila_dos_sonhos",
                name      = "Ecoponto Vila dos Sonhos",
                address   = "No final da Rua Carmelita da Conceição, Caju (São Sebastião)",
                latitude  = -22.87741667,
                longitude = -43.22130556,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_acari",
                name      = "Ecoponto Acari",
                address   = "Rua Roberto Carlos, s/n, Acari",
                latitude  = -22.82144444,
                longitude = -43.34188889,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_agua_de_ouro",
                name      = "Ecoponto Água de Ouro",
                address   = "Rua General Cândido, 56, Inhaúma",
                latitude  = -22.88063889,
                longitude = -43.28088889,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_alemao",
                name      = "Ecoponto Alemão",
                address   = "Rua Nova, em frente a estação do Itararé, Complexo do Alemão",
                latitude  = -22.86152778,
                longitude = -43.27180556,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_av_dom_helder_camara_suipa",
                name      = "Ecoponto Av. Dom Helder Câmara - Suípa",
                address   = "Avenida Dom Helder Câmara, Jacarezinho (Jacaré)",
                latitude  = -22.88408333,
                longitude = -43.25366667,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_boogie_woogie",
                name      = "Ecoponto Boogie Woogie",
                address   = "Rua dos Monjolos, com Campo de São João, Pitangueiras",
                latitude  = -22.81727778,
                longitude = -43.1815,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_camarista_meier",
                name      = "Ecoponto Camarista Méier",
                address   = "Rua Camarista Méier, 850, Engenho de Dentro",
                latitude  = -22.91194444,
                longitude = -43.29744444,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_coelho_neto",
                name      = "Ecoponto Coelho Neto",
                address   = "Rua Aratangi, s/n, Colégio",
                latitude  = -22.83494444,
                longitude = -43.34063889,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_comunidade_do_guarda",
                name      = "Ecoponto Comunidade do Guarda",
                address   = "Rua Ministro Mavignier, Del Castilho",
                latitude  = -22.88136111,
                longitude = -43.28130556,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_costa_barros",
                name      = "Ecoponto Costa Barros",
                address   = "Estrada de Botafogo, próximo à UPA, Costa Barros",
                latitude  = -22.82138889,
                longitude = -43.36686111,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_fazendinha_campo_do_seu_ze",
                name      = "Ecoponto Fazendinha Campo do Seu Zé",
                address   = "Rua Austregésilo, s/n, Complexo do Alemão",
                latitude  = -22.86361111,
                longitude = -43.27911111,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_fazendinha_praca_da_paloma",
                name      = "Ecoponto Fazendinha Praça da Paloma",
                address   = "Rua Austregésilo, 311, Complexo do Alemão",
                latitude  = -22.86563889,
                longitude = -43.2775,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_guarabu",
                name      = "Ecoponto Guarabu",
                address   = "Rua Berna, lado oposto ao n°142, Jardim Carioca",
                latitude  = -22.80347222,
                longitude = -43.19452778,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_juramento",
                name      = "Ecoponto Juramento",
                address   = "Praça Cotigi, 200, Vicente de Carvalho",
                latitude  = -22.85672222,
                longitude = -43.31630556,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_kelson",
                name      = "Ecoponto Kelson",
                address   = "Rua Kelson, s/n, Penha Circular",
                latitude  = -22.82297222,
                longitude = -43.27475,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_madureira",
                name      = "Ecoponto Madureira",
                address   = "Rua João Pereira, 63, Madureira",
                latitude  = -22.87494444,
                longitude = -43.33461111,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_merindiba",
                name      = "Ecoponto Merindiba",
                address   = "Rua Jacupema, s/n próximo ao largo da Penha, Penha",
                latitude  = -22.84608333,
                longitude = -43.27777778,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_paim_pamplona",
                name      = "Ecoponto Paim Pamplona",
                address   = "Rua Paim Pamplona, Sampaio",
                latitude  = -22.89777778,
                longitude = -43.26083333,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_pantoja",
                name      = "Ecoponto Pantoja",
                address   = "Rua Pastor Martin Luther King Jr., s/n, Acari (Coelho Neto)",
                latitude  = -22.82897222,
                longitude = -43.34552778,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_para_pedro",
                name      = "Ecoponto Para-Pedro",
                address   = "Estrada da Pedreira, esquina com Travessa do Colégio, Colégio",
                latitude  = -22.83772222,
                longitude = -43.33525,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_parada_de_lucas",
                name      = "Ecoponto Parada de Lucas",
                address   = "Avenida Brasil, 14.000, Parada de Lucas",
                latitude  = -22.814,
                longitude = -43.29366667,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_paranapua",
                name      = "Ecoponto Paranapuã",
                address   = "Avenida Paranapuã, s/n, Tauá (Cova da Onça)",
                latitude  = -22.79594444,
                longitude = -43.17944444,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_parque_proletario",
                name      = "Ecoponto Parque Proletário",
                address   = "Praça São Lucas, 1 A, Penha",
                latitude  = -22.84669444,
                longitude = -43.28441667,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_parque_royal",
                name      = "Ecoponto Parque Royal",
                address   = "Estrada Governador Chagas Freitas, s/n, Portuguesa",
                latitude  = -22.79611111,
                longitude = -43.20905556,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_pedreira",
                name      = "Ecoponto Pedreira",
                address   = "Rua Pastor Martin Luther King Jr., 11537, Parque Colúmbia (Pavuna)",
                latitude  = -22.82188889,
                longitude = -43.35136111,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_piscinao_de_ramos",
                name      = "Ecoponto Piscinão de Ramos",
                address   = "Avenida Guanabara, s/n, Maré",
                latitude  = -22.83944444,
                longitude = -43.25283333,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_pixunas",
                name      = "Ecoponto Pixunas",
                address   = "Avenida Doutor Agenor de Almeida Loyola, Freguesia (Ilha)",
                latitude  = -22.78744444,
                longitude = -43.17486111,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_praca_do_amarelinho",
                name      = "Ecoponto Praça do Amarelinho",
                address   = "Avenida Brasil, 18500, Acari (Irajá / Amarelinho)",
                latitude  = -22.82647222,
                longitude = -43.33966667,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_praia_da_rosa",
                name      = "Ecoponto Praia da Rosa",
                address   = "Praia da Rosa, Tauá",
                latitude  = -22.79361111,
                longitude = -43.18777778,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_predinhos",
                name      = "Ecoponto Predinhos",
                address   = "Conjunto da Embratel, Manguinhos",
                latitude  = -22.88430556,
                longitude = -43.24477778,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_quatro_bicas",
                name      = "Ecoponto Quatro Bicas",
                address   = "Rua Paul Muller, com Rua Professor Otávio Freitas, Penha",
                latitude  = -22.84619444,
                longitude = -43.27961111,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_querosene",
                name      = "Ecoponto Querosene",
                address   = "Rua Maestro Arturo Tosacanini, Tauá",
                latitude  = -22.79841667,
                longitude = -43.18241667,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_sargento_silvio_hollenbach",
                name      = "Ecoponto Sargento Silvio Hollenbach",
                address   = "Rua Sargento Sílvio Hollenbach, Barros Filho (Fazenda Botafogo)",
                latitude  = -22.82633333,
                longitude = -43.36019444,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_thomas_coelho",
                name      = "Ecoponto Thomás Coelho",
                address   = "Avenida Pastor Martin Luther King Jr., Vicente de Carvalho",
                latitude  = -22.86077778,
                longitude = -43.30730556,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_tribo",
                name      = "Ecoponto Tribo",
                address   = "Rua Cabo Fleury, s/n, Cocotá (Tribo / Dendê)",
                latitude  = -22.80325,
                longitude = -43.18275,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_vigario_geral",
                name      = "Ecoponto Vigário Geral",
                address   = "Rua Doutor Adauto (entrada da comunidade), Vigário Geral",
                latitude  = -22.80330556,
                longitude = -43.30427778,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_vila_joaniza",
                name      = "Ecoponto Vila Joaniza",
                address   = "Estrada das Canárias, s/n, Galeão",
                latitude  = -22.81136111,
                longitude = -43.22791667,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_campo_grande_viaduto",
                name      = "Ecoponto Campo Grande",
                address   = "Avenida Maria Teresa, embaixo do viaduto de Campo Grande",
                latitude  = -22.90425,
                longitude = -43.56605556,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_campo_grande_rua",
                name      = "Ecoponto Campo Grande II",
                address   = "Rua Laudelino Campos com Rua Campo Grande",
                latitude  = -22.90302778,
                longitude = -43.56655556,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_catiri",
                name      = "Ecoponto Catiri",
                address   = "Rua Roque Barbosa, 390, Bangu",
                latitude  = -22.85066667,
                longitude = -43.46372222,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_cinco_marias",
                name      = "Ecoponto Cinco Marias",
                address   = "Estrada do Magarça, 8.487, Guaratiba",
                latitude  = -22.97838889,
                longitude = -43.63897222,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_estrada_de_urucania",
                name      = "Ecoponto Estrada de Urucânia",
                address   = "Rua Adalberto Mortati, s/n, Santa Cruz",
                latitude  = -22.91752778,
                longitude = -43.65425,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_jacques_ouriques",
                name      = "Ecoponto Jacques Ouriques",
                address   = "Rua Jacques Ouriques, Padre Miguel",
                latitude  = -22.86852778,
                longitude = -43.444,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_jardim_novo",
                name      = "Ecoponto Jardim Novo",
                address   = "Rua Salvador Sabaté, 237, Realengo",
                latitude  = -22.888,
                longitude = -43.418,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_morar_carioca_do_aco",
                name      = "Ecoponto Morar Carioca do Aço",
                address   = "Rua Nassapê, 120, Santa Cruz",
                latitude  = -22.93330556,
                longitude = -43.64980556,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_pedra_de_guaratiba",
                name      = "Ecoponto Pedra de Guaratiba",
                address   = "Rua Belchior da Fonseca, 267, Pedra de Guaratiba",
                latitude  = -22.99997222,
                longitude = -43.64194444,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_petrarca",
                name      = "Ecoponto Petrarca",
                address   = "Praça Petrarca - Avenida Brasil, 32.327, Bangu",
                latitude  = -22.85941667,
                longitude = -43.46136111,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_realengo",
                name      = "Ecoponto Realengo",
                address   = "Rua Bernardo Vasconcelos, 1746, Realengo",
                latitude  = -22.87522222,
                longitude = -43.43841667,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_sao_tome",
                name      = "Ecoponto São Tomé",
                address   = "Rua São Tomé, 171, Santa Cruz",
                latitude  = -22.92458333,
                longitude = -43.69466667,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_vagao",
                name      = "Ecoponto Vagão",
                address   = "Avenida Brasil com Rua Recife, Realengo",
                latitude  = -22.86408333,
                longitude = -43.43655556,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_vila_alianca",
                name      = "Ecoponto Vila Aliança",
                address   = "Rua Coronel Tamarindo, 1960, Bangu",
                latitude  = -22.87536111,
                longitude = -43.47377778,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_vila_jurema",
                name      = "Ecoponto Vila Jurema",
                address   = "Rua Itajaí, 249, Realengo",
                latitude  = -22.86313889,
                longitude = -43.43813889,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_vila_kennedy",
                name      = "Ecoponto Vila Kennedy",
                address   = "Avenida Brasil, 38.048, Bangu",
                latitude  = -22.85633333,
                longitude = -43.49577778,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_vila_vintem",
                name      = "Ecoponto Vila Vintém",
                address   = "Rua Santo Everardo, ao lado do CIEP, Padre Miguel",
                latitude  = -22.87411111,
                longitude = -43.4475,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_areal",
                name      = "Ecoponto Areal",
                address   = "Avenida Governador Leonel de Moura Brizola, Jacarepaguá",
                latitude  = -22.97691667,
                longitude = -43.33794444,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_ayrton_senna",
                name      = "Ecoponto Ayrton Senna",
                address   = "Avenida Ayrton Senna, ao lado do Assaí, Gardênia Azul",
                latitude  = -22.95866667,
                longitude = -43.35691667,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_beira_rio_pantanal",
                name      = "Ecoponto Beira Rio - Pantanal",
                address   = "Rua Célia Ribeiro da Silva Mendes, Recreio do Bandeirantes",
                latitude  = -23.00127778,
                longitude = -43.44208333,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_beira_rio_servidao_g7",
                name      = "Ecoponto Beira Rio - Servidão G7",
                address   = "Rua da Servidão - G7, Recreio dos Bandeirantes",
                latitude  = -23.00330556,
                longitude = -43.45097222,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_cdd_karate",
                name      = "Ecoponto CDD Karatê",
                address   = "Avenida Arroio Fundo, Jacarepaguá",
                latitude  = -22.95461111,
                longitude = -43.36561111,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_chacrinha",
                name      = "Ecoponto Chacrinha",
                address   = "Estrada Comandante Luis Souto, Praça Seca",
                latitude  = -22.9045,
                longitude = -43.3575,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_cidade_de_deus",
                name      = "Ecoponto Cidade de Deus",
                address   = "Avenida José de Arimatéia, Cidade de Deus",
                latitude  = -22.95119444,
                longitude = -43.36236111,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_conjunto_habitacional_bandeirantes",
                name      = "Ecoponto Conjunto Habitacional Bandeirantes",
                address   = "Estrada dos Bandeirantes, 11227, Vargem Pequena",
                latitude  = -22.98886111,
                longitude = -43.43344444,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_curva_do_pinheiro",
                name      = "Ecoponto Curva do Pinheiro",
                address   = "Estrada de Jacarepaguá, 4460, Jacarepaguá (Rio das Pedras)",
                latitude  = -22.96830556,
                longitude = -43.33583333,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_gilka_machado",
                name      = "Ecoponto Gilka Machado",
                address   = "Rua Gilka Machado, Recreio dos Bandeirantes (Terreirão)",
                latitude  = -23.02861111,
                longitude = -43.47341667,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_mont_serrat",
                name      = "Ecoponto Mont Serrat",
                address   = "Final da Rua Cláudio Jacoby, Vargem Pequena",
                latitude  = -22.97927778,
                longitude = -43.46422222,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_muzema",
                name      = "Ecoponto Muzema",
                address   = "Avenida Engenheiro Souza Filho com Estrada de Jacarepaguá, Itanhangá",
                latitude  = -22.98861111,
                longitude = -43.32347222,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_nova_esperanca",
                name      = "Ecoponto Nova Esperança",
                address   = "Rua Bocaiúva Cunha, s/n, Gardênia Azul",
                latitude  = -22.95744444,
                longitude = -43.35397222,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_novo_lar",
                name      = "Ecoponto Novo Lar",
                address   = "Avenida das Américas, 1900, Vargem Grande",
                latitude  = -23.01969444,
                longitude = -43.49877778,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_pinheiro",
                name      = "Ecoponto Pinheiro",
                address   = "Estrada de Jacarepaguá, 3.502, Jacarepaguá (Rio das Pedras)",
                latitude  = -22.97194444,
                longitude = -43.33244444,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_rio_das_pedras",
                name      = "Ecoponto Rio das Pedras",
                address   = "Avenida Engenheiro Souza Filho, Itanhangá",
                latitude  = -22.97808333,
                longitude = -43.33422222,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_santa_maria",
                name      = "Ecoponto Santa Maria",
                address   = "Ladeira Santa Maria, Jacarepaguá (Rio das Pedras)",
                latitude  = -22.91530556,
                longitude = -43.42180556,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_sertao",
                name      = "Ecoponto Sertão",
                address   = "Estrada do Sertão, 859, Jacarepaguá",
                latitude  = -22.96652778,
                longitude = -43.32372222,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_teixeira_brandao",
                name      = "Ecoponto Teixeira Brandão",
                address   = "Avenida Teixeira Brandão, Jacarepaguá",
                latitude  = -22.94813889,
                longitude = -43.39405556,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_terreirao",
                name      = "Ecoponto Terreirão",
                address   = "Avenida Guiomar Novaes, Recreio dos Bandeirantes",
                latitude  = -23.02461111,
                longitude = -43.48188889,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_via_light",
                name      = "Ecoponto Via Light",
                address   = "Via Light Rio das Pedras, Itanhangá",
                latitude  = -22.97644444,
                longitude = -43.33225,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_199",
                name      = "Ecoponto 199",
                address   = "Estrada da Gávea, 199, Rocinha",
                latitude  = -22.98575,
                longitude = -43.2435,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_biquinha_vidigal",
                name      = "Ecoponto Biquinha Vidigal",
                address   = "Avenida João Goulart, s/n, Vidigal",
                latitude  = -22.99597222,
                longitude = -43.24055556,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_borda_do_mato",
                name      = "Ecoponto Borda do Mato",
                address   = "Rua Borda do Mato, Grajaú",
                latitude  = -22.92855556,
                longitude = -43.26508333,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_chacara_do_ceu_vidigal",
                name      = "Ecoponto Chácara do Céu",
                address   = "Rua Aperana, Parque dois Irmãos, Vidigal",
                latitude  = -22.98994444,
                longitude = -43.23322222,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_chacara_do_ceu_borel",
                name      = "Ecoponto Chácara do Céu - Borel",
                address   = "Estrada da Caixa D'Água, 47, Tijuca",
                latitude  = -22.93541667,
                longitude = -43.2535,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_cruzeiro_do_sul",
                name      = "Ecoponto Cruzeiro do Sul",
                address   = "Rua Cruzeiro do Sul, 296, Catete (Comunidade Tavares Bastos)",
                latitude  = -22.92677778,
                longitude = -43.18208333,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_mata_machado",
                name      = "Ecoponto Mata Machado",
                address   = "Estrada de Furnas, Alto da Boa Vista (Comunidade de Furnas)",
                latitude  = -22.97475,
                longitude = -43.28591667,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_morro_do_cruz",
                name      = "Ecoponto Morro do Cruz",
                address   = "Rua Tenente Marques de Sousa, Andaraí",
                latitude  = -22.93075,
                longitude = -43.25055556,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_nova_divineia",
                name      = "Ecoponto Nova Divinéia",
                address   = "Rua Alfredo Pujol, na Comunidade Nova Divinéia, Grajaú",
                latitude  = -22.92841667,
                longitude = -43.26355556,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_pavao_pavaozinho",
                name      = "Ecoponto Pavão Pavãozinho",
                address   = "Estrada do Cantagalo, 80, Copacabana",
                latitude  = -22.981,
                longitude = -43.19663889,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_pedro_americo_santo_amaro",
                name      = "Ecoponto Pedro Américo (Santo Amaro)",
                address   = "Rua Pedro Américo, 77, Catete (Comunidade Santo Amaro)",
                latitude  = -22.92394444,
                longitude = -43.17816667,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_pedro_americo_715",
                name      = "Ecoponto Pedro Américo 715",
                address   = "Rua Pedro Américo, 715, Catete",
                latitude  = -22.92502778,
                longitude = -43.18286111,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_pereira_da_silva",
                name      = "Ecoponto Pereira da Silva",
                address   = "Final da Rua Pereira da Silva, Laranjeiras",
                latitude  = -22.93113889,
                longitude = -43.19158333,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_recanto_do_trovador",
                name      = "Ecoponto Recanto do Trovador",
                address   = "Rua Amando Albuquerque, 323, Vila Isabel (Morro dos Macacos)",
                latitude  = -22.91588889,
                longitude = -43.25941667,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_rocinha_pastor_almir",
                name      = "Ecoponto Rocinha - Pastor Almir",
                address   = "Estrada da Gávea, 486, Rocinha",
                latitude  = -22.98897222,
                longitude = -43.25077778,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_roupa_suja",
                name      = "Ecoponto Roupa Suja",
                address   = "Auto Estrada Lagoa Barra, saída do túnel Zuzu Angel, São Conrado",
                latitude  = -22.99233333,
                longitude = -43.24963889,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_rua_1",
                name      = "Ecoponto Rua 1",
                address   = "Estrada da Gávea com Rua 1, Rocinha",
                latitude  = -22.98669444,
                longitude = -43.24558333,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_sa_viana",
                name      = "Ecoponto Sá Viana",
                address   = "Rua Sá Viana, Grajaú",
                latitude  = -22.92913889,
                longitude = -43.26158333,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_salgueiro_i",
                name      = "Ecoponto Salgueiro I",
                address   = "Rua General Rocca, 99, Tijuca (Salgueiro)",
                latitude  = -22.92827778,
                longitude = -43.22822222,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_salgueiro_ii",
                name      = "Ecoponto Salgueiro II",
                address   = "Rua Francisco Garça, s/n, Tijuca (Salgueiro)",
                latitude  = -22.93066667,
                longitude = -43.22669444,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_santo_amaro",
                name      = "Ecoponto Santo Amaro",
                address   = "Rua Santo Amaro, em frente n°349, Catete",
                latitude  = -22.92397222,
                longitude = -43.18141667,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_sistema_lagunar",
                name      = "Ecoponto Sistema Lagunar",
                address   = "Avenida Borges de Medeiros, Lagoa (Próximo ao clube Piraquê)",
                latitude  = -22.96930556,
                longitude = -43.217,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_tavares_bastos",
                name      = "Ecoponto Tavares Bastos",
                address   = "Rua Tavares Bastos, Catete",
                latitude  = -22.92791667,
                longitude = -43.18319444,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_turano",
                name      = "Ecoponto Turano",
                address   = "Rua Joaquim Pizarro, 2, Tijuca",
                latitude  = -22.92244444,
                longitude = -43.21533333,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_umuarama",
                name      = "Ecoponto Umuarama",
                address   = "Rua Umuarama, Rocinha",
                latitude  = -22.98586111,
                longitude = -43.24238889,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_valao",
                name      = "Ecoponto Valão",
                address   = "Rua do Valão, Rocinha",
                latitude  = -22.99033333,
                longitude = -43.24886111,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_vila_verde",
                name      = "Ecoponto Vila Verde",
                address   = "Estrada da Gávea, 525, Rocinha",
                latitude  = -22.98961111,
                longitude = -43.25241667,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
            RecyclingPoint(
                id        = "ecoponto_vitoria_regia",
                name      = "Ecoponto Vitória Régia",
                address   = "Rua Vitória Régia, Lagoa",
                latitude  = -22.96483333,
                longitude = -43.19736111,
                materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
                type      = PointType.ECOPONTO
            ),
        )
    }
}