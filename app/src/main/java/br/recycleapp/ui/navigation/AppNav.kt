package br.recycleapp.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.recycleapp.domain.model.ClassificationResult
import br.recycleapp.ui.components.BottomNavBar
import br.recycleapp.ui.mapper.toLabelPt
import br.recycleapp.ui.screens.CameraCaptureScreen
import br.recycleapp.ui.screens.ConfirmPhotoScreen
import br.recycleapp.ui.screens.GalleryPickerScreen
import br.recycleapp.ui.screens.HomeScreen
import br.recycleapp.ui.screens.LearnScreen
import br.recycleapp.ui.screens.LoadingScreen
import br.recycleapp.ui.screens.MapScreen
import br.recycleapp.ui.screens.ProgramsScreen
import br.recycleapp.ui.screens.ResultScreen
import br.recycleapp.ui.screens.SplashScreen
import br.recycleapp.ui.viewmodel.ClassificationViewModel
import br.recycleapp.util.tryDeleteCapturedCacheFile

/**
 * Define todas as rotas de navegação do app.
 */
sealed class Screen(val route: String) {
    data object Splash : Screen("splash")

    data object Home     : Screen("home")
    data object MapTab   : Screen("map_tab")
    data object Learn    : Screen("learn")
    data object Programs : Screen("programs")

    data object Camera  : Screen("camera")
    data object Gallery : Screen("gallery")

    data object ConfirmPhoto : Screen("confirm_photo/{photoUri}/{fromCamera}") {
        fun build(photoUri: String, fromCamera: Boolean) =
            "confirm_photo/${Uri.encode(photoUri)}/$fromCamera"
    }

    data object Loading : Screen("loading")
    data object Result  : Screen("result")
}

// ── Helpers de rota ───────────────────────────────────────────────────────────

private val BOTTOM_NAV_ROUTES = setOf(
    Screen.Home.route,
    Screen.MapTab.route,
    Screen.Learn.route,
    Screen.Programs.route
)

/** * O currentRoute agora avalia diretamente o template da rota do NavController,
 * o que torna a verificação mais segura que usar `startsWith()`.
 */
private fun String?.isClassificationFlow() =
    this == Screen.ConfirmPhoto.route || this == Screen.Result.route

private fun String?.showBottomNav() =
    this in BOTTOM_NAV_ROUTES || this.isClassificationFlow()

private fun String?.activeNavRoute(): String? = when {
    this in BOTTOM_NAV_ROUTES   -> this
    this.isClassificationFlow() -> Screen.Home.route
    else                        -> null
}

