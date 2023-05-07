package unipd.dei.cashelper.ui

import android.app.DatePickerDialog
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import unipd.dei.cashelper.R
import unipd.dei.cashelper.adapters.HomeListAdapter
import unipd.dei.cashelper.helpers.DBHelper
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates


class IncomingFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabBack: ExtendedFloatingActionButton
    private lateinit var fabNext: ExtendedFloatingActionButton
    private lateinit var monthTextView: TextView
    private lateinit var yearTextView: TextView

    private lateinit var db: DBHelper

    // variabili per il chart
    private lateinit var pieChart: PieChart
    private lateinit var entries: MutableList<PieEntry>
    private lateinit var set: PieDataSet
    private lateinit var data: PieData

    private lateinit var month : String
    private var year by Delegates.notNull<Int>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_incoming, container,false)

        db = DBHelper(context as Context)

        month = IncomingFragmentArgs.fromBundle(requireArguments()).month
        year = IncomingFragmentArgs.fromBundle(requireArguments()).year

        //qui mi serve per passare all'adapter gli elementi
        var itemInfo = mutableListOf<DBHelper.ItemInfo>()
        itemInfo = db.getItem(month, year)



        fabBack = view.findViewById<ExtendedFloatingActionButton>(R.id.back_month)
        fabNext = view.findViewById<ExtendedFloatingActionButton>(R.id.next_month)
        monthTextView = view.findViewById<TextView>(R.id.month_text)
        yearTextView = view.findViewById<TextView>(R.id.year_text)
        recyclerView = view.findViewById(R.id.recycler_view)
        //creare un nuovo adapter e passargli itemInfo come parametro
        recyclerView.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)


        return view
    }

    private fun getOnlyIncoming(allItem: MutableList<DBHelper.ItemInfo>): MutableList<DBHelper.ItemInfo>{
        var onlyIncoming = mutableListOf<DBHelper.ItemInfo>()
        for (item in allItem) {
            if(item.type == "Entrata")
                onlyIncoming.add(item)
        }
        return onlyIncoming
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //imposto le textview di mese e anno nel mese e anno che mi vengono passati dalla schermata home
        monthTextView.text = month
        yearTextView.text = year.toString()

        fabBack.setOnClickListener {
            // cambia mese anno
            if(month == "Gennaio")
                year--
            month = backMonth(month)
            monthTextView.text = month
            yearTextView.text = year.toString()
            updateAll(month, year)
        }
        fabNext.setOnClickListener {
            // non posso andare più avanti del mese corrente, no mesi futuri
            if(month != getCurrentMonth() || year != getCurrentYear()) {
                // cambia mese anno
                if(month == "Dicembre")
                    year++
                month = nextMonth(month)
                monthTextView.text = month
                yearTextView.text = year.toString()
                updateAll(month, year)
            }
            else
                fabNext.hide()
        }

        //se entro nel fragment nel mese corrente nascondo il bottone per spostarsi al mese successivo
        if((month == getCurrentMonth()) && (year == getCurrentYear()))
            fabNext.hide()


        //imposto il pulsante per scegliere il mese precedente a quello selezionato
        fabBack.setOnClickListener {
            // cambio l'anno se siamo a gennaio
            if(month == "Gennaio")
                year--
            month = backMonth(month)
            monthTextView.text = month
            yearTextView.text = year.toString()
            updateAll(month, year)
        }

        //imposto il pulsante per scegliere il mese successivo a quello selezionato
        fabNext.setOnClickListener {
            //elimino la possibilità di scegliere dei mesi futuri a quello corrente
            if(month == getCurrentMonth() && year == getCurrentYear())
                fabNext.hide()
            else {
                // cambio l'anno se siamo a dicembre
                if(month == "Dicembre")
                    year++
                month = nextMonth(month)
                monthTextView.text = month
                yearTextView.text = year.toString()
                updateAll(month, year)
            }
        }
    }

    private fun createPieChart(view: View, itemInfo: MutableList<DBHelper.ItemInfo>) {
        pieChart = view.findViewById(R.id.chart_incoming)

    }

    private fun updateAll(month: String, year: Int) {
        if (!(month == getCurrentMonth() && year == getCurrentYear()))
            fabNext.show()
        else fabNext.hide()
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

    private fun nextMonth(month: String): String {
        return when(month) {
            "Gennaio" -> "Febbraio"
            "Febbraio" -> "Marzo"
            "Marzo" -> "Aprile"
            "Aprile" -> "Maggio"
            "Maggio" -> "Giugno"
            "Giugno" -> "Luglio"
            "Luglio" -> "Agosto"
            "Agosto" -> "Settembre"
            "Settembre" -> "Ottobre"
            "Ottobre" -> "Novembre"
            "Novembre" -> "Dicembre"
            else -> "Gennaio"
        }
    }

    private fun backMonth(month: String): String {
        return when(month) {
            "Gennaio" -> "Dicembre"
            "Dicembre" -> "Novembre"
            "Novembre" -> "Ottobre"
            "Ottobre" -> "Settembre"
            "Settembre" -> "Agosto"
            "Agosto" -> "Luglio"
            "Luglio" -> "Giugno"
            "Giugno" -> "Maggio"
            "Maggio" -> "Aprile"
            "Aprile" -> "Marzo"
            "Marzo" -> "Febbraio"
            else -> "Gennaio"
        }
    }

    private fun isDarkModeOn(context: Context): Boolean {
        val currentNightMode = context.resources.configuration.uiMode and  Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }


}