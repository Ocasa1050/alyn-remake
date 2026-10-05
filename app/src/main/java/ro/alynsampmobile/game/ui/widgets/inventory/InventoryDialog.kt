package ro.alynsampmobile.game.ui.widgets.inventory

import android.app.Activity
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ro.alynsampmobile.launcher.R

/**
 * Inventory screen (Black Russia layout). The server drives it with messages "#UI|INV|<command>|..." (see README).
 * Taps are sent back to the server as chat commands: /invcell <matrix> <pos> and /invbtn <id>.
 *
 * Matrix 1 = 8 fixed cells (4 "ACTIVE SLOT" + 4 "CHARACTER"), matrix 2 = 16 fixed cells (my items),
 * matrix 3 = shop / storage list (extra panel).
 */
class InventoryDialog(private val activity: Activity, private val listener: Listener) : InventoryListener {

    interface Listener {
        fun _sendInventoryCommand(cmd: String)
    }

    private companion object {
        const val TAG = "InventoryDialog"

        const val MATRIX_PLAYER = 1
        const val MATRIX_MAIN = 2
        const val MATRIX_ADDITIONAL = 3

        const val PLAYER_CELLS = 8
        const val MAIN_CELLS = 16
        const val ADDITIONAL_CELLS = 3 * 49

        const val BUTTON_TRADE = 1
        const val BUTTON_DELETE = 2
        const val BUTTON_USE = 3
        const val BUTTON_INFO = 4
        const val BUTTON_EXIT = 6

        val ACTIVE_IDS = intArrayOf(R.id.inv_active_1, R.id.inv_active_2, R.id.inv_active_3, R.id.inv_active_4)
        val PLAYER_IDS = intArrayOf(R.id.inv_player_1, R.id.inv_player_2, R.id.inv_player_3, R.id.inv_player_4)
        val MAIN_IDS = intArrayOf(
            R.id.inv_main_1, R.id.inv_main_2, R.id.inv_main_3, R.id.inv_main_4,
            R.id.inv_main_5, R.id.inv_main_6, R.id.inv_main_7, R.id.inv_main_8,
            R.id.inv_main_9, R.id.inv_main_10, R.id.inv_main_11, R.id.inv_main_12,
            R.id.inv_main_13, R.id.inv_main_14, R.id.inv_main_15, R.id.inv_main_16
        )
    }

    private val root: View = activity.layoutInflater.inflate(R.layout.inventory, null)

    private val playerList = List(PLAYER_CELLS) { InventoryItem() }
    private val mainList = List(MAIN_CELLS) { InventoryItem() }
    private val additionalAdapter = InventoryAdapter(MATRIX_ADDITIONAL, List(ADDITIONAL_CELLS) { InventoryItem() }, this)

    private val playerCells: List<ViewGroup> = (ACTIVE_IDS + PLAYER_IDS).map { root.findViewById<ViewGroup>(it) }
    private val mainCells: List<ViewGroup> = MAIN_IDS.map { root.findViewById<ViewGroup>(it) }
    private val captions = HashMap<ViewGroup, TextView>()

    private val healthText: TextView = root.findViewById(R.id.inv_health_text)
    private val satietyText: TextView = root.findViewById(R.id.inv_satiety_text)
    private val massText: TextView = root.findViewById(R.id.inv_progress_text)
    private val massProgress: ProgressBar = root.findViewById(R.id.inv_progress)
    private val additionalMassText: TextView = root.findViewById(R.id.additionalMassText)
    private val additionalTopCaption: TextView = root.findViewById(R.id.additionalTopCaption)
    private val additionalLayout: View = root.findViewById(R.id.additionalLayout)

    private var selMat = -1
    private var selPos = -1

