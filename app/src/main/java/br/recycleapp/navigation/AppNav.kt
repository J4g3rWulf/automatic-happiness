package br.recycleapp.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.recycleapp.data.model.ClassificationResult
import br.recycleapp.ui.screens.CameraCaptureScreen
import br.recycleapp.ui.screens.ConfirmPhotoScreen
import br.recycleapp.ui.screens.GalleryPickerScreen
import br.recycleapp.ui.screens.HomeScreen
import br.recycleapp.ui.screens.LoadingScreen
import br.recycleapp.ui.screens.ResultScreen
import br.recycleapp.ui.viewmodel.ClassificationViewModel

sealed class Screen(val route: String) {
    data object Home        : Screen("home")
    data object Camera      : Screen("camera")
    data object Gallery     : Screen("gallery")
    data object ConfirmPhoto : Screen("confirm_photo/{photoUri}") {
        fun build(photoUri: String) = "confirm_photo/${Uri.encode(photoUri)}"
    }
    data object Loading : Screen("loading")
    data object Result  : Screen("result")
}

@Composable
fun AppNavHost() {
    val nav = rememberNavController()
    val viewModel: ClassificationViewModel = viewModel()

    NavHost(navController = nav, startDestination = Screen.Home.route) {

        composable(Screen.Home.route) {
            HomeScreen(
                onOpenCamera  = { nav.navigate(Screen.Camera.route) },
                onOpenGallery = { nav.navigate(Screen.Gallery.route) }
            )
        }

        composable(Screen.Camera.route) {
            CameraCaptureScreen(
                onBack       = { nav.navigateUp() },
                onPhotoTaken = { uri ->
                    nav.navigate(Screen.ConfirmPhoto.build(uri))
                }
            )
        }

        composable(Screen.Gallery.route) {
            GalleryPickerScreen(
                onBack        = { nav.navigateUp() },
                onPhotoPicked = { uri ->
                    nav.navigate(Screen.ConfirmPhoto.build(uri))
                }
            )
        }

        composable(
            route = Screen.ConfirmPhoto.route,
            arguments = listOf(navArgument("photoUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("photoUri").orEmpty()
            val uri = Uri.decode(encoded)

            ConfirmPhotoScreen(
                photoUri = uri,
                onBack   = { nav.navigateUp() },
                onSend   = { photo ->
                    viewModel.classify(Uri.parse(photo))
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

            // Guarda o último label válido — evita flash de "Indefinido" durante a navegação
            var cachedLabel by remember { mutableStateOf("Indefinido") }
            if (uiState is ClassificationViewModel.UiState.Result) {
                cachedLabel = when (
                    val r = (uiState as ClassificationViewModel.UiState.Result).result
                ) {
                    is ClassificationResult.Success    -> r.material.labelPt
                    is ClassificationResult.Indefinido -> "Indefinido"
                    is ClassificationResult.Error      -> "Indefinido"
                }
            }

            ResultScreen(
                photoUri     = viewModel.imageUri.toString(),
                label        = cachedLabel,
                onBackToHome = {
                    nav.popBackStack(Screen.Home.route, inclusive = false)
                    viewModel.reset() // reseta DEPOIS de iniciar a navegação
                }
            )
        }
    }
}