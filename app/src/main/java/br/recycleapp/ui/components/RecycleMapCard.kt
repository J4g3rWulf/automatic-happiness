package br.recycleapp.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.preference.PreferenceManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import br.recycleapp.ui.theme.PlaceholderLight
import br.recycleapp.ui.theme.TextSecondary
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File

// ── Ecopontos do Rio de Janeiro ───────────────────────────────────────────────
// Coordenadas verificadas via Google Maps / Prefeitura do Rio.
// Futuramente: substituir por chamada à API de pontos de coleta.
private data class RecyclingPoint(
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val description: String
)

private val ECOPONTOS_RIO = listOf(
    RecyclingPoint(-22.9213, -43.2197, "Ecoponto Tijuca",         "Ponto de coleta seletiva"),
    RecyclingPoint(-22.9583, -43.3658, "Ecoponto Jacarepaguá",    "Ponto de coleta seletiva"),
    RecyclingPoint(-23.0120, -43.3650, "Ecoponto Barra da Tijuca","Ponto de coleta seletiva"),
    RecyclingPoint(-23.0203, -43.4639, "Ecoponto Recreio",        "Ponto de coleta seletiva"),
    RecyclingPoint(-22.8758, -43.4742, "Ecoponto Bangu",          "Ponto de coleta seletiva"),
    RecyclingPoint(-22.9025, -43.5600, "Ecoponto Campo Grande",   "Ponto de coleta seletiva"),
    RecyclingPoint(-22.9164, -43.6983, "Ecoponto Santa Cruz",     "Ponto de coleta seletiva"),
    RecyclingPoint(-22.8742, -43.4286, "Ecoponto Realengo",       "Ponto de coleta seletiva"),
)

// Localização padrão enquanto o GPS não responde
private val RIO_CENTER = GeoPoint(-22.9068, -43.1729)

/**
 * Card com mapa OpenStreetMap exibindo a localização do usuário
 * e os ecopontos mais próximos do Rio de Janeiro.
 *
 * Solicita permissão de localização ao ser exibido pela primeira vez.
 * Se negada, exibe um placeholder informativo.
 *
 * @param toneColor cor temática do material atual (usada no placeholder)
 */
@Composable
fun RecycleMapCard(
    toneColor: Color,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var permissionGranted by remember {
        mutableStateOf(context.hasLocationPermission())
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionGranted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!permissionGranted) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        if (permissionGranted) {
            OsmMapView()
        } else {
            MapPermissionPlaceholder(toneColor = toneColor)
        }
    }
}

// ── Mapa OSM ──────────────────────────────────────────────────────────────────

@Composable
private fun OsmMapView() {
    val context      = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Configura o osmdroid uma única vez
    // Usa o cacheDir do app - sem necessidade de WRITE_EXTERNAL_STORAGE
    remember {
        Configuration.getInstance().apply {
            load(context, PreferenceManager.getDefaultSharedPreferences(context))
            userAgentValue  = context.packageName
            osmdroidTileCache = File(context.cacheDir, "osmdroid")
        }
    }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            isTilesScaledToDpi = true
            controller.setZoom(12.0)
            controller.setCenter(RIO_CENTER)
        }
    }

    // Overlay de localização do usuário - mostra o ponto azul no mapa
    val locationOverlay = remember {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply {
            enableMyLocation()
            // Quando o GPS responder pela primeira vez, centraliza e aumenta o zoom
            runOnFirstFix {
                mapView.post {
                    mapView.controller.animateTo(myLocation)
                    mapView.controller.setZoom(14.0)
                }
            }
        }
    }

    // Adiciona overlays uma única vez
    LaunchedEffect(mapView) {
        mapView.overlays.add(locationOverlay)

        ECOPONTOS_RIO.forEach { ponto ->
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

    // Gerencia o ciclo de vida do MapView corretamente no Compose
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

// ── Placeholder quando permissão negada ───────────────────────────────────────

@Composable
private fun MapPermissionPlaceholder(toneColor: Color) {
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(PlaceholderLight),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(horizontal = 24.dp)
        ) {
            Icon(
                imageVector        = Icons.Filled.LocationOff,
                contentDescription = null,
                tint               = toneColor,
                modifier           = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text      = "Permita o acesso à localização\npara ver ecopontos próximos",
                color     = TextSecondary,
                fontSize  = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Extension helper ──────────────────────────────────────────────────────────

private fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED