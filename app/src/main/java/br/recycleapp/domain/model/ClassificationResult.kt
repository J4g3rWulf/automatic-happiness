package br.recycleapp.domain.model

/**
 * Resultado da classificação - domínio puro, sem dependências Android.
 *
 * - [Success]: classificação com confiança suficiente (≥ threshold)
 * - [Indefinido]: confiança insuficiente ou material não reconhecido
 * - [Error]: erro durante o processamento
 */
sealed class ClassificationResult {

    data class Success(
        val materialType: MaterialType,
        val confidence: Float,
        val fineLabel: String
    ) : ClassificationResult()

    object Indefinido : ClassificationResult()

    data class Error(val exception: Throwable) : ClassificationResult()
}