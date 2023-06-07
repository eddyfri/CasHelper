package unipd.dei.cashelper

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.util.SizeF
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import unipd.dei.cashelper.adapters.ListWidgetService
import unipd.dei.cashelper.helpers.DBHelper
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Implementation of App Widget functionality.
 */
class WidgetApp : AppWidgetProvider() {
    private lateinit var db: DBHelper
    private lateinit var pieChart: PieChart
    private lateinit var entries: MutableList<PieEntry>
    private lateinit var set: PieDataSet
    private lateinit var data: PieData
    private lateinit var smallView: RemoteViews
    private lateinit var wideView: RemoteViews
    private lateinit var tallView: RemoteViews
    private lateinit var appWidgetManager: AppWidgetManager
    private lateinit var remoteViews: RemoteViews

    private lateinit var serviceIntent: Intent

    // saving widget ids, in case of widget update due to the modification of an element
    // in the database, the onReceive method will be called.
    // In onReceive I have no visibility on the widget ids used, so
    // I keep this data saved with the sharedPreferences.
    companion object {
        private const val PREFS_NAME = "WidgetPrefs"
        private const val KEY_WIDGET_IDS = "widgetIds"
    }
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // salvataggio widget ids
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(KEY_WIDGET_IDS, appWidgetIds.joinToString(","))
        editor.apply()
        // creo un istanza del db
        db = DBHelper(context as Context)

        // I initialize the widget list service if it hasn't been initialized yet,
        // i.e. only during the first creation of the widget and not every update
        // (here with update I don't mean the update of the db but the temporal update of the widget,
        // indicated file widget_app_info.xml)
        if(!::serviceIntent.isInitialized)
            serviceIntent = Intent(context, ListWidgetService::class.java)

