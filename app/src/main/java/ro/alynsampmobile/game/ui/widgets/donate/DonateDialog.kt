package ro.alynsampmobile.game.ui.widgets.donate

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ro.alynsampmobile.launcher.R
import java.text.NumberFormat
import java.util.Locale

/**
 * Donate / wallet screen. The server drives it with "#UI|DON|<command>|..." chat messages:
 *   DON|SHOW|1|<balance>             open the screen   (DON|SHOW|0 closes it)
 *   DON|BAL|<balance>                update the balance
 *   DON|CLEAR                        remove every item
 *   DON|ITEM|<cat>|<value>|<sqlId>|<price>|<sprite>|<name>
 * Taps are sent back to the server as chat commands (see Listener).
 */
class DonateDialog(private val activity: Activity, private val listener: Listener) : DonateListener {

    interface Listener {
        fun _sendDonateCommand(cmd: String)
    }

    private companion object {
        const val TAG = "DonateDialog"
        const val CAT_STASH = -1
        const val CAT_ALL = 0
        const val CAT_CARS = 1
        const val CAT_SKINS = 2
        const val CAT_ACS = 3
        const val CAT_VIP = 4
        const val CAT_OTHER = 5
        const val CURRENCY = "DH"
    }

    private val root: View = activity.layoutInflater.inflate(R.layout.donate_screen, null)
    private val adapter = DonateAdapter(activity, this)
    private val balanceText: TextView = root.findViewById(R.id.donate_balance_text)
    private val searchEdit: EditText = root.findViewById(R.id.donate_search_edit)
    private val sortSpinner: Spinner = root.findViewById(R.id.donate_sort)
    private val numberFormat: NumberFormat = NumberFormat.getIntegerInstance(Locale.US)

    private val allItems = ArrayList<DonateItem>()
    private var category = CAT_ALL
    private var activeCategoryView: View? = null
    private val categoryViews: List<Pair<Int, View>>

    private val refreshRunnable = Runnable { refresh() }

    init {
        activity.addContentView(
            root,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
        root.visibility = View.GONE

        root.findViewById<RecyclerView>(R.id.donateRecycle).apply {
            layoutManager = GridLayoutManager(activity, 3)
            adapter = this@DonateDialog.adapter
            setHasFixedSize(false)
            setItemViewCacheSize(10)
        }

        categoryViews = listOf(
            CAT_STASH to root.findViewById<View>(R.id.donate_category_my_item),
            CAT_ALL to root.findViewById<View>(R.id.donate_category_buy_all),
            CAT_CARS to root.findViewById<View>(R.id.donate_category_buy_cars),
            CAT_SKINS to root.findViewById<View>(R.id.donate_category_buy_skins),
            CAT_ACS to root.findViewById<View>(R.id.donate_category_buy_acs),
            CAT_VIP to root.findViewById<View>(R.id.donate_category_buy_vip),
            CAT_OTHER to root.findViewById<View>(R.id.donate_category_buy_other)
        )
        for ((cat, view) in categoryViews) {
            view.setOnClickListener { setCategory(cat) }
        }

        root.findViewById<View>(R.id.donate_deposit_butt).setOnClickListener { send("/donbtn 1") }
        root.findViewById<View>(R.id.donate_check_butt).setOnClickListener { send("/donbtn 2") }
        root.findViewById<View>(R.id.donate_history_butt).setOnClickListener { send("/donbtn 3") }
        root.findViewById<View>(R.id.donate_change_butt).setOnClickListener { send("/donbtn 4") }
        root.findViewById<View>(R.id.donate_exit_button).setOnClickListener { close(true) }

        val sortOptions = arrayOf("Newest first", "Most expensive", "Cheapest")
        val sortAdapter = ArrayAdapter(activity, android.R.layout.simple_spinner_item, sortOptions)
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner.adapter = sortAdapter
        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                refresh()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        searchEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                refresh()
            }
        })

        setCategory(CAT_ALL)
        setBalance(0)
    }

    private fun send(cmd: String) {
        listener._sendDonateCommand(cmd)
    }

    private fun close(notifyServer: Boolean) {
        root.visibility = View.GONE
        if (notifyServer) send("/donexit")
    }

    private fun setBalance(value: Long) {
        balanceText.text = "Balance: ${numberFormat.format(value)} $CURRENCY"
    }

    private fun setCategory(cat: Int) {
        category = cat
        activeCategoryView = null
        for ((c, view) in categoryViews) {
            if (c == cat) {
                view.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFFF00"))
                activeCategoryView = view
            } else {
                view.backgroundTintList = null
            }
        }
        refresh()
    }

    private fun refresh() {
        val query = searchEdit.text?.toString()?.trim()?.lowercase(Locale.ROOT).orEmpty()
        var list = allItems.filter { item ->
            val inCategory = when (category) {
                CAT_STASH -> item.sqlId != 0
                CAT_ALL -> item.sqlId == 0
                else -> item.sqlId == 0 && item.category == category
            }
            inCategory && (query.isEmpty() || item.name.lowercase(Locale.ROOT).contains(query))
        }
        when (sortSpinner.selectedItemPosition) {
            1 -> list = list.sortedByDescending { it.price }
            2 -> list = list.sortedBy { it.price }
        }
        adapter.setItems(list)
    }

    /** data example: "DON|ITEM|5|3|0|100|donate_warn|Remove warn" */
    fun onData(data: String) {
        val p = data.split("|")
        activity.runOnUiThread {
            try {
                handle(p)
            } catch (e: Exception) {
                Log.e(TAG, "bad donate data: $data", e)
            }
        }
    }

    private fun handle(p: List<String>) {
        when (p.getOrNull(1)) {
            "SHOW" -> {
                if (p.getOrNull(2) == "1") {
                    p.getOrNull(3)?.trim()?.toLongOrNull()?.let { setBalance(it) }
                    root.visibility = View.VISIBLE
                    refresh()
                } else {
                    close(false)
                }
            }
            "BAL" -> p.getOrNull(2)?.trim()?.toLongOrNull()?.let { setBalance(it) }
            "CLEAR" -> {
                allItems.clear()
                scheduleRefresh()
            }
            "ITEM" -> {
                // the name is last so that it may be anything
                val name = p.drop(7).joinToString("|")
                allItems.add(
                    DonateItem(
                        category = p[2].trim().toInt(),
                        value = p[3].trim().toInt(),
                        sqlId = p[4].trim().toInt(),
                        price = p[5].trim().toInt(),
                        sprite = p[6].trim(),
                        name = name
                    )
                )
                scheduleRefresh()
            }
        }
    }

    private fun scheduleRefresh() {
        root.removeCallbacks(refreshRunnable)
        root.postDelayed(refreshRunnable, 60)
    }

    override fun onBuy(item: DonateItem) = send("/donbuy ${item.category} ${item.value}")
    override fun onInfo(item: DonateItem) = send("/doninfo ${item.category} ${item.value}")
    override fun onUse(item: DonateItem) = send("/donuse ${item.sqlId}")
    override fun onSell(item: DonateItem) = send("/donsell ${item.sqlId}")
}
