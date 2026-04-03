package br.recycleapp.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import br.recycleapp.ui.theme.PlaceholderLight
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File

// ── PEVs de coleta seletiva do Rio de Janeiro ─────────────────────────────────
// Coordenadas verificadas via Google Maps / Portal 1746.rio / Recicloteca.org.br
// Futuramente: substituir por chamada à API de pontos de coleta.
private data class RecyclingPoint(
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val description: String
)

private val PEVS_COLETA_SELETIVA_RIO = listOf(

    // ── PEVs da Comlurb (funcionam 24h) ──────────────────────────────────────
    // Recebem: papel, plástico, vidro e metal
    // Fonte: Portal 1746.rio / Recicloteca.org.br

    RecyclingPoint(
        -22.85046155285499, -43.46413468350324,
        "PEV Bangu",
        "Rua Roque Barbosa, 348 - Bangu"
    ),
    RecyclingPoint(
        -22.87516362916978, -43.33496706988172,
        "PEV Madureira",
        "Sob o Viaduto Prefeito Negrão de Lima"
    ),
    RecyclingPoint(
        -22.927124968864515, -43.229079230075605,
        "PEV Tijuca",
        "Rua Dr. Renato Rocco, 400"
    ),

    // ── Outros pontos de coleta seletiva ─────────────────────────────────────

)

// Localização padrão se o GPS não responder dentro do timeout
private val RIO_CENTER = GeoPoint(-22.9068, -43.1729)

/**
 * Mapa OpenStreetMap com a localização do usuário e os PEVs de coleta seletiva.
 *
 * Obtém a localização via FusedLocationProvider antes de renderizar,
 * exibindo um indicador de carregamento enquanto aguarda.
 *
 * Permissão de localização já garantida pelo [RecycleMapCard] pai.
 */
@Composable
fun OsmMapView() {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var startCenter by remember { mutableStateOf<GeoPoint?>(null) }

    LaunchedEffect(Unit) {
        val location = getUserLocation(context)
        startCenter  = location
            ?.let { GeoPoint(it.latitude, it.longitude) }
            ?: RIO_CENTER
    }

    if (startCenter == null) {
        Box(
            modifier         = Modifier
                .fillMaxSize()
                .background(PlaceholderLight),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color    = Color.Gray,
                modifier = Modifier.size(32.dp)
            )
        }
    } else {
        OsmMapContent(
            startCenter    = startCenter!!,
            context        = context,
            lifecycleOwner = lifecycleOwner
        )
    }
}

/**
 * Renderiza o MapView com o centro já definido.
 * Separado do [OsmMapView] para garantir que o MapView
 * só é criado uma vez, com a localização correta.
 */
@Composable
private fun OsmMapContent(
    startCenter: GeoPoint,
    context: Context,
    lifecycleOwner: LifecycleOwner
) {
    remember {
        Configuration.getInstance().apply {
            load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
            userAgentValue    = context.packageName
            osmdroidTileCache = File(context.cacheDir, "osmdroid")
        }
    }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            isTilesScaledToDpi = true
            controller.setZoom(14.0)
            controller.setCenter(startCenter)
        }
    }

    val locationOverlay = remember {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply {
            enableMyLocation()
        }
    }

    LaunchedEffect(mapView) {
        mapView.overlays.add(locationOverlay)

        PEVS_COLETA_SELETIVA_RIO.forEach { ponto ->
            Marker(mapView).apply {
                position = GeoPoint(ponto.latitude, ponto.longitude)
                title    = ponto.name
                snippet  = ponto.description
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                mapView.overlays.add(this)
            }
        }

        mapView.invalidate()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    mapView.onResume()
                    locationOverlay.enableMyLocation()
                }
                Lifecycle.Event.ON_PAUSE  -> {
                    mapView.onPause()
                    locationOverlay.disableMyLocation()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            locationOverlay.disableMyLocation()
            mapView.onDetach()
        }
    }

    AndroidView(
        factory  = { mapView },
        modifier = Modifier.fillMaxSize()
    )
}

// ── Helpers ───────────────────────────────────────────────────────────────────

/**
 * Obtém a localização do usuário de forma suspensa.
 * Força leitura fresca via requestLocationUpdates com timeout de 10 segundos.
 */
@android.annotation.SuppressLint("MissingPermission")
private suspend fun getUserLocation(context: Context): android.location.Location? {
    if (!context.hasLocationPermission()) return null
    return try {
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val deferred    = CompletableDeferred<android.location.Location?>()

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 0)
            .setMaxUpdates(1)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.locations.firstOrNull() ?: result.lastLocation
                deferred.complete(loc)
            }
        }

        fusedClient.requestLocationUpdates(
            request,
            callback,
            android.os.Looper.getMainLooper()
        )

        val location = withTimeoutOrNull(10_000) { deferred.await() }
        fusedClient.removeLocationUpdates(callback)
        location
    } catch (_: Exception) {
        null
    }
}