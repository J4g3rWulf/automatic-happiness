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
import br.recycleapp.ui.theme.PlaceholderLight
import br.recycleapp.ui.theme.TextSecondary
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority

/**
 * Card com mapa exibindo a localização do usuário
 * e os PEVs de coleta seletiva do Rio de Janeiro.
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
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionGranted = context.hasLocationPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Conta as negações manualmente
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

// ── Placeholder quando permissão negada ───────────────────────────────────────

/**
 * Exibido quando a permissão de localização foi negada.
 *
 * - [permanentlyDenied] false: botão "Permitir localização" + "Abrir configurações"
 * - [permanentlyDenied] true:  apenas "Abrir configurações" (2ª negação atingida)
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
 * Retorna true se o app tem permissão de localização fina ou aproximada.
 */
internal fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED