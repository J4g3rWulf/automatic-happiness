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
import com.example.recycleapp.ui.screens.HomeScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Camera : Screen("camera")
    data object ConfirmPhoto : Screen("confirm_photo/{photoUri}") {
        // Encoda a URI para caber na rota sem quebrar
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
                onOpenGallery = { /* conectar depois */ }
            )
        }

        composable(Screen.Camera.route) {
            CameraCaptureScreen(
                onBack = { nav.navigateUp() },
                onPhotoTaken = { uri ->
                    nav.navigate(Screen.ConfirmPhoto.build(uri)) {
                        popUpTo(Screen.Camera.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.ConfirmPhoto.route,
            arguments = listOf(navArgument("photoUri") { type = NavType.StringType })
        ) { backStackEntry ->
            // Decoda para usar a URI original
            val encoded = backStackEntry.arguments?.getString("photoUri").orEmpty()
            val uri = Uri.decode(encoded)

            ConfirmPhotoScreen(
                photoUri = uri,
                onBack = { nav.navigate(Screen.Camera.route) { popUpTo(Screen.Home.route) } },
                onSend = {
                    // volta para a Home removendo as telas acima dela
                    nav.popBackStack(Screen.Home.route, inclusive = false)
                }
            )
        }
    }
}
