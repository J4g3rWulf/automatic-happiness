package com.example.recycleapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.recycleapp.R
import com.example.recycleapp.ui.theme.GreenPrimary
import com.example.recycleapp.ui.theme.WhiteText
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadingScreen(
    photoUri: String,
    onBack: () -> Unit,
    onResult: (String) -> Unit
) {
    // botão/gesto de voltar
    BackHandler { onBack() }

    // AQUI seria a chamada real da API. Por enquanto só simulamos.
    LaunchedEffect(photoUri) {
        // TODO: substituir esse delay pela chamada HTTP real
        delay(2000)
        val fakeLabel = "Plástico"   // resultado falso só pra testar fluxo
        onResult(fakeLabel)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.loading_title),
                        color = WhiteText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
        Box(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(color = WhiteText)
                Text(
                    text = stringResource(R.string.loading_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = WhiteText
                )
            }
        }
    }
}
