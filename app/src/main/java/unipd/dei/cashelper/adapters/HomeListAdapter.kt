package unipd.dei.cashelper.adapters

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import unipd.dei.cashelper.R
import unipd.dei.cashelper.helpers.DBHelper
import unipd.dei.cashelper.ui.HomeFragment
import unipd.dei.cashelper.ui.HomeFragmentDirections

class HomeListAdapter(private val itemList: MutableList<DBHelper.ItemInfo>, private var listener: OnItemDeletedListener): RecyclerView.Adapter<HomeListAdapter.ItemViewHolder>() {
    private var selectedItemId: Int = -1
    private lateinit var db: DBHelper

    private val onClickListener = View.OnClickListener { v ->
        selectedItemId = v.tag.toString().toInt()
        val action = HomeFragmentDirections.actionHomeFragmentToUpdateFragment(selectedItemId)
        v.findNavController().navigate(action)
    }

    private val onLongClickListener = View.OnLongClickListener { v ->
        selectedItemId = v.tag.toString().toInt()
        val builder = AlertDialog.Builder(v.context)
        builder.setMessage("Sei sicuro di voler eliminare questo elemento?")
            .setPositiveButton("Elimina") { _, _ ->
                db.removeItem(selectedItemId)

                listener.onItemDeleted()
            }
            .setNegativeButton("Annulla") { dialog, _ ->
                dialog.cancel()
            }
            .show()
        return@OnLongClickListener true
    }

    class ItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnCreateContextMenuListener {
        private val itemCategory: TextView = itemView.findViewById(R.id.category_item)
        private val itemPrice: TextView = itemView.findViewById(R.id.price_item)
        private val itemDate: TextView = itemView.findViewById(R.id.date_item)

        fun bind(itemInfo: DBHelper.ItemInfo) {
            itemCategory.text = itemInfo.category
            itemPrice.text = itemInfo.price.toString() + " €"
            val date =
                itemInfo.day.toString() + "/" + getNumberMonth(itemInfo.month) + "/" + itemInfo.year.toString()
            itemDate.text = date
            itemView.setOnCreateContextMenuListener(this)
            itemView.tag = itemInfo.id
            /*
            itemView.setOnClickListener {
                val action = HomeFragmentDirections.actionHomeFragmentToUpdateFragment(itemInfo.id)
                itemView.findNavController().navigate(action)
            }

             */
        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
            menu?.setHeaderTitle("")
            menu?.add(Menu.NONE, R.id.delete_item, Menu.NONE, R.string.deleteItem)
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

        view.setOnLongClickListener(onLongClickListener)
        view.setOnClickListener(onClickListener)

        if (!::db.isInitialized){
            db = DBHelper(parent.context)
        }

        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    interface OnItemDeletedListener {
        fun onItemDeleted()
    }

}