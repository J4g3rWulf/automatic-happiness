package br.recycleapp.data.model

import androidx.compose.ui.graphics.Color

/**
 * Representa os materiais recicláveis que o modelo pode classificar.
 *
 * Separa dados de domínio ([materialKey]) de dados de apresentação ([labelPt], [color]).
 * A propriedade [materialKey] é usada internamente pela camada de classificação,
 * enquanto [labelPt] e [color] são usados pela UI.
 *
 * @property materialKey Identificador interno do material ("glass", "paper", "plastic", "metal")
 * @property labelPt Label em português para exibição na UI
 * @property color Cor associada ao material para UI
 */
enum class Material(
    val materialKey: String,
    val labelPt: String,
    val color: Color
) {
    VIDRO(
        materialKey = "glass",
        labelPt = "Vidro",
        color = Color(0xFF43A047)
    ),
    PAPEL(
        materialKey = "paper",
        labelPt = "Papel",
        color = Color(0xFF1E88E5)
    ),
    PLASTICO(
        materialKey = "plastic",
        labelPt = "Plástico",
        color = Color(0xFFE53935)
    ),
    METAL(
        materialKey = "metal",
        labelPt = "Metal",
        color = Color(0xFFFDD835)
    );

    companion object {
        /**
         * Converte uma chave de material ("glass", "paper", etc.) para o enum correspondente.
         * Retorna null se a chave não for reconhecida.
         */
        fun fromMaterialKey(key: String): Material? =
            entries.find { it.materialKey == key }
    }
}