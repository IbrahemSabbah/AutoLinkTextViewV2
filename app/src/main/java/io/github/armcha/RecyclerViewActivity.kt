package io.github.armcha

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import io.github.armcha.autolink.*
import kotlinx.android.synthetic.main.activity_recycler_view.*
import kotlinx.android.synthetic.main.activity_static_text.view.*
import kotlinx.android.synthetic.main.recycler_item.view.*
import kotlinx.android.synthetic.main.recycler_item.view.autoLinkTextView


class RecyclerViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_view)

        recyclerView.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val view = layoutInflater.inflate(R.layout.recycler_item, parent, false)
                return object : RecyclerView.ViewHolder(view) {}
            }

            override fun getItemCount() = 200

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

                val autoLinkText = AutoLinkText(applicationContext)
                val context = holder.itemView.context
                val custom = MODE_CUSTOM("\\sAndroid\\b")

                autoLinkText.addAutoLinkMode(
                        MODE_HASHTAG,
                        MODE_URL,
                        MODE_PHONE,
                        MODE_EMAIL,
                        custom,
                        MODE_MENTION)

                autoLinkText.addUrlTransformations(
                        "https://google.com" to "Google",
                        "https://en.wikipedia.org/wiki/Cyberpunk_2077" to "Cyberpunk",
                        "https://en.wikipedia.org/wiki/Fire_OS" to "FIRE",
                        "https://en.wikipedia.org/wiki/Wear_OS" to "Wear OS")

                autoLinkText.addSpan(MODE_URL, StyleSpan(Typeface.BOLD_ITALIC), UnderlineSpan())
                autoLinkText.addSpan(custom, StyleSpan(Typeface.BOLD))
                autoLinkText.addSpan(MODE_HASHTAG, BackgroundColorSpan(Color.GRAY), UnderlineSpan(), ForegroundColorSpan(Color.WHITE))

                autoLinkText.hashTagModeColor = ContextCompat.getColor(context, R.color.color2)
                autoLinkText.customModeColor = ContextCompat.getColor(context, R.color.color1)
                autoLinkText.mentionModeColor = ContextCompat.getColor(context, R.color.color3)
                autoLinkText.emailModeColor = ContextCompat.getColor(context, R.color.colorPrimary)
                autoLinkText.phoneModeColor = ContextCompat.getColor(context, R.color.colorAccent)

                val text = when {
                    position % 3 == 1 -> R.string.android_text_short
                    position % 3 == 2 -> R.string.android_text_short_second
                    else -> R.string.text_third
                }

                holder.itemView.autoLinkTextView.text = autoLinkText.makeSpannableString(getString(text))
                holder.itemView.autoLinkTextView.movementMethod = LinkTouchMovementMethod()

                autoLinkText.onAutoLinkClick {
                    val message = if (it.originalText == it.transformedText) it.originalText
                    else "Original text - ${it.originalText} \n\nTransformed text - ${it.transformedText}"
                    val url = if (it.mode is MODE_URL) it.originalText else null
                    showDialog(it.mode.modeName, message, url)
                }
            }
        }
    }
}
