package unipd.dei.cashelper.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import unipd.dei.cashelper.R
import unipd.dei.cashelper.helpers.DBHelper
import unipd.dei.cashelper.ui.UpdateCategoryFragment

class CategoryListAdapter(private val categoryList: ArrayList<String>, private var listener: CategoryListAdapter.OnCategoryDeletedListener, private val updateCategoryFragment: UpdateCategoryFragment) : RecyclerView.Adapter<CategoryListAdapter.ItemsViewHolder>() {
    private lateinit var db: DBHelper
    private lateinit var defaultCategories: ArrayList<String>
    inner class ItemsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val buttonDelete: Button = itemView.findViewById(R.id.delete_basket)
        private val categoryText: TextView = itemView.findViewById(R.id.category_name)

        fun bind(category: String) {
            categoryText.text = category
            buttonDelete.isEnabled = !defaultCategories.contains(category)
            if(!buttonDelete.isEnabled)
                buttonDelete.foregroundTintList =
                    AppCompatResources.getColorStateList(updateCategoryFragment.requireContext(), R.color.Disable)

            buttonDelete.setOnClickListener { v ->
                val builder = AlertDialog.Builder(v.context)
                builder.setMessage("Sei sicuro di voler eliminare questo elemento? " +
                        "Cancellerai tutti gli elementi di questa categoria!")
                    .setPositiveButton("Elimina") { _, _ ->
                        this@CategoryListAdapter.listener.onCategoryDeleted(category)
                    }
                    .setNegativeButton("Annulla") { dialog, _ ->
                        dialog.cancel()
                    }
                    .show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_fragment_category, parent, false)

        if (!::db.isInitialized){
            db = DBHelper(parent.context)
            defaultCategories = db.getDefaultCategories()
        }

        return ItemsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemsViewHolder, position: Int) {
        holder.bind(categoryList[position])
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    interface OnCategoryDeletedListener {
        fun onCategoryDeleted(category: String)
    }
}