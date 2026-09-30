package com.nordic.keyboard

import android.content.Context

class StyleStore(context: Context) {
    private val prefs = context.getSharedPreferences("nordic_keyboard", Context.MODE_PRIVATE)

    fun getSelectedStyle(): NordicStyle {
        return when (prefs.getString("style_id", NordicStyles.default.id)) {
            NordicStyles.cyan.id -> NordicStyles.cyan
            else -> NordicStyles.default
        }
    }

    fun setSelectedStyle(style: NordicStyle) {
        prefs.edit().putString("style_id", style.id).apply()
    }
}
