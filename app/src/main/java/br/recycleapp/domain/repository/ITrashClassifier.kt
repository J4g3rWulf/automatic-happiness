package br.recycleapp.domain.repository

import br.recycleapp.domain.model.ClassificationResult

/**
 * Contrato da camada de domínio para classificação de resíduos.
 * A URI é tratada como simples String identificadora - sem import Android.
 */
interface ITrashClassifier {

    /**
     * Classifica a imagem apontada por [imageUri].
     * @param imageUri URI da imagem como String
     */
    suspend fun classify(imageUri: String): ClassificationResult

    /** Libera recursos do modelo de IA. */
    fun close()
}