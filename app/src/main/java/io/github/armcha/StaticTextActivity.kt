package io.github.armcha

import android.graphics.Typeface
import android.os.Bundle
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.text.style.UnderlineSpan
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import io.github.armcha.autolink.*
import kotlinx.android.synthetic.main.activity_static_text.*
import kotlinx.coroutines.*

class StaticTextActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_static_text)

        val custom = MODE_CUSTOM("\\sAndroid\\b", "\\smobile\\b")
        val autoLinkText = AutoLinkText()
        autoLinkText.addAutoLinkMode(
                MODE_HASHTAG,
                MODE_EMAIL,
                MODE_URL,
                MODE_PHONE,
                custom,
                MODE_MENTION)

//        autoLinkText.addUrlTransformations(
//                "https://en.wikipedia.org/wiki/Wear_OS" to "Wear OS",
//                "https://en.wikipedia.org/wiki/Fire_OS" to "FIRE")


        autoLinkText.attachMentionProcessor {item->
            when {
                item.contains("@962786217262") -> "Google"
                item.contains("@user_962786217261:ibraq4.t2.sa") -> "Facebook"
                item.contains("@user_962786217260:ibraq4.t2.sa") -> "Amazon"
                item.contains("github") -> "Github"
                else ->""
            }
        }

        autoLinkText.addSpan(MODE_URL, StyleSpan(Typeface.ITALIC), UnderlineSpan())
        autoLinkText.addSpan(MODE_HASHTAG, UnderlineSpan(), TypefaceSpan("monospace"))
        autoLinkText.addSpan(custom, StyleSpan(Typeface.BOLD))

        autoLinkText.hashTagModeColor = ContextCompat.getColor(this, R.color.color5)
        autoLinkText.phoneModeColor = ContextCompat.getColor(this, R.color.color3)
        autoLinkText.customModeColor = ContextCompat.getColor(this, R.color.color1)
        autoLinkText.mentionModeColor = ContextCompat.getColor(this, R.color.color6)
        autoLinkText.emailModeColor = ContextCompat.getColor(this, R.color.colorPrimary)
        autoLinkText.setBuildMentionPattern("@", "")
        autoLinkTextView.text = autoLinkText.makeSpannableString("@962786217262")

        autoLinkTextView.movementMethod = LinkTouchMovementMethod()




        autoLinkText.onAutoLinkClick {
            val message = if (it.originalText == it.transformedText) it.originalText
            else "Original text - ${it.originalText} \n\nTransformed text - ${it.transformedText}"
            val url = if (it.mode is MODE_URL) it.originalText else null
            showDialog(it.mode.modeName, message, url)
        }
    }

    fun getName(item: String): String {
        return when {
            item.contains("@user_962786217262:ibraq4.t2.sa") -> "Google"
            item.contains("@user_962786217261:ibraq4.t2.sa") -> "Facebook"
            item.contains("@user_962786217260:ibraq4.t2.sa") -> "Amazon"
            item.contains("github") -> "Github"
            else -> item
        }
    }

}
