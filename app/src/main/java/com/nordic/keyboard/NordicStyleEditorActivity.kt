package com.nordic.keyboard

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.*

class NordicStyleEditorActivity : Activity() {
    private lateinit var manager: NordicStyleManager
    private lateinit var preview: KeyboardPreviewView

    private val presets = listOf(
        "NORDIC CORE" to 0xFF38D9FF.toInt(),
        "WARNING" to 0xFFE63946.toInt(),
        "STEEL" to 0xFFB6C2CF.toInt(),
        "VOID" to 0xFF06080B.toInt(),
        "FACILITY" to 0xFF1A5F7A.toInt()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        manager = NordicStyleManager(this)
        buildEditor(manager.customOrNull() ?: manager.selected())
    }

    private fun buildEditor(start: NordicStyle) {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(18))
            setBackgroundColor(0xFF06080B.toInt())
        }
        root.addView(label("NORDIC // STYLE EDITOR", 20f, 0xFF38D9FF.toInt()))
        root.addView(label("EDIT THE STYLE // NOT THE KEYBOARD", 10f, 0xFF667387.toInt()))
        preview = KeyboardPreviewView(this, start)
        root.addView(preview)

        root.addView(label("ACCENT COLOUR", 10f, 0xFF667387.toInt()).apply { setPadding(0, dp(14), 0, dp(6)) })
        val colors = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        presets.forEach { (name, color) ->
            colors.addView(Button(this).apply {
                text = name
                textSize = 9f
                setTextColor(color)
                typeface = Typeface.MONOSPACE
                setOnClickListener { draft = draft.copy(keyText=color, specialText=color, primaryColor=color); preview = KeyboardPreviewView(this@NordicStyleEditorActivity, draft); root.removeViewAt(3); root.addView(preview, 3) }
            })
        }
        root.addView(colors)

        root.addView(label("KEY CORNER RADIUS", 10f, 0xFF667387.toInt()).apply { setPadding(0, dp(12), 0, dp(2)) })
        val radius = SeekBar(this).apply { max = 12; progress = start.keyRadiusDp.toInt().coerceIn(0,12) }
        root.addView(radius)

        root.addView(label("LETTER SIZE", 10f, 0xFF667387.toInt()).apply { setPadding(0, dp(8), 0, dp(2)) })
        val size = SeekBar(this).apply { max = 10; progress = (start.keyTextSizeSp - 13f).toInt().coerceIn(0,10) }
        root.addView(size)

        val bold = CheckBox(this).apply { text = "BOLD TECHNICAL LETTERING"; isChecked = start.keyTextBold; setTextColor(0xFFB6C2CF.toInt()) }
        root.addView(bold)

        val save = Button(this).apply {
            text = "SAVE STYLE // ACTIVATE"
            setOnClickListener {
                draft = draft.copy(keyRadiusDp = radius.progress.toFloat(), keyTextSizeSp = 13f + size.progress, keyTextBold = bold.isChecked)
                manager.saveCustom(draft)
                finish()
            }
        }
        root.addView(save)
        setContentView(root)
    }

    private var draft: NordicStyle = NordicStyles.default
    private fun label(t:String,s:Float,c:Int)=TextView(this).apply{ text=t; textSize=s; setTextColor(c); typeface=Typeface.MONOSPACE; setPadding(0,0,0,dp(7)) }
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
}