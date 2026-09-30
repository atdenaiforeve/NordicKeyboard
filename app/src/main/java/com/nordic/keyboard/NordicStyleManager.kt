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
}