package unipd.dei.cashelper

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
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
import java.text.SimpleDateFormat
import java.util.*

/**
 * Implementation of App Widget functionality.
 */
class WidgetApp : AppWidgetProvider(), DBHelper.DatabaseObserver {
    private lateinit var db: DBHelper
    private lateinit var pieChart: PieChart
    private lateinit var entries: MutableList<PieEntry>
    private lateinit var set: PieDataSet
    private lateinit var data: PieData
    private lateinit var smallView: RemoteViews
    private lateinit var wideView: RemoteViews
    private lateinit var tallView: RemoteViews
    private lateinit var remoteViews: RemoteViews

    private lateinit var serviceIntent: Intent

    // Metodo che viene chiamato dal sistema Android quando un'applicazione Widget deve essere aggiornata.
    // Questo metodo viene definito all'interno di una classe che estende AppWidgetProvider,
    // e si occupa di aggiornare l'interfaccia utente del widget
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        /*
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            // updateAppWidget(context, appWidgetManager, appWidgetId)
            val views = RemoteViews(context.packageName, R.layout.widget_app)

            // Crea il PieChart e imposta i dati e la configurazione
            db = DBHelper(context as Context)

            val itemInfo: MutableList<DBHelper.ItemInfo>
            itemInfo = db.getItem(getCurrentMonth(), getCurrentYear())
            pieChart = PieChart(context)
            createPieChartWidget(views, itemInfo, context)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
         */

        // Crea il PieChart e imposta i dati e la configurazione
        db = DBHelper(context as Context)

