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
        // Same place the old key panel (ESC/TAB/.../Y/N) used to occupy: left edge, 35% down the screen.
        val lp = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.TOP or Gravity.START
        )
        lp.setMargins(dp(4), (activity.resources.displayMetrics.heightPixels * 0.35f).toInt(), 0, 0)
        activity.addContentView(menuButton, lp)
    }

    private fun dp(value: Int): Int = (value * activity.resources.displayMetrics.density).toInt()

    private fun buildItems() {
        items.add(DataDialogMenu(1, R.drawable.menu_icon_gps, "GPS"))
        items.add(DataDialogMenu(2, R.drawable.menu_icon_mm, "Menu"))
        items.add(DataDialogMenu(3, R.drawable.menu_icon_inv, "Inventory"))
        items.add(DataDialogMenu(4, R.drawable.menu_icon_anim, "Animations"))
        items.add(DataDialogMenu(5, R.drawable.br_menu_ruble, "Wallet"))
        items.add(DataDialogMenu(6, R.drawable.menu_icon_car, "Vehicles"))
        items.add(DataDialogMenu(7, R.drawable.menu_report_icon, "Report"))
        items.add(DataDialogMenu(8, R.drawable.menu_promocode_icon, "Promo code"))
        items.add(DataDialogMenu(9, R.drawable.players_icon, "Players"))
        items.add(DataDialogMenu(10, R.drawable.menu_icon_family, "Family"))
        items.add(DataDialogMenu(11, R.drawable.menu_icon_achivments, "Achievements"))
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
