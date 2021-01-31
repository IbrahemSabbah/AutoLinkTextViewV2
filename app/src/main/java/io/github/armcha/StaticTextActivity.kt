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
import kotlinx.android.synthetic.main.recycler_item.view.*

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

        autoLinkText.attachUrlProcessor {
            when {
                it.contains("@user_962786217262:ibraq4.t2.sa") -> "Google"
                it.contains("github") -> "Github"
                else -> it
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
        autoLinkText.setBuildMentionPattern("@user_",":ibraq4.t2.sa")
        autoLinkTextView.text = autoLinkText.makeSpannableString("@user_962786217262:ibraq4.t2.sa 962785345342 www.google.com http://google.com i.sabah91@gmail.com")
        autoLinkTextView.movementMethod = LinkTouchMovementMethod()

        autoLinkText.onAutoLinkClick {
            val message = if (it.originalText == it.transformedText) it.originalText
            else "Original text - ${it.originalText} \n\nTransformed text - ${it.transformedText}"
            val url = if (it.mode is MODE_URL) it.originalText else null
            showDialog(it.mode.modeName, message, url)
        }
    }
}
