package com.foodhealth.app.ml

import android.graphics.Bitmap
import kotlinx.coroutines.delay

data class Recognition(
    val mealId: String,         // this must equal Meal.id in a line in label.txt in the future
    val confidence: Float
)

interface MealRecognizer{
    // photo = picture taken by camera (null for now)
    // returns null when Model is not confident
    // "suspend" --> running in bg
    suspend fun recognize(photo: Bitmap?): Recognition?
}

// Place holder for how the model would think and answer
class FakeMealRecognizer : MealRecognizer {
    override suspend fun recognize(photo: Bitmap?): Recognition? {
        delay(1200) // pretending the model is thinking
        return Recognition(mealId = "plate", confidence = 0.92f)
    }
}