package unipd.dei.cashelper

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.github.mikephil.charting.charts.PieChart
import unipd.dei.cashelper.R
import unipd.dei.cashelper.helpers.DBHelper
import java.text.SimpleDateFormat
import java.util.*

/**
 * Implementation of App Widget functionality.
 */
class WidgetApp : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
}

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