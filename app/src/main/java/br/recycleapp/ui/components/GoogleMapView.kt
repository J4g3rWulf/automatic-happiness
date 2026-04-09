package br.recycleapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull

// Localização padrão se o GPS não responder dentro do timeout
private val RIO_CENTER_GOOGLE = LatLng(-22.9068, -43.1729)

/**
 * Mapa Google Maps com a localização do usuário e os pontos de coleta
 * buscados via Places API (com cache geográfico).
 *
 * Usa Marker Clustering para agrupar marcadores próximos, evitando
 * sobrecarga de memória ao renderizar 100+ pontos simultaneamente.
 *
 * Permissão de localização já garantida pelo [RecycleMapCard] pai.
 *
 * @param toneColor     cor temática do material atual — usada nos clusters e marcadores de PEV
 * @param onMarkerClick callback chamado quando o usuário toca num marcador individual
 */
@android.annotation.SuppressLint("MissingPermission")
@Composable
fun GoogleMapView(
    toneColor: Color = Color(0xFF1565C0),
    onMarkerClick: (RecyclingPoint) -> Unit = {}
) {
    val context = LocalContext.current

    var userLocation    by remember { mutableStateOf<LatLng?>(null) }
    var recyclingPoints by remember { mutableStateOf<List<RecyclingPoint>>(emptyList()) }
    var isLoading       by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val deferred    = CompletableDeferred<LatLng?>()

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 0)
            .setMaxUpdates(1)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.locations.firstOrNull() ?: result.lastLocation
                deferred.complete(loc?.let { LatLng(it.latitude, it.longitude) })
            }
        }

        fusedClient.requestLocationUpdates(request, callback, android.os.Looper.getMainLooper())

        val location    = withTimeoutOrNull(10_000) { deferred.await() }
        fusedClient.removeLocationUpdates(callback)

        val center      = location ?: RIO_CENTER_GOOGLE
        userLocation    = center
        val repository  = AppModule.provideRecyclingPointRepository(context)
        recyclingPoints = repository.getNearbyPoints(center.latitude, center.longitude)
        isLoading       = false
    }

    if (isLoading) {
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
        GoogleMapContent(
            center        = userLocation ?: RIO_CENTER_GOOGLE,
            points        = recyclingPoints,
            toneColor     = toneColor,
            onMarkerClick = onMarkerClick
        )
    }
}

/**
 * Renderiza o GoogleMap com clustering automático dos pontos de coleta.
 *
 * Marcadores próximos são agrupados em clusters numerados.
 * Ao aumentar o zoom, os clusters se dividem nos pins individuais.
 */
@OptIn(MapsComposeExperimentalApi::class)
@Composable
private fun GoogleMapContent(
    center: LatLng,
    points: List<RecyclingPoint>,
    toneColor: Color,
    onMarkerClick: (RecyclingPoint) -> Unit
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(center, 12f)
    }

    val clusterItems = remember(points) {
        points.map { RecyclingPointClusterItem(it) }
    }

    GoogleMap(
        modifier            = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties          = MapProperties(isMyLocationEnabled = true),
        uiSettings          = MapUiSettings(
            zoomControlsEnabled     = false,
            myLocationButtonEnabled = true
        )
    ) {
        Clustering(
            items = clusterItems,

            // ── Toque no cluster → apenas previne comportamento padrão ─
            onClusterClick = { false },

            // ── Toque no marcador individual → abre bottom sheet ───────
            onClusterItemClick = { item ->
                onMarkerClick(item.point)
                false
            },

            // ── Visual do cluster (bolha numerada) ────────────────────
            clusterContent = { cluster ->
                Box(
                    modifier         = Modifier
                        .size(40.dp)
                        .background(toneColor.copy(alpha = 0.85f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = cluster.size.toString(),
                        color      = Color.White,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },

            // ── Visual do marcador individual ─────────────────────────
            clusterItemContent = { item ->
                when (item.point.type) {
                    PointType.ECOPONTO -> androidx.compose.foundation.Image(
                        painter            = androidx.compose.ui.res.painterResource(R.drawable.pin_ecoponto_rio),
                        contentDescription = "Ecoponto",
                        modifier           = Modifier.size(width = 32.dp, height = 48.dp)
                    )
                    PointType.PEV -> androidx.compose.foundation.Image(
                        painter            = androidx.compose.ui.res.painterResource(R.drawable.pin_pev_rio),
                        contentDescription = "PEV",
                        modifier           = Modifier.size(width = 32.dp, height = 48.dp)
                    )
                }
            }
        )
    }
}