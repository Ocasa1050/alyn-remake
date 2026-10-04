package ro.alynsampmobile.game.ui.widgets.battlepass

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import ro.alynsampmobile.game.ui.widgets.donate.IMAGE_BASE_URL

/**
 * Shows a picture by name: a drawable inside the app first, then a full http(s) URL,
 * then a file on the image host (IMAGE_BASE_URL + name, ".png" is added when there is no extension).
 */
object SpriteLoader {
    fun load(context: Context, view: ImageView, sprite: String) {
        Glide.with(context).clear(view)
        if (sprite.isEmpty() || sprite == "none") {
            view.setImageDrawable(null)
            return
        }
        val resId = context.resources.getIdentifier(sprite, "drawable", context.packageName)
        if (resId != 0) {
            view.setImageResource(resId)
            return
        }
        val url = when {
            sprite.startsWith("http://") || sprite.startsWith("https://") -> sprite
            sprite.contains('.') -> IMAGE_BASE_URL + sprite
            else -> "$IMAGE_BASE_URL$sprite.png"
        }
        view.setImageDrawable(null)
        Glide.with(context).load(url).into(view)
    }
}
