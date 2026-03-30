package br.recycleapp.data.classifier

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.core.graphics.scale
import androidx.core.net.toUri
import org.tensorflow.lite.Interpreter
import java.io.Closeable
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * Classificador de resíduos recicláveis usando TensorFlow Lite.
 *
 * Processa imagens de resíduos e identifica o material (vidro, papel, plástico ou metal)
 * com base em um modelo treinado que reconhece 10 classes finas.
 *
 * Implementa [Closeable] para gerenciamento adequado de recursos do interpretador TFLite.
 * Deve ser fechado explicitamente quando não for mais necessário.
 *
 * @property context Contexto da aplicação para acesso a assets e ContentResolver
 */
class TrashClassifier(private val context: Context) : Closeable {

    private var interpreter: Interpreter? = null

    /**
     * Retorna o interpretador existente ou cria um novo se ainda não foi inicializado.
     */
    private fun getInterpreter(): Interpreter {
        if (interpreter == null) {
            interpreter = Interpreter(
                loadModelFile(),
                Interpreter.Options().apply {
                    setNumThreads(4)
                }
            )
        }
        return interpreter!!
    }

    /**
     * Resultado interno da classificação antes de aplicar threshold.
     */
    data class RawClassification(
        val materialKey: String,
        val fineLabel: String,
        val confidence: Float
    )

    /**
     * Classifica a imagem apontada por [uriString].
     *
     * Retorna dados estruturados da classificação incluindo material, confiança e label detalhada.
     * Retorna null se houver erro no carregamento/processamento da imagem.
     *
     * **Nota:** Esta função NÃO aplica threshold de confiança. A camada de Repository
     * é responsável por decidir se o resultado é confiável o suficiente (≥ 60%).
     *
     * @param uriString URI da imagem a ser classificada
     * @return Dados brutos da classificação ou null em caso de erro
     */
    fun classifyMaterial(uriString: String): RawClassification? {
        return try {
            val bitmap = loadBitmapFromUri(uriString) ?: return null

            val resized: Bitmap = bitmap.scale(IMG_SIZE, IMG_SIZE)
            val inputBuffer = convertBitmapToBuffer(resized)

            // Output shape: [1, 10] - vetor de probabilidades para 10 classes
            val output = Array(1) { FloatArray(NUM_CLASSES) }
            getInterpreter().run(inputBuffer, output)

            val probs = output[0]

            // Encontra índice da maior probabilidade
            var bestIdx = 0
            var bestScore = probs[0]
            for (i in 1 until NUM_CLASSES) {
                if (probs[i] > bestScore) {
                    bestScore = probs[i]
                    bestIdx = i
                }
            }

            val fineLabel = FINE_LABELS[bestIdx]
            val materialKey = fineToMaterial(fineLabel)

            Log.d(
                TAG,
                "classIdx=$bestIdx fine=$fineLabel material=$materialKey conf=${"%.3f".format(bestScore)}"
            )

            RawClassification(
                materialKey = materialKey,
                fineLabel = fineLabel,
                confidence = bestScore
            )
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao classificar imagem: $uriString", e)
            null
        }
    }

    override fun close() {
        interpreter?.let {
            it.close()
            interpreter = null
            Log.d(TAG, "Interpreter fechado e liberado")
        }
    }

    private fun loadBitmapFromUri(uriString: String): Bitmap? {
        return try {
            val uri = uriString.toUri()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.isMutableRequired = true
                }
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao carregar bitmap a partir de URI: $uriString", e)
            null
        }
    }

    /**
     * Converte o Bitmap 256x256 em um ByteBuffer de float32,
     * no formato [1, 256, 256, 3], com valores 0–255.
     *
     * Não normalizamos aqui porque o modelo já tem uma camada Rescaling(1./255).
     */
    private fun convertBitmapToBuffer(bitmap: Bitmap): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(1 * IMG_SIZE * IMG_SIZE * 3 * 4)
        buffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(IMG_SIZE * IMG_SIZE)
        bitmap.getPixels(
            intValues,
            0,
            bitmap.width,
            0,
            0,
            bitmap.width,
            bitmap.height
        )

        for (pixel in intValues) {
            val r = ((pixel shr 16) and 0xFF).toFloat()
            val g = ((pixel shr 8) and 0xFF).toFloat()
            val b = (pixel and 0xFF).toFloat()

            buffer.putFloat(r)
            buffer.putFloat(g)
            buffer.putFloat(b)
        }

        buffer.rewind()
        return buffer
    }

    private fun loadModelFile(): MappedByteBuffer {
        try {
            context.assets.openFd(MODEL_NAME).use { fileDescriptor ->
                FileInputStream(fileDescriptor.fileDescriptor).use { input ->
                    val fileChannel = input.channel
                    val startOffset = fileDescriptor.startOffset
                    val declaredLength = fileDescriptor.declaredLength
                    return fileChannel.map(
                        FileChannel.MapMode.READ_ONLY,
                        startOffset,
                        declaredLength
                    )
                }
            }
        } catch (e: IOException) {
            throw RuntimeException("Erro ao carregar modelo TFLite: $MODEL_NAME", e)
        }
    }

    companion object {
        private const val TAG = "TrashClassifier"
        private const val IMG_SIZE = 256
        private const val NUM_CLASSES = 10
        private const val MODEL_NAME = "model_v03.tflite"

        private val FINE_LABELS = arrayOf(
            "glass_bottle",           // 0
            "glass_cup",              // 1
            "metal_can",              // 2
            "paper_bag",              // 3
            "paper_ball",             // 4
            "paper_milk_package",     // 5
            "paper_package",          // 6
            "plastic_bottle",         // 7
            "plastic_cup",            // 8
            "plastic_transparent_cup" // 9
        )

        private fun fineToMaterial(fineLabel: String): String =
            when (fineLabel) {
                "glass_bottle", "glass_cup" -> "glass"
                "metal_can" -> "metal"
                "paper_bag", "paper_ball", "paper_package", "paper_milk_package" -> "paper"
                "plastic_bottle", "plastic_cup", "plastic_transparent_cup" -> "plastic"
                else -> "unknown"
            }
    }
}