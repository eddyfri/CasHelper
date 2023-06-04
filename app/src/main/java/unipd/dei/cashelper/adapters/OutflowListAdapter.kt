package unipd.dei.cashelper.adapters

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import unipd.dei.cashelper.R
import unipd.dei.cashelper.helpers.DBHelper
import unipd.dei.cashelper.ui.OutflowFragment
import java.text.DecimalFormat

class OutflowListAdapter(private val itemByCategory: MutableMap<String, ArrayList<DBHelper.ItemInfo>>, private val categoryColor: ArrayList<Int>, private val rateArray: ArrayList<Double>, private val outflowFragment: OutflowFragment) : RecyclerView.Adapter<OutflowListAdapter.CategoryViewHolder>() {
    private lateinit var db :DBHelper


    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //declare the circle that contains the color for every category
        private val categoryItem: TextView = itemView.findViewById(R.id.category_item)
        //declare the textview that contains the name of the category
        private val circle = itemView.findViewById<View>(R.id.category_color)
        //declare the textview that contains the rate of the category in relation to the total
        private val rateItem: TextView = itemView.findViewById(R.id.rate_item)
        //declare the textview that contains the total for the category
        private val totalItem: TextView = itemView.findViewById(R.id.total_item)

        @SuppressLint("SetTextI18n")
        fun bind(categoryName: String, total: Double, colorName: Int, rate: String){
            circle.backgroundTintList = ColorStateList.valueOf(colorName)
            categoryItem.text = categoryName
            rateItem.text = "$rate%"
            totalItem.text = total.toString() + "€"
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
        //this variable contains all the category that have at least one incoming
        val categoryName = catchKeys()
        //this variable contains the total amount for all the categories, in the same order
        val totalByCategory = getTotalCategory()
        //these variables is needed to specify the format of the total
        val decimalFormat = DecimalFormat("#.#")
        var rateFormatted = decimalFormat.format(rateArray[position])
        //replace "." instead of ","
        rateFormatted = rateFormatted.replace(",",".", true)

        holder.bind(categoryName[position], totalByCategory[position], categoryColor[position], rateFormatted)

        //here createPopUp in incoming fragment is called. The name of the selected category and
        //the mutable list of category and item are passed as parameters.
        holder.itemView.setOnClickListener{
            outflowFragment.createPopUp(categoryName[position], itemByCategory)
        }

    }

    //his method returns an arraylist containing the keys of the mutable list,
    //which are the categories that have at least one incoming
    private fun catchKeys(): ArrayList<String>{
        return ArrayList<String>(itemByCategory.keys)
    }

    //this method return an arraylist that contains the total for every category
    //in the same order of the category.
    private fun getTotalCategory(): ArrayList<Double>{
        val totalCategory = ArrayList<Double>(itemByCategory.size)
        var total = 0.0
        for (itemList in itemByCategory.values) {
            for (element in itemList) {
                total += element.price
            }
            totalCategory.add(total)
            total = 0.0
        }

        return  totalCategory
    }

}