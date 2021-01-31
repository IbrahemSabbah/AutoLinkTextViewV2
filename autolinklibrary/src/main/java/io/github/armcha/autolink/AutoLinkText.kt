package io.github.armcha.autolink

import android.content.Context
import android.graphics.Color
import android.text.DynamicLayout
import android.text.SpannableString
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.StaticLayout
import android.text.style.CharacterStyle
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import java.lang.reflect.Field

class AutoLinkText() {

    companion object {
        internal val TAG = AutoLinkText::class.java.simpleName
        private const val MIN_PHONE_NUMBER_LENGTH = 7
        private const val MAX_PHONE_NUMBER_LENGTH = 15
        private const val DEFAULT_COLOR = Color.RED
    }

    private val spanMap = mutableMapOf<Mode, HashSet<CharacterStyle>>()
    private val transformations = mutableMapOf<String, String>()
    private val modes = mutableSetOf<Mode>()
    private var onAutoLinkClick: ((AutoLinkItem) -> Unit)? = null
    private var urlProcessor: ((String) -> String)? = null

    var pressedTextColor = Color.LTGRAY
    var mentionModeColor = DEFAULT_COLOR
    var hashTagModeColor = DEFAULT_COLOR
    var customModeColor = DEFAULT_COLOR
    var phoneModeColor = DEFAULT_COLOR
    var emailModeColor = DEFAULT_COLOR
    var urlModeColor = DEFAULT_COLOR


    fun addAutoLinkMode(vararg modes: Mode) {
        this.modes.addAll(modes)
    }

    fun addSpan(mode: Mode, vararg spans: CharacterStyle) {
        spanMap[mode] = spans.toHashSet()
    }

    fun onAutoLinkClick(body: (AutoLinkItem) -> Unit) {
        onAutoLinkClick = body
    }

    fun addUrlTransformations(vararg pairs: Pair<String, String>) {
        transformations.putAll(pairs.toMap())
    }

    fun attachUrlProcessor(processor: (String) -> String) {
        urlProcessor = processor
    }

    fun makeSpannableString(text: CharSequence): SpannableString {

        val autoLinkItems = matchedRanges(text)
        val transformedText = transformLinks(text, autoLinkItems)
        val spannableString = SpannableString(transformedText)

        for (autoLinkItem in autoLinkItems) {
            val mode = autoLinkItem.mode
            val currentColor = getColorByMode(mode)

            val clickableSpan = object : TouchableSpan(currentColor, pressedTextColor) {
                override fun onClick(widget: View) {
                    onAutoLinkClick?.invoke(autoLinkItem)
                }
            }

            spannableString.addSpan(clickableSpan, autoLinkItem)
            spanMap[mode]?.forEach {
                spannableString.addSpan(CharacterStyle.wrap(it), autoLinkItem)
            }
        }

        return spannableString
    }

    private fun transformLinks(text: CharSequence, autoLinkItems: List<AutoLinkItem>): String {
        if (transformations.isEmpty())
            return text.toString()

        val stringBuilder = StringBuilder(text)
        var shift = 0

        autoLinkItems
                .sortedBy { it.startPoint }
                .forEach {
                    if ((it.mode is MODE_URL || it.mode is MODE_MENTION) && it.originalText != it.transformedText) {
                        val originalTextLength = it.originalText.length
                        val transformedTextLength = it.transformedText.length
                        val diff = originalTextLength - transformedTextLength
                        shift += diff
                        it.startPoint = it.startPoint - shift + diff
                        it.endPoint = it.startPoint + transformedTextLength
                        stringBuilder.replace(it.startPoint, it.startPoint + originalTextLength, it.transformedText)
                    } else if (shift > 0) {
                        it.startPoint = it.startPoint - shift
                        it.endPoint = it.startPoint + it.originalText.length
                    }
                }
        return stringBuilder.toString()
    }

    private fun matchedRanges(text: CharSequence): List<AutoLinkItem> {
        val autoLinkItems = mutableListOf<AutoLinkItem>()
        modes.forEach {
            val patterns = it.toPattern()
            patterns.forEach { pattern ->
                val matcher = pattern.matcher(text)
                while (matcher.find()) {
                    var group = matcher.group()
                    var startPoint = matcher.start()
                    val endPoint = matcher.end()
                    when (it) {
                        is MODE_PHONE -> {
                            if (!MENTION_PATTERN.matcher(text).find()) {
                                val digits = group.replace("[^0-9]".toRegex(), "")
                                if (digits.length in MIN_PHONE_NUMBER_LENGTH..MAX_PHONE_NUMBER_LENGTH) {
                                    val item = AutoLinkItem(startPoint, endPoint, group, group, it)
                                    autoLinkItems.add(item)
                                }
                            }
                        }
                        is MODE_MENTION -> {
                            if (startPoint > 0) {
                                startPoint += 1
                            }
                            group = group.trimStart()
                            if (urlProcessor != null) {
                                val transformedUrl = urlProcessor?.invoke(group) ?: group
                                if (transformedUrl != group)
                                    transformations[group] = transformedUrl
                            }

                            val matchedText = transformations[group] ?: group
                            val item = AutoLinkItem(startPoint, endPoint, group,
                                    transformedText = matchedText, mode = it)
                            autoLinkItems.add(item)
                        }
                        else -> {
                            if (!MENTION_PATTERN.matcher(text).find()) {
                                val isUrl = it is MODE_URL
                                if (isUrl) {
                                    if (startPoint > 0) {
                                        startPoint += 1
                                    }
                                    group = group.trimStart()
                                    if (urlProcessor != null) {
                                        val transformedUrl = urlProcessor?.invoke(group) ?: group
                                        if (transformedUrl != group)
                                            transformations[group] = transformedUrl
                                    }
                                }
                                val matchedText = if (isUrl && transformations.containsKey(group)) {
                                    transformations[group] ?: group
                                } else {
                                    group
                                }
                                val item = AutoLinkItem(startPoint, endPoint, group,
                                        transformedText = matchedText, mode = it)
                                autoLinkItems.add(item)
                            }
                        }
                    }
                }
            }
        }
        return autoLinkItems
    }

    private fun SpannableString.addSpan(span: Any, autoLinkItem: AutoLinkItem) {
        setSpan(span, autoLinkItem.startPoint, autoLinkItem.endPoint, SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private fun getColorByMode(mode: Mode): Int {
        return when (mode) {
            is MODE_HASHTAG -> hashTagModeColor
            is MODE_MENTION -> mentionModeColor
            is MODE_URL -> urlModeColor
            is MODE_PHONE -> phoneModeColor
            is MODE_EMAIL -> emailModeColor
            is MODE_CUSTOM -> customModeColor
        }
    }


}
