package br.recycleapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import br.recycleapp.R
import br.recycleapp.ui.navigation.Screen
import br.recycleapp.ui.theme.GreenPrimary
import br.recycleapp.ui.theme.GreenNavIndicator

private data class BottomNavItem(
    val route: String,
    val icon: Int, // <-- Recebe direto o R.drawable...
    val label: String
)

private val NAV_ITEMS = listOf(
    BottomNavItem(Screen.Home.route,     R.drawable.ic_home_nav_bar,     "Início"),
    BottomNavItem(Screen.MapTab.route,   R.drawable.ic_map_nav_bar,      "Mapa"),
    BottomNavItem(Screen.Learn.route,    R.drawable.ic_learn_nav_bar,     "Aprender"),
    BottomNavItem(Screen.Programs.route, R.drawable.ic_programs_nav_bar,  "Programas"),
)

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    // Trocamos a NavigationBar teimosa por uma simples Row (Linha)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color.White), // Fundo da barra
        // Isso aqui é a mágica: Agrupa os itens no centro da tela!
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NAV_ITEMS.forEach { item ->
            val selected = currentRoute == item.route

            // Cada botão é apenas um Box clicável agora
            Box(
                modifier = Modifier
                    // 1. ESPAÇO ENTRE OS BOTÕES:
                    .padding(horizontal = 20.dp)

                    // 2. Formato do fundo verde
                    .clip(RoundedCornerShape(6.dp))

                    // 3. Transforma o Box num botão que navega
                    .clickable { onNavigate(item.route) }

                    // 4. Pinta o fundo se estiver selecionado
                    .background(if (selected) GreenNavIndicator else Color.Transparent)

                    // 5. TAMANHO DO BOTÃO (O espaço do verde ao redor do ícone)
                    .padding(horizontal = 8.dp, vertical = 8.dp),

                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter            = painterResource(id = item.icon),
                    contentDescription = item.label,
                    tint               = if (selected) GreenPrimary else Color.Gray,
                    modifier           = Modifier.size(20.dp)
                )
            }
        }
    }
}