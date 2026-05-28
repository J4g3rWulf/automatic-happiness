package br.recycleapp.ui.screens

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier

/**
 * Tela sem UI (headless) — abre o seletor de mídia do sistema imediatamente ao entrar.
 *
 * Utiliza o Photo Picker moderno (`PickVisualMedia`) para garantir privacidade
 * sem solicitar permissão de armazenamento. O `Box` vazio evita flicker durante
 * a transição. Se o usuário escolher uma imagem, chama [onPhotoPicked] com o URI;
 * se cancelar, chama [onBack].
 *
 * @param onBack       chamado ao fechar o seletor sem escolher imagem
 * @param onPhotoPicked chamado com o URI (String) da imagem selecionada
 */
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