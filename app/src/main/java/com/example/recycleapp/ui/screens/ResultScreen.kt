package com.example.recycleapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import com.example.recycleapp.R
import com.example.recycleapp.ui.theme.GreenDark
import com.example.recycleapp.ui.theme.GreenPrimary
import com.example.recycleapp.ui.theme.WhiteText
import com.example.recycleapp.util.tryDeleteCapturedCacheFile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    photoUri: String,
    label: String,
    onBackToHome: () -> Unit
) {
    val ctx = LocalContext.current

    fun clearAndBack() {
        // tenta apagar arquivo da câmera (se for da galeria, não acontece nada)
        photoUri.tryDeleteCapturedCacheFile(ctx)
        onBackToHome()
    }

    BackHandler { clearAndBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.result_title),
                        color = WhiteText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { clearAndBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = WhiteText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary
                )
            )
        },
        containerColor = GreenPrimary
    ) { inner ->
        BoxWithConstraints(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            val maxImageWidth = maxWidth - 48.dp
            val frameWidth = maxImageWidth.coerceAtMost(320.dp)
            val frameHeight = 240.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Foto analisada
                Surface(
                    modifier = Modifier
                        .width(frameWidth)
                        .height(frameHeight),
                    shape = RoundedCornerShape(10.dp),
                    color = Color.Transparent,
                    border = BorderStroke(3.dp, Color.White)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(model = photoUri.toUri()),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Resultado
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = GreenDark,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = stringResource(R.string.result_intro),
                        style = MaterialTheme.typography.bodyMedium,
                        color = WhiteText,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = label,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WhiteText,
                        textAlign = TextAlign.Center
                    )

                    // Texto de exemplo (depois a gente troca conforme o retorno real da API)
                    Text(
                        text = stringResource(R.string.result_hint_example, label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = WhiteText,
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = { clearAndBack() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenDark,
                        contentColor = WhiteText
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.result_button_home),
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}
