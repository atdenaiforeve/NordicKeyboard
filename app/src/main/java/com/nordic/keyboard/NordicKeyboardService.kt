package com.nordic.keyboard

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection

class NordicKeyboardService : InputMethodService() {
    private lateinit var styleStore: StyleStore
    private var caps = false

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
        listOf("QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM").forEach { letters ->
            val row = LinearLayout(this).apply {
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
            }
            letters.forEach { letter -> row.addView(makeKey(letter.toString(), style)) }
            root.addView(row)
        }
        val bottom = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
        }
        bottom.addView(makeKey("⇧", style) { caps = !caps; setInputView(onCreateInputView()) })
        bottom.addView(makeKey("SPACE", style, 4f) { currentInputConnection?.commitText(" ", 1) })
        bottom.addView(makeKey("⌫", style) { currentInputConnection?.deleteSurroundingText(1, 0) })
        bottom.addView(makeKey("↵", style) { sendEnter() })
        root.addView(bottom)
        return root
    }

    private fun makeKey(label: String, style: NordicStyle, weight: Float = 1f, action: (() -> Unit)? = null): Button =
        Button(this).apply {
            text = if (label.length == 1 && label[0].isLetter()) (if (caps) label else label.lowercase()) else label
            setTextColor(style.keyText)
            textSize = style.keyTextSizeSp
            typeface = if (style.keyTextBold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            background = GradientDrawable().apply {
                setColor(style.keyBackground)
                cornerRadius = dp(style.keyRadiusDp)
            }
            gravity = Gravity.CENTER
            isAllCaps = false
            stateListAnimator = null
            setPadding(0, 0, 0, 0)
            layoutParams = LinearLayout.LayoutParams(0, -1, weight).apply { setMargins(dp(2), dp(2), dp(2), dp(2)) }
            setOnClickListener {
                action?.invoke() ?: currentInputConnection?.commitText(if (caps) label else label.lowercase(), 1)
            }
        }

    private fun sendEnter() {
        val ic = currentInputConnection ?: return
        val action = currentInputEditorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION) ?: EditorInfo.IME_ACTION_NONE
        if (action != EditorInfo.IME_ACTION_NONE && !ic.performEditorAction(action)) sendEnterKey(ic)
        else if (action == EditorInfo.IME_ACTION_NONE) sendEnterKey(ic)
    }

    private fun sendEnterKey(ic: InputConnection) {
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
    private fun dp(value: Float): Float = value * resources.displayMetrics.density
}
