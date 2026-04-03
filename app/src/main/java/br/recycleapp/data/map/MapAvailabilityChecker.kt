package br.recycleapp.data.map

import android.content.Context
import androidx.core.content.edit
import br.recycleapp.BuildConfig
import br.recycleapp.domain.map.IMapAvailabilityChecker
import br.recycleapp.domain.map.MapProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * Verifica se o Google Maps está disponível fazendo uma chamada
 * leve à Maps Static API (imagem de 1×1px).
 *
 * O resultado é cacheado em SharedPreferences por [CACHE_DURATION_MS]
 * para evitar consumo desnecessário de cota a cada abertura de tela.
 *
 * Fluxo:
 *  - HTTP 200 → [MapProvider.GOOGLE]
 *  - Qualquer outro código ou exceção → [MapProvider.OSM]
 *
 * @param context usado para acessar SharedPreferences
 */
class MapAvailabilityChecker(
    private val context: Context
) : IMapAvailabilityChecker {

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // ── API pública ───────────────────────────────────────────────────────────

    override suspend fun getAvailableProvider(): MapProvider {
        getCachedProvider()?.let { return it }

        val provider = checkGoogleAvailability()
        cacheProvider(provider)
        return provider
    }

    // ── Cache ─────────────────────────────────────────────────────────────────

    private fun getCachedProvider(): MapProvider? {
        val timestamp = prefs.getLong(KEY_TIMESTAMP, 0L)
        val expired   = System.currentTimeMillis() - timestamp > CACHE_DURATION_MS
        if (expired) return null

        return when (prefs.getString(KEY_PROVIDER, null)) {
            MapProvider.GOOGLE.name -> MapProvider.GOOGLE
            MapProvider.OSM.name    -> MapProvider.OSM
            else                    -> null
        }
    }

    private fun cacheProvider(provider: MapProvider) {
        prefs.edit {
            putString(KEY_PROVIDER, provider.name)
            putLong(KEY_TIMESTAMP, System.currentTimeMillis())
        }
    }

    // ── Verificação de disponibilidade ────────────────────────────────────────

    private suspend fun checkGoogleAvailability(): MapProvider = withContext(Dispatchers.IO) {
        try {
            val url = URL(
                "https://maps.googleapis.com/maps/api/staticmap" +
                        "?center=0,0&zoom=1&size=1x1&key=${BuildConfig.MAPS_CHECKER_KEY}"
            )
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 5_000
                readTimeout    = 5_000
                requestMethod  = "GET"
            }
            val code = connection.responseCode
            connection.disconnect()

            if (code == HttpURLConnection.HTTP_OK) MapProvider.GOOGLE else MapProvider.OSM
        } catch (_: Exception) {
            MapProvider.OSM
        }
    }

    // ── Constantes ────────────────────────────────────────────────────────────

    companion object {
        private const val PREFS_NAME        = "map_availability_cache"
        private const val KEY_PROVIDER      = "map_provider"
        private const val KEY_TIMESTAMP     = "map_check_timestamp"
        private const val CACHE_DURATION_MS = 60 * 60 * 1_000L // 1 hora
    }
}