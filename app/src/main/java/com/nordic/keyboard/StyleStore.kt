package com.nordic.keyboard

import android.content.Context

class StyleStore(context: Context) {
    private val prefs = context.getSharedPreferences("nordic_keyboard", Context.MODE_PRIVATE)

    fun isStyleEnabled(): Boolean = prefs.getBoolean("style_enabled", true)

    fun setStyleEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("style_enabled", enabled).apply()
    }

    fun getSelectedStyle(): NordicStyle =
        if (!isStyleEnabled()) NordicStyles.default
        else when (prefs.getString("style_id", NordicStyles.default.id)) {
            NordicStyles.cyan.id -> NordicStyles.cyan
            else -> NordicStyles.default
        }

    fun setSelectedStyle(style: NordicStyle) {
        prefs.edit().putString("style_id", style.id).apply()
    }
}