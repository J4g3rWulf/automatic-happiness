package br.recycleapp.data.repository

import android.content.Context
import android.net.Uri
import br.recycleapp.data.classifier.TrashClassifier
import br.recycleapp.data.model.ClassificationResult
import br.recycleapp.data.model.Material
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ClassifierRepository(private val context: Context) {

    private val classifier = TrashClassifier(context)

    suspend fun classify(imageUri: Uri): ClassificationResult {
        return withContext(Dispatchers.IO) {
            try {
                val result = classifier.classifyMaterial(imageUri.toString())

                when (result) {
                    "Vidro"    -> ClassificationResult.Success(Material.VIDRO)
                    "Papel"    -> ClassificationResult.Success(Material.PAPEL)
                    "Plástico" -> ClassificationResult.Success(Material.PLASTICO)
                    "Metal"    -> ClassificationResult.Success(Material.METAL)
                    else       -> ClassificationResult.Indefinido
                }
            } catch (e: Exception) {
                ClassificationResult.Error(e)
            }
        }
    }
}