package com.nordic.keyboard

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class NordicStyleActivity : Activity() {
    private lateinit var manager: NordicStyleManager
    private lateinit var list: LinearLayout
    private lateinit var toggle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        manager = NordicStyleManager(this)
        buildScreen()
    }

    private fun buildScreen() {
        list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            setBackgroundColor(0xFF06080B.toInt())
        }

        list.addView(label("NORDIC // STYLE CONTROL", 20f, 0xFF38D9FF.toInt(), 0.08f))
        list.addView(label("STYLE DATABASE // ONLINE", 11f, 0xFF667387.toInt(), 0f))

        toggle = TextView(this).apply {
            setTextColor(if (manager.isStyleEnabled()) 0xFF38D9FF.toInt() else 0xFFE63946.toInt())
            textSize = 15f
            typeface = Typeface.MONOSPACE
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(16), dp(14), dp(16))
            setOnClickListener {
                manager.setStyleEnabled(!manager.isStyleEnabled())
                buildScreen()
            }
        }
        updateToggleText()
        list.addView(toggle)

        list.addView(label("LIVE KEYBOARD PREVIEW", 10f, 0xFF667387.toInt(), 0f).apply {
            setPadding(0, dp(18), 0, dp(7))
        })
        list.addView(KeyboardPreviewView(this, manager.selected()))

        list.addView(label("SELECTED STYLE", 10f, 0xFF667387.toInt(), 0f).apply {
            setPadding(0, dp(18), 0, dp(7))
        })
        rebuildStyles()
        setContentView(list)
    }

    private fun label(value: String, size: Float, color: Int, spacing: Float) =
        TextView(this).apply {
            text = value
            setTextColor(color)
            textSize = size
            typeface = Typeface.MONOSPACE
            letterSpacing = spacing
            setPadding(0, 0, 0, dp(10))
        }

    private fun updateToggleText() {
        toggle.text = if (manager.isStyleEnabled()) "●  STYLE SYSTEM // ON" else "■  STYLE SYSTEM // OFF"
        toggle.setTextColor(if (manager.isStyleEnabled()) 0xFF38D9FF.toInt() else 0xFFE63946.toInt())
        toggle.setBackgroundColor(if (manager.isStyleEnabled()) 0xFF11151B.toInt() else 0xFF1A0C10.toInt())
    }

    private fun rebuildStyles() {
        manager.all().forEach { item ->
            val active = manager.selected().id == item.id
            list.addView(TextView(this).apply {
                text = if (active) "●  " + item.name.uppercase() + "  // ACTIVE" else "○  " + item.name.uppercase()
                setTextColor(if (active) 0xFF38D9FF.toInt() else 0xFFB6C2CF.toInt())
                textSize = 14f
                typeface = Typeface.MONOSPACE
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(12), dp(14), dp(12), dp(14))
                setBackgroundColor(if (active) 0xFF11151B.toInt() else 0xFF0D1117.toInt())
                setOnClickListener { manager.setSelected(item.id); buildScreen() }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        if (::list.isInitialized) buildScreen()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}

private class KeyboardPreviewView(
    context: android.content.Context,
    private val style: NordicStyle
) : android.view.View(context) {
    private val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas: android.graphics.Canvas) {
        super.onDraw(canvas)
        val d = resources.displayMetrics.density
        canvas.drawColor(style.surfaceColor)
        paint.typeface = Typeface.MONOSPACE
        paint.textAlign = android.graphics.Paint.Align.CENTER

        val rows = listOf("QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM")
        val rowH = 31f * d
        val gap = 3f * d
        val keyW = 28f * d
        val totalW = 10f * keyW + 9f * gap
        val left = (width - totalW) / 2f
        var y = 12f * d

        rows.forEachIndexed { index, row ->
            val count = row.length
            val rowWidth = count * keyW + (count - 1) * gap
            val x0 = (width - rowWidth) / 2f
            row.forEachIndexed { i, ch ->
                drawKey(canvas, x0 + i * (keyW + gap), y, keyW, rowH, ch.toString(), false, d)
            }
            y += rowH + gap
        }

        val controls = listOf("⇧", "SPACE", "⌫", "↵")
        var x = left
        controls.forEachIndexed { i, text ->
            val w = if (text == "SPACE") 82f * d else 29f * d
            drawKey(canvas, x, y + 2f * d, w, rowH, text, true, d)
            x += w + gap
        }

        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = d
        paint.color = style.primaryColor
        paint.alpha = 100
        canvas.drawRect(d, d, width - d, height - d, paint)
        paint.style = android.graphics.Paint.Style.FILL
    }

    private fun drawKey(canvas: android.graphics.Canvas, x: Float, y: Float, w: Float, h: Float, text: String, special: Boolean, d: Float) {
        paint.color = if (special) style.specialKeyBackground else style.keyBackground
        paint.alpha = 255
        canvas.drawRoundRect(x, y, x + w, y + h, style.keyRadiusDp * d, style.keyRadiusDp * d, paint)
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = d
        paint.color = if (special) style.specialBorderColor else style.borderColor
        canvas.drawRoundRect(x, y, x + w, y + h, style.keyRadiusDp * d, style.keyRadiusDp * d, paint)
        paint.style = android.graphics.Paint.Style.FILL
        paint.color = if (special) style.specialText else style.keyText
        paint.textSize = if (special) style.specialTextSizeSp * d else style.keyTextSizeSp * d
        paint.typeface = if (style.keyTextBold) Typeface.create(Typeface.MONOSPACE, Typeface.BOLD) else Typeface.MONOSPACE
        canvas.drawText(text, x + w / 2f, y + h / 2f - (paint.ascent() + paint.descent()) / 2f, paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), (128f * resources.displayMetrics.density).toInt())
    }
}