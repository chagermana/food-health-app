package com.foodhealth.app.ml

import android.content.Context
import android.graphics.Bitmap
import com.foodhealth.app.data.FoodClass
import org.tensorflow.lite.Interpreter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder

// One food and the model's confidence in it (0f..1f).
data class FoodPrediction(val food: FoodClass, val probability: Float)

// Runs food_classifier.tflite on a photo.
// Model facts from the notebook (Step 11):
//   input  = 1 x 260 x 260 x 3, float32, RAW 0-255 pixels (no /255 here,
//            the normalisation lives inside the model)
//   output = 1 x 13 probabilities in the order of labels.txt / FoodClass
class FoodClassifier(context: Context) : AutoCloseable {

    private val interpreter: Interpreter
    private val inputSize: Int
    private val classCount: Int
    private val inputBuffer: ByteBuffer   // reused for every photo
    private val pixels: IntArray

    init {
        val appContext = context.applicationContext

        // Safety check: labels.txt must match FoodClass, in the same order
        val labels = appContext.assets.open(LABELS_FILE).bufferedReader().use { reader ->
            reader.readLines().map { it.trim() }.filter { it.isNotEmpty() }
        }
        val expected = FoodClass.entries.map { it.label }
        check(labels == expected) {
            "labels.txt does not match FoodClass. labels=$labels expected=$expected"
        }

        // Load the model into a direct buffer (no need to uncompress the asset)
        val modelBytes = appContext.assets.open(MODEL_FILE).use { it.readBytes() }
        val modelBuffer = ByteBuffer.allocateDirect(modelBytes.size).order(ByteOrder.nativeOrder())
        modelBuffer.put(modelBytes)
        modelBuffer.rewind()
        interpreter = Interpreter(modelBuffer)

        // Read the sizes from the model itself instead of hard-coding 260
        inputSize = interpreter.getInputTensor(0).shape()[1]
        classCount = interpreter.getOutputTensor(0).shape()[1]
        check(classCount == expected.size) {
            "Model outputs $classCount classes but FoodClass has ${expected.size}"
        }

        inputBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
            .order(ByteOrder.nativeOrder())
        pixels = IntArray(inputSize * inputSize)
    }

    // All 13 foods, most likely first. Runs off the main thread.
    suspend fun classify(bitmap: Bitmap): List<FoodPrediction> =
        withContext(Dispatchers.Default) { classifyBlocking(bitmap) }

    @Synchronized   // the interpreter handles one photo at a time
    private fun classifyBlocking(bitmap: Bitmap): List<FoodPrediction> {
        // squash to inputSize x inputSize, same as the notebook's resize
        val scaled = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
        scaled.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)
        if (scaled !== bitmap) scaled.recycle()

        // fill the input with R, G, B as raw 0..255 floats
        inputBuffer.rewind()
        for (p in pixels) {
            inputBuffer.putFloat(((p shr 16) and 0xFF).toFloat())   // red
            inputBuffer.putFloat(((p shr 8) and 0xFF).toFloat())    // green
            inputBuffer.putFloat((p and 0xFF).toFloat())            // blue
        }
        inputBuffer.rewind()

        val output = Array(1) { FloatArray(classCount) }
        interpreter.run(inputBuffer, output)

        return FoodClass.entries
            .mapIndexed { i, food -> FoodPrediction(food, output[0][i]) }
            .sortedByDescending { it.probability }
    }

    override fun close() = interpreter.close()

    private companion object {
        const val MODEL_FILE = "food_classifier.tflite"
        const val LABELS_FILE = "labels.txt"
    }
}