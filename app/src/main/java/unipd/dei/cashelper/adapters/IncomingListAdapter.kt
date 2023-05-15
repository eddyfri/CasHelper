package unipd.dei.cashelper.adapters

import android.content.res.ColorStateList
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import unipd.dei.cashelper.R
import unipd.dei.cashelper.helpers.DBHelper
import unipd.dei.cashelper.ui.IncomingFragment
import java.text.DecimalFormat


class IncomingListAdapter(private val itemByCategory: MutableMap<String, ArrayList<DBHelper.ItemInfo>>, private val categoryColor: ArrayList<Int>, private val rateArray: ArrayList<Double>, private val incomingFragment: IncomingFragment) : RecyclerView.Adapter<IncomingListAdapter.CategoryViewHolder>() {
    private lateinit var db :DBHelper




    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val circle = itemView.findViewById<View>(R.id.category_color)
        private val categoryItem: TextView = itemView.findViewById(R.id.category_item)
        private val rateItem: TextView = itemView.findViewById(R.id.rate_item)
        private val totalItem: TextView = itemView.findViewById(R.id.total_item)
        fun bind(categoryName: String, total: Double, colorName: Int, rate: Double){
            rateItem.text = rate.toString() + "%"
            categoryItem.text = categoryName
            totalItem.text = total.toString() + "€"
            circle.backgroundTintList = ColorStateList.valueOf(colorName)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_incoming_outflow, parent, false)


        if (!::db.isInitialized){
            db = DBHelper(parent.context)
        }

        return CategoryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemByCategory.size
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val categoryName = catchKeys()
        val totalByCategory = getTotalCategory()
        val decimalFormat = DecimalFormat("#.#")
        val rateFormatted = decimalFormat.format(rateArray[position])
        val selectedItem = categoryName[position]

        holder.bind(selectedItem, totalByCategory[position], categoryColor[position], rateFormatted.toDouble())

        holder.itemView.setOnClickListener{
            incomingFragment.createPopUp(selectedItem)
        }
    }




    //metodo per estrarre l'array di chiavi
    private fun catchKeys(): ArrayList<String>{
        return ArrayList(itemByCategory.keys)
    }

    //metodo per calcolarsi il totale di una categoria
    private fun getTotalCategory(): ArrayList<Double>{
        var totalSize = itemByCategory.size
        var totalCategory = ArrayList<Double>(totalSize)
        var total = 0.0
        for (itemList in itemByCategory.values) {
            for (element in itemList) {
                total = total + element.price
            }
            totalCategory.add(total)
            total = 0.0
        }
        return  totalCategory
    }


}