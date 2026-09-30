package com.nordic.keyboard

import android.content.Context

class NordicStyleManager(context: Context) {
    fun selected(): NordicStyle = NordicStyles.default
}