    init {
        activity.addContentView(
            root,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
        root.visibility = View.GONE
        additionalLayout.visibility = View.GONE

        playerCells.forEachIndexed { i, c ->
            prepareCell(c)
            c.setOnClickListener { onSelectedItem(MATRIX_PLAYER, i) }
        }
        mainCells.forEachIndexed { i, c ->
            prepareCell(c)
            c.setOnClickListener { onSelectedItem(MATRIX_MAIN, i) }
        }
        root.findViewById<RecyclerView>(R.id.additionalMatrixRecycle).apply {
            layoutManager = GridLayoutManager(activity, 3)
            adapter = additionalAdapter
        }

        root.findViewById<View>(R.id.inv_del_butt).setOnClickListener { sendButton(BUTTON_DELETE) }
        root.findViewById<View>(R.id.inv_sell_butt).setOnClickListener { sendButton(BUTTON_TRADE) }
        root.findViewById<View>(R.id.inv_use_butt).setOnClickListener { sendButton(BUTTON_USE) }
        root.findViewById<View>(R.id.inv_inf_butt).setOnClickListener { sendButton(BUTTON_INFO) }
        root.findViewById<View>(R.id.inv_close_butt).setOnClickListener { sendButton(BUTTON_EXIT) }

        refreshPlayer()
        refreshMain()
    }

    /** Adds a small caption text to a cell (used only when an item has no picture). */
    private fun prepareCell(cell: ViewGroup) {
        val tv = TextView(activity)
        tv.id = View.generateViewId()
        tv.setTextColor(0xFFFFFFFF.toInt())
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8f)
        tv.maxLines = 2
        tv.gravity = Gravity.CENTER
        tv.includeFontPadding = false
        val lp = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        lp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        lp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        lp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        lp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        lp.marginStart = 4
        lp.marginEnd = 4
        cell.addView(tv, lp)
        captions[cell] = tv
    }

    private fun bindCell(cell: ViewGroup, item: InventoryItem, selected: Boolean) {
        val image = cell.getChildAt(0) as ImageView
        val badge = cell.getChildAt(1) as ViewGroup
        val badgeText = badge.getChildAt(0) as TextView

        val resId = if (item.sprite.isEmpty()) 0 else activity.resources.getIdentifier(item.sprite, "drawable", activity.packageName)
        if (resId != 0) {
            image.setImageResource(resId)
            image.visibility = View.VISIBLE
        } else {
            image.setImageDrawable(null)
            image.visibility = View.INVISIBLE
        }
        captions[cell]?.text = if (resId == 0) item.caption else ""

        if (item.count.isNotEmpty()) {
            badgeText.text = item.count
            badge.visibility = View.VISIBLE
        } else {
            badge.visibility = View.INVISIBLE
        }
        cell.setBackgroundResource(if (selected) R.drawable.invb_bg_shape_active else R.drawable.invb_bg_shape)
    }

    private fun refreshPlayer() {
        playerCells.forEachIndexed { i, c ->
            bindCell(c, playerList[i], selMat == MATRIX_PLAYER && selPos == i)
        }
    }

    private fun refreshMain() {
        mainCells.forEachIndexed { i, c ->
            bindCell(c, mainList[i], selMat == MATRIX_MAIN && selPos == i)
        }
    }

    private fun refresh(m: Int) {
        when (m) {
            MATRIX_PLAYER -> refreshPlayer()
            MATRIX_MAIN -> refreshMain()
            MATRIX_ADDITIONAL -> additionalAdapter.notifyDataSetChanged()
        }
    }

    private fun listFor(m: Int): List<InventoryItem>? = when (m) {
        MATRIX_PLAYER -> playerList
        MATRIX_MAIN -> mainList
        MATRIX_ADDITIONAL -> additionalAdapter.list
        else -> null
    }

    private fun sendButton(id: Int) {
        listener._sendInventoryCommand("/invbtn $id")
    }

    override fun onSelectedItem(matrixId: Int, pos: Int) {
        listener._sendInventoryCommand("/invcell $matrixId $pos")
    }

    /** data example: "INV|ITEM|2|0|sprite_name|Caption|3|2|0" */
    fun onData(data: String) {
        val p = data.split("|")
        activity.runOnUiThread {
            try {
                handle(p)
            } catch (e: Exception) {
                Log.e(TAG, "bad inventory data: $data", e)
            }
        }
    }

