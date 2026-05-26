package br.recycleapp.data.classifier

import android.content.Context
import br.recycleapp.domain.model.ClassificationResult
import br.recycleapp.domain.model.MaterialType
import br.recycleapp.domain.repository.TrashClassifierContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementação concreta do classificador.
 * Depende do Context e do modelo TFLite - por isso fica na camada data.
 * Implementa [br.recycleapp.domain.repository.TrashClassifierContract] definido no domínio.
 */
class ClassifierRepository(
    context: Context
) : TrashClassifierContract {

    private val classifier = TrashClassifier(context)

    override suspend fun classify(imageUri: String): ClassificationResult {
        return withContext(Dispatchers.IO) {
            try {
                val rawResult = classifier.classifyMaterial(imageUri)
                    ?: return@withContext ClassificationResult.Indefinido

                if (rawResult.confidence < CONFIDENCE_THRESHOLD) {
                    return@withContext ClassificationResult.Indefinido
                }

                val materialType = MaterialType.Companion.fromMaterialKey(rawResult.materialKey)

                if (materialType == MaterialType.UNKNOWN) {
                    return@withContext ClassificationResult.Indefinido
                }

                ClassificationResult.Success(
                    materialType = materialType,
                    confidence = rawResult.confidence,
                    fineLabel = rawResult.fineLabel
                )
            } catch (e: Exception) {
                ClassificationResult.Error(e)
            }
        }
    }

    override fun close() {
        classifier.close()
    }

    companion object {
        private const val CONFIDENCE_THRESHOLD = 0.40f
    }
}