package com.nordic.keyboard

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView

class NordicStyleActivity : Activity() {
    private lateinit var list: LinearLayout
    private lateinit var updater: NordicUpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updater = NordicUpdateManager(this)
        buildScreen()
    }

    private fun buildScreen() {
        list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            setBackgroundColor(0xFF06080B.toInt())
        }
        list.addView(label("NORDIC // KEYBOARD", 20f, 0xFF38D9FF.toInt(), 0.08f))
        list.addView(label("FULL ANDROID INPUT CORE // FIXED DESIGN", 11f, 0xFF667387.toInt(), 0f))
        list.addView(label("KEYBOARD PREVIEW", 10f, 0xFF667387.toInt(), 0f).apply { setPadding(0, dp(18), 0, dp(7)) })
        list.addView(KeyboardPreviewView(this, NordicStyles.default))
        list.addView(label("SETUP", 10f, 0xFF667387.toInt(), 0f).apply { setPadding(0, dp(18), 0, dp(7)) })
        list.addView(action("ENABLE NORDIC KEYBOARD") { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) })
        list.addView(action("SELECT NORDIC KEYBOARD") { (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker() })
        list.addView(label("UPDATES", 10f, 0xFF667387.toInt(), 0f).apply { setPadding(0, dp(18), 0, dp(7)) })
        list.addView(action("CHECK FOR UPDATE") { updater.checkForUpdate(manual = true) })
        list.addView(label("NORDIC DESIGN // ACTIVE", 10f, 0xFF667387.toInt(), 0f).apply { setPadding(0, dp(18), 0, dp(7)) })
        list.addView(TextView(this).apply {
            text = "●  NORDIC DEFAULT  // FIXED"
            setTextColor(0xFF38D9FF.toInt()); textSize = 14f; typeface = Typeface.MONOSPACE
            gravity = Gravity.CENTER_VERTICAL; setPadding(dp(12), dp(14), dp(12), dp(14)); setBackgroundColor(0xFF11151B.toInt())
        })
        list.addView(label("NO STYLE EDITOR // NO CUSTOM LAYOUT", 10f, 0xFF667387.toInt(), 0f).apply { setPadding(0, dp(18), 0, dp(7)) })
        list.addView(TextView(this).apply {
            text = "This keyboard uses one pre-built Nordic design. The appearance and keyboard layout are fixed."
            setTextColor(0xFFB6C2CF.toInt()); textSize = 13f; typeface = Typeface.MONOSPACE
            setPadding(dp(12), dp(12), dp(12), dp(12)); setBackgroundColor(0xFF0D1117.toInt())
        })
        setContentView(list)
    }

    private fun action(value: String, click: () -> Unit) = TextView(this).apply {
        text = value; setTextColor(0xFF38D9FF.toInt()); textSize = 13f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD); gravity = Gravity.CENTER
        setPadding(dp(12), dp(14), dp(12), dp(14)); setBackgroundColor(0xFF11151B.toInt())
        setOnClickListener { click() }
    }

    private fun label(value: String, size: Float, color: Int, spacing: Float) = TextView(this).apply {
        text = value; setTextColor(color); textSize = size; typeface = Typeface.MONOSPACE
        letterSpacing = spacing; setPadding(0, 0, 0, dp(10))
    }

    override fun onResume() {
        super.onResume()
        if (::list.isInitialized) buildScreen()
        if (::updater.isInitialized) updater.checkForUpdate(manual = false)
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}

class KeyboardPreviewView(context: android.content.Context, private val style: NordicStyle) : android.view.View(context) {
    private val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
    override fun onDraw(canvas: android.graphics.Canvas) {
        super.onDraw(canvas)
        val d = resources.displayMetrics.density
        canvas.drawColor(style.surfaceColor); paint.typeface = Typeface.MONOSPACE
        paint.textAlign = android.graphics.Paint.Align.CENTER
        val rows = listOf("QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM")
        val rowH = 31f * d; val gap = 3f * d; val keyW = 28f * d
        val totalW = 10f * keyW + 9f * gap; val left = (width - totalW) / 2f
        var y = 27f * d
        rows.forEach { row ->
            val rowWidth = row.length * keyW + (row.length - 1) * gap
            val x0 = (width - rowWidth) / 2f
            row.forEachIndexed { i, ch -> drawKey(canvas, x0 + i * (keyW + gap), y, keyW, rowH, ch.toString(), false, d) }
            y += rowH + gap
        }
        val controls = listOf("⇧", "SPACE", "⌫", "↵"); var x = left
        controls.forEach { text ->
            val w = if (text == "SPACE") 82f * d else 29f * d
            drawKey(canvas, x, y + 2f * d, w, rowH, text, true, d); x += w + gap
        }
        paint.style = android.graphics.Paint.Style.STROKE; paint.strokeWidth = d; paint.color = style.primaryColor; paint.alpha = 100
        canvas.drawRect(d, d, width - d, height - d, paint); paint.style = android.graphics.Paint.Style.FILL
    }
    private fun drawKey(canvas: android.graphics.Canvas, x: Float, y: Float, w: Float, h: Float, text: String, special: Boolean, d: Float) {
        paint.color = if (special) style.specialKeyBackground else style.keyBackground; paint.alpha = 255
        canvas.drawRoundRect(x, y, x + w, y + h, style.keyRadiusDp * d, style.keyRadiusDp * d, paint)
        paint.style = android.graphics.Paint.Style.STROKE; paint.strokeWidth = d
        paint.color = if (special) style.specialBorderColor else style.borderColor
        canvas.drawRoundRect(x, y, x + w, y + h, style.keyRadiusDp * d, style.keyRadiusDp * d, paint)
        paint.style = android.graphics.Paint.Style.FILL; paint.color = if (special) style.specialText else style.keyText
        paint.textSize = if (special) style.specialTextSizeSp * d else style.keyTextSizeSp * d
        paint.typeface = if (style.keyTextBold) Typeface.create(Typeface.MONOSPACE, Typeface.BOLD) else Typeface.MONOSPACE
        canvas.drawText(text, x + w / 2f, y + h / 2f - (paint.ascent() + paint.descent()) / 2f, paint)
    }
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), (128f * resources.displayMetrics.density).toInt())
    }
}
