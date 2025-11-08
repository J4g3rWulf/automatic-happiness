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

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Camera : Screen("camera")
    data object Gallery : Screen("gallery")
    data object ConfirmPhoto : Screen("confirm_photo/{photoUri}") {
        fun build(photoUri: String) = "confirm_photo/${Uri.encode(photoUri)}"
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
                onBack = { nav.navigateUp() }, // volta para Camera OU Galeria, conforme origem
                onSend = {
                    // Enviar -> Home
                    nav.popBackStack(Screen.Home.route, inclusive = false)
                }
            )
        }
    }
}
