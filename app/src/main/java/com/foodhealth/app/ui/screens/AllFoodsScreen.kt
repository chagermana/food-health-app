package com.foodhealth.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodhealth.app.R
import com.foodhealth.app.data.FoodClass
import com.foodhealth.app.data.Meal
import com.foodhealth.app.ui.components.MealPhotoCard
import com.foodhealth.app.ui.components.ScreenTopBar
import com.foodhealth.app.ui.theme.Cream
import com.foodhealth.app.ui.theme.Peach
import com.foodhealth.app.util.drawableByName

// Wraps a FoodClass as a Meal so it can go through the same flow as a preset meal.
// MealFlowState.startManual() only needs the id (= the food label) and the image.
fun foodAsMeal(food: FoodClass, imageRes: Int): Meal = Meal(
    id = food.label,
    name = food.displayName,
    imageRes = imageRes,
    calories = 0, proteinG = 0, carbsG = 0, fatG = 0, fibreG = 0,
    category = food.category
)

// AllFoodsScreen.kt : the "See all" page. All 13 foods in a scrollable grid.
@Composable
fun AllFoodsScreen(
    onBack: () -> Unit,
    onFoodClick: (Meal) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Peach, Cream, Cream)))
            .padding(horizontal = 20.dp)
    ) {
        ScreenTopBar(
            title = "All foods",
            onBack = onBack,
            infoText = "Every food the app can recognise. Tap one to choose your portion and see its nutrition."
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(FoodClass.entries, key = { it.label }) { food ->
                val image = drawableByName(context, "food_${food.label}", R.drawable.meal_plate)
                MealPhotoCard(
                    title = food.displayName,
                    imageRes = image,
                    modifier = Modifier.fillMaxWidth(),
                    height = 150.dp,
                    labelSize = 13.sp,
                    onClick = { onFoodClick(foodAsMeal(food, image)) }
                )
            }
        }
    }
}