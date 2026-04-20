package br.recycleapp.ui.screens

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import br.recycleapp.R
import br.recycleapp.ui.theme.GreenPrimary

// ── Modelo de dados ───────────────────────────────────────────────────────────

/**
 * Representa um programa ou parceria de reciclagem.
 *
 * @param name        nome do programa
 * @param description descrição curta
 * @param color       cor de fundo do card (identidade visual do programa)
 * @param url         link para a página oficial
 */
private data class Program(
    val name: String,
    val description: String,
    val color: Color,
    val url: String
)

/**
 * Seção de programas agrupados por categoria.
 *
 * @param title    título da seção (ex: "Troque e Ganhe")
 * @param programs lista de programas da seção
 */
private data class ProgramSection(
    val title: String,
    val programs: List<Program>
)

// ── Dados dos programas ───────────────────────────────────────────────────────

private val PROGRAMS = listOf(
    ProgramSection(
        title = "Troque e Ganhe",
        programs = listOf(
            Program(
                name        = "Recicla Niterói\nRecicla PUDs",
                description = "Pontos voluntários com benefícios",
                color       = Color(0xFF1A3A7E),
                url         = "https://reciclaniteroi.com.br"
            ),
            Program(
                name        = "CLIN",
                description = "Companhia de Limpeza de Niterói",
                color       = Color(0xFFE8450A),
                url         = "https://www.clin.rj.gov.br"
            ),
        )
    ),
    ProgramSection(
        title = "Coleta Seletiva Municipal",
        programs = listOf(
            Program(
                name        = "Recicla Niterói\nRecicla PUDs",
                description = "Coleta domiciliar seletiva",
                color       = Color(0xFF1A3A7E),
                url         = "https://reciclaniteroi.com.br"
            ),
        )
    ),
    ProgramSection(
        title = "Impacto Social e Comunidades",
        programs = listOf(
            Program(
                name        = "CLIN",
                description = "Companhia de Limpeza de Niterói",
                color       = Color(0xFFE8450A),
                url         = "https://www.clin.rj.gov.br"
            ),
            Program(
                name        = "Light\nRecicla",
                description = "Desconto na conta de energia",
                color       = Color(0xFF1C7B6E),
                url         = "https://lightrecicla.com.br"
            ),
            Program(
                name        = "Recicla\nSão Gonçalo",
                description = "Programa municipal de reciclagem",
                color       = Color(0xFFEAEAEA),
                url         = "https://www.saogoncalo.rj.gov.br"
            ),
        )
    ),
)

// ── Tela ─────────────────────────────────────────────────────────────────────

/**
 * Tela de programas e parcerias de reciclagem.
 *
 * Acessível via aba "Programas" da bottom nav. Lista programas municipais,
 * estaduais e de empresas agrupados por categoria. Tocar em um card abre
 * a página oficial do programa no navegador.
 *
 * Os cards exibem a cor de identidade visual de cada programa. Futuramente
 * podem receber drawables com logos reais via campo [Program.color].
 */
@Composable
fun ProgramsScreen() {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GreenPrimary)
    ) {

        // ── Arte decorativa superior ──────────────────────────────────────
        Image(
            painter            = painterResource(R.drawable.art_top_v1),
            contentDescription = null,
            contentScale       = ContentScale.FillWidth,
            alignment          = Alignment.TopCenter,
            modifier           = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        )

        // ── Arte decorativa inferior ──────────────────────────────────────
        Image(
            painter            = painterResource(R.drawable.art_bottom_home),
            contentDescription = null,
            contentScale       = ContentScale.FillWidth,
            alignment          = Alignment.BottomCenter,
            modifier           = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(200.dp)
                .offset(y = 24.dp)
        )

        // ── Conteúdo principal ────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
        ) {

            Spacer(Modifier.height(48.dp))

            // Título
            Text(
                text       = "Programas e parcerias",
                fontSize   = 32.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White,
                lineHeight = 38.sp,
                style      = MaterialTheme.typography.headlineLarge
            )

            Spacer(Modifier.height(32.dp))

            // Seções de programas
            PROGRAMS.forEachIndexed { index, section ->
                if (index > 0) {
                    Spacer(Modifier.height(24.dp))
                }

                // ── Cabeçalho da seção ────────────────────────────────────
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier  = Modifier.weight(1f),
                        color     = Color.White.copy(alpha = 0.4f),
                        thickness = 1.dp
                    )
                    Text(
                        text      = section.title,
                        fontSize  = 13.sp,
                        color     = Color.White.copy(alpha = 0.85f),
                        modifier  = Modifier.padding(horizontal = 12.dp),
                        textAlign = TextAlign.Center
                    )
                    HorizontalDivider(
                        modifier  = Modifier.weight(1f),
                        color     = Color.White.copy(alpha = 0.4f),
                        thickness = 1.dp
                    )
                }

                Spacer(Modifier.height(14.dp))

                // ── Carrossel de cards ────────────────────────────────────
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding        = PaddingValues(end = 8.dp)
                ) {
                    items(section.programs) { program ->
                        ProgramCard(
                            program = program,
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, program.url.toUri())
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }

            // Espaço para não sobrepor a ilustração
            Spacer(Modifier.height(220.dp))
        }
    }
}

// ── Card de programa ──────────────────────────────────────────────────────────

@Composable
private fun ProgramCard(
    program: Program,
    onClick: () -> Unit
) {
    // Usa texto branco sobre fundos escuros e texto escuro sobre fundos claros
    val textColor = if (program.color.luminance() > 0.5f) Color(0xFF1A1A1A) else Color.White

    Surface(
        modifier = Modifier
            .size(width = 130.dp, height = 90.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = ripple(color = Color.Black.copy(alpha = 0.1f)),
                onClick           = onClick
            ),
        shape           = RoundedCornerShape(12.dp),
        color           = program.color,
        shadowElevation = 6.dp
    ) {
        Box(
            modifier         = Modifier
                .fillMaxSize()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = program.name,
                fontSize   = 13.sp,
                fontWeight = FontWeight.Bold,
                color      = textColor,
                textAlign  = TextAlign.Center,
                lineHeight = 17.sp
            )
        }
    }
}

// ── Helper ────────────────────────────────────────────────────────────────────

/** Calcula luminância relativa da cor para determinar contraste de texto. */
private fun Color.luminance(): Float {
    val r = red.toDouble()
    val g = green.toDouble()
    val b = blue.toDouble()
    return (0.2126 * r + 0.7152 * g + 0.0722 * b).toFloat()
}