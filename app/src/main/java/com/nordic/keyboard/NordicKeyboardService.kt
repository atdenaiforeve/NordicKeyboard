package com.nordic.keyboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class NordicKeyboardService : InputMethodService() {
    private lateinit var styleStore: StyleStore
    private var caps = false

    override fun onCreate() {
        super.onCreate()
        styleStore = StyleStore(this)
    }

    override fun onCreateInputView(): View {
        val nordicStyle = styleStore.getSelectedStyle()
        val root = NordicKeyboardLayout(this, nordicStyle).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(5), dp(4), dp(5), dp(5))
            setBackgroundColor(nordicStyle.surfaceColor)
        }

        val header = TextView(this).apply {
            text = "NORDIC // INPUT CORE    [" + nordicStyle.name.uppercase() + "]"
            setTextColor(nordicStyle.keyText)
            textSize = 9f
            typeface = Typeface.MONOSPACE
            letterSpacing = 0.08f
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(7), 0, dp(7), 0)
            layoutParams = LinearLayout.LayoutParams(-1, dp(24))
            background = GradientDrawable().apply {
                setColor(nordicStyle.panelColor)
                setStroke(dp(1), nordicStyle.borderColor)
                cornerRadius = dp(3)
            }
        }
        root.addView(header)
        root.addView(View(this).apply { layoutParams = LinearLayout.LayoutParams(-1, dp(2)) })

        listOf("QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM").forEach { letters ->
            val row = LinearLayout(this).apply {
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
            }
            letters.forEach { letter -> row.addView(makeKey(letter.toString(), nordicStyle)) }
            root.addView(row)
        }

        val bottom = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
        }
        bottom.addView(makeKey("⇧", nordicStyle, special = true) {
            caps = !caps
            setInputView(onCreateInputView())
        })
        bottom.addView(makeKey("SPACE", nordicStyle, 4f) { currentInputConnection?.commitText(" ", 1) })
        bottom.addView(makeKey("⌫", nordicStyle, special = true) { currentInputConnection?.deleteSurroundingText(1, 0) })
        bottom.addView(makeKey("↵", nordicStyle, special = true) { sendEnter() })
        root.addView(bottom)
        return root
    }

    private fun makeKey(label: String, nordicStyle: NordicStyle, weight: Float = 1f, special: Boolean = false, action: (() -> Unit)? = null): Button =
        Button(this).apply {
            text = if (label.length == 1 && label[0].isLetter()) {
                if (caps) label else label.lowercase()
            } else label
            setTextColor(if (special) nordicStyle.specialText else nordicStyle.keyText)
            textSize = if (special) nordicStyle.specialTextSizeSp else nordicStyle.keyTextSizeSp
            typeface = Typeface.MONOSPACE
            gravity = Gravity.CENTER
            isAllCaps = false
            stateListAnimator = null
            setPadding(0, 0, 0, 0)

            val normal = GradientDrawable().apply {
                setColor(if (special) nordicStyle.specialKeyBackground else nordicStyle.keyBackground)
                cornerRadius = dp(nordicStyle.keyRadiusDp)
                setStroke(dp(1), if (special) nordicStyle.specialBorderColor else nordicStyle.borderColor)
            }
            val pressed = GradientDrawable().apply {
                setColor(nordicStyle.pressedKeyBackground)
                cornerRadius = dp(nordicStyle.keyRadiusDp)
                setStroke(dp(1), nordicStyle.keyText)
            }
            background = normal
            layoutParams = LinearLayout.LayoutParams(0, -1, weight).apply {
                setMargins(dp(2), dp(2), dp(2), dp(2))
            }
            setOnTouchListener { view, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> view.background = pressed
                    android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> view.background = normal
                }
                false
            }
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

private class NordicKeyboardLayout(context: Context, private val nordicStyle: NordicStyle) : LinearLayout(context) {
    init { setWillNotDraw(false) }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val density = resources.displayMetrics.density
        val paint = android.graphics.Paint().apply {
            color = nordicStyle.scanlineColor
            alpha = nordicStyle.scanlineAlpha
            strokeWidth = 1f
        }
        var y = 0f
        val gap = (4f * density).coerceAtLeast(3f)
        while (y < height) {
            canvas.drawLine(0f, y, width.toFloat(), y, paint)
            y += gap
        }
        val edge = android.graphics.Paint().apply {
            color = nordicStyle.primaryColor
            alpha = 90
            strokeWidth = density
            style = android.graphics.Paint.Style.STROKE
        }
        canvas.drawRect(1f, 1f, width - 1f, height - 1f, edge)
    }
}