    private fun handle(p: List<String>) {
        when (p.getOrNull(1)) {
            "SHOW" -> {
                if (p[2] == "1") {
                    healthText.text = p.getOrElse(3) { "100" }
                    satietyText.text = p.getOrElse(5) { "100" }
                    showSkin(p.getOrElse(6) { "" }.trim())
                    root.visibility = View.VISIBLE
                } else {
                    resetSelected()
                    root.visibility = View.GONE
                }
            }
            "ADD" -> toggleAdditional(p[2] == "1", p.getOrElse(3) { "" })
            "MASS" -> {
                val text = p.getOrElse(3) { "" }
                when (p[2].trim().toInt()) {
                    MATRIX_MAIN -> {
                        massText.text = text
                        val parts = text.split("/")
                        val used = parts.getOrNull(0)?.filter { it.isDigit() }?.toIntOrNull()
                        val max = parts.getOrNull(1)?.filter { it.isDigit() }?.toIntOrNull()
                        if (used != null && max != null && max > 0) {
                            massProgress.max = max
                            massProgress.progress = used
                        }
                    }
                    MATRIX_ADDITIONAL -> additionalMassText.text = text
                }
            }
            "ITEM" -> updateItem(
                p[2].trim().toInt(),
                p[3].trim().toInt(),
                p.getOrElse(4) { "" },
                p.getOrElse(5) { "" },
                p.getOrElse(6) { "" },
                p.getOrElse(7) { "0" }.trim().toIntOrNull() ?: 0,
                p.getOrElse(8) { "0" }.trim() == "1"
            )
            "SEL" -> {
                val m = p[2].trim().toInt()
                val pos = p[3].trim().toInt()
                resetSelected()
                if (p.getOrElse(4) { "1" }.trim() == "1") setSelected(m, pos)
            }
            "CLEAR" -> {
                val m = p[2].trim().toInt()
                val list = listFor(m) ?: return
                if (selMat == m) resetSelected()
                for (item in list) {
                    item.sprite = ""
                    item.caption = ""
                    item.count = ""
                    item.rare = 0
                }
                if (m == MATRIX_PLAYER) showSkin("")
                refresh(m)
            }
            else -> Log.w(TAG, "unknown inventory command: $p")
        }
    }

    private fun showSkin(skin: String) {
        var name = "skin_$skin"
        if (skin.isEmpty() || activity.resources.getIdentifier(name, "drawable", activity.packageName) == 0) {
            name = "invx_player_placeholder"
        }
        playerList[0].sprite = name
        refreshPlayer()
    }

    private fun updateItem(m: Int, pos: Int, sprite: String, caption: String, count: String, rare: Int, active: Boolean) {
        val list = listFor(m) ?: return
        if (pos < 0 || pos >= list.size) return

        val item = list[pos]
        item.sprite = sprite
        item.caption = caption
        item.count = count
        item.rare = rare

        if (selMat == m && selPos == pos && !active) resetSelected()
        if (active) {
            resetSelected()
            setSelected(m, pos)
        }
        if (m == MATRIX_ADDITIONAL) additionalAdapter.notifyItemChanged(pos) else refresh(m)
    }

    private fun resetSelected() {
        val m = selMat
        val pos = selPos
        selMat = -1
        selPos = -1
        if (m == MATRIX_ADDITIONAL) {
            additionalAdapter.selectedPos = -1
            if (pos >= 0) additionalAdapter.notifyItemChanged(pos)
        } else if (m in MATRIX_PLAYER..MATRIX_MAIN) {
            refresh(m)
        }
    }

    private fun setSelected(m: Int, pos: Int) {
        val list = listFor(m) ?: return
        if (pos < 0 || pos >= list.size) return
        selMat = m
        selPos = pos
        if (m == MATRIX_ADDITIONAL) {
            additionalAdapter.selectedPos = pos
            additionalAdapter.notifyItemChanged(pos)
        } else {
            refresh(m)
        }
    }

    private fun toggleAdditional(show: Boolean, caption: String) {
        if (show) {
            additionalLayout.visibility = View.VISIBLE
            additionalTopCaption.text = caption
        } else {
            additionalLayout.visibility = View.GONE
        }
    }
}
