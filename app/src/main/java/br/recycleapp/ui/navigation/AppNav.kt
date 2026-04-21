package br.recycleapp.ui.navigation

import android.app.Activity
import android.net.Uri
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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
import br.recycleapp.ui.screens.ColorsScreen
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

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Home     : Screen("home")
    data object MapTab   : Screen("map_tab")
    data object Learn    : Screen("learn")
    data object Programs : Screen("programs")
    data object Colors   : Screen("colors")
    data object Camera  : Screen("camera")
    data object Gallery : Screen("gallery")
    data object ConfirmPhoto : Screen("confirm_photo/{photoUri}/{fromCamera}") {
        fun build(photoUri: String, fromCamera: Boolean) =
            "confirm_photo/${Uri.encode(photoUri)}/$fromCamera"
    }
    data object Loading : Screen("loading")
    data object Result  : Screen("result")
}

private val BOTTOM_NAV_ROUTES = setOf(
    Screen.Home.route,
    Screen.MapTab.route,
    Screen.Learn.route,
    Screen.Programs.route
)

private fun String?.isClassificationFlow() =
    this == Screen.ConfirmPhoto.route || this == Screen.Result.route

private fun String?.showBottomNav() =
    this in BOTTOM_NAV_ROUTES || this == Screen.Colors.route || this.isClassificationFlow()

private fun String?.activeNavRoute(): String? = when {
    this in BOTTOM_NAV_ROUTES   -> this
    this == Screen.Colors.route -> Screen.Learn.route
    this.isClassificationFlow() -> Screen.Home.route
    else                        -> null
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun AppNavHost(windowSizeClass: WindowSizeClass) {
    val nav       = rememberNavController()
    val viewModel : ClassificationViewModel = viewModel()
    val context   = LocalContext.current
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute   = backStackEntry?.destination?.route

    val view = LocalView.current
    LaunchedEffect(currentRoute) {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, view)
            if (currentRoute == Screen.MapTab.route) {
                insetsController.hide(WindowInsetsCompat.Type.statusBars())
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                insetsController.show(WindowInsetsCompat.Type.statusBars())
            }
        }
    }

    Scaffold(
        containerColor      = Color.Transparent,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (currentRoute.showBottomNav()) {
                BottomNavBar(
                    currentRoute = currentRoute.activeNavRoute(),
                    onNavigate   = { route ->
                        val activeTab = currentRoute.activeNavRoute()

                        // Lógica para retornar à raiz da aba se já estiver nela
                        if (route == activeTab && currentRoute != route) {
                            nav.popBackStack(route, inclusive = false)
                        }
                        else if (currentRoute.isClassificationFlow()) {
                            if (currentRoute == Screen.ConfirmPhoto.route) {
                                nav.currentBackStackEntry
                                    ?.arguments
                                    ?.getString("photoUri")
                                    ?.let { Uri.decode(it).tryDeleteCapturedCacheFile(context) }
                            }
                            viewModel.reset()
                            nav.popBackStack(Screen.Home.route, inclusive = false)
                            if (route != Screen.Home.route) {
                                nav.navigate(route) {
                                    popUpTo(Screen.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            }
                        } else {
                            if (route == Screen.Home.route) {
                                nav.popBackStack(Screen.Home.route, inclusive = false)
                            } else {
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
            composable(Screen.Splash.route) {
                SplashScreen(onSplashFinished = {
                    nav.navigate(Screen.Home.route) { popUpTo(Screen.Splash.route) { inclusive = true } }
                })
            }
            composable(Screen.Home.route) {
                HomeScreen(windowSizeClass = windowSizeClass, onOpenCamera = { nav.navigate(Screen.Camera.route) }, onOpenGallery = { nav.navigate(Screen.Gallery.route) })
            }
            composable(Screen.MapTab.route) { MapScreen() }
            composable(Screen.Learn.route) {
                LearnScreen(onOpenColors = { nav.navigate(Screen.Colors.route) })
            }
            composable(Screen.Programs.route) { ProgramsScreen() }
            composable(Screen.Colors.route) {
                ColorsScreen(onBack = { nav.navigateUp() })
            }
            composable(Screen.Camera.route) {
                CameraCaptureScreen(onBack = { nav.navigateUp() }, onPhotoTaken = { uri -> nav.navigate(Screen.ConfirmPhoto.build(uri, fromCamera = true)) })
            }
            composable(Screen.Gallery.route) {
                GalleryPickerScreen(onBack = { nav.navigateUp() }, onPhotoPicked = { uri -> nav.navigate(Screen.ConfirmPhoto.build(uri, fromCamera = false)) })
            }
            composable(
                route     = Screen.ConfirmPhoto.route,
                arguments = listOf(navArgument("photoUri") { type = NavType.StringType }, navArgument("fromCamera") { type = NavType.BoolType })
            ) { backStackEntry ->
                val encoded = backStackEntry.arguments?.getString("photoUri").orEmpty()
                val fromCamera = backStackEntry.arguments?.getBoolean("fromCamera") ?: true
                ConfirmPhotoScreen(windowSizeClass = windowSizeClass, photoUri = Uri.decode(encoded), retakeLabel = if (fromCamera) "Tirar outra" else "Escolher outra", onBack = { nav.navigateUp() }, onSend = { photo ->
                    viewModel.classify(photo.toUri())
                    nav.navigate(Screen.Loading.route)
                })
            }
            composable(Screen.Loading.route) {
                val uiState by viewModel.uiState.collectAsState()
                LoadingScreen(uiState = uiState, onBack = { nav.navigateUp() }, onResult = { nav.navigate(Screen.Result.route) { popUpTo(Screen.Loading.route) { inclusive = true } } })
            }
            composable(Screen.Result.route) {
                val uiState by viewModel.uiState.collectAsState()
                var cachedLabel by remember { mutableStateOf("Indefinido") }
                if (uiState is ClassificationViewModel.UiState.Result) {
                    val r = (uiState as ClassificationViewModel.UiState.Result).result
                    cachedLabel = if (r is ClassificationResult.Success) r.materialType.toLabelPt() else "Indefinido"
                }
                ResultScreen(photoUri = viewModel.imageUri.toString(), label = cachedLabel, onBackToHome = {
                    nav.popBackStack(Screen.Home.route, inclusive = false)
                    viewModel.reset()
                })
            }
        }
    }
}