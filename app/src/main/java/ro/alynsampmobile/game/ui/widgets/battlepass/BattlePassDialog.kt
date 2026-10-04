package ro.alynsampmobile.game.ui.widgets.battlepass

import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ro.alynsampmobile.launcher.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * LIVE PASS screen. The server drives it with "#UI|BP|<command>|..." chat messages:
 *   BP|SHOW|1 (or 0)
 *   BP|CLEAR
 *   BP|DATA|<level>|<pointsForUp>|<endUnix>|<tasksRefreshUnix>|<placeByLevel>|<placeByTasks>
 *   BP|MAIN|<id>|<sprite>|<rare 0-5>|<state 0 locked,1 available,2 claimed>|<name>
 *   BP|TASK|<id>|<cur>|<max>|<reward>|<caption>|<description>
 *   BP|RATE|<1 by level, 2 by tasks>|<id>|<points>|<nick>
 * Taps go back as chat commands: /bpclaim <id>, /bprefresh <id>, /bpbuy, /bpgift, /bpexit.
 */
class BattlePassDialog(private val activity: Activity, private val listener: Listener) {

    interface Listener {
        fun _sendBattlePassCommand(cmd: String)
    }

    private companion object {
        const val TAG = "BattlePassDialog"
        const val PAGE_MAIN = 0
        const val PAGE_TASKS = 1
        const val PAGE_RATE = 2
    }

    private val root: View = activity.layoutInflater.inflate(R.layout.battlepass, null)

    private val mainAdapter = BpMainAdapter(activity) { pos -> send("/bpclaim $pos") }
    private val tasksAdapter = BpTasksAdapter(activity) { pos -> send("/bprefresh $pos") }
    private val rateAdapter = BpRateAdapter(activity)

    private val mainItems = HashMap<Int, BpMainItem>()
    private val taskItems = HashMap<Int, BpTaskItem>()
    private val rateLevel = HashMap<Int, BpRateItem>()
    private val rateTasks = HashMap<Int, BpRateItem>()

    private var placeByLevel = 0
    private var placeByTasks = 0
    private var rateByLevel = true
    private var tasksRefreshUnix = 0L

    private val endDateText: TextView = root.findViewById(R.id.endDate)
    private val levelText: TextView = root.findViewById(R.id.currentLvlText)
    private val pointsForUpText: TextView = root.findViewById(R.id.pointForUp)
    private val tasksTimeText: TextView = root.findViewById(R.id.updateTasksTime)
    private val placeText: TextView = root.findViewById(R.id.rateYouPlaceText)

    private val mainPage: View = root.findViewById(R.id.mainPageLayout)
    private val tasksPage: View = root.findViewById(R.id.tasksPageLayout)
    private val ratePage: View = root.findViewById(R.id.ratePageLayout)
    private val mainTab: View = root.findViewById(R.id.mainPageButton)
    private val tasksTab: View = root.findViewById(R.id.tasksPageButton)
    private val rateTab: View = root.findViewById(R.id.ratePageButton)
    private val rateLevelTab: View = root.findViewById(R.id.rateCatByLvl)
    private val rateTasksTab: View = root.findViewById(R.id.rateNoDonateCat)

    private val refreshRunnable = Runnable { refreshLists() }
    private val tickRunnable = object : Runnable {
        override fun run() {
            updateTasksTimer()
            root.postDelayed(this, 1000)
        }
    }

