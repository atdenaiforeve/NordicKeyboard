package com.nordic.keyboard

import android.content.Context

class NordicStyleManager(private val context: Context) {
    private val store = context.getSharedPreferences("nordic_styles", Context.MODE_PRIVATE)
    private val styles = linkedMapOf(
        NordicStyles.default.id to NordicStyles.default,
        NordicStyles.cyan.id to NordicStyles.cyan
    )

    fun all(): List<NordicStyle> = styles.values.toList()
    fun selected(): NordicStyle = styles[store.getString("selected_style", NordicStyles.default.id)] ?: NordicStyles.default
    fun setSelected(id: String) { if (styles.containsKey(id)) store.edit().putString("selected_style", id).apply() }

    fun saveCustom(style: NordicStyle) {
        store.edit().apply {
            putString("custom_name", style.name)
            putInt("surface", style.surfaceColor)
            putInt("panel", style.panelColor)
            putInt("key", style.keyBackground)
            putInt("pressed", style.pressedKeyBackground)
            putInt("special", style.specialKeyBackground)
            putInt("text", style.keyText)
            putInt("special_text", style.specialText)
            putInt("border", style.borderColor)
            putInt("special_border", style.specialBorderColor)
            putInt("primary", style.primaryColor)
            putInt("scanline", style.scanlineColor)
            putInt("scanline_alpha", style.scanlineAlpha)
            putFloat("radius", style.keyRadiusDp)
            putFloat("text_size", style.keyTextSizeSp)
            putFloat("special_size", style.specialTextSizeSp)
            putBoolean("bold", style.keyTextBold)
            putString("selected_style", "nordic-custom")
            apply()
        }
    }

    fun customOrNull(): NordicStyle? {
        if (!store.contains("custom_name")) return null
        return NordicStyle(
            "nordic-custom", store.getString("custom_name", "Custom Nordic")!!,
            store.getInt("surface", NordicStyles.default.surfaceColor),
            store.getInt("panel", NordicStyles.default.panelColor),
            store.getInt("key", NordicStyles.default.keyBackground),
            store.getInt("pressed", NordicStyles.default.pressedKeyBackground),
            store.getInt("special", NordicStyles.default.specialKeyBackground),
            store.getInt("text", NordicStyles.default.keyText),
            store.getInt("special_text", NordicStyles.default.specialText),
            store.getInt("border", NordicStyles.default.borderColor),
            store.getInt("special_border", NordicStyles.default.specialBorderColor),
            store.getInt("primary", NordicStyles.default.primaryColor),
            store.getInt("scanline", NordicStyles.default.scanlineColor),
            store.getInt("scanline_alpha", NordicStyles.default.scanlineAlpha),
            store.getFloat("radius", NordicStyles.default.keyRadiusDp),
            store.getFloat("text_size", NordicStyles.default.keyTextSizeSp),
            store.getFloat("special_size", NordicStyles.default.specialTextSizeSp),
            store.getBoolean("bold", true)
        )
    }
}