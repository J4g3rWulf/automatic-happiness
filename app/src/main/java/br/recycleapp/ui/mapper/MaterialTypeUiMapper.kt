package br.recycleapp.ui.mapper

import br.recycleapp.domain.model.MaterialType

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