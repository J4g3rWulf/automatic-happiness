package br.recycleapp.ui.navigation

import android.net.Uri
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.recycleapp.domain.model.ClassificationResult
import br.recycleapp.ui.mapper.toLabelPt
import br.recycleapp.ui.screens.CameraCaptureScreen
import br.recycleapp.ui.screens.ConfirmPhotoScreen
import br.recycleapp.ui.screens.GalleryPickerScreen
import br.recycleapp.ui.screens.HomeScreen
import br.recycleapp.ui.screens.LoadingScreen
import br.recycleapp.ui.screens.ResultScreen
import br.recycleapp.ui.viewmodel.ClassificationViewModel

/**
 * Define todas as rotas de navegação do app.
 * Cada `data object` representa uma tela — a rota é a string usada
 * para navegar entre elas via [NavHost].
 */
sealed class Screen(val route: String) {
    data object Home         : Screen("home")
    data object Camera       : Screen("camera")
    data object Gallery      : Screen("gallery")

    /** A rota do ConfirmPhoto recebe dois argumentos:
     *  - photoUri: URI da foto capturada (encodada para não quebrar a rota)
     *  - fromCamera: booleano que indica se veio da câmera ou da galeria
     */
    data object ConfirmPhoto : Screen("confirm_photo/{photoUri}/{fromCamera}") {
        fun build(photoUri: String, fromCamera: Boolean) =
            "confirm_photo/${Uri.encode(photoUri)}/$fromCamera"
    }
    data object Loading : Screen("loading")
    data object Result  : Screen("result")
}

/**
 * Host de navegação principal do app.
 * Recebe o [windowSizeClass] para repassar às telas que precisam
 * de layout responsivo (HomeScreen, ConfirmPhotoScreen).
 *
 * O [ClassificationViewModel] é compartilhado entre todas as telas
 * para que o resultado da IA persista da LoadingScreen até a ResultScreen.
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun AppNavHost(windowSizeClass: WindowSizeClass) {
    val nav       = rememberNavController()
    val viewModel : ClassificationViewModel = viewModel()

    NavHost(navController = nav, startDestination = Screen.Home.route) {

        // ── Tela inicial ──────────────────────────────────────────────
        composable(Screen.Home.route) {
            HomeScreen(
                windowSizeClass = windowSizeClass,
                onOpenCamera    = { nav.navigate(Screen.Camera.route) },
                onOpenGallery   = { nav.navigate(Screen.Gallery.route) }
            )
        }

        // ── Captura pela câmera ───────────────────────────────────────
        composable(Screen.Camera.route) {
            CameraCaptureScreen(
                onBack       = { nav.navigateUp() },
                onPhotoTaken = { uri ->
                    nav.navigate(Screen.ConfirmPhoto.build(uri, fromCamera = true))
                }
            )
        }

        // ── Seleção da galeria ────────────────────────────────────────
        composable(Screen.Gallery.route) {
            GalleryPickerScreen(
                onBack        = { nav.navigateUp() },
                onPhotoPicked = { uri ->
                    nav.navigate(Screen.ConfirmPhoto.build(uri, fromCamera = false))
                }
            )
        }

        // ── Confirmação da foto ───────────────────────────────────────
        // fromCamera determina o texto e proporção do botão esquerdo
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

            if (fromCamera) {
                // Fluxo câmera — "Tirar outra" é curto, botão esquerdo menor
                ConfirmPhotoScreen(
                    windowSizeClass    = windowSizeClass,
                    photoUri           = uri,
                    retakeLabel        = "Tirar outra",
                    retakeButtonWeight = 0.40f,  // ← ajuste aqui para câmera
                    sendButtonWeight   = 0.60f,  // ← ajuste aqui para câmera
                    onBack             = { nav.navigateUp() },
                    onSend             = { photo ->
                        viewModel.classify(photo.toUri())
                        nav.navigate(Screen.Loading.route)
                    }
                )
            } else {
                // Fluxo galeria — "Escolher outra" é mais longo, botão esquerdo maior
                ConfirmPhotoScreen(
                    windowSizeClass    = windowSizeClass,
                    photoUri           = uri,
                    retakeLabel        = "Escolher outra",
                    retakeButtonWeight = 0.45f,  // ← ajuste aqui para galeria
                    sendButtonWeight   = 0.57f,  // ← ajuste aqui para galeria
                    onBack             = { nav.navigateUp() },
                    onSend             = { photo ->
                        viewModel.classify(photo.toUri())
                        nav.navigate(Screen.Loading.route)
                    }
                )
            }
        }

        // ── Loading / análise da IA ───────────────────────────────────
        composable(Screen.Loading.route) {
            val uiState by viewModel.uiState.collectAsState()

            LoadingScreen(
                uiState  = uiState,
                onBack   = { nav.navigateUp() },
                onResult = {
                    // Remove a LoadingScreen da pilha para o botão voltar não retornar a ela
                    nav.navigate(Screen.Result.route) {
                        popUpTo(Screen.Loading.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Resultado da classificação ────────────────────────────────
        composable(Screen.Result.route) {
            val uiState by viewModel.uiState.collectAsState()

            // cachedLabel evita o flash de "Indefinido" que ocorreria se o
            // uiState fosse resetado antes da tela ser removida da pilha
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
                    // Volta para Home limpando toda a pilha de navegação
                    nav.popBackStack(Screen.Home.route, inclusive = false)
                    viewModel.reset()
                }
            )
        }
    }
}