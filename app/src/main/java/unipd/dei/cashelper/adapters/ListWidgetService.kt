package unipd.dei.cashelper.adapters

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import unipd.dei.cashelper.R
import unipd.dei.cashelper.helpers.DBHelper
import java.text.SimpleDateFormat
import java.util.*

class ListWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return ListRemoteViewsFactory(this.applicationContext)
    }

    class ListRemoteViewsFactory(private val context: Context) : RemoteViewsFactory {

        private lateinit var db : DBHelper
        private lateinit var itemList: MutableList<DBHelper.ItemInfo>

        override fun onCreate() {
            db = DBHelper(context)
            itemList = db.getItem(getCurrentMonth(), getCurrentYear())
            itemList = sortByDate(itemList)
        }

        override fun onDataSetChanged() {
            db = DBHelper(context)
            itemList = db.getItem(getCurrentMonth(), getCurrentYear())
            itemList = sortByDate(itemList)
        }

        override fun onDestroy() {
            db.close()
        }

        override fun getCount(): Int {
            return itemList.size
        }

        override fun getViewAt(position: Int): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.item_widget)
            views.setTextViewText(R.id.category_item_widget, itemList[position].category)
            if(itemList[position].type == "Uscita")
                views.setTextViewText(R.id.price_item_widget, "-" + itemList[position].price.toString())
            else
                views.setTextViewText(R.id.price_item_widget, itemList[position].price.toString())
            views.setTextViewText(R.id.date_item_widget, "${itemList[position].day}/${getNumberMonth(itemList[position].month)}/${itemList[position].year}")
            return views
        }

        override fun getLoadingView(): RemoteViews? {
            return null
        }

        override fun getViewTypeCount(): Int {
            return 1
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun hasStableIds(): Boolean {
            return true
        }

        private fun getCurrentMonth() : String {
            return when (SimpleDateFormat("MM", Locale.ENGLISH).format(Date())) {
                "01"   -> "Gennaio"
                "02"   -> "Febbraio"
                "03"   -> "Marzo"
                "04"   -> "Aprile"
                "05"   -> "Maggio"
                "06"   -> "Giugno"
                "07"  -> "Luglio"
                "08"  -> "Agosto"
                "09"  -> "Settembre"
                "10"  -> "Ottobre"
                "11"  -> "Novembre"
                else  -> "Dicembre"
            }
        }

        private fun getCurrentYear(): Int {
            return SimpleDateFormat("YYYY", Locale.ENGLISH).format(Date()).toInt()
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
        private fun sortByDate(itemInfo: MutableList<DBHelper.ItemInfo>) : MutableList<DBHelper.ItemInfo>{
            itemInfo.sortByDescending { it.day } //it is a lambda expression link to itemInfo
            return itemInfo
        }
    }
}