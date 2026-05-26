package br.recycleapp.domain.usecase

import br.recycleapp.domain.model.ClassificationResult
import br.recycleapp.domain.repository.TrashClassifierContract

/**
 * Caso de uso que encapsula a lógica de classificação de resíduos.
 * Recebe a interface do domínio - nunca a implementação concreta.
 */
class ClassifyImageUseCase(
    private val classifier: TrashClassifierContract
) {
    suspend operator fun invoke(imageUri: String): ClassificationResult {
        return classifier.classify(imageUri)
    }

    fun dispose() {
        classifier.close()
    }
}