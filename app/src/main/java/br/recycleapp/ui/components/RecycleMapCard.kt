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
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import br.recycleapp.di.AppModule
import br.recycleapp.domain.map.MapProvider
import br.recycleapp.domain.map.RecyclingPoint
import br.recycleapp.ui.theme.PlaceholderLight
import br.recycleapp.ui.theme.TextSecondary
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority

/**
 * Card com mapa exibindo a localização do usuário
 * e os pontos de coleta seletiva próximos.
 *
 * Usa o Google Maps como mapa principal e OpenStreetMap como mapa reserva.
 * A troca é feita automaticamente via [br.recycleapp.data.map.MapAvailabilityChecker].
 *
 * @param toneColor     cor temática do material atual (usada no placeholder)
 * @param onMarkerClick callback chamado quando o usuário toca num marcador
 */
@Composable
fun RecycleMapCard(
    toneColor: Color,
    modifier: Modifier = Modifier,
    onMarkerClick: (RecyclingPoint) -> Unit = {}
) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var permissionGranted by remember {
        mutableStateOf(context.hasLocationPermission())
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionGranted = context.hasLocationPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var denialCount by remember { mutableIntStateOf(0) }
    val permanentlyDenied = denialCount >= 2

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
            denialCount++
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
            MapWithFallback(
                toneColor     = toneColor,
                onMarkerClick = onMarkerClick
            )
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

// ── Orquestração dos mapas ────────────────────────────────────────────────────

@Composable
private fun MapWithFallback(
    toneColor: Color,
    onMarkerClick: (RecyclingPoint) -> Unit
) {
    val context = LocalContext.current

    var mapProvider by remember { mutableStateOf<MapProvider?>(null) }
    var isChecking  by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val checker = AppModule.provideMapAvailabilityChecker(context)
        mapProvider = checker.getAvailableProvider()
        isChecking  = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isChecking -> {
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
            }

            mapProvider == MapProvider.GOOGLE -> {
                GoogleMapView(
                    toneColor     = toneColor,
                    onMarkerClick = onMarkerClick)
            }

            else -> {
                OsmMapView(
                    toneColor     = toneColor,
                    onMarkerClick = onMarkerClick)
                MapFallbackBanner(
                    modifier  = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

// ── Banner de fallback ────────────────────────────────────────────────────────

@Composable
private fun MapFallbackBanner(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.60f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text      = "Mapa principal indisponível — exibindo mapa reserva",
            color     = Color.White,
            fontSize  = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}

// ── Placeholder quando permissão negada ───────────────────────────────────────

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

internal fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED