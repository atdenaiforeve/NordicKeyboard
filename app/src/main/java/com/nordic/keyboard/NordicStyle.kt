package com.nordic.keyboard

data class NordicStyle(
    val id: String, val name: String,
    val surfaceColor: Int, val panelColor: Int,
    val keyBackground: Int, val pressedKeyBackground: Int, val specialKeyBackground: Int,
    val keyText: Int, val specialText: Int,
    val borderColor: Int, val specialBorderColor: Int,
    val primaryColor: Int, val scanlineColor: Int,
    val scanlineAlpha: Int = 28, val keyRadiusDp: Float = 4f,
    val keyTextSizeSp: Float = 17f, val specialTextSizeSp: Float = 15f,
    val keyTextBold: Boolean = true
)

object NordicStyles {
    val default = NordicStyle(
        "nordic-default", "Nordic Default",
        0xFF06080B.toInt(), 0xFF11151B.toInt(), 0xFF11151B.toInt(),
        0xFF1A5F7A.toInt(), 0xFF0D1117.toInt(), 0xFFB6C2CF.toInt(),
        0xFF38D9FF.toInt(), 0xFF273243.toInt(), 0xFF38D9FF.toInt(),
        0xFF38D9FF.toInt(), 0xFF000000.toInt(), 22
    )
    val cyan = NordicStyle(
        "nordic-cyan", "Nordic Cyan",
        0xFF06080B.toInt(), 0xFF11151B.toInt(), 0xFF06080B.toInt(),
        0xFF1A5F7A.toInt(), 0xFF0B1015.toInt(), 0xFF38D9FF.toInt(),
        0xFFE63946.toInt(), 0xFF1A5F7A.toInt(), 0xFFE63946.toInt(),
        0xFF38D9FF.toInt(), 0xFF000000.toInt(), 30
    )
}
