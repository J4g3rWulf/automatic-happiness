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
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull

// ── PEVs de coleta seletiva do Rio de Janeiro ─────────────────────────────────
// Mesmos pontos do OsmMapView - fonte única futuramente substituída por API
private data class GoogleRecyclingPoint(
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val snippet: String
)

private val PEVS_GOOGLE = listOf(

    // ── PEVs da Comlurb (funcionam 24h) ──────────────────────────────────────
    // Recebem: papel, plástico, vidro e metal

    GoogleRecyclingPoint(
        -22.85046155285499, -43.46413468350324,
        "PEV Bangu",
        "Rua Roque Barbosa, 348 - Bangu"
    ),
    GoogleRecyclingPoint(
        -22.87516362916978, -43.33496706988172,
        "PEV Madureira",
        "Sob o Viaduto Prefeito Negrão de Lima"
    ),
    GoogleRecyclingPoint(
        -22.927124968864515, -43.229079230075605,
        "PEV Tijuca",
        "Rua Dr. Renato Rocco, 400"
    ),
)

// Localização padrão se o GPS não responder dentro do timeout
private val RIO_CENTER_GOOGLE = LatLng(-22.9068, -43.1729)

/**
 * Mapa Google Maps com a localização do usuário e os PEVs de coleta seletiva.
 *
 * Obtém a localização via FusedLocationProvider antes de renderizar,
 * exibindo um indicador de carregamento enquanto aguarda.
 *
 * Permissão de localização já garantida pelo [RecycleMapCard] pai.
 */
@android.annotation.SuppressLint("MissingPermission")
@Composable
fun GoogleMapView() {
    val context = LocalContext.current

    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var locationReady by remember { mutableStateOf(false) }

    // Obtém localização antes de renderizar o mapa
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

        fusedClient.requestLocationUpdates(
            request,
            callback,
            android.os.Looper.getMainLooper()
        )

        val location = withTimeoutOrNull(10_000) { deferred.await() }
        fusedClient.removeLocationUpdates(callback)

        userLocation  = location ?: RIO_CENTER_GOOGLE
        locationReady = true
    }

    if (!locationReady) {
        // ── Loading enquanto obtém localização ────────────────────────
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
        // ── Mapa Google Maps ──────────────────────────────────────────
        GoogleMapContent(center = userLocation ?: RIO_CENTER_GOOGLE)
    }
}

/**
 * Renderiza o GoogleMap com câmera centrada em [center] e marcadores dos PEVs.
 * Separado do [GoogleMapView] para garantir que o mapa só é criado
 * uma vez, com a localização correta.
 *
 * @param center posição inicial da câmera
 */
@Composable
private fun GoogleMapContent(center: LatLng) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(center, 14f)
    }

    GoogleMap(
        modifier            = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties          = MapProperties(isMyLocationEnabled = true),
        uiSettings          = MapUiSettings(
            zoomControlsEnabled  = false,
            myLocationButtonEnabled = true
        )
    ) {
        // ── Marcadores dos PEVs ───────────────────────────────────────
        PEVS_GOOGLE.forEach { ponto ->
            Marker(
                state   = MarkerState(
                    position = LatLng(ponto.latitude, ponto.longitude)
                ),
                title   = ponto.name,
                snippet = ponto.snippet
            )
        }
    }
}