package com.nordic.keyboard

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.os.SystemClock
import android.speech.RecognizerIntent
import android.view.Gravity
import android.text.InputType
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class NordicKeyboardService : InputMethodService() {
    private lateinit var styleStore: StyleStore
    private var caps = false
    private var capsLocked = false
    private var symbolMode = false
    private var lastShiftTap = 0L
    private val suggestionViews = mutableListOf<TextView>()
    private var inputViewRoot: View? = null

    override fun onCreate() {
        super.onCreate()
        styleStore = StyleStore(this)
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        caps = false
        capsLocked = false
        symbolMode = false
    }

    override fun onCreateInputView(): View {
        val s = styleStore.getSelectedStyle()
        val root = NordicKeyboardLayout(this, s).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(5), dp(4), dp(5), dp(5))
            setBackgroundColor(s.surfaceColor)
        }

        val header = TextView(this).apply {
            text = if (symbolMode) {
                "NORDIC // SYMBOL CORE"
            } else {
                "NORDIC // INPUT CORE    [" + s.name.uppercase() + "]"
            }
            setTextColor(s.keyText)
            textSize = 9f
            typeface = Typeface.MONOSPACE
            letterSpacing = 0.08f
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(7), 0, dp(7), 0)
            layoutParams = LinearLayout.LayoutParams(-1, dp(24))
            background = GradientDrawable().apply {
                setColor(s.panelColor)
                setStroke(dp(1), s.borderColor)
                cornerRadius = dp(3f)
            }
        }
        root.addView(header)
        root.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(-1, dp(2))
        })

        root.addView(createToolbar(s))

        if (!symbolMode && supportsSuggestions()) {
            root.addView(createSuggestionRow(s))
        }

        if (symbolMode) {
            addRow(root, arrayOf("1","2","3","4","5","6","7","8","9","0"), s)
            addRow(root, arrayOf("@","#","$","%","&","*","-","+","=","/"), s)
            addRow(root, arrayOf("(",")","[","]","{","}","<",">","?","!"), s)
        } else {
            addRow(root, "QWERTYUIOP".map { it.toString() }.toTypedArray(), s)
            addRow(root, "ASDFGHJKL".map { it.toString() }.toTypedArray(), s)
            addRow(root, "ZXCVBNM".map { it.toString() }.toTypedArray(), s)
        }

        val bottom = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
        }

        bottom.addView(makeKey(if (symbolMode) "ABC" else "123", s, special = true) {
            symbolMode = !symbolMode
            setInputView(onCreateInputView())
        })

        bottom.addView(makeKey(
            if (capsLocked) "⇧•" else "⇧",
            s,
            special = true
        ) {
            val now = SystemClock.uptimeMillis()
            if (now - lastShiftTap < 350) {
                capsLocked = true
                caps = true
            } else if (capsLocked) {
                capsLocked = false
                caps = false
            } else {
                caps = !caps
            }
            lastShiftTap = now
            setInputView(onCreateInputView())
        })

        bottom.addView(makeKey("SPACE", s, 3f) {
            autoCorrectCurrentWord()
            currentInputConnection?.commitText(" ", 1)
            refreshSuggestions()
        })

        bottom.addView(makeKey("⌫", s, special = true, onLongClick = {
            deletePreviousWord()
        }) {
            currentInputConnection?.deleteSurroundingText(1, 0)
            refreshSuggestions()
        })

        bottom.addView(makeKey("↵", s, special = true) {
            sendEnter()
        })

        root.addView(bottom)
        inputViewRoot = root
        return root
    }

    private fun createToolbar(s: NordicStyle): LinearLayout {
        return LinearLayout(this).apply {
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(-1, dp(34))
            val items = listOf(
                "MENU" to { showNordicMenu() },
                "EMOJI" to { insertEmoji() },
                "GIF" to { showUnavailableFeature("GIF") },
                "TOOLS" to { showNordicTools() },
                "LANG" to { showUnavailableFeature("Language tools") },
                "VOICE" to { startVoiceInput() }
            )
            items.forEach { (label, click) ->
                addView(makeToolbarButton(label, s, click))
            }
        }
    }

    private fun makeToolbarButton(label: String, s: NordicStyle, click: () -> Unit): TextView =
        TextView(this).apply {
            text = label
            gravity = Gravity.CENTER
            setTextColor(s.keyText)
            textSize = 9f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            letterSpacing = 0.04f
            background = GradientDrawable().apply {
                setColor(s.panelColor)
                setStroke(dp(1), s.borderColor)
                cornerRadius = dp(3f)
            }
            layoutParams = LinearLayout.LayoutParams(0, -1, 1f).apply {
                setMargins(dp(2), dp(1), dp(2), dp(1))
            }
            setOnClickListener { click() }
        }

    private fun showNordicMenu() {
        AlertDialog.Builder(this)
            .setTitle("NORDIC // MENU")
            .setItems(arrayOf("Keyboard settings", "Input method settings", "Close")) { dialog, which ->
                when (which) {
                    0 -> startActivity(Intent(this, NordicStyleActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    1 -> startActivity(Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    else -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun showNordicTools() {
        AlertDialog.Builder(this)
            .setTitle("NORDIC // TOOLS")
            .setItems(arrayOf("Copy", "Cut", "Paste", "Select all")) { _, which ->
                val ic = currentInputConnection ?: return@setItems
                when (which) {
                    0 -> ic.performContextMenuAction(android.R.id.copy)
                    1 -> ic.performContextMenuAction(android.R.id.cut)
                    2 -> ic.performContextMenuAction(android.R.id.paste)
                    3 -> ic.performContextMenuAction(android.R.id.selectAll)
                }
            }
            .show()
    }

    private fun insertEmoji() {
        currentInputConnection?.commitText("😀", 1)
        refreshSuggestions()
    }

    private fun startVoiceInput() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (_: Exception) {
            showUnavailableFeature("Voice input")
        }
    }

    private fun showUnavailableFeature(feature: String) {
        AlertDialog.Builder(this)
            .setTitle("NORDIC // $feature")
            .setMessage("$feature is not connected yet. The Nordic toolbar is ready for it.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun addRow(root: LinearLayout, labels: Array<String>, s: NordicStyle) {
        val row = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
        }
        labels.forEach { row.addView(makeKey(it, s)) }
        root.addView(row)
    }

    private fun makeKey(
        label: String,
        s: NordicStyle,
        weight: Float = 1f,
        special: Boolean = false,
        onLongClick: (() -> Unit)? = null,
        action: (() -> Unit)? = null
    ): Button =
        Button(this).apply {
            tag = label
            text = if (!special && label.length == 1 && label[0].isLetter()) {
                if (caps) label else label.lowercase()
            } else {
                label
            }
            setTextColor(if (special) s.specialText else s.keyText)
            textSize = if (special) s.specialTextSizeSp else s.keyTextSizeSp
            typeface = Typeface.MONOSPACE
            gravity = Gravity.CENTER
            isAllCaps = false
            stateListAnimator = null
            setPadding(0, 0, 0, 0)

            val normal = GradientDrawable().apply {
                setColor(if (special) s.specialKeyBackground else s.keyBackground)
                cornerRadius = dp(s.keyRadiusDp)
                setStroke(dp(1), if (special) s.specialBorderColor else s.borderColor)
            }
            val pressed = GradientDrawable().apply {
                setColor(s.pressedKeyBackground)
                cornerRadius = dp(s.keyRadiusDp)
                setStroke(dp(1), s.keyText)
            }
            background = normal
            layoutParams = LinearLayout.LayoutParams(0, -1, weight).apply {
                setMargins(dp(2), dp(2), dp(2), dp(2))
            }

            setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> view.background = pressed
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> view.background = normal
                }
                false
            }

            setOnLongClickListener {
                if (onLongClick != null) {
                    onLongClick.invoke()
                    true
                } else {
                    false
                }
            }

            setOnClickListener { performKeyAction(label, action, special) }
        }

    private fun performKeyAction(label: String, action: (() -> Unit)?, special: Boolean) {
        if (action != null) {
            action.invoke()
            return
        }
        val output = if (!special && label.length == 1 && label[0].isLetter()) {
            if (caps) label else label.lowercase()
        } else label
        currentInputConnection?.commitText(output, 1)
        refreshSuggestions()
        if (!special && label.length == 1 && label[0].isLetter() && caps && !capsLocked) {
            caps = false
            updateLetterLabels()
        }
    }

    private fun updateLetterLabels() {
        val root = inputViewRoot as? LinearLayout ?: return
        for (i in 0 until root.childCount) {
            val row = root.getChildAt(i) as? LinearLayout ?: continue
            for (j in 0 until row.childCount) {
                val child = row.getChildAt(j) as? Button ?: continue
                val label = child.tag as? String ?: continue
                if (label.length == 1 && label[0].isLetter()) {
                    child.text = if (caps) label else label.lowercase()
                }
            }
        }
    }

    private fun deletePreviousWord() {
        val ic = currentInputConnection ?: return
        val before = ic.getTextBeforeCursor(64, 0)?.toString() ?: return
        if (before.isEmpty()) return

        var count = 0
        var i = before.length - 1

        while (i >= 0 && before[i].isWhitespace()) {
            count++
            i--
        }
        while (i >= 0 && !before[i].isWhitespace()) {
            count++
            i--
        }

        if (count > 0) {
            ic.deleteSurroundingText(count, 0)
            refreshSuggestions()
        }
    }


    private fun createSuggestionRow(s: NordicStyle): LinearLayout {
        suggestionViews.clear()
        return LinearLayout(this).apply {
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(-1, dp(36))
            repeat(3) { index ->
                val view = TextView(this@NordicKeyboardService).apply {
                    gravity = Gravity.CENTER
                    setTextColor(s.keyText)
                    textSize = 13f
                    typeface = Typeface.MONOSPACE
                    background = GradientDrawable().apply {
                        setColor(s.panelColor)
                        setStroke(dp(1), s.borderColor)
                        cornerRadius = dp(3f)
                    }
                    layoutParams = LinearLayout.LayoutParams(0, -1, 1f).apply {
                        setMargins(dp(2), 0, dp(2), 0)
                    }
                    setOnClickListener { applySuggestion(text.toString()) }
                }
                suggestionViews.add(view)
                addView(view)
            }
            post { refreshSuggestions() }
        }
    }

    private fun supportsSuggestions(): Boolean {
        val type = currentInputEditorInfo?.inputType ?: return true
        val variation = type and InputType.TYPE_MASK_VARIATION
        return variation != InputType.TYPE_TEXT_VARIATION_PASSWORD &&
            variation != InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD &&
            variation != InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
    }

    private fun currentWord(): String {
        val before = currentInputConnection?.getTextBeforeCursor(40, 0)?.toString() ?: return ""
        return before.takeLastWhile { it.isLetter() }
    }

    private fun refreshSuggestions() {
        if (suggestionViews.isEmpty()) return

        val word = currentWord().lowercase()
        val suggestions = if (word.isNotBlank()) suggestionList(word) else nextWordSuggestions()

        suggestionViews.forEachIndexed { index, view ->
            val suggestion = suggestions.getOrNull(index)
            view.text = suggestion ?: ""
            view.isEnabled = suggestion != null
        }
    }

    private fun autoCorrectCurrentWord() {
        val ic = currentInputConnection ?: return
        val word = currentWord()
        if (word.isBlank()) return

        val corrected = corrections[word.lowercase()] ?: return
        ic.deleteSurroundingText(word.length, 0)
        ic.commitText(matchCase(word, corrected), 1)
    }

    private fun matchCase(original: String, replacement: String): String {
        return when {
            original.all { it.isUpperCase() } -> replacement.uppercase()
            original.firstOrNull()?.isUpperCase() == true ->
                replacement.replaceFirstChar { it.uppercase() }
            else -> replacement
        }
    }

    private val corrections = mapOf(
        "teh" to "the", "adn" to "and", "taht" to "that",
        "becuase" to "because", "recieve" to "receive",
        "seperate" to "separate", "definately" to "definitely",
        "occured" to "occurred", "tomorow" to "tomorrow",
        "dont" to "don't", "cant" to "can't", "wont" to "won't",
        "im" to "i'm", "ive" to "i've", "ill" to "i'll", "id" to "i'd",
        "youre" to "you're", "theyre" to "they're"
    )

    private fun suggestionList(prefix: String): List<String> {
        val matches = englishWords.filter { it.startsWith(prefix) && it != prefix }
        val correction = corrections[prefix]
        return buildList {
            if (correction != null) add(correction)
            addAll(matches.filterNot { contains(it) })
        }.take(3)
    }

    private fun nextWordSuggestions(): List<String> {
        val before = currentInputConnection?.getTextBeforeCursor(80, 0)?.toString() ?: return emptyList()
        val words = before.trimEnd().split(Regex("\\s+"))
        val last = words.lastOrNull()?.lowercase()?.trim { !it.isLetter() } ?: return emptyList()
        return nextWords[last].orEmpty().take(3)
    }

    private val nextWords = mapOf(
        "hello" to listOf("there", "world", "everyone"),
        "how" to listOf("are", "is", "do"),
        "are" to listOf("you", "we", "they"),
        "what" to listOf("is", "are", "do"),
        "where" to listOf("are", "is", "do"),
        "when" to listOf("is", "are", "will"),
        "why" to listOf("is", "are", "do"),
        "i" to listOf("am", "can", "will"),
        "you" to listOf("are", "can", "have"),
        "we" to listOf("are", "can", "will"),
        "they" to listOf("are", "have", "will"),
        "this" to listOf("is", "will", "can"),
        "that" to listOf("is", "was", "will"),
        "the" to listOf("next", "same", "best"),
        "can" to listOf("you", "we", "i"),
        "could" to listOf("you", "we", "i"),
        "would" to listOf("you", "be", "like"),
        "please" to listOf("help", "send", "let"),
        "thank" to listOf("you", "god", "you"),
        "good" to listOf("morning", "night", "luck"),
        "see" to listOf("you", "the", "if"),
        "let" to listOf("me", "us", "them"),
        "make" to listOf("it", "a", "sure"),
        "going" to listOf("to", "back", "home"),
        "want" to listOf("to", "a", "you"),
        "need" to listOf("to", "a", "some"),
        "have" to listOf("to", "a", "been"),
        "will" to listOf("be", "have", "do"),
        "just" to listOf("a", "want", "need"),
        "really" to listOf("good", "want", "like"),
        "nordic" to listOf("keyboard", "system", "style"),
        "keyboard" to listOf("is", "with", "for"),
        "game" to listOf("is", "and", "with"),
        "games" to listOf("are", "and", "like"),
        "today" to listOf("is", "i", "we"),
        "tomorrow" to listOf("is", "i", "we")
    )

    private val englishWords = listOf(
        "a","about","again","all","am","and","are","as","at","back","be","because","been","best",
        "but","by","can","can't","could","did","do","does","don't","for","from","game","games",
        "going","good","great","have","hello","help","here","how","i","i'd","i'll","i'm","i've",
        "if","in","is","it","just","keyboard","let","like","look","make","made","me","more","much",
        "my","need","new","next","no","normal","not","now","of","on","only","or","other","our",
        "please","really","receive","right","same","see","send","should","so","some","something",
        "sure","system","thank","thanks","that","the","their","them","then","there","they",
        "they're","this","time","to","today","tomorrow","want","was","we","were","what","when",
        "where","which","who","will","with","work","working","world","would","yes","you","your",
        "you're"
    )

    private fun applySuggestion(suggestion: String) {
        val ic = currentInputConnection ?: return
        val word = currentWord()
        if (word.isEmpty()) return
        ic.deleteSurroundingText(word.length, 0)
        val replacement = if (caps || capsLocked) {
            suggestion.replaceFirstChar { it.uppercase() }
        } else suggestion
        ic.commitText(replacement, 1)
        refreshSuggestions()
    }

    private fun sendEnter() {
        val ic = currentInputConnection ?: return
        val action = currentInputEditorInfo?.imeOptions
            ?.and(EditorInfo.IME_MASK_ACTION)
            ?: EditorInfo.IME_ACTION_NONE

        if (action != EditorInfo.IME_ACTION_NONE && ic.performEditorAction(action)) {
            return
        }

        sendEnterKey(ic)
    }

    private fun sendEnterKey(ic: InputConnection) {
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun dp(value: Float): Float =
        value * resources.displayMetrics.density
}

private class NordicKeyboardLayout(
    context: Context,
    private val s: NordicStyle
) : LinearLayout(context) {
    init {
        setWillNotDraw(false)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val density = resources.displayMetrics.density
        val paint = android.graphics.Paint().apply {
            color = s.scanlineColor
            alpha = s.scanlineAlpha
            strokeWidth = 1f
        }

        var y = 0f
        val gap = (4f * density).coerceAtLeast(3f)
        while (y < height) {
            canvas.drawLine(0f, y, width.toFloat(), y, paint)
            y += gap
        }

        val edge = android.graphics.Paint().apply {
            color = s.primaryColor
            alpha = 90
            strokeWidth = density
            style = android.graphics.Paint.Style.STROKE
        }
        canvas.drawRect(1f, 1f, width - 1f, height - 1f, edge)
    }
}
