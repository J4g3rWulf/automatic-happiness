package br.recycleapp.data.repository

import android.content.Context
import android.net.Uri
import br.recycleapp.data.classifier.TrashClassifier
import br.recycleapp.data.model.ClassificationResult
import br.recycleapp.data.model.Material
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositório responsável pela classificação de imagens de resíduos.
 *
 * Gerencia a instância do [TrashClassifier] e aplica regras de negócio como
 * o threshold de confiança mínimo de 60% para aceitar uma classificação.
 */
class ClassifierRepository(context: Context) {

    private val classifier = TrashClassifier(context)

    /**
     * Classifica uma imagem de resíduo.
     *
     * Aplica threshold de confiança: classificações com confiança < 60% são
     * retornadas como [ClassificationResult.Indefinido] para evitar resultados incorretos.
     *
     * @param imageUri URI da imagem a ser classificada
     * @return Resultado da classificação (Success, Indefinido ou Error)
     */
    suspend fun classify(imageUri: Uri): ClassificationResult {
        return withContext(Dispatchers.IO) {
            try {
                val rawResult = classifier.classifyMaterial(imageUri.toString())
                    ?: return@withContext ClassificationResult.Indefinido

                // Threshold de 60%: só aceita classificação se confiança >= 0.60
                if (rawResult.confidence < CONFIDENCE_THRESHOLD) {
                    return@withContext ClassificationResult.Indefinido
                }

                val material = Material.fromMaterialKey(rawResult.materialKey)
                    ?: return@withContext ClassificationResult.Indefinido

                ClassificationResult.Success(
                    material = material,
                    confidence = rawResult.confidence,
                    fineLabel = rawResult.fineLabel
                )
            } catch (e: Exception) {
                ClassificationResult.Error(e)
            }
        }
    }

    /**
     * Libera recursos do classificador.
     * Deve ser chamado quando o repositório não for mais necessário.
     */
    fun close() {
        classifier.close()
    }

    companion object {
        private const val CONFIDENCE_THRESHOLD = 0.40f
    }
}