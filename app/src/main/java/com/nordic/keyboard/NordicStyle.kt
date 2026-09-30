package com.nordic.keyboard

data class NordicStyle(
    val id: String, val name: String, val keyBackground: Int, val keyText: Int,
    val keyRadiusDp: Float = 8f, val keyTextSizeSp: Float = 18f, val keyTextBold: Boolean = false
)

object NordicStyles {
    val default = NordicStyle("nordic-default", "Nordic Default", 0xFF11151B.toInt(), 0xFFB6C2CF.toInt())
    val cyan = NordicStyle("nordic-cyan", "Nordic Cyan", 0xFF06080B.toInt(), 0xFF38D9FF.toInt(), 7f, 18f, true)
}
