package br.recycleapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import br.recycleapp.di.AppModule
import br.recycleapp.domain.map.PointType
import br.recycleapp.domain.map.RecyclingPoint
import br.recycleapp.domain.map.toPinDrawable
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

private val RIO_CENTER_GOOGLE = LatLng(-22.9068, -43.1729)

/**
 * Ordem de exibição dos materiais no filtro e no carrossel do bottom sheet.
 * Materiais não listados aqui aparecem ao final, em ordem de inserção.
 */
private val MATERIAL_PRIORITY_ORDER = listOf(
    "Vidro", "Plástico", "Papel", "Metal",
    "Óleo vegetal", "Pilhas e baterias", "Eletrônicos",
    "Pneus", "Orgânicos", "Galhadas", "Lixo domiciliar",
    "Entulho", "Bens inservíveis"
)

/**
 * Mapa Google Maps com a localização do usuário e os pontos de coleta
 * buscados via Places API (com cache geográfico).
 *
 * Usa Marker Clustering para agrupar marcadores próximos, evitando
 * sobrecarga de memória ao renderizar 100+ pontos simultaneamente.
 *
 * Permissão de localização já garantida pelo [RecycleMapCard] pai.
 *
 * @param toneColor     cor temática do material atual — usada nos clusters
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
 * Renderiza o GoogleMap com clustering automático e filtros duplos:
 * por tipo de ponto e por material aceito.
 *
 * O estado de visibilidade de tipos é um Map<PointType, Boolean> — novos tipos
 * aparecem automaticamente no filtro sem alterar este arquivo.
 *
 * O estado de visibilidade de materiais é derivado dinamicamente dos pontos
 * carregados: apenas materiais presentes em ao menos um ponto aparecem no filtro,
 * ordenados conforme [MATERIAL_PRIORITY_ORDER].
 *
 * Lógica de filtro: um ponto é exibido se seu tipo está visível E (não declara
 * materiais OU ao menos um de seus materiais está visível).
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

    // ── Filtro por tipo ───────────────────────────────────────────────────
    // Todos os tipos visíveis por padrão. Tipos ausentes retornam true via ?: true.
    var typeVisibility by remember {
        mutableStateOf(PointType.entries.associateWith { true })
    }

    // ── Filtro por material ───────────────────────────────────────────────
    // Derivado dinamicamente dos pontos: apenas materiais presentes em pelo
    // menos 1 ponto aparecem no filtro. Todos visíveis por padrão.
    val allMaterials = remember(points) {
        points.flatMap { it.materials }
            .toSortedSet(compareBy { mat ->
                val idx = MATERIAL_PRIORITY_ORDER.indexOf(mat)
                if (idx >= 0) idx else Int.MAX_VALUE  // desconhecidos vão pro final
            })
    }
    var materialVisibility by remember(allMaterials) {
        mutableStateOf(allMaterials.associateWith { true })
    }

    var showFilterSheet by remember { mutableStateOf(false) }

    // ── Filtragem combinada ───────────────────────────────────────────────
    // Tipo: deve estar visível.
    // Material: se o ponto não declara materiais, passa livre; caso contrário,
    // ao menos um de seus materiais deve estar visível.
    val filteredPoints = remember(points, typeVisibility, materialVisibility) {
        points.filter { point ->
            val typeOk     = typeVisibility[point.type] != false
            val materialOk = point.materials.isEmpty() ||
                    point.materials.any { materialVisibility[it] != false }
            typeOk && materialOk
        }
    }

    val clusterItems = remember(filteredPoints) {
        filteredPoints.map { RecyclingPointClusterItem(it) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                items              = clusterItems,
                onClusterClick     = { false },
                onClusterItemClick = { item ->
                    onMarkerClick(item.point)
                    false
                },
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
                clusterItemContent = { item ->
                    androidx.compose.foundation.Image(
                        painter            = androidx.compose.ui.res.painterResource(
                            item.point.type.toPinDrawable()
                        ),
                        contentDescription = item.point.name,
                        modifier           = Modifier.size(width = 32.dp, height = 48.dp)
                    )
                }
            )
        }

        MapFilterBar(
            toneColor    = toneColor,
            onOpenFilter = { showFilterSheet = true },
            modifier     = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )

        if (showFilterSheet) {
            MapFilterBottomSheet(
                typeVisibility     = typeVisibility,
                onToggle           = { type ->
                    typeVisibility = typeVisibility.toMutableMap().apply {
                        this[type] = !(this[type] ?: true)
                    }
                },
                materialVisibility = materialVisibility,
                onMaterialToggle   = { mat ->
                    materialVisibility = materialVisibility.toMutableMap().apply {
                        this[mat] = !(this[mat] ?: true)
                    }
                },
                toneColor          = toneColor,
                onDismiss          = { showFilterSheet = false }
            )
        }
    }
}