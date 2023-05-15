package unipd.dei.cashelper.adapters

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.res.ColorStateList
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import unipd.dei.cashelper.R
import unipd.dei.cashelper.helpers.DBHelper
import unipd.dei.cashelper.ui.HomeFragment
import unipd.dei.cashelper.ui.HomeFragmentDirections
import java.text.DecimalFormat
import java.text.FieldPosition

class OutflowListAdapter(private val itemByCategory: MutableMap<String, ArrayList<DBHelper.ItemInfo>>, private val categoryColor: ArrayList<Int>, private val rateArray: ArrayList<Double>) : RecyclerView.Adapter<OutflowListAdapter.CategoryViewHolder>() {
    private lateinit var db :DBHelper


    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryItem: TextView = itemView.findViewById(R.id.category_item)
        private val rateItem: TextView = itemView.findViewById(R.id.rate_item)
        private val totalItem: TextView = itemView.findViewById(R.id.total_item)
        private val circle = itemView.findViewById<View>(R.id.category_color)
        fun bind(categoryName: String, total: Double, colorName: Int, rate: Double){
            categoryItem.text = categoryName
            rateItem.text = rate.toString() + "%"
            totalItem.text = total.toString() + "€"
            circle.backgroundTintList = ColorStateList.valueOf(colorName)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_incoming, parent, false)


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
        holder.bind(categoryName[position], totalByCategory[position], categoryColor[position], rateFormatted.toDouble())
    }

    //metodo per estrarre l'array di chiavi
    private fun catchKeys(): ArrayList<String>{
        return ArrayList<String>(itemByCategory.keys)
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