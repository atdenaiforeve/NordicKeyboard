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

        list.addView(TextView(this).apply {
            text = "NORDIC // STYLE CONTROL"
            setTextColor(0xFF38D9FF.toInt())
            textSize = 20f
            typeface = Typeface.MONOSPACE
            letterSpacing = 0.08f
            setPadding(0, 0, 0, dp(8))
        })

        list.addView(TextView(this).apply {
            text = "STYLE DATABASE // ONLINE"
            setTextColor(0xFF667387.toInt())
            textSize = 11f
            typeface = Typeface.MONOSPACE
            setPadding(0, 0, 0, dp(12))
        })

        toggle = TextView(this).apply {
            setTextColor(if (manager.isStyleEnabled()) 0xFF38D9FF.toInt() else 0xFFE63946.toInt())
            textSize = 15f
            typeface = Typeface.MONOSPACE
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(16), dp(14), dp(16))
            setBackgroundColor(if (manager.isStyleEnabled()) 0xFF11151B.toInt() else 0xFF1A0C10.toInt())
            setOnClickListener {
                manager.setStyleEnabled(!manager.isStyleEnabled())
                buildScreen()
            }
        }
        updateToggleText()
        list.addView(toggle)

        list.addView(TextView(this).apply {
            text = "SELECTED STYLE"
            setTextColor(0xFF667387.toInt())
            textSize = 10f
            typeface = Typeface.MONOSPACE
            setPadding(0, dp(18), 0, dp(7))
        })

        rebuildStyles()
        setContentView(list)
    }

    private fun updateToggleText() {
        toggle.text = if (manager.isStyleEnabled())
            "●  STYLE SYSTEM // ON"
        else
            "■  STYLE SYSTEM // OFF"
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
                setOnClickListener {
                    manager.setSelected(item.id)
                    buildScreen()
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        if (::list.isInitialized) buildScreen()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}