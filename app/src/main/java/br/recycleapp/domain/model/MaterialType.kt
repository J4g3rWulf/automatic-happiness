package br.recycleapp.domain.model

/**
 * Enum puro Kotlin representando os tipos de materiais recicláveis.
 * Sem dependências Android - pode ser usado em testes unitários puros.
 */
enum class MaterialType {
    GLASS,
    PAPER,
    PLASTIC,
    METAL,
    UNKNOWN;

    companion object {
        /**
         * Converte a chave interna do classificador ("glass", "paper", etc.)
         * para o enum correspondente.
         */
        fun fromMaterialKey(key: String): MaterialType = when (key) {
            "glass"   -> GLASS
            "paper"   -> PAPER
            "plastic" -> PLASTIC
            "metal"   -> METAL
            else      -> UNKNOWN
        }
    }
}