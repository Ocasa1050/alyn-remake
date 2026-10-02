package ro.alynsampmobile.game.ui.widgets.inventory

import android.graphics.Color

object Rare {
    fun color(rare: Int): Int = when (rare) {
        1 -> Color.parseColor("#5e98d9")
        2 -> Color.parseColor("#4b69ff")
        3 -> Color.parseColor("#b28a33")
        4 -> Color.parseColor("#8847ff")
        5 -> Color.parseColor("#eb4b4b")
        else -> Color.parseColor("#00000000")
    }
}
