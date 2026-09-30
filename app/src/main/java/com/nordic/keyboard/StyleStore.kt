package com.nordic.keyboard

import android.content.Context

class StyleStore(context: Context) {
    fun isStyleEnabled(): Boolean = true
    fun setStyleEnabled(enabled: Boolean) {}
    fun getSelectedStyle(): NordicStyle = NordicStyles.default
}