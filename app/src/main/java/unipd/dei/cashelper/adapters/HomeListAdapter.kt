package unipd.dei.cashelper.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import unipd.dei.cashelper.R
import unipd.dei.cashelper.helpers.DBHelper

class HomeListAdapter(private val itemList: MutableList<DBHelper.ItemInfo>): RecyclerView.Adapter<HomeListAdapter.ItemViewHolder>() {

    class ItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val itemCategory: TextView = itemView.findViewById(R.id.category_item)
        private val itemPrice: TextView = itemView.findViewById(R.id.price_item)
        private val itemDate: TextView = itemView.findViewById(R.id.date_item)

        fun bind(itemInfo: DBHelper.ItemInfo) {
            itemCategory.text = itemInfo.category
            itemPrice.text = itemInfo.price.toString()
            val date =
                itemInfo.day.toString() + "/" + itemInfo.month + "/" + itemInfo.year.toString()
            itemDate.text = date
            // da testare e decommentare dopo la creazione di AddItemActivity
            /*
            itemView.setOnClickListener { view ->
                val intent = Intent(view.context, AddItemActivity::class.java)
                intent.putExtra("category", itemCategory)
                intent.putExtra("price", itemPrice)
                intent.putExtra("date", itemDate)
                itemView.context.startActivity(intent)
            }
            */
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_home, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}