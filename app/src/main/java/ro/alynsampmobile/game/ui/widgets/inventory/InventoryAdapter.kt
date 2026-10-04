package ro.alynsampmobile.game.ui.widgets.inventory

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ro.alynsampmobile.launcher.R

interface InventoryListener {
    fun onSelectedItem(matrixId: Int, pos: Int)
}

class InventoryAdapter(
    private val matrixId: Int,
    val list: List<InventoryItem>,
    private val listener: InventoryListener,
    private val offset: Int = 0,
    private val count: Int = list.size
) : RecyclerView.Adapter<InventoryAdapter.ViewHolder>() {

    var selectedPos: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.inventory_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[offset + position]

        holder.image.backgroundTintList = ColorStateList.valueOf(Rare.color(item.rare))
        holder.caption.text = item.caption

        holder.layout.setBackgroundResource(
            if (selectedPos == offset + position) R.drawable.invb_bg_shape_active else R.drawable.invb_bg_shape
        )

        if (item.count.isNotEmpty()) {
            holder.valueBg.visibility = View.VISIBLE
            holder.valueText.text = item.count
        } else {
            holder.valueBg.visibility = View.GONE
        }

        val ctx = holder.image.context
        val resId = if (item.sprite.isEmpty()) 0 else ctx.resources.getIdentifier(item.sprite, "drawable", ctx.packageName)
        holder.image.setImageResource(resId)
    }

    override fun getItemCount(): Int = count

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layout: View = view.findViewById(R.id.itemLayout)
        val image: ImageView = view.findViewById(R.id.image)
        val caption: TextView = view.findViewById(R.id.caption)
        val valueBg: View = view.findViewById(R.id.valueBg)
        val valueText: TextView = view.findViewById(R.id.valueText)

        init {
            view.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onSelectedItem(matrixId, offset + pos)
                }
            }
        }
    }
}
