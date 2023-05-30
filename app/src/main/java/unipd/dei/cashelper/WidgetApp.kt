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

    // salvataggio dei widget ids, in caso di aggiornamento del widget dovuto alla modifica di un item
    // nel database, verrà richiamato il metodo onReceive. In questo metodo non ho visibilità sui
    // widget ids utilizzati, quindi mantengo salvati con le sharedPreferences questi dati.
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
        //this.appWidgetManager = appWidgetManager

        // inizializzo il service della lista del widget se non ancora inizializzato, cioé solamente
        // durante la prima creazione del widget e non ogni aggiornamento (qui con aggiornamento non
        // intendo l'aggiornamento del db ma l'aggioramento temporale del widget, indicato file
        // widget_app_info.xml)
        if(!::serviceIntent.isInitialized)
            serviceIntent = Intent(context, ListWidgetService::class.java)

        for(appWidgetId in appWidgetIds) {
            // aggiornamento del widget
            updateWidget(context)
            // aggiunge il widget id come dato extra di serviceIntent, specifica quale widget deve modificare
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            // gli elementi del widget id vengono aggiornati a quelli correnti
            serviceIntent.data = Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME))
            // viene settato il serviceIntent come remoteAdapter di item_list_widget
            tallView.setRemoteAdapter(R.id.item_list_widget, serviceIntent)

            // creazione di un unica remoteView composta dalle tre diverse remoteView di dimensioni differenti,
            // a seconda della dimensione del widget cambierà il file xml corrispondente:
            // larghezza <= 140f e altezza <= 110f allora carica la smallView,
            // 140f <= larghezza <= 140f oppure altezza <= 110f allora carica la wideView,
            // larghezza >= 140f oppure altezza >= 110f allora carica la tallView,
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
        // entra all'interno del widget solo quando deve essere aggiornato, il programma è stato
        // gestito in modo tale che intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE solo quando
        // viene effettuata la richiesta broadcast da parte del database.
        if(intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            // recupera l'istanza di appWidgetManager
            val appWidgetManager = AppWidgetManager.getInstance(context)
            // recupero tramite le sharedPreferences dei widget ids
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val widgetIdsString = prefs.getString(KEY_WIDGET_IDS, null)
            val widgetIds = widgetIdsString?.split(",")?.mapNotNull { it.toIntOrNull() }?.toIntArray()

            // per ogni widget ids aggiorno i suoi elementi
            widgetIds?.let { widgetIds ->
                for(appWidgetId in widgetIds) {
                    // aggiornamento widget
                    updateWidget(context)
                    // aggiornamento lista del widget, notifico al service che i dati sono stati cambiati,
                    // esso provvederà a recuperare l'istanza aggiornata del database e a modificare i dati
                    // della lista.
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

    // Creazione PieChart
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

    // Aggiornamento effettivo widget, tutti gli elementi tranne la lista
    private fun updateWidget(context: Context) {
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
