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
import br.recycleapp.di.AppModule
import br.recycleapp.domain.map.RecyclingPoint
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

// Localização padrão se o GPS não responder dentro do timeout
private val RIO_CENTER = GeoPoint(-22.9068, -43.1729)

/**
 * Mapa OpenStreetMap com a localização do usuário e os pontos de coleta
 * buscados via repositório (mesmo cache do GoogleMapView).
 *
 * Permissão de localização já garantida pelo [RecycleMapCard] pai.
 *
 * @param onMarkerClick callback chamado quando o usuário toca num marcador
 */
@Composable
fun OsmMapView(
    onMarkerClick: (RecyclingPoint) -> Unit = {}
) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var startCenter     by remember { mutableStateOf<GeoPoint?>(null) }
    var recyclingPoints by remember { mutableStateOf<List<RecyclingPoint>>(emptyList()) }

    LaunchedEffect(Unit) {
        val location = getUserLocation(context)
        val center   = location
            ?.let { GeoPoint(it.latitude, it.longitude) }
            ?: RIO_CENTER

        startCenter = center

        // Busca pontos de coleta via repositório (mesmo cache do GoogleMapView)
        val repository  = AppModule.provideRecyclingPointRepository(context)
        recyclingPoints = repository.getNearbyPoints(center.latitude, center.longitude)
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
            points         = recyclingPoints,
            context        = context,
            lifecycleOwner = lifecycleOwner,
            onMarkerClick  = onMarkerClick
        )
    }
}

/**
 * Renderiza o MapView OSM com marcadores dos pontos de coleta.
 */
@Composable
private fun OsmMapContent(
    startCenter: GeoPoint,
    points: List<RecyclingPoint>,
    context: Context,
    lifecycleOwner: LifecycleOwner,
    onMarkerClick: (RecyclingPoint) -> Unit
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

    LaunchedEffect(mapView, points) {
        mapView.overlays.clear()
        mapView.overlays.add(locationOverlay)

        points.forEach { point ->
            Marker(mapView).apply {
                position = GeoPoint(point.latitude, point.longitude)
                title    = point.name
                snippet  = point.address
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                setOnMarkerClickListener { _, _ ->
                    onMarkerClick(point)
                    true
                }
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
 * Obtém a localização do usuário de forma suspensa com timeout de 10 segundos.
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