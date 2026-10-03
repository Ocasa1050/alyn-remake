package ro.alynsampmobile.game.ui.widgets.donate

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import ro.alynsampmobile.launcher.R
import java.text.NumberFormat
import java.util.Locale

interface DonateListener {
    fun onBuy(item: DonateItem)
    fun onInfo(item: DonateItem)
    fun onUse(item: DonateItem)
    fun onSell(item: DonateItem)
}

class DonateAdapter(
    private val context: Context,
    private val listener: DonateListener
) : RecyclerView.Adapter<DonateAdapter.ViewHolder>() {

    private val inflater = LayoutInflater.from(context)
    private val priceFormat: NumberFormat = NumberFormat.getIntegerInstance(Locale.US)

    var items: List<DonateItem> = emptyList()
        private set
    private var selected = -1

    fun setItems(newItems: List<DonateItem>) {
        items = newItems
        selected = -1
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.donate_cell, parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.priceText.text = if (item.price > 0) priceFormat.format(item.price.toLong()) else "-"
        holder.nameText.text = item.name

        val resId = if (item.sprite.isEmpty() || item.sprite == "none") 0
        else context.resources.getIdentifier(item.sprite, "drawable", context.packageName)
        if (resId != 0) holder.image.setImageResource(resId) else holder.image.setImageDrawable(null)

        if (position == selected) {
            if (item.sqlId != 0) {
                holder.myButtons.visibility = View.VISIBLE
                holder.shopButtons.visibility = View.GONE
            } else {
                holder.shopButtons.visibility = View.VISIBLE
                holder.myButtons.visibility = View.GONE
            }
        } else {
            holder.myButtons.visibility = View.GONE
            holder.shopButtons.visibility = View.GONE
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val priceText: TextView = view.findViewById(R.id.donate_cell_price_text)
        val nameText: TextView = view.findViewById(R.id.donate_cell_name)
        val image: ImageView = view.findViewById(R.id.donate_cell_img)
        val shopButtons: ConstraintLayout = view.findViewById(R.id.donate_cell_buttons_layout)
        val myButtons: ConstraintLayout = view.findViewById(R.id.donate_cell_my_buttons_layout)

        private fun current(): DonateItem? {
            val pos = adapterPosition
            return if (pos in items.indices) items[pos] else null
        }

        private fun deselect() {
            val old = selected
            selected = -1
            if (old in items.indices) notifyItemChanged(old)
        }

        init {
            view.setOnClickListener {
                val pos = adapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                val old = selected
                selected = if (selected == pos) -1 else pos
                if (old in items.indices) notifyItemChanged(old)
                notifyItemChanged(pos)
            }
            view.findViewById<MaterialButton>(R.id.donate_cell_buy_butt).setOnClickListener {
                current()?.let { listener.onBuy(it) }
            }
            view.findViewById<MaterialButton>(R.id.donate_cell_info_butt).setOnClickListener {
                current()?.let { listener.onInfo(it) }
            }
            view.findViewById<MaterialButton>(R.id.donate_cell_cancel_butt).setOnClickListener { deselect() }
            view.findViewById<MaterialButton>(R.id.donate_cell_my_use_butt).setOnClickListener {
                current()?.let { listener.onUse(it) }
            }
            view.findViewById<MaterialButton>(R.id.donate_cell_my_sell_butt).setOnClickListener {
                current()?.let { listener.onSell(it) }
            }
            view.findViewById<MaterialButton>(R.id.donate_cell_my_cancel_butt).setOnClickListener { deselect() }
        }
    }
}
