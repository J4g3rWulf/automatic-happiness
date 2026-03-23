package br.recycleapp.data.model

sealed class ClassificationResult {
    data class Success(val material: Material) : ClassificationResult()
    object Indefinido : ClassificationResult()
    data class Error(val exception: Throwable) : ClassificationResult()
}