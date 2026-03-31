package br.recycleapp.ui.mapper

import androidx.compose.ui.graphics.Color
import br.recycleapp.domain.model.MaterialType
import br.recycleapp.ui.theme.*

/**
 * Mappers de UI para MaterialType.
 * Mantém a camada de domínio livre de dependências Android/Compose.
 */

/** Retorna o nome em português para exibição na UI. */
fun MaterialType.toLabelPt(): String = when (this) {
    MaterialType.GLASS   -> "Vidro"
    MaterialType.PAPER   -> "Papel"
    MaterialType.PLASTIC -> "Plástico"
    MaterialType.METAL   -> "Metal"
    MaterialType.UNKNOWN -> "Indefinido"
}

/** Retorna a cor associada ao material para uso nos componentes de UI. */
@Suppress("unused") // será usado nas próximas estapas
fun MaterialType.toColor(): Color = when (this) {
    MaterialType.GLASS   -> GlassBg
    MaterialType.PAPER   -> PaperBg
    MaterialType.PLASTIC -> PlasticBg
    MaterialType.METAL   -> MetalBg
    MaterialType.UNKNOWN -> UnknownBg
}