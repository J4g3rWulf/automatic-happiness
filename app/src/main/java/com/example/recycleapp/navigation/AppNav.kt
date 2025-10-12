package com.example.recycleapp.navigation

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
        fun build(photoUri: String) = "confirm_photo/$photoUri"
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
            val uri = backStackEntry.arguments?.getString("photoUri").orEmpty()
            ConfirmPhotoScreen(
                photoUri = uri,
                onBack = { nav.navigate(Screen.Camera.route) { popUpTo(Screen.Home.route) } },
                onSend = { /* TODO próxima etapa */ }
            )
        }
    }
}