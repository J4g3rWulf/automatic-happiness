package br.recycleapp.domain.usecase

import br.recycleapp.domain.model.ClassificationResult
import br.recycleapp.domain.repository.ITrashClassifier

/**
 * Caso de uso que encapsula a lógica de classificação de resíduos.
 * Recebe a interface do domínio - nunca a implementação concreta.
 */
class ClassifyImageUseCase(
    private val classifier: ITrashClassifier
) {
    suspend operator fun invoke(imageUri: String): ClassificationResult {
        return classifier.classify(imageUri)
    }

    fun dispose() {
        classifier.close()
    }
}