        for(appWidgetId in appWidgetIds) {
            db.addObserver(this, appWidgetManager, appWidgetId)

            val itemInfo: MutableList<DBHelper.ItemInfo>
            itemInfo = db.getItem(getCurrentMonth(), getCurrentYear())
            pieChart = PieChart(context)
            // Crea una Bitmap del PieChart
            val width = 400 // Larghezza della Bitmap
            val height = 400 // Altezza della Bitmap
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            smallView = RemoteViews(context.packageName, R.layout.widget_app)
            createPieChartWidget(itemInfo, context)
            // Imposta la Bitmap come immagine della ImageView all'interno della RemoteViews
            smallView.setImageViewBitmap(R.id.pieChartWidget, bitmap)
            pieChart.layout(0, 0, width, height)
            pieChart.draw(canvas)
            smallView.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            wideView = RemoteViews(context.packageName, R.layout.widget_app_wide)
            wideView.setImageViewBitmap(R.id.pieChartWidgetWide, bitmap)
            pieChart.layout(0, 0, width, height)
            pieChart.draw(canvas)
            wideView.setTextViewText(R.id.month_year_widget_text, "${getCurrentMonth()} ${getCurrentYear()}")
            wideView.setTextViewText(R.id.incoming_widget_text, "Entrate: ${getIncoming(itemInfo)} €")
            wideView.setTextViewText(R.id.outflow_widget_text, "Uscite: ${getExits(itemInfo)} €")
            wideView.setTextViewText(R.id.total_widget_text, "Totale: ${getTotal(itemInfo)} €")
            wideView.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            tallView = RemoteViews(context.packageName, R.layout.widget_app_tall)
            tallView.setImageViewBitmap(R.id.pieChartWidgetTall, bitmap)
            pieChart.layout(0, 0, width, height)
            pieChart.draw(canvas)
            tallView.setTextViewText(R.id.month_year_widget_text, "${getCurrentMonth()} ${getCurrentYear()}")
            tallView.setTextViewText(R.id.incoming_widget_text, "Entrate: ${getIncoming(itemInfo)} €")
            tallView.setTextViewText(R.id.outflow_widget_text, "Uscite: ${getExits(itemInfo)} €")
            tallView.setTextViewText(R.id.total_widget_text, "Totale: ${getTotal(itemInfo)} €")
            tallView.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            serviceIntent = Intent(context, ListWidgetService::class.java)
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            serviceIntent.data = Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME))
            tallView.setRemoteAdapter(R.id.item_list_widget, serviceIntent)
        }

        val viewMapping: Map<SizeF, RemoteViews> = mapOf(
            SizeF(140f, 110f) to smallView,
            SizeF(270f, 110f) to wideView,
            SizeF(140f, 280f) to tallView
        )
        remoteViews = RemoteViews(viewMapping)

        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews)
    }

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
        //enable percent values
        pieChart.setUsePercentValues(true)
        //set Entry label's color (temporally dis-activated)
        pieChart.data.setValueTextColor(Color.rgb(255, 255, 255))
        //set Entry text
        pieChart.data.setValueTextSize(0f)
        //delete description
        pieChart.description.text = ""
        //hole
        pieChart.holeRadius = 30f
        pieChart.setTransparentCircleAlpha(0)
    }

    private fun updatePieChartWidget(itemInfo: MutableList<DBHelper.ItemInfo>, context: Context) {
        val totIncoming = getIncoming(itemInfo)
        val totExits = getExits(itemInfo)
        entries.clear()
        entries.add(PieEntry(totIncoming.toFloat(), "Entrate"))
        entries.add(PieEntry(totExits.toFloat(), "Uscite"))
        set = PieDataSet(entries, "")
        data = PieData(set)
        pieChart.data = data
        val colors: MutableList<Int> = ArrayList()
        val exitColor = ContextCompat.getColor(context, R.color.Entries)
        colors.add(exitColor)
        val entriesColor = ContextCompat.getColor(context, R.color.Exits)
        colors.add(entriesColor)
        set.colors = colors
        pieChart.data.setValueTextSize(0f)
        data.notifyDataChanged()
        set.notifyDataSetChanged()
        pieChart.notifyDataSetChanged()
        pieChart.invalidate()
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
    override fun changeWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        Log.d(TAG, "AGGIORNAMENTO WIDGET")
        // aggiornare widget
        val itemInfo = db.getItem(getCurrentMonth(), getCurrentYear())
        // Crea una Bitmap del PieChart
        val width = 400 // Larghezza della Bitmap
        val height = 400 // Altezza della Bitmap
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        Log.d(TAG, "itemInfo: $itemInfo")
        Log.d(TAG, "change widget: incomings=${getIncoming(itemInfo)}, outflows=${getExits(itemInfo)}")
        smallView = RemoteViews(context.packageName, R.layout.widget_app)
        updatePieChartWidget(itemInfo, context)
        // Imposta la Bitmap come immagine della ImageView all'interno della RemoteViews
        smallView.setImageViewBitmap(R.id.pieChartWidget, bitmap)
        pieChart.layout(0, 0, width, height)
        pieChart.draw(canvas)
        appWidgetManager.updateAppWidget(appWidgetId, smallView)
        wideView = RemoteViews(context.packageName, R.layout.widget_app_wide)
        wideView.setImageViewBitmap(R.id.pieChartWidgetWide, bitmap)
        pieChart.layout(0, 0, width, height)
        pieChart.draw(canvas)
        wideView.setTextViewText(R.id.incoming_widget_text, "Entrate: ${getIncoming(itemInfo)} €")
        wideView.setTextViewText(R.id.outflow_widget_text, "Uscite: ${getExits(itemInfo)} €")
        wideView.setTextViewText(R.id.total_widget_text, "Totale: ${getTotal(itemInfo)} €")
        appWidgetManager.updateAppWidget(appWidgetId, wideView)
        tallView = RemoteViews(context.packageName, R.layout.widget_app_tall)
        tallView.setImageViewBitmap(R.id.pieChartWidgetTall, bitmap)
        pieChart.layout(0, 0, width, height)
        pieChart.draw(canvas)
        tallView.setTextViewText(R.id.incoming_widget_text, "Entrate: ${getIncoming(itemInfo)} €")
        tallView.setTextViewText(R.id.outflow_widget_text, "Uscite: ${getExits(itemInfo)} €")
        tallView.setTextViewText(R.id.total_widget_text, "Totale: ${getTotal(itemInfo)} €")
        appWidgetManager.updateAppWidget(appWidgetId, tallView)
        serviceIntent.data = Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME))
        tallView.setRemoteAdapter(R.id.item_list_widget, serviceIntent)
        val viewMapping: Map<SizeF, RemoteViews> = mapOf(
            SizeF(140f, 110f) to smallView,
            SizeF(270f, 110f) to wideView,
            SizeF(140f, 280f) to tallView
        )
        remoteViews = RemoteViews(viewMapping)
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
    }
}

// Metodo che consente di aggiornare direttamente l'interfaccia utente di un singolo widget, senza dover passare attraverso il sistema Android.
// Questo metodo viene chiamato all'interno di una classe che estende AppWidgetProvider,
// o all'interno di un'attività o di un servizio, per aggiornare l'interfaccia utente di un widget specifico
internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
    /*
    val widgetText = context.getString(R.string.appwidget_text)
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.widget_app)
    views.setTextViewText(R.id.appwidget_text, widgetText)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
     */
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
