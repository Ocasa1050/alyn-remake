package ro.alynsampmobile.game.ui.widgets.inventory

import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ro.alynsampmobile.launcher.R

/**
 * Inventory screen. The server drives it with messages "#UI|INV|<command>|..." (see README).
 * Taps are sent back to the server as chat commands: /invcell <matrix> <pos> and /invbtn <id>.
 */
class InventoryDialog(private val activity: Activity, private val listener: Listener) : InventoryListener {

    interface Listener {
        fun _sendInventoryCommand(cmd: String)
    }

    private companion object {
        const val TAG = "InventoryDialog"

        const val MATRIX_NONE = 0
        const val MATRIX_PLAYER = 1
        const val MATRIX_MAIN = 2
        const val MATRIX_ADDITIONAL = 3

        const val PLAYER_CELLS = 8
        const val MAIN_CELLS = 4 * 12
        const val ADDITIONAL_CELLS = 3 * 49

        const val BUTTON_TRADE = 1
        const val BUTTON_DELETE = 2
        const val BUTTON_USE = 3
        const val BUTTON_INFO = 4
        const val BUTTON_EXIT = 6
    }

    private val root: View = activity.layoutInflater.inflate(R.layout.inventory, null)

    private val playerList = List(PLAYER_CELLS) { InventoryItem() }

    private val matrices: List<InventoryAdapter> = listOf(
        InventoryAdapter(MATRIX_NONE, emptyList(), this),
        InventoryAdapter(MATRIX_PLAYER, playerList, this, 0, 4),
        InventoryAdapter(MATRIX_MAIN, List(MAIN_CELLS) { InventoryItem() }, this),
        InventoryAdapter(MATRIX_ADDITIONAL, List(ADDITIONAL_CELLS) { InventoryItem() }, this)
    )

    /** second half of the player matrix (the "CHARACTER" panel), shares the same list */
    private val playerAdapter2 = InventoryAdapter(MATRIX_PLAYER, playerList, this, 4, 4)

    private val healthText: TextView = root.findViewById(R.id.healthText)
    private val armourText: TextView = root.findViewById(R.id.armourText)
    private val satietyText: TextView = root.findViewById(R.id.satietyText)
    private val myMassText: TextView = root.findViewById(R.id.myMassText)
    private val myMassProgress: ProgressBar = root.findViewById(R.id.myMassProgress)
    private val additionalMassText: TextView = root.findViewById(R.id.additionalMassText)
    private val additionalTopCaption: TextView = root.findViewById(R.id.additionalTopCaption)
    private val additionalLayout: View = root.findViewById(R.id.additionalLayout)
    private val exitButt: View = root.findViewById(R.id.exitButt)

    private var selMat = -1
    private var selPos = -1

    init {
        activity.addContentView(
            root,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
        root.visibility = View.GONE
        additionalLayout.visibility = View.GONE

        root.findViewById<RecyclerView>(R.id.playerMatrixRecycle).apply {
            layoutManager = GridLayoutManager(activity, 1)
            adapter = matrices[MATRIX_PLAYER]
        }
        root.findViewById<RecyclerView>(R.id.charMatrixRecycle).apply {
            layoutManager = GridLayoutManager(activity, 1)
            adapter = playerAdapter2
        }
        root.findViewById<RecyclerView>(R.id.myMatrixRecycle).apply {
            layoutManager = GridLayoutManager(activity, 4)
            adapter = matrices[MATRIX_MAIN]
        }
        root.findViewById<RecyclerView>(R.id.additionalMatrixRecycle).apply {
            layoutManager = GridLayoutManager(activity, 3)
            adapter = matrices[MATRIX_ADDITIONAL]
        }

        root.findViewById<View>(R.id.inv_del_butt).setOnClickListener { sendButton(BUTTON_DELETE) }
        root.findViewById<View>(R.id.inv_sell_butt).setOnClickListener { sendButton(BUTTON_TRADE) }
        root.findViewById<View>(R.id.inv_use_butt).setOnClickListener { sendButton(BUTTON_USE) }
        root.findViewById<View>(R.id.inv_inf_butt).setOnClickListener { sendButton(BUTTON_INFO) }
        exitButt.setOnClickListener { sendButton(BUTTON_EXIT) }
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
                    armourText.text = p.getOrElse(4) { "0" }
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
                        myMassText.text = text
                        val parts = text.split("/")
                        val used = parts.getOrNull(0)?.filter { it.isDigit() }?.toIntOrNull()
                        val max = parts.getOrNull(1)?.filter { it.isDigit() }?.toIntOrNull()
                        if (used != null && max != null && max > 0) {
                            myMassProgress.max = max
                            myMassProgress.progress = used
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
                if (m in MATRIX_PLAYER..MATRIX_ADDITIONAL) {
                    if (selMat == m) resetSelected()
                    for (item in matrices[m].list) {
                        item.sprite = ""
                        item.caption = ""
                        item.count = ""
                        item.rare = 0
                    }
                    matrices[m].notifyDataSetChanged()
                    if (m == MATRIX_PLAYER) {
                        playerAdapter2.notifyDataSetChanged()
                        showSkin("")
                    }
                }
            }
            else -> Log.w(TAG, "unknown inventory command: $p")
        }
    }

    private fun updateItem(m: Int, pos: Int, sprite: String, caption: String, count: String, rare: Int, active: Boolean) {
        if (m !in MATRIX_PLAYER..MATRIX_ADDITIONAL) return
        val matrix = matrices[m]
        if (pos < 0 || pos >= matrix.list.size) return

        val item = matrix.list[pos]
        item.sprite = sprite
        item.caption = caption
        item.count = count
        item.rare = rare

        if (selMat == m && selPos == pos && !active) resetSelected()
        if (active) {
            resetSelected()
            setSelected(m, pos)
        }
        refresh(m, pos)
    }

    private fun refresh(m: Int, pos: Int) {
        if (m == MATRIX_PLAYER) {
            matrices[m].notifyDataSetChanged()
            playerAdapter2.notifyDataSetChanged()
        } else {
            matrices[m].notifyItemChanged(pos)
        }
    }

    private fun showSkin(skin: String) {
        val item = playerList[0]
        val ctx = activity
        var name = "skin_$skin"
        if (skin.isEmpty() || ctx.resources.getIdentifier(name, "drawable", ctx.packageName) == 0) {
            name = "invx_player_placeholder"
        }
        item.sprite = name
        matrices[MATRIX_PLAYER].notifyDataSetChanged()
    }

    private fun resetSelected() {
        if (selMat in MATRIX_PLAYER..MATRIX_ADDITIONAL && selPos >= 0) {
            val matrix = matrices[selMat]
            matrix.selectedPos = -1
            if (selMat == MATRIX_PLAYER) playerAdapter2.selectedPos = -1
            refresh(selMat, selPos)
        }
        selMat = -1
        selPos = -1
    }

    private fun setSelected(m: Int, pos: Int) {
        if (m !in MATRIX_PLAYER..MATRIX_ADDITIONAL) return
        val matrix = matrices[m]
        if (pos < 0 || pos >= matrix.list.size) return

        matrix.selectedPos = pos
        if (m == MATRIX_PLAYER) playerAdapter2.selectedPos = pos
        selMat = m
        selPos = pos
        refresh(m, pos)
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
