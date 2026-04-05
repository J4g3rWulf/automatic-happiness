package br.recycleapp.ui.components

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
import br.recycleapp.di.AppModule
import br.recycleapp.domain.map.PointType
import br.recycleapp.domain.map.RecyclingPoint
import br.recycleapp.ui.theme.PlaceholderLight
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull

// Localização padrão se o GPS não responder dentro do timeout
private val RIO_CENTER_GOOGLE = LatLng(-22.9068, -43.1729)

/**
 * Mapa Google Maps com a localização do usuário e os pontos de coleta
 * buscados via Places API (com cache geográfico).
 *
 * Permissão de localização já garantida pelo [RecycleMapCard] pai.
 *
 * @param onMarkerClick callback chamado quando o usuário toca num marcador
 */
@android.annotation.SuppressLint("MissingPermission")
@Composable
fun GoogleMapView(
    onMarkerClick: (RecyclingPoint) -> Unit = {}
) {
    val context = LocalContext.current

    var userLocation  by remember { mutableStateOf<LatLng?>(null) }
    var recyclingPoints by remember { mutableStateOf<List<RecyclingPoint>>(emptyList()) }
    var isLoading     by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // Obtém localização
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

        fusedClient.requestLocationUpdates(
            request,
            callback,
            android.os.Looper.getMainLooper()
        )

        val location = withTimeoutOrNull(10_000) { deferred.await() }
        fusedClient.removeLocationUpdates(callback)

        val center = location ?: RIO_CENTER_GOOGLE
        userLocation = center

        // Busca pontos de coleta via repositório (cache ou API)
        val repository = AppModule.provideRecyclingPointRepository(context)
        recyclingPoints = repository.getNearbyPoints(center.latitude, center.longitude)

        isLoading = false
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
            center         = userLocation ?: RIO_CENTER_GOOGLE,
            points         = recyclingPoints,
            onMarkerClick  = onMarkerClick
        )
    }
}

/**
 * Renderiza o GoogleMap com câmera centrada em [center] e marcadores dos pontos de coleta.
 */
@Composable
private fun GoogleMapContent(
    center: LatLng,
    points: List<RecyclingPoint>,
    onMarkerClick: (RecyclingPoint) -> Unit
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(center, 14f)
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
        points.forEach { point ->
            Marker(
                state   = MarkerState(
                    position = LatLng(point.latitude, point.longitude)
                ),
                title   = point.name,
                snippet = point.address,
                icon    = BitmapDescriptorFactory.defaultMarker(
                    BitmapDescriptorFactory.HUE_AZURE
                ),
                onClick = {
                    onMarkerClick(point)
                    false
                }
            )
        }
    }
}