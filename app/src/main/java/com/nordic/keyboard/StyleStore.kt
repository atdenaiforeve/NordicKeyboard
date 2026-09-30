package com.nordic.keyboard

import android.content.Context

class StyleStore(context: Context) {
    private val prefs = context.getSharedPreferences("nordic_keyboard", Context.MODE_PRIVATE)
    private val stylePrefs = context.getSharedPreferences("nordic_styles", Context.MODE_PRIVATE)

    fun isStyleEnabled(): Boolean = prefs.getBoolean("style_enabled", true)
    fun setStyleEnabled(enabled: Boolean) { prefs.edit().putBoolean("style_enabled", enabled).apply() }

    fun getSelectedStyle(): NordicStyle {
        if (!isStyleEnabled()) return NordicStyles.default
        return when (stylePrefs.getString("selected_style", NordicStyles.default.id)) {
            NordicStyles.cyan.id -> NordicStyles.cyan
            "nordic-custom" -> customStyle()
            else -> NordicStyles.default
        }
    }

    private fun customStyle(): NordicStyle = NordicStyle(
        "nordic-custom", stylePrefs.getString("custom_name", "Custom Nordic")!!,
        stylePrefs.getInt("surface", NordicStyles.default.surfaceColor),
        stylePrefs.getInt("panel", NordicStyles.default.panelColor),
        stylePrefs.getInt("key", NordicStyles.default.keyBackground),
        stylePrefs.getInt("pressed", NordicStyles.default.pressedKeyBackground),
        stylePrefs.getInt("special", NordicStyles.default.specialKeyBackground),
        stylePrefs.getInt("text", NordicStyles.default.keyText),
        stylePrefs.getInt("special_text", NordicStyles.default.specialText),
        stylePrefs.getInt("border", NordicStyles.default.borderColor),
        stylePrefs.getInt("special_border", NordicStyles.default.specialBorderColor),
        stylePrefs.getInt("primary", NordicStyles.default.primaryColor),
        stylePrefs.getInt("scanline", NordicStyles.default.scanlineColor),
        stylePrefs.getInt("scanline_alpha", NordicStyles.default.scanlineAlpha),
        stylePrefs.getFloat("radius", NordicStyles.default.keyRadiusDp),
        stylePrefs.getFloat("text_size", NordicStyles.default.keyTextSizeSp),
        stylePrefs.getFloat("special_size", NordicStyles.default.specialTextSizeSp),
        stylePrefs.getBoolean("bold", true)
    )
}