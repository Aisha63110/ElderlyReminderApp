
package com.example.elderlyreminderapp

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

object TextSizeHelper {

    fun apply(root: View, context: Context) {
        val scale = AppPreferences.getTextSizeScale(context)
        applyToView(root, scale)
    }

    private fun applyToView(view: View, scale: Float) {

        if (view is TextView) {

            val originalSize = view.getTag(
                R.id.original_text_size
            ) as? Float ?: view.textSize.also {
                view.setTag(R.id.original_text_size, it)
            }

            view.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                originalSize * scale
            )
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                applyToView(view.getChildAt(i), scale)
            }
        }
    }
}
