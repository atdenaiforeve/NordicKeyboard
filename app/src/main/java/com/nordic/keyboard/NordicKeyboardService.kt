package com.nordic.keyboard

import android.graphics.Typeface
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout

class NordicKeyboardService : InputMethodService() {
    private lateinit var styleStore: StyleStore

    override fun onCreate() {
        super.onCreate()
        styleStore = StyleStore(this)
    }

    override fun onCreateInputView(): View {
        val style = styleStore.getSelectedStyle()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(4), dp(4), dp(4), dp(4))
            setBackgroundColor(0xFF06080B.toInt())
        }

        val rows = listOf(
            "QWERTYUIOP",
            "ASDFGHJKL",
            "ZXCVBNM"
        )

        rows.forEach { letters ->
            val row = LinearLayout(this).apply {
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
            }

            letters.forEach { letter ->
                row.addView(makeKey(letter.toString(), style))
            }

            root.addView(row)
        }

        val bottom = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
        }
        bottom.addView(makeKey("⇧", style) { sendKey("SHIFT") })
        bottom.addView(makeKey("SPACE", style, 4f) { currentInputConnection.commitText(" ", 1) })
        bottom.addView(makeKey("⌫", style) { sendKey("BACKSPACE") })
        bottom.addView(makeKey("↵", style) { sendKey("ENTER") })
        root.addView(bottom)

        return root
    }

    private fun makeKey(
        label: String,
        style: NordicStyle,
        weight: Float = 1f,
        action: (() -> Unit)? = null
    ): Button {
        return Button(this).apply {
            text = label
            setTextColor(style.keyText)
            textSize = style.keyTextSizeSp
            typeface = if (style.keyTextBold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            setBackgroundColor(style.keyBackground)
            gravity = Gravity.CENTER
            isAllCaps = false
            setPadding(0, 0, 0, 0)
            layoutParams = LinearLayout.LayoutParams(0, -1, weight).apply {
                setMargins(dp(2), dp(2), dp(2), dp(2))
            }
            setOnClickListener {
                if (action != null) action()
                else currentInputConnection.commitText(label.lowercase(), 1)
            }
        }
    }

    private fun sendKey(type: String) {
        val ic = currentInputConnection ?: return
        when (type) {
            "BACKSPACE" -> ic.deleteSurroundingText(1, 0)
            "ENTER" -> ic.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_ENTER))
            "SHIFT" -> Unit
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
