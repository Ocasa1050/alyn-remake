package ro.alynsampmobile.game.ui.widgets.donate

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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

/** Folder (public, https) where product pictures are uploaded. Must end with "/". */
const val IMAGE_BASE_URL = "https://raw.githubusercontent.com/Ocasa1050/alyn-assets/main/wallet/"

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

    /**
     * 1) a drawable inside the app with that name,
     * 2) a full http(s) URL,
     * 3) a file on the image host: IMAGE_BASE_URL + sprite (".png" is added when there is no extension).
     * Downloaded images are cached on the phone by Glide.
     */
    private fun loadImage(view: ImageView, sprite: String) {
        Glide.with(context).clear(view)
        if (sprite.isEmpty() || sprite == "none") {
            view.setImageDrawable(null)
            return
        }
        val resId = context.resources.getIdentifier(sprite, "drawable", context.packageName)
        if (resId != 0) {
            view.setImageResource(resId)
            return
        }
        val url = when {
            sprite.startsWith("http://") || sprite.startsWith("https://") -> sprite
            sprite.contains('.') -> IMAGE_BASE_URL + sprite
            else -> "$IMAGE_BASE_URL$sprite.png"
        }
        view.setImageDrawable(null)
        Glide.with(context).load(url).into(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.donate_cell, parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.priceText.text = if (item.price > 0) priceFormat.format(item.price.toLong()) else "-"
        holder.nameText.text = item.name

        loadImage(holder.image, item.sprite)

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
