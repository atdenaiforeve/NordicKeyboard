package com.nordic.keyboard

data class NordicStyle(
    val id: String,
    val name: String,
    val keyBackground: Int,
    val keyText: Int,
    val keyRadiusDp: Float = 8f,
    val keyTextSizeSp: Float = 18f,
    val keyTextBold: Boolean = false
)

object NordicStyles {
    val default = NordicStyle(
        id = "nordic-default",
        name = "Nordic Default",
        keyBackground = 0xFF11151B.toInt(),
        keyText = 0xFFB6C2CF.toInt()
    )

    val cyan = NordicStyle(
        id = "nordic-cyan",
        name = "Nordic Cyan",
        keyBackground = 0xFF06080B.toInt(),
        keyText = 0xFF38D9FF.toInt(),
        keyRadiusDp = 7f,
        keyTextSizeSp = 18f,
        keyTextBold = true
    )
}
