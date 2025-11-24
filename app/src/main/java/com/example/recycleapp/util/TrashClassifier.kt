package com.example.recycleapp.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.core.graphics.scale
import androidx.core.net.toUri
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class TrashClassifier(private val context: Context) {

    // Interpreter lazy para só carregar o modelo quando for realmente usar
    private val interpreter: Interpreter by lazy {
        Interpreter(
            loadModelFile(),
            Interpreter.Options().apply {
                setNumThreads(4) // ajusta se quiser
            }
        )
    }

    /**
     * Classifica a imagem apontada por [uriString] e retorna
     * o MATERIAL em português para mostrar na UI:
     *  - "Vidro", "Metal", "Papel" ou "Plástico"
     *  - Em caso de erro, retorna "Indefinido"
     */
    fun classifyMaterial(uriString: String): String {
        return try {
            val bitmap = loadBitmapFromUri(uriString)
                ?: return "Indefinido"

            // Redimensiona para 256x256 (tamanho esperado pelo modelo)
            val resized: Bitmap = bitmap.scale(IMG_SIZE, IMG_SIZE)

            val inputBuffer = convertBitmapToBuffer(resized)

            // Saída [1, 7] => vetor de probabilidades
            val output = Array(1) { FloatArray(NUM_CLASSES) }
            interpreter.run(inputBuffer, output)

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
            val materialDisplay = materialKeyToDisplay(materialKey)

            Log.d(
                TAG,
                "classIdx=$bestIdx fine=$fineLabel material=$materialKey ($materialDisplay) conf=${"%.3f".format(
                    bestScore
                )}"
            )

            materialDisplay
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao classificar imagem: $uriString", e)
            "Indefinido"
        }
    }

    // ---------- Helpers internos ----------

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
     * Importante: NÃO dividimos por 255 aqui, porque o modelo
     * já tem uma camada Rescaling(1./255).
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

        // Itera diretamente pelos pixels, sem precisar de x/y
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

        // Índices do modelo -> classes finas (seguir a MESMA ordem do treino)
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

        // Classe fina -> material (4 grupos)
        private fun fineToMaterial(fineLabel: String): String =
            when (fineLabel) {
                "glass_bottle", "glass_cup" ->
                    "glass"

                "metal_can" ->
                    "metal"

                "paper_bag", "paper_ball", "paper_package", "paper_milk_package" ->
                    "paper"

                "plastic_bottle", "plastic_cup", "plastic_transparent_cup" ->
                    "plastic"

                else -> "unknown"
            }

        // Material -> label para UI (PT-BR)
        fun materialKeyToDisplay(materialKey: String): String =
            when (materialKey) {
                "glass"   -> "Vidro"
                "metal"   -> "Metal"
                "paper"   -> "Papel"
                "plastic" -> "Plástico"
                else      -> "Indefinido"
            }
    }
}