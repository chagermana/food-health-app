package com.foodhealth.app.util

import android.content.Context
import androidx.annotation.DrawableRes

// Finds res/drawable/<name> at run time. Returns [fallback] if the image has not
// been added yet, so the app never crashes over a missing picture.
@DrawableRes
fun drawableByName(context: Context, name: String, @DrawableRes fallback: Int): Int {
    val id = context.resources.getIdentifier(name, "drawable", context.packageName)
    return if (id != 0) id else fallback
}