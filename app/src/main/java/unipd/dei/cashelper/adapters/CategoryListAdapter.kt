package unipd.dei.cashelper.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import unipd.dei.cashelper.R

class CategoryListAdapter(private val categoryList: ArrayList<String>) : RecyclerView.Adapter<CategoryListAdapter.ItemsViewHolder>() {

    class ItemsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val buttonDelete: Button = itemView.findViewById(R.id.delete_basket)
        private val categoryText: TextView = itemView.findViewById(R.id.category_name)

        fun bind(category: String) {
            categoryText.text = category
            // impostazioni di eliminazione categoria
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_fragment_category, parent, false)

        return ItemsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemsViewHolder, position: Int) {
        holder.bind(categoryList[position])
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }
}