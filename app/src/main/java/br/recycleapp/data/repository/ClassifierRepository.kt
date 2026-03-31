package br.recycleapp.data.repository

import android.content.Context
import br.recycleapp.data.classifier.TrashClassifier
import br.recycleapp.domain.model.ClassificationResult   // ← ALTERADO: agora vem do domain
import br.recycleapp.domain.model.MaterialType           // ← NOVO
import br.recycleapp.domain.repository.ITrashClassifier  // ← NOVO: implementa a interface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementação concreta do classificador.
 * Depende do Context e do modelo TFLite - por isso fica na camada data.
 * Implementa [ITrashClassifier] definido no domínio.
 */
class ClassifierRepository(
    context: Context
) : ITrashClassifier {

    private val classifier = TrashClassifier(context)

    override suspend fun classify(imageUri: String): ClassificationResult {
        return withContext(Dispatchers.IO) {
            try {
                val rawResult = classifier.classifyMaterial(imageUri)
                    ?: return@withContext ClassificationResult.Indefinido

                if (rawResult.confidence < CONFIDENCE_THRESHOLD) {
                    return@withContext ClassificationResult.Indefinido
                }

                val materialType = MaterialType.fromMaterialKey(rawResult.materialKey)

                if (materialType == MaterialType.UNKNOWN) {
                    return@withContext ClassificationResult.Indefinido
                }

                ClassificationResult.Success(
                    materialType = materialType,
                    confidence   = rawResult.confidence,
                    fineLabel    = rawResult.fineLabel
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