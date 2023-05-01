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
            itemPrice.text = itemInfo.price.toString() + " €"
            val date =
                itemInfo.day.toString() + "/" + getNumberMonth(itemInfo.month) + "/" + itemInfo.year.toString()
            itemDate.text = date
            itemView.setOnClickListener {
                // view -> updateItem con updateItemFragment
            }
        }

        private fun getNumberMonth(month: String): String {
            return when(month) {
                "Gennaio" -> "01"
                "Febbraio" -> "02"
                "Marzo" -> "03"
                "Aprile" -> "04"
                "Maggio" -> "05"
                "Giugno" -> "06"
                "Luglio" -> "07"
                "Agosto" -> "08"
                "Settembre" -> "09"
                "Ottobre" -> "10"
                "Novembre" -> "11"
                else -> "12"
            }
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