package br.recycleapp.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.scale
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import br.recycleapp.R
import br.recycleapp.di.AppModule
import br.recycleapp.domain.map.PointType
import br.recycleapp.domain.map.RecyclingPoint
import br.recycleapp.ui.theme.PlaceholderLight
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer
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
 * Usa RadiusMarkerClusterer (OSMBonusPack) para agrupar marcadores próximos,
 * evitando sobrecarga ao renderizar 100+ pontos simultaneamente.
 *
 * Permissão de localização já garantida pelo [RecycleMapCard] pai.
 *
 * @param toneColor     cor temática do material atual — usada nos clusters
 * @param onMarkerClick callback chamado quando o usuário toca num marcador
 */
@Composable
fun OsmMapView(
    toneColor: Color = Color(0xFF1565C0),
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
            toneColor      = toneColor,
            context        = context,
            lifecycleOwner = lifecycleOwner,
            onMarkerClick  = onMarkerClick
        )
    }
}

/**
 * Renderiza o MapView OSM com clustering via [RadiusMarkerClusterer] e filtros por tipo.
 */
@Composable
private fun OsmMapContent(
    startCenter: GeoPoint,
    points: List<RecyclingPoint>,
    toneColor: Color,
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

    var showPev         by remember { mutableStateOf(true) }
    var showEcoponto    by remember { mutableStateOf(true) }
    var showFilterSheet by remember { mutableStateOf(false) }


    val filteredPoints = remember(points, showPev, showEcoponto) {
        points.filter { point ->
            when (point.type) {
                PointType.PEV      -> showPev
                PointType.ECOPONTO -> showEcoponto
            }
        }
    }

    LaunchedEffect(mapView, filteredPoints) {
        val density  = context.resources.displayMetrics.density
        val widthPx  = (32 * density).toInt()
        val heightPx = (48 * density).toInt()

        val iconPev = withContext(Dispatchers.IO) {
            android.graphics.BitmapFactory
                .decodeResource(context.resources, R.drawable.pin_pev_rio)
                .scale(widthPx, heightPx)
                .toDrawable(context.resources)
        }

        val iconEcoponto = withContext(Dispatchers.IO) {
            android.graphics.BitmapFactory
                .decodeResource(context.resources, R.drawable.pin_ecoponto_rio)
                .scale(widthPx, heightPx)
                .toDrawable(context.resources)
        }

        mapView.overlays.clear()
        mapView.overlays.add(locationOverlay)

        val clusterer     = RadiusMarkerClusterer(context)
        val radiusPx      = (20 * density).toInt()
        val clusterBitmap = createBitmap(radiusPx * 2, radiusPx * 2)
        val canvas        = Canvas(clusterBitmap)
        val paint         = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = toneColor.toArgb() }
        canvas.drawCircle(radiusPx.toFloat(), radiusPx.toFloat(), radiusPx.toFloat(), paint)

        clusterer.setIcon(clusterBitmap)
        clusterer.mTextAnchorU = Marker.ANCHOR_CENTER
        clusterer.mTextAnchorV = Marker.ANCHOR_CENTER

        filteredPoints.forEach { point ->
            val marker = Marker(mapView).apply {
                position = GeoPoint(point.latitude, point.longitude)
                title    = point.name
                snippet  = point.address
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = when (point.type) {
                    PointType.PEV      -> iconPev
                    PointType.ECOPONTO -> iconEcoponto
                }
                setOnMarkerClickListener { _, _ ->
                    onMarkerClick(point)
                    true
                }
            }
            clusterer.add(marker)
        }

        mapView.overlays.add(clusterer)
        mapView.invalidate()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory  = { mapView },
            modifier = Modifier.fillMaxSize()
        )

        // ── Filtros ───────────────────────────────────────────────────
        MapFilterBar(
            toneColor    = toneColor,
            onOpenFilter = { showFilterSheet = true },
            modifier     = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )

        if (showFilterSheet) {
            MapFilterBottomSheet(
                showPev          = showPev,
                showEcoponto     = showEcoponto,
                toneColor        = toneColor,
                onTogglePev      = { showPev = !showPev },
                onToggleEcoponto = { showEcoponto = !showEcoponto },
                onDismiss        = { showFilterSheet = false }
            )
        }
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
}

// ── Helpers ───────────────────────────────────────────────────────────────────

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