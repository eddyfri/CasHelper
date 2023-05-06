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
    private lateinit var totIncomingTextView: TextView
    private lateinit var totExitsTextView: TextView
    private lateinit var totalTextView: TextView
    private lateinit var addButton: ExtendedFloatingActionButton

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


        var itemInfo = mutableListOf<DBHelper.ItemInfo>()
        itemInfo = db.getItem(month, year)

        fabBack = view.findViewById<ExtendedFloatingActionButton>(R.id.back_month)
        fabNext = view.findViewById<ExtendedFloatingActionButton>(R.id.next_month)
        monthTextView = view.findViewById<TextView>(R.id.month_text)
        yearTextView = view.findViewById<TextView>(R.id.year_text)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.adapter = HomeListAdapter(itemInfo)
        recyclerView.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)

        //test
        monthTextView.text = month
        yearTextView.text = year.toString()

        return view
    }

    private fun createPieChart(view: View, itemInfo: MutableList<DBHelper.ItemInfo>) {
        pieChart = view.findViewById(R.id.chart_incoming)

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