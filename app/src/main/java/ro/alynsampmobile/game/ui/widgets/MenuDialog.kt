package ro.alynsampmobile.game.ui.widgets

import android.app.Activity
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ro.alynsampmobile.launcher.R

class MenuDialog(private val activity: Activity, private val listener: Listener) {

    interface Listener {
        fun _sendMenuClick(id: Int)
    }

    private val root: View = activity.layoutInflater.inflate(R.layout.menu_action_dialog, null)
    private val menuButton: Button = Button(activity)
    private val items = ArrayList<DataDialogMenu>()

    init {
        activity.addContentView(
            root,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
        root.visibility = View.GONE
        root.findViewById<View>(R.id.closeButton).setOnClickListener { showMenu(false) }

        buildItems()

        val recycler = root.findViewById<RecyclerView>(R.id.br_rec_view_menu)
        recycler.layoutManager = object : GridLayoutManager(activity, 4) {
            override fun checkLayoutParams(lp: RecyclerView.LayoutParams): Boolean {
                val f = 30.0f / activity.resources.displayMetrics.density
                val m = f.toInt()
                lp.marginStart = m
                lp.marginEnd = m
                lp.setMargins(0, m, 0, 0)
                lp.width = ((width / spanCount).toFloat() - f).toInt()
                return true
            }
        }
        recycler.adapter = DialogMenuAdapter(items) { data, _ ->
            showMenu(false)
            listener._sendMenuClick(data.id)
        }

        menuButton.text = "MENU"
        menuButton.visibility = View.GONE
        menuButton.setOnClickListener { showMenu(true) }
        val lp = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.TOP or Gravity.END
        )
        lp.setMargins(0, dp(8), dp(8), 0)
        activity.addContentView(menuButton, lp)
    }

    private fun dp(value: Int): Int = (value * activity.resources.displayMetrics.density).toInt()

    private fun buildItems() {
        items.add(DataDialogMenu(1, R.drawable.menu_icon_gps, "Навигатор"))
        items.add(DataDialogMenu(2, R.drawable.menu_icon_mm, "Меню"))
        items.add(DataDialogMenu(3, R.drawable.menu_icon_inv, "Инвентарь"))
        items.add(DataDialogMenu(4, R.drawable.menu_icon_anim, "Анимации"))
        items.add(DataDialogMenu(5, R.drawable.br_menu_ruble, "Донат"))
        items.add(DataDialogMenu(6, R.drawable.menu_icon_car, "Автомобили"))
        items.add(DataDialogMenu(7, R.drawable.menu_report_icon, "Жалоба"))
        items.add(DataDialogMenu(8, R.drawable.menu_promocode_icon, "Промокод"))
        items.add(DataDialogMenu(9, R.drawable.players_icon, "Игроки"))
        items.add(DataDialogMenu(10, R.drawable.menu_icon_family, "Семья"))
        items.add(DataDialogMenu(11, R.drawable.menu_icon_achivments, "Достижения"))
        items.add(DataDialogMenu(12, R.drawable.menu_icon_battlepass, "LIVE PASS"))
    }

    /** Shows or hides the small MENU button on screen. */
    fun showButton(show: Boolean) {
        menuButton.visibility = if (show) View.VISIBLE else View.GONE
        if (!show) root.visibility = View.GONE
    }

    fun showMenu(show: Boolean) {
        if (show) {
            root.alpha = 0f
            root.visibility = View.VISIBLE
            root.animate().alpha(1f).setDuration(200).start()
        } else {
            root.animate().alpha(0f).setDuration(200).withEndAction {
                root.visibility = View.GONE
            }.start()
        }
    }
}