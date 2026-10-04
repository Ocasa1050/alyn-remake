package ro.alynsampmobile.game.ui.widgets.battlepass

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import ro.alynsampmobile.game.ui.widgets.inventory.Rare
import ro.alynsampmobile.launcher.R
import java.text.NumberFormat
import java.util.Locale

// ---------------------------------------------------------------- rewards row

class BpMainAdapter(
    private val context: Context,
    private val onClaim: (Int) -> Unit
) : RecyclerView.Adapter<BpMainAdapter.ViewHolder>() {

    var items: List<BpMainItem> = emptyList()
    var itemHeight = 0

    fun setData(list: List<BpMainItem>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.battlepass_main_item, parent, false)
        view.layoutParams.width = (context.resources.displayMetrics.widthPixels * 0.165).toInt()
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        if (itemHeight > 0) holder.itemView.layoutParams.height = itemHeight

        when (item.state) {
            1 -> {
                holder.full.alpha = 1f
                holder.give.visibility = View.VISIBLE
                holder.status.setImageResource(0)
            }
            2 -> {
                holder.full.alpha = 1f
                holder.give.visibility = View.GONE
                holder.status.setImageResource(R.drawable.battlepass_main_item_completed_icon)
            }
            else -> {
                holder.full.alpha = 0.5f
                holder.give.visibility = View.GONE
                holder.status.setImageResource(R.drawable.battlepass_main_item_lock_icon)
            }
        }
        holder.level.text = (position + 1).toString()
        holder.name.text = item.name
        holder.image.backgroundTintList = ColorStateList.valueOf(Rare.color(item.rare))
        SpriteLoader.load(context, holder.image, item.sprite)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val full: View = view.findViewById(R.id.itemFull)
        val level: TextView = view.findViewById(R.id.level)
        val name: TextView = view.findViewById(R.id.name)
        val image: ImageView = view.findViewById(R.id.image)
        val status: ImageView = view.findViewById(R.id.statusIcon)
        val give: MaterialButton = view.findViewById(R.id.giveButton)

        init {
            give.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) onClaim(pos)
            }
        }
    }
}

// ---------------------------------------------------------------- tasks grid

class BpTasksAdapter(
    private val context: Context,
    private val onRefresh: (Int) -> Unit
) : RecyclerView.Adapter<BpTasksAdapter.ViewHolder>() {

    var items: List<BpTaskItem> = emptyList()

    fun setData(list: List<BpTaskItem>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.battlepass_task_item, parent, false)
        val metrics = context.resources.displayMetrics
        view.layoutParams.width = (metrics.widthPixels / 2.15).toInt()
        view.layoutParams.height = metrics.heightPixels / 4
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.caption.text = item.caption
        holder.description.text = item.description
        holder.progress.max = if (item.max > 0) item.max else 1
        holder.progress.progress = item.cur
        holder.progressText.text = "${item.cur} / ${item.max}"
        holder.reward.text = "+${item.reward} points"
        holder.completed.visibility = if (item.cur >= item.max) View.VISIBLE else View.GONE
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val caption: TextView = view.findViewById(R.id.caption)
        val description: TextView = view.findViewById(R.id.description)
        val progress: ProgressBar = view.findViewById(R.id.progress)
        val progressText: TextView = view.findViewById(R.id.progressText)
        val reward: TextView = view.findViewById(R.id.rewardText)
        val completed: View = view.findViewById(R.id.completedLayout)

        init {
            view.findViewById<View>(R.id.refreshButton).setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) onRefresh(pos)
            }
        }
    }
}

// ---------------------------------------------------------------- ranking list

class BpRateAdapter(private val context: Context) : RecyclerView.Adapter<BpRateAdapter.ViewHolder>() {

    var items: List<BpRateItem> = emptyList()
    private val numberFormat: NumberFormat = NumberFormat.getIntegerInstance(Locale.US)

    fun setData(list: List<BpRateItem>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.battlepass_rate_item, parent, false)
        view.layoutParams.width = (context.resources.displayMetrics.widthPixels * 0.69).toInt()
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.placeIcon.setImageResource(
            when (position + 1) {
                1 -> R.drawable.battlepass_rate_item_place_1_icon
                2 -> R.drawable.battlepass_rate_item_place_2_icon
                3 -> R.drawable.battlepass_rate_item_place_3_icon
                else -> R.drawable.battlepass_rate_item_place_4_icon
            }
        )
        holder.placeText.text = (position + 1).toString()
        holder.nick.text = item.nick
        holder.points.text = numberFormat.format(item.points.toLong())
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val placeIcon: ImageView = view.findViewById(R.id.placeIcon)
        val placeText: TextView = view.findViewById(R.id.placeText)
        val nick: TextView = view.findViewById(R.id.nick)
        val points: TextView = view.findViewById(R.id.pointsText)
    }
}