        for(appWidgetId in appWidgetIds) {
            // update widget
            updateWidget(context)
            // add widget id as extra data of serviceIntent, specify which widget to modify
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            // the widget id items are updated to the current ones
            serviceIntent.data = Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME))
            // set the serviceIntent as remoteAdapter of item_list_widget
            tallView.setRemoteAdapter(R.id.item_list_widget, serviceIntent)

            // creation of a single remoteView made up of three different remoteViews of different sizes,
            // depending on the size of the widget the corresponding xml file will change:
            // width <= 140f and height <= 110f then load the smallView,
            // 140f <= width <= 140f or height <= 110f then load the wideView,
            // width >= 140f or height >= 110f then load the tallView
            val viewMapping: Map<SizeF, RemoteViews> = mapOf(
                SizeF(140f, 110f) to smallView,
                SizeF(270f, 110f) to wideView,
                SizeF(140f, 280f) to tallView
            )
            remoteViews = RemoteViews(viewMapping)
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        db = DBHelper(context as Context)
        // enters the widget only when it needs to be updated, the program has been
        // handled so that intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE only when
        // the broadcast request is made by the database.
        if(intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            // restore appWidgetManager instance
            val appWidgetManager = AppWidgetManager.getInstance(context)
            // restore widgetIds with sharedPreferences
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val widgetIdsString = prefs.getString(KEY_WIDGET_IDS, null)
            val widgetIds = widgetIdsString?.split(",")?.mapNotNull { it.toIntOrNull() }?.toIntArray()

            // for each widgetIds update the elements
            widgetIds?.let { widgetIds ->
                for(appWidgetId in widgetIds) {
                    // update widget
                    updateWidget(context)
                    // updating the widget list, notifying the service that the data has been changed,
                    // it will fetch the updated instance of the database and modify the data
                    // of the list.
                    tallView.setRemoteAdapter(R.id.item_list_widget, Intent(context, ListWidgetService::class.java))
                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.item_list_widget)
                    appWidgetManager.updateAppWidget(appWidgetId, tallView)
                    val viewMapping: Map<SizeF, RemoteViews> = mapOf(
                        SizeF(140f, 110f) to smallView,
                        SizeF(270f, 110f) to wideView,
                        SizeF(140f, 280f) to tallView
                    )
                    remoteViews = RemoteViews(viewMapping)

                    appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
                }
            }
        }
    }

    // create PieChart
    private fun createPieChartWidget(itemInfo: MutableList<DBHelper.ItemInfo>, context: Context) {
        val totIncoming = getIncoming(itemInfo)
        val totExits = getExits(itemInfo)
        //entries values
        entries = ArrayList()
        entries.add(PieEntry(totIncoming.toFloat(), "Entrate"))
        entries.add(PieEntry(totExits.toFloat(), "Uscite"))
        set = PieDataSet(entries, "")
        data = PieData(set)
        pieChart.data = data

        //setting PieChart
        //color's Entry
        val colors: MutableList<Int> = ArrayList()
        val exitColor = ContextCompat.getColor(context, R.color.Entries)
        colors.add(exitColor)
        val entriesColor = ContextCompat.getColor(context, R.color.Exits)
        colors.add(entriesColor)
        set.colors = colors
        //not draw labels
        pieChart.setDrawEntryLabels(false)
        //disable text value
        pieChart.data.setValueTextSize(0f)
        //delete description
        pieChart.description.text = ""

        pieChart.legend.textColor = ContextCompat.getColor(context, R.color.legend)


        //hole
        pieChart.holeRadius = 30f
        pieChart.setHoleColor(ContextCompat.getColor(context, R.color.hole))
        pieChart.setTransparentCircleAlpha(0)
    }

    // update widget, all the elements except the listView
    private fun updateWidget(context: Context) {
        val itemInfo: MutableList<DBHelper.ItemInfo>
        itemInfo = db.getItem(getCurrentMonth(), getCurrentYear())
        pieChart = PieChart(context)

        // create Bitmap of the PieChart
        val width = 400 // Bitmap width
        val height = 400 // Bitmap height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        smallView = RemoteViews(context.packageName, R.layout.widget_app)
        createPieChartWidget(itemInfo, context)
        // Set the Bitmap as an image of the ImageView inside the RemoteViews
        smallView.setImageViewBitmap(R.id.pieChartWidget, bitmap)
        pieChart.layout(0, 0, width, height)
        pieChart.draw(canvas)
        smallView.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
        wideView = RemoteViews(context.packageName, R.layout.widget_app_wide)
        wideView.setImageViewBitmap(R.id.pieChartWidgetWide, bitmap)
        pieChart.layout(0, 0, width, height)
        pieChart.draw(canvas)
        wideView.setTextViewText(R.id.month_year_widget_text, "${getCurrentMonth()} ${getCurrentYear()}")
        wideView.setTextViewText(R.id.incoming_widget_text, "Entrate: ${DecimalFormat("#.##").format(getIncoming(itemInfo)).replace(",",".", true)} €")
        wideView.setTextViewText(R.id.outflow_widget_text, "Uscite: ${DecimalFormat("#.##").format(getExits(itemInfo)).replace(",",".", true)} €")
        wideView.setTextViewText(R.id.total_widget_text, "Totale: ${DecimalFormat("#.##").format(getTotal(itemInfo)).replace(",",".", true)} €")
        wideView.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
        val buttonIntent = Intent(context, MainActivity::class.java)
        buttonIntent.action = "OPEN_ADD_ITEM_FRAGMENT"
        val pendingButtonIntent = PendingIntent.getActivity(context, 0, buttonIntent, PendingIntent.FLAG_IMMUTABLE)
        wideView.setOnClickPendingIntent(R.id.add_item_widget, pendingButtonIntent)
        tallView = RemoteViews(context.packageName, R.layout.widget_app_tall)
        tallView.setImageViewBitmap(R.id.pieChartWidgetTall, bitmap)
        pieChart.layout(0, 0, width, height)
        pieChart.draw(canvas)
        tallView.setTextViewText(R.id.month_year_widget_text, "${getCurrentMonth()} ${getCurrentYear()}")
        tallView.setTextViewText(R.id.incoming_widget_text, "Entrate: ${DecimalFormat("#.##").format(getIncoming(itemInfo)).replace(",",".", true)} €")
        tallView.setTextViewText(R.id.outflow_widget_text, "Uscite: ${DecimalFormat("#.##").format(getExits(itemInfo)).replace(",",".", true)} €")
        tallView.setTextViewText(R.id.total_widget_text, "Totale: ${DecimalFormat("#.##").format(getTotal(itemInfo)).replace(",",".", true)} €")
        tallView.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
        tallView.setOnClickPendingIntent(R.id.add_item_widget, pendingButtonIntent)

    }

    private fun getIncoming(itemInfo: MutableList<DBHelper.ItemInfo>): Double {
        var totIncoming = 0.0
        for (item in itemInfo) {
            if(item.type == "Entrata")
                totIncoming += item.price
        }
        return totIncoming
    }

    private fun getExits(itemInfo: MutableList<DBHelper.ItemInfo>): Double {
        var totExits = 0.0
        for (item in itemInfo) {
            if(item.type == "Uscita")
                totExits += item.price
        }
        return totExits
    }

    private fun getTotal(itemInfo: MutableList<DBHelper.ItemInfo>): Double = getIncoming(itemInfo) - getExits(itemInfo)

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
