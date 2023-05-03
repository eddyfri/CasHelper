package unipd.dei.cashelper.ui

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import unipd.dei.cashelper.R
import unipd.dei.cashelper.adapters.HomeListAdapter
import unipd.dei.cashelper.helpers.DBHelper
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates


class HomeFragment: Fragment() {
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
        // transizioni ingresso e uscita schermata
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        db = DBHelper(context as Context)

        month = getCurrentMonth()
        year = getCurrentYear()


        var itemInfo = mutableListOf<DBHelper.ItemInfo>()
        itemInfo = db.getItem(month, year)

        fabBack = view.findViewById<ExtendedFloatingActionButton>(R.id.back_month)
        fabNext = view.findViewById<ExtendedFloatingActionButton>(R.id.next_month)
        monthTextView = view.findViewById<TextView>(R.id.month_text)
        yearTextView = view.findViewById<TextView>(R.id.year_text)
        totIncomingTextView = view.findViewById<TextView>(R.id.entries_label_home)
        totExitsTextView = view.findViewById<TextView>(R.id.exits_label_home)
        totalTextView = view.findViewById<TextView>(R.id.total_label_home)
        addButton = view.findViewById<ExtendedFloatingActionButton>(R.id.add_item)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.adapter = HomeListAdapter(itemInfo)
        recyclerView.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var itemInfo = mutableListOf<DBHelper.ItemInfo>()
        itemInfo = db.getItem(month, year)




        monthTextView.text = month
        yearTextView.text = year.toString()
        totIncomingTextView.text = getIncoming(itemInfo).toString() + " €"
        totExitsTextView.text = getExits(itemInfo).toString() + " €"
        totalTextView.text = getTotal(itemInfo).toString() + " €"
        createPieChart(view, itemInfo)
        fabNext.hide()

        fabBack.setOnClickListener {
            // cambia mese anno
            if(month == "Gennaio")
                year--
            month = backMonth(month)
            monthTextView.text = month
            yearTextView.text = year.toString()
            if(!(month == getCurrentMonth() && year == getCurrentYear()))
                fabNext.show()
            // aggiorna dati
            itemInfo = db.getItem(month, year)
            // aggiorna pieChart
            updatePieChart(itemInfo)
            // aggiorna text view totali
            updateTotalTextViews(itemInfo)
            // aggiorna recyclerView
            recyclerView.adapter = HomeListAdapter(itemInfo)
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
                if(!(month == getCurrentMonth() && year == getCurrentYear()))
                    fabNext.show()
                else fabNext.hide()
                // aggiorna dati
                itemInfo = db.getItem(month, year)
                // aggiorna pieChart
                updatePieChart(itemInfo)
                // aggiorna text view totali
                updateTotalTextViews(itemInfo)
                // aggiorna recyclerView
                recyclerView.adapter = HomeListAdapter(itemInfo)
            }
            else
                fabNext.hide()

        }

        addButton.setOnClickListener {
            Log.d(TAG, "items: $itemInfo")
            val action = HomeFragmentDirections.actionHomeFragmentToAddFragment()
            view.findNavController().navigate(action)
        }
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

    private fun isDarkModeOn(context: Context): Boolean {
        val currentNightMode = context.resources.configuration.uiMode and  Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }

    private fun createPieChart(view: View, itemInfo: MutableList<DBHelper.ItemInfo>) {
        pieChart = view.findViewById<PieChart>(R.id.chart)
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
        val exitColor = ContextCompat.getColor(this.requireContext(), R.color.Entries)
        colors.add(exitColor)
        val entriesColor = ContextCompat.getColor(this.requireContext(), R.color.Exits)
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

        val holeColor : Int
        if(isDarkModeOn(this.requireContext())) {
            holeColor = ContextCompat.getColor(this.requireContext(), R.color.pink_salomon)
        }
        else {
            holeColor = ContextCompat.getColor(this.requireContext(), R.color.violet)
        }
        pieChart.setHoleColor(holeColor)

        //pieChart position
        pieChart.setExtraOffsets(-70f, 0f, 0f, 0f)

        //Legend
        pieChart.legend.textSize = 20f
        pieChart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        pieChart.legend.verticalAlignment = Legend.LegendVerticalAlignment.CENTER
        pieChart.legend.yOffset = -50f
        pieChart.legend.xOffset = 30f
        pieChart.legend.orientation = Legend.LegendOrientation.VERTICAL

        // refresh
        pieChart.invalidate()
    }

    private fun updatePieChart(itemInfo: MutableList<DBHelper.ItemInfo>) {
        val totIncoming = getIncoming(itemInfo)
        val totExits = getExits(itemInfo)
        entries.clear()
        entries.add(PieEntry(totIncoming.toFloat(), "Entrate"))
        entries.add(PieEntry(totExits.toFloat(), "Uscite"))
        set = PieDataSet(entries, "")
        data = PieData(set)
        pieChart.data = data
        val colors: MutableList<Int> = ArrayList()
        val exitColor = ContextCompat.getColor(this.requireContext(), R.color.Entries)
        colors.add(exitColor)
        val entriesColor = ContextCompat.getColor(this.requireContext(), R.color.Exits)
        colors.add(entriesColor)
        set.colors = colors
        pieChart.data.setValueTextSize(0f)
        data.notifyDataChanged()
        set.notifyDataSetChanged()
        pieChart.notifyDataSetChanged()
        pieChart.invalidate()
    }

    private fun updateTotalTextViews(itemInfo: MutableList<DBHelper.ItemInfo>) {
        totIncomingTextView.text = getIncoming(itemInfo).toString() + " €"
        totExitsTextView.text = getExits(itemInfo).toString() + " €"
        totalTextView.text = getTotal(itemInfo).toString() + " €"
    }

}