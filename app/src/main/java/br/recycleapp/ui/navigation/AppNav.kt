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
import br.recycleapp.ui.screens.HowToDiscardScreen
import br.recycleapp.ui.screens.LearnScreen
import br.recycleapp.ui.screens.LoadingScreen
import br.recycleapp.ui.screens.MapScreen
import br.recycleapp.ui.screens.ProgramsScreen
import br.recycleapp.ui.screens.ResultScreen
import br.recycleapp.ui.screens.TermsScreen
import br.recycleapp.ui.screens.SplashScreen
import br.recycleapp.ui.screens.WhatToDiscardScreen
import br.recycleapp.ui.viewmodel.ClassificationViewModel
import br.recycleapp.util.tryDeleteCapturedCacheFile

// ── Rotas de navegação ────────────────────────────────────────────────────────
// Cada objeto representa uma tela do app. Rotas com parâmetros dinâmicos
// expõem uma função build() para montar a URL com os argumentos necessários.

sealed class Screen(val route: String) {
    // ── Fluxo inicial ──
    data object Splash : Screen("splash")

    // ── Abas principais da BottomNavBar ──
    data object Home     : Screen("home")
    data object MapTab   : Screen("map_tab")
    data object Learn    : Screen("learn")
    data object Programs : Screen("programs")

    // ── Sub-telas da aba Aprender ──
    data object Colors        : Screen("colors")
    data object WhatToDiscard : Screen("what_to_discard")
    data object HowToDiscard  : Screen("how_to_discard")
    data object Terms : Screen("terms")

    // ── Fluxo de classificação de resíduos ──
    data object Camera  : Screen("camera")
    data object Gallery : Screen("gallery")
    data object ConfirmPhoto : Screen("confirm_photo/{photoUri}/{fromCamera}") {
        fun build(photoUri: String, fromCamera: Boolean) =
            "confirm_photo/${Uri.encode(photoUri)}/$fromCamera"
    }
    data object Loading : Screen("loading")
    data object Result  : Screen("result")
}

// ── Rotas que exibem a BottomNavBar ──────────────────────────────────────────
// Inclui as abas principais e sub-telas que devem manter a barra visível.

private val BOTTOM_NAV_ROUTES = setOf(
    Screen.Home.route,
    Screen.MapTab.route,
    Screen.Learn.route,
    Screen.Programs.route
)

// ── Helpers de rota ──────────────────────────────────────────────────────────

/** Retorna true se a rota atual faz parte do fluxo de classificação de imagem. */
private fun String?.isClassificationFlow() =
    this == Screen.ConfirmPhoto.route || this == Screen.Result.route

/** Retorna true se a BottomNavBar deve ser exibida na rota atual. */
private fun String?.showBottomNav() =
    this in BOTTOM_NAV_ROUTES
            || this == Screen.Colors.route
            || this == Screen.WhatToDiscard.route
            || this == Screen.HowToDiscard.route
            || this == Screen.Terms.route
            || this.isClassificationFlow()

/**
 * Retorna a rota da aba que deve ficar destacada na BottomNavBar.
 * Sub-telas de "Aprender" apontam para [Screen.Learn].
 * Telas do fluxo de classificação apontam para [Screen.Home].
 */
private fun String?.activeNavRoute(): String? = when {
    this in BOTTOM_NAV_ROUTES          -> this
    this == Screen.Colors.route        -> Screen.Learn.route
    this == Screen.WhatToDiscard.route -> Screen.Learn.route
    this == Screen.HowToDiscard.route  -> Screen.Learn.route
    this == Screen.Terms.route -> Screen.Learn.route
    this.isClassificationFlow()        -> Screen.Home.route
    else                               -> null
}

// ── Host de navegação principal ──────────────────────────────────────────────

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun AppNavHost(windowSizeClass: WindowSizeClass) {
    val nav       = rememberNavController()
    val viewModel : ClassificationViewModel = viewModel()
    val context   = LocalContext.current
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute   = backStackEntry?.destination?.route

    // ── Controle da status bar ──
    // Oculta a status bar na tela do mapa para experiência imersiva;
    // restaura nas demais telas.
    val view = LocalView.current
    LaunchedEffect(currentRoute) {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, view)
            if (currentRoute == Screen.MapTab.route) {
                insetsController.hide(WindowInsetsCompat.Type.statusBars())
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
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

                        when {
                            // Já está na aba — volta para a raiz dela (pop até a rota)
                            route == activeTab && currentRoute != route -> {
                                nav.popBackStack(route, inclusive = false)
                            }

                            // Navega a partir do fluxo de classificação —
                            // limpa o estado e o arquivo temporário de câmera antes de sair
                            currentRoute.isClassificationFlow() -> {
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
                            }

                            // Navegação padrão entre abas
                            else -> {
                                if (route == Screen.Home.route) {
                                    nav.popBackStack(Screen.Home.route, inclusive = false)
                                } else {
                                    nav.navigate(route) {
                                        popUpTo(Screen.Home.route) {
                                            saveState = false  // não salva sub-telas no histórico
                                        }
                                        launchSingleTop = true
                                        restoreState    = false  // não restaura histórico anterior da aba
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->

        // ── Grafo de navegação ────────────────────────────────────────────────
        NavHost(
            navController    = nav,
            startDestination = Screen.Splash.route,
            modifier         = Modifier.padding(innerPadding)
        ) {

            // ── Splash ──
            composable(Screen.Splash.route) {
                SplashScreen(onSplashFinished = {
                    nav.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                })
            }

            // ── Abas principais ──
            composable(Screen.Home.route) {
                HomeScreen(
                    windowSizeClass = windowSizeClass,
                    onOpenCamera    = { nav.navigate(Screen.Camera.route) },
                    onOpenGallery   = { nav.navigate(Screen.Gallery.route) }
                )
            }
            composable(Screen.MapTab.route) { MapScreen() }

            composable(Screen.Learn.route) {
                LearnScreen(
                    onOpenColors       = { nav.navigate(Screen.Colors.route) },
                    onOpenWhatToDiscard = { nav.navigate(Screen.WhatToDiscard.route) },
                    onOpenHowToDiscard  = { nav.navigate(Screen.HowToDiscard.route) },
                    onOpenTerms         = { nav.navigate(Screen.Terms.route) }
                )
            }
            composable(Screen.Programs.route) { ProgramsScreen() }

            // ── Sub-telas de Aprender ──
            composable(Screen.Colors.route) {
                ColorsScreen(onBack = { nav.navigateUp() })
            }
            composable(Screen.WhatToDiscard.route) {
                WhatToDiscardScreen(onBack = { nav.navigateUp() })
            }
            composable(Screen.HowToDiscard.route) {
                HowToDiscardScreen(onBack = { nav.navigateUp() })
            }
            composable(Screen.Terms.route) {
                TermsScreen(onBack = { nav.navigateUp() })
            }

            // ── Fluxo de classificação de resíduos ──
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
                ConfirmPhotoScreen(
                    windowSizeClass = windowSizeClass,
                    photoUri        = Uri.decode(encoded),
                    retakeLabel     = if (fromCamera) "Tirar outra" else "Escolher outra",
                    onBack          = { nav.navigateUp() },
                    onSend          = { photo ->
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
                // Cache do label para evitar recomposição ao resetar o ViewModel
                var cachedLabel by remember { mutableStateOf("Indefinido") }
                if (uiState is ClassificationViewModel.UiState.Result) {
                    val r = (uiState as ClassificationViewModel.UiState.Result).result
                    cachedLabel = if (r is ClassificationResult.Success)
                        r.materialType.toLabelPt() else "Indefinido"
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