
package com.example.elderlyreminderapp

import android.content.Context

object AppPreferences {

    private const val PREFS_NAME = "elderly_app_preferences"
    private const val LANGUAGE_KEY = "selected_language"
    private const val TEXT_SIZE_KEY = "selected_text_size"

    fun isKiswahili(context: Context): Boolean {
        val preferences = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        return preferences.getBoolean(LANGUAGE_KEY, false)
    }

    fun saveLanguage(context: Context, kiswahili: Boolean) {
        val preferences = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        preferences.edit()
            .putBoolean(LANGUAGE_KEY, kiswahili)
            .apply()
    }

    fun saveTextSize(context: Context, size: Int) {
        val preferences = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        preferences.edit()
            .putInt(TEXT_SIZE_KEY, size)
            .apply()
    }

    fun getTextSize(context: Context): Int {
        val preferences = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        return preferences.getInt(TEXT_SIZE_KEY, 2)
    }

    fun getTextSizeScale(context: Context): Float {
        return when (getTextSize(context)) {
            0 -> 0.85f
            1 -> 1.0f
            else -> 1.25f
        }
    }
}
