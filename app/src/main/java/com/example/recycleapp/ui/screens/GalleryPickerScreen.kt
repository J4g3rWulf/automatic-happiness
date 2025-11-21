package com.example.recycleapp.ui.screens

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier

@Composable
fun GalleryPickerScreen(
    onBack: () -> Unit,
    onPhotoPicked: (String) -> Unit
) {
    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            Log.d("GALLERY", "picked=$uri")
            onPhotoPicked(uri.toString())
        } else {
            Log.d("GALLERY", "cancelled/back")
            onBack()
        }
    }

    LaunchedEffect(Unit) {
        pickMedia.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    // headless (sem UI) para não ter flicker
    Box(Modifier.fillMaxSize())
}