// ── Host de navegação ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun AppNavHost(windowSizeClass: WindowSizeClass) {
    val nav       = rememberNavController()
    val viewModel : ClassificationViewModel = viewModel()
    val context   = LocalContext.current

    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute   = backStackEntry?.destination?.route

    Scaffold(
        containerColor      = Color.Transparent,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (currentRoute.showBottomNav()) {
                BottomNavBar(
                    currentRoute = currentRoute.activeNavRoute(),
                    onNavigate   = { route ->
                        if (currentRoute.isClassificationFlow()) {
                            // 1. Limpa arquivo e viewModel
                            if (currentRoute == Screen.ConfirmPhoto.route) {
                                nav.currentBackStackEntry
                                    ?.arguments
                                    ?.getString("photoUri")
                                    ?.let { Uri.decode(it).tryDeleteCapturedCacheFile(context) }
                            }
                            viewModel.reset()

                            // 2. Destrói o fluxo de classificação de forma limpa,
                            // retornando a pilha até a Home (sem recriar a Home)
                            nav.popBackStack(Screen.Home.route, inclusive = false)

                            // 3. Se o usuário clicou em outra aba (não a Home), navega para ela
                            if (route != Screen.Home.route) {
                                nav.navigate(route) {
                                    popUpTo(Screen.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            }
                        } else {
                            // Comportamento normal quando já está nas abas
                            if (route == Screen.Home.route) {
                                // Se clicou na Home estando em outra aba, esvazia até a Home
                                nav.popBackStack(Screen.Home.route, inclusive = false)
                            } else {
                                // Navegação entre Mapa, Aprender, Programas
                                nav.navigate(route) {
                                    popUpTo(Screen.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = nav,
            startDestination = Screen.Splash.route,
            modifier         = Modifier.padding(innerPadding)
        ) {
            // ── Splash ────────────────────────────────────────────────────
            composable(Screen.Splash.route) {
                SplashScreen(
                    onSplashFinished = {
                        nav.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // ── Abas da bottom nav ────────────────────────────────────────
            composable(Screen.Home.route) {
                HomeScreen(
                    windowSizeClass = windowSizeClass,
                    onOpenCamera    = { nav.navigate(Screen.Camera.route) },
                    onOpenGallery   = { nav.navigate(Screen.Gallery.route) }
                )
            }

            composable(Screen.MapTab.route) { MapScreen() }
            composable(Screen.Learn.route) { LearnScreen() }
            composable(Screen.Programs.route) { ProgramsScreen() }

            // ── Fluxo de classificação ────────────────────────────────────
            composable(Screen.Camera.route) {
                CameraCaptureScreen(
                    onBack       = { nav.navigateUp() },
                    onPhotoTaken = { uri ->
                        nav.navigate(Screen.ConfirmPhoto.build(uri, fromCamera = true))
                    }
                )
            }

            composable(Screen.Gallery.route) {
                GalleryPickerScreen(
                    onBack        = { nav.navigateUp() },
                    onPhotoPicked = { uri ->
                        nav.navigate(Screen.ConfirmPhoto.build(uri, fromCamera = false))
                    }
                )
            }

            composable(
                route     = Screen.ConfirmPhoto.route,
                arguments = listOf(
                    navArgument("photoUri")   { type = NavType.StringType },
                    navArgument("fromCamera") { type = NavType.BoolType }
                )
            ) { backStackEntry ->
                val encoded    = backStackEntry.arguments?.getString("photoUri").orEmpty()
                val fromCamera = backStackEntry.arguments?.getBoolean("fromCamera") ?: true
                val uri        = Uri.decode(encoded)

                ConfirmPhotoScreen(
                    windowSizeClass    = windowSizeClass,
                    photoUri           = uri,
                    retakeLabel        = if (fromCamera) "Tirar outra" else "Escolher outra",
                    retakeButtonWeight = if (fromCamera) 0.40f else 0.45f,
                    sendButtonWeight   = if (fromCamera) 0.60f else 0.57f,
                    onBack             = { nav.navigateUp() },
                    onSend             = { photo ->
                        viewModel.classify(photo.toUri())
                        nav.navigate(Screen.Loading.route)
                    }
                )
            }

            composable(Screen.Loading.route) {
                val uiState by viewModel.uiState.collectAsState()
                LoadingScreen(
                    uiState  = uiState,
                    onBack   = { nav.navigateUp() },
                    onResult = {
                        nav.navigate(Screen.Result.route) {
                            popUpTo(Screen.Loading.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Result.route) {
                val uiState by viewModel.uiState.collectAsState()

                var cachedLabel by remember { mutableStateOf("Indefinido") }
                if (uiState is ClassificationViewModel.UiState.Result) {
                    cachedLabel = when (
                        val r = (uiState as ClassificationViewModel.UiState.Result).result
                    ) {
                        is ClassificationResult.Success    -> r.materialType.toLabelPt()
                        is ClassificationResult.Indefinido -> "Indefinido"
                        is ClassificationResult.Error      -> "Indefinido"
                    }
                }

                ResultScreen(
                    photoUri     = viewModel.imageUri.toString(),
                    label        = cachedLabel,
                    onBackToHome = {
                        nav.popBackStack(Screen.Home.route, inclusive = false)
                        viewModel.reset()
                    }
                )
            }
        }
    }
}