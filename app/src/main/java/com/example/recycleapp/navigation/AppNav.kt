package com.example.recycleapp.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.recycleapp.ui.screens.CameraCaptureScreen
import com.example.recycleapp.ui.screens.ConfirmPhotoScreen
import com.example.recycleapp.ui.screens.GalleryPickerScreen
import com.example.recycleapp.ui.screens.HomeScreen
import com.example.recycleapp.ui.screens.LoadingScreen   // NOVO
import com.example.recycleapp.ui.screens.ResultScreen    // NOVO

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Camera : Screen("camera")
    data object Gallery : Screen("gallery")
    data object ConfirmPhoto : Screen("confirm_photo/{photoUri}") {
        fun build(photoUri: String) = "confirm_photo/${Uri.encode(photoUri)}"
    }

    // NOVO: tela de carregamento
    data object Loading : Screen("loading/{photoUri}") {
        fun build(photoUri: String) = "loading/${Uri.encode(photoUri)}"
    }

    // NOVO: tela de resultado
    data object Result : Screen("result/{photoUri}/{label}") {
        fun build(photoUri: String, label: String) =
            "result/${Uri.encode(photoUri)}/${Uri.encode(label)}"
    }
}

@Composable
fun AppNavHost() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Screen.Home.route) {

        composable(Screen.Home.route) {
            HomeScreen(
                onOpenCamera = { nav.navigate(Screen.Camera.route) },
                onOpenGallery = { nav.navigate(Screen.Gallery.route) }
            )
        }

        // CÂMERA
        composable(Screen.Camera.route) {
            CameraCaptureScreen(
                onBack = { nav.navigateUp() }, // voltar da câmera -> Home
                onPhotoTaken = { uri ->
                    nav.navigate(Screen.ConfirmPhoto.build(uri))
                }
            )
        }

        // GALERIA
        composable(Screen.Gallery.route) {
            GalleryPickerScreen(
                onBack = { nav.navigateUp() }, // voltar da galeria -> Home
                onPhotoPicked = { uri ->
                    nav.navigate(Screen.ConfirmPhoto.build(uri))
                }
            )
        }

        // CONFIRMAÇÃO
        composable(
            route = Screen.ConfirmPhoto.route,
            arguments = listOf(navArgument("photoUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("photoUri").orEmpty()
            val uri = Uri.decode(encoded)

            ConfirmPhotoScreen(
                photoUri = uri,
                onBack = { nav.navigateUp() },
                onSend = { photo ->
                    // AGORA vai para a tela de carregamento
                    nav.navigate(Screen.Loading.build(photo))
                }
            )
        }

        // ===== NOVO: tela de carregamento =====
        composable(
            route = Screen.Loading.route,
            arguments = listOf(navArgument("photoUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("photoUri").orEmpty()
            val uri = Uri.decode(encoded)

            LoadingScreen(
                photoUri = uri,
                onBack = { nav.navigateUp() }, // volta pra tela de confirmação
                onResult = { label ->
                    nav.navigate(Screen.Result.build(uri, label)) {
                        // assim, ao sair do resultado, o loading some da pilha
                        popUpTo(Screen.Loading.route) { inclusive = true }
                    }
                }
            )
        }

        // ===== NOVO: tela de resultado =====
        composable(
            route = Screen.Result.route,
            arguments = listOf(
                navArgument("photoUri") { type = NavType.StringType },
                navArgument("label") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedUri = backStackEntry.arguments?.getString("photoUri").orEmpty()
            val encodedLabel = backStackEntry.arguments?.getString("label").orEmpty()
            val uri = Uri.decode(encodedUri)
            val label = Uri.decode(encodedLabel)

            ResultScreen(
                photoUri = uri,
                label = label,
                onBackToHome = {
                    nav.popBackStack(Screen.Home.route, inclusive = false)
                }
            )
        }
    }
}