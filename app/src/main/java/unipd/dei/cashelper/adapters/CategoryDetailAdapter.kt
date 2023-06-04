package unipd.dei.cashelper.adapters

import android.annotation.SuppressLint
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import unipd.dei.cashelper.R
import unipd.dei.cashelper.helpers.DBHelper


class CategoryDetailAdapter(private val itemsArray: ArrayList<DBHelper.ItemInfo>?) : RecyclerView.Adapter<CategoryDetailAdapter.ItemsViewHolder>() {
    private lateinit var db :DBHelper

    class ItemsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //declare the textview to show the category of the transaction
        private val itemCategory: TextView = itemView.findViewById(R.id.category_item_detail)
        //declare the textview to show the price of the transaction
        private val itemPrice: TextView = itemView.findViewById(R.id.price_item_detail)
        //declare the textview to show the date of the transaction
        private val itemDate: TextView = itemView.findViewById(R.id.date_item_detail)

        @SuppressLint("SetTextI18n")
        fun bind(categoryName: String, price: Double, date: String){
            itemCategory.text = categoryName
            itemPrice.text = price.toString() + "€"
            itemDate.text = date
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category_detail, parent, false)


        if (!::db.isInitialized){
            db = DBHelper(parent.context)
        }

        return ItemsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemsArray?.size ?: 0
    }

    override fun onBindViewHolder(holder: ItemsViewHolder, position: Int) {
        val name = itemsArray!![position].category
        val price = itemsArray[position].price
        val date = itemsArray[position].day.toString() + "/" + getNumberMonth(itemsArray[position].month) + "/" + itemsArray[position].year.toString()

        holder.bind(name, price, date)
    }

    private fun getNumberMonth(month: String?): String {
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