    init {
        activity.addContentView(
            root,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
        root.visibility = View.GONE

        val lvlBg = root.findViewById<View>(R.id.lvlBg)
        lvlBg.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                if (lvlBg.height > 0) {
                    lvlBg.viewTreeObserver.removeOnPreDrawListener(this)
                    mainAdapter.itemHeight = lvlBg.height
                    mainAdapter.notifyDataSetChanged()
                }
                return true
            }
        })

        root.findViewById<RecyclerView>(R.id.recycler).apply {
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            adapter = mainAdapter
        }
        root.findViewById<RecyclerView>(R.id.tasksRecycler).apply {
            layoutManager = androidx.recyclerview.widget.GridLayoutManager(activity, 2)
            adapter = tasksAdapter
        }
        root.findViewById<RecyclerView>(R.id.rateRecycler).apply {
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            adapter = rateAdapter
        }

        root.findViewById<View>(R.id.exitButt).setOnClickListener { close(true) }
        mainTab.setOnClickListener { setPage(PAGE_MAIN) }
        tasksTab.setOnClickListener { setPage(PAGE_TASKS) }
        rateTab.setOnClickListener { setPage(PAGE_RATE) }
        rateLevelTab.setOnClickListener { setRateMode(true) }
        rateTasksTab.setOnClickListener { setRateMode(false) }
        root.findViewById<View>(R.id.buyLvlButton).setOnClickListener { send("/bpbuy") }
        root.findViewById<View>(R.id.giftButton).setOnClickListener { send("/bpgift") }

        setPage(PAGE_MAIN)
        setRateMode(true)
    }

    private fun send(cmd: String) {
        listener._sendBattlePassCommand(cmd)
    }

    private fun close(notifyServer: Boolean) {
        root.removeCallbacks(tickRunnable)
        root.visibility = View.GONE
        if (notifyServer) send("/bpexit")
    }

    private fun setPage(page: Int) {
        mainTab.setBackgroundResource(0)
        tasksTab.setBackgroundResource(0)
        rateTab.setBackgroundResource(0)
        mainPage.visibility = View.GONE
        tasksPage.visibility = View.GONE
        ratePage.visibility = View.GONE
        when (page) {
            PAGE_MAIN -> {
                mainTab.setBackgroundResource(R.drawable.magicstore_cat_active_bg)
                mainPage.visibility = View.VISIBLE
            }
            PAGE_TASKS -> {
                tasksTab.setBackgroundResource(R.drawable.magicstore_cat_active_bg)
                tasksPage.visibility = View.VISIBLE
            }
            else -> {
                rateTab.setBackgroundResource(R.drawable.magicstore_cat_active_bg)
                ratePage.visibility = View.VISIBLE
            }
        }
    }

    private fun setRateMode(byLevel: Boolean) {
        rateByLevel = byLevel
        rateLevelTab.setBackgroundResource(if (byLevel) R.drawable.magicstore_cat_active_bg else 0)
        rateTasksTab.setBackgroundResource(if (byLevel) 0 else R.drawable.magicstore_cat_active_bg)
        refreshLists()
    }

    private fun refreshLists() {
        mainAdapter.setData(mainItems.toSortedMap().values.toList())
        tasksAdapter.setData(taskItems.toSortedMap().values.toList())
        rateAdapter.setData((if (rateByLevel) rateLevel else rateTasks).toSortedMap().values.toList())
        val place = if (rateByLevel) placeByLevel else placeByTasks
        placeText.text = if (place > 0) "#$place" else "-"
    }

    private fun scheduleRefresh() {
        root.removeCallbacks(refreshRunnable)
        root.postDelayed(refreshRunnable, 60)
    }

    private fun updateTasksTimer() {
        val diff = tasksRefreshUnix - System.currentTimeMillis() / 1000
        tasksTimeText.text = if (diff > 0) {
            String.format(Locale.US, "Tasks refresh in: %02d:%02d:%02d", diff / 3600, (diff % 3600) / 60, diff % 60)
        } else {
            "Tasks refresh in: --:--:--"
        }
    }

    fun onData(data: String) {
        val p = data.split("|")
        activity.runOnUiThread {
            try {
                handle(p)
            } catch (e: Exception) {
                Log.e(TAG, "bad live pass data: $data", e)
            }
        }
    }

    private fun handle(p: List<String>) {
        when (p.getOrNull(1)) {
            "SHOW" -> {
                if (p.getOrNull(2) == "1") {
                    root.visibility = View.VISIBLE
                    root.removeCallbacks(tickRunnable)
                    root.post(tickRunnable)
                } else {
                    close(false)
                }
            }
            "CLEAR" -> {
                mainItems.clear()
                taskItems.clear()
                rateLevel.clear()
                rateTasks.clear()
                scheduleRefresh()
            }
            "DATA" -> {
                levelText.text = "Level ${p[2].trim()}"
                pointsForUpText.text = p[3].trim()
                val endUnix = p[4].trim().toLong()
                val fmt = SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH)
                fmt.timeZone = TimeZone.getTimeZone("UTC")
                endDateText.text = "End date: " + fmt.format(Date(endUnix * 1000L))
                tasksRefreshUnix = p[5].trim().toLong()
                placeByLevel = p[6].trim().toInt()
                placeByTasks = p[7].trim().toInt()
                updateTasksTimer()
                scheduleRefresh()
            }
            "MAIN" -> {
                mainItems[p[2].trim().toInt()] = BpMainItem(
                    name = p.drop(6).joinToString("|"),
                    sprite = p[3].trim(),
                    rare = p[4].trim().toInt(),
                    state = p[5].trim().toInt()
                )
                scheduleRefresh()
            }
            "TASK" -> {
                taskItems[p[2].trim().toInt()] = BpTaskItem(
                    caption = p[7 - 1],
                    description = p.drop(7).joinToString("|"),
                    cur = p[3].trim().toInt(),
                    max = p[4].trim().toInt(),
                    reward = p[5].trim().toInt()
                )
                scheduleRefresh()
            }
            "RATE" -> {
                val item = BpRateItem(
                    nick = p.drop(5).joinToString("|"),
                    points = p[4].trim().toInt()
                )
                val id = p[3].trim().toInt()
                if (p[2].trim() == "1") rateLevel[id] = item else rateTasks[id] = item
                scheduleRefresh()
            }
        }
    }
}
