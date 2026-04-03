package br.recycleapp.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import br.recycleapp.ui.theme.PlaceholderLight
import br.recycleapp.ui.theme.TextSecondary
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
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

    //OK
    RecyclingPoint(-22.85046155285499, -43.46413468350324,
        "PEV Bangu",
        "Rua Roque Barbosa, 348 - Bangu"),

    //OK
    RecyclingPoint(-22.87516362916978, -43.33496706988172,
        "PEV Madureira",
        "Sob o Viaduto Prefeito Negrão de Lima"),

    //OK
    RecyclingPoint(-22.927124968864515, -43.229079230075605,
        "PEV Tijuca",
        "Rua Dr. Renato Rocco, 400"),


    // ── Outros pontos de coleta seletiva ─────────────────────────────────────

)

// Localização padrão se o GPS não responder dentro do timeout
private val RIO_CENTER = GeoPoint(-22.9068, -43.1729)

/**
 * Card com mapa OpenStreetMap exibindo a localização do usuário
 * e os PEVs de coleta seletiva do Rio de Janeiro.
 *
 * Usa FusedLocationProvider para localização rápida e precisa,
 * e OpenStreetMap para renderizar o mapa (sem API key).
 *
 * Solicita permissão de localização ao ser exibido pela primeira vez.
 * Se o GPS estiver desligado, exibe diálogo nativo para ativá-lo.
 * Se a permissão for negada:
 *   - 1ª negação: exibe botão "Permitir localização" + "Abrir configurações"
 *   - 2ª negação: exibe apenas "Abrir configurações"
 * Atualiza o estado de permissão ao retornar de outras telas (ON_RESUME).
 *
 * @param toneColor cor temática do material atual (usada no placeholder)
 */
@Composable
fun RecycleMapCard(
    toneColor: Color,
    modifier: Modifier = Modifier
) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var permissionGranted by remember {
        mutableStateOf(context.hasLocationPermission())
    }

    // Re-verifica permissão ao retornar de outras telas (ex: Settings)
    // Resolve o caso em que o usuário concede nas configurações e volta ao app
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionGranted = context.hasLocationPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Conta as negações manualmente — mais confiável que shouldShowRequestPermissionRationale,
    // que tem comportamento inconsistente entre fabricantes e versões do Android
    var denialCount by remember { mutableIntStateOf(0) }
    val permanentlyDenied = denialCount >= 2  // bloqueado após 2 negações

    val enableGpsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        permissionGranted = granted

        if (granted) {
            denialCount = 0
            requestEnableGps(context, enableGpsLauncher)
        } else {
            denialCount++  // incrementa a cada negação
        }
    }

    LaunchedEffect(Unit) {
        if (!permissionGranted) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            requestEnableGps(context, enableGpsLauncher)
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
            MapPermissionPlaceholder(
                toneColor           = toneColor,
                permanentlyDenied   = permanentlyDenied,
                onRequestPermission = {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                onOpenSettings = {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null)
                        )
                    )
                }
            )
        }
    }
}

// ── Mapa OSM ──────────────────────────────────────────────────────────────────

/**
 * Obtém a localização antes de renderizar o mapa.
 * Enquanto aguarda, exibe um indicador de carregamento.
 * Só cria o MapView quando já tem o centro correto — sem pulos.
 */
@Composable
private fun OsmMapView() {
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
 * Separado do OsmMapView para garantir que o MapView
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
                Lifecycle.Event.ON_PAUSE -> {
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

/**
 * Exibido quando a permissão de localização foi negada.
 *
 * - [permanentlyDenied] false: botão "Permitir localização" + "Abrir configurações"
 * - [permanentlyDenied] true:  apenas "Abrir configurações" (2ª negação atingida)
 *
 * Ripple neutro via Box clicável — evita a cor verde do tema Material.
 */
@Composable
private fun MapPermissionPlaceholder(
    toneColor: Color,
    permanentlyDenied: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit
) {
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
                text = if (permanentlyDenied)
                    "Localização bloqueada.\nAcesse as configurações para permitir."
                else
                    "Permita o acesso à localização\npara ver pontos de coleta próximos",
                color     = TextSecondary,
                fontSize  = 12.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))

            // Botão "Permitir localização" - visível apenas antes do bloqueio
            if (!permanentlyDenied) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = ripple(color = Color.Black.copy(alpha = 0.08f)),
                            onClick           = onRequestPermission
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text     = "Permitir localização",
                        color    = toneColor,
                        fontSize = 13.sp
                    )
                }
            }

            // Botão "Abrir configurações" - sempre visível,
            // torna-se o único após a 2ª negação
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = ripple(color = Color.Black.copy(alpha = 0.08f)),
                        onClick           = onOpenSettings
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text     = "Abrir configurações",
                    color    = if (permanentlyDenied) toneColor else TextSecondary,
                    fontSize = if (permanentlyDenied) 13.sp else 12.sp
                )
            }
        }
    }
}

// ── Extension helpers ─────────────────────────────────────────────────────────

/**
 * Verifica se o GPS está ativo e, se não estiver,
 * exibe o diálogo nativo do Android para ativar sem sair do app.
 */
private fun requestEnableGps(
    context: Context,
    launcher: androidx.activity.result.ActivityResultLauncher<IntentSenderRequest>
) {
    val request = LocationSettingsRequest.Builder()
        .addLocationRequest(
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 0).build()
        )
        .setAlwaysShow(true)
        .build()

    LocationServices.getSettingsClient(context)
        .checkLocationSettings(request)
        .addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                runCatching {
                    launcher.launch(
                        IntentSenderRequest.Builder(exception.resolution).build()
                    )
                }
            }
        }
}

/**
 * Obtém a localização do usuário de forma suspensa.
 * Força leitura fresca via requestLocationUpdates com timeout de 10 segundos.
 * Garante posição atual mesmo após deslocamento — lastLocation ignorado.
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

private fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED