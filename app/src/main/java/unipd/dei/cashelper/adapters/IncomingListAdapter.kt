package unipd.dei.cashelper.adapters

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import unipd.dei.cashelper.R
import unipd.dei.cashelper.helpers.DBHelper
import unipd.dei.cashelper.ui.HomeFragment
import unipd.dei.cashelper.ui.HomeFragmentDirections
import java.text.FieldPosition

class IncomingListAdapter(private val itemByCategory: MutableMap<String, ArrayList<DBHelper.ItemInfo>>) : RecyclerView.Adapter<IncomingListAdapter.CategoryViewHolder>() {
    private lateinit var db :DBHelper


    //DA COMPLETARE TUTTA, DA IMPLEMENTARE UN METODO PER FARE RICEVERE GIà UNA LISTA DELLE CATEGORIE CON ENTRATE E LA SOMMA PER QUELLA CATEGORIA NELL'INCOMING FRAGMENT
    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryItem: TextView = itemView.findViewById(R.id.category_item)

        fun bind(categoryName: String){
            categoryItem.text = categoryName
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
        holder.bind(categoryName[position])
    }

    //metodo per estrarre l'array di chiavi
    private fun catchKeys(): ArrayList<String>{
        return ArrayList<String>(itemByCategory.keys)
    }
}