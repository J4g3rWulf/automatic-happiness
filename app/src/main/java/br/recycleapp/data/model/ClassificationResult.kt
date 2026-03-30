package br.recycleapp.data.model

/**
 * Resultado da classificação de uma imagem.
 *
 * - [Success]: Classificação bem-sucedida com confiança suficiente (≥ 60%)
 * - [Indefinido]: Modelo não conseguiu classificar com confiança suficiente (< 60%)
 * - [Error]: Erro durante o processamento da imagem
 */
sealed class ClassificationResult {
    /**
     * Classificação bem-sucedida.
     *
     * @property material Material classificado
     * @property confidence Confiança da classificação (0.0 a 1.0)
     * @property fineLabel Label detalhada do modelo (ex: "plastic_bottle", "glass_cup")
     */
    data class Success(
        val material: Material,
        val confidence: Float,
        val fineLabel: String
    ) : ClassificationResult()

    /**
     * Classificação com confiança insuficiente (< 60%) ou material não reconhecido.
     * O modelo retornou um resultado, mas não é confiável o suficiente.
     */
    object Indefinido : ClassificationResult()

    /**
     * Erro durante o processamento da imagem ou inferência do modelo.
     *
     * @property exception Exceção que causou o erro
     */
    data class Error(val exception: Throwable) : ClassificationResult()
}