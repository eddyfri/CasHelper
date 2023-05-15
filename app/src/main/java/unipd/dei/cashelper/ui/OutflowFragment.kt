package unipd.dei.cashelper.ui

import android.app.DatePickerDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.nfc.Tag
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View.OnFocusChangeListener
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
import unipd.dei.cashelper.helpers.DBHelper
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates
import android.util.Log
import android.view.*
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import com.github.mikephil.charting.components.Legend
import unipd.dei.cashelper.MainActivity
import unipd.dei.cashelper.adapters.IncomingListAdapter
import unipd.dei.cashelper.adapters.OutflowListAdapter


class OutflowFragment : Fragment(), MenuProvider {

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

        //enable backroll arrow
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //set icon backroll
        if ((activity as MainActivity).isDarkModeOn(requireContext()))
            (activity as MainActivity)?.supportActionBar?.setHomeAsUpIndicator(R.drawable.backroll_dark)
        else
            (activity as MainActivity)?.supportActionBar?.setHomeAsUpIndicator(R.drawable.backroll_light)

        val view = inflater.inflate(R.layout.fragment_outflow, container,false)

        db = DBHelper(context as Context)

        month = IncomingFragmentArgs.fromBundle(requireArguments()).month
        year = IncomingFragmentArgs.fromBundle(requireArguments()).year

        //qui mi serve per passare all'adapter gli elementi
        var allItemOutflows = db.getItemsByType("Uscita", month, year)
        var allCategories = db.getCategoryName()
        var itemByCategory = getOutflowByCategory(allCategories, allItemOutflows)
        var colorByCategory = setColorCategory(allCategories, itemByCategory)
        var totalAmount = getTotalAmount(allItemOutflows)
        var rateArray = getRateByCategory(itemByCategory, totalAmount)


        fabBack = view.findViewById<ExtendedFloatingActionButton>(R.id.back_month)
        fabNext = view.findViewById<ExtendedFloatingActionButton>(R.id.next_month)
        monthTextView = view.findViewById<TextView>(R.id.month_text)
        yearTextView = view.findViewById<TextView>(R.id.year_text)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.adapter = OutflowListAdapter(itemByCategory, colorByCategory, rateArray)
        recyclerView.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)


        return view
    }

    //get an array of rate for all the categories that have at least one incoming
    private fun getRateByCategory(itemByCategory: MutableMap<String, ArrayList<DBHelper.ItemInfo>>, totalAmount: Double): ArrayList<Double>{
        var rateArray = ArrayList<Double>(itemByCategory.size)
        for (item in itemByCategory) {
            val totalThisCategory = getTotalCategory(item.key, itemByCategory)
            val rate = (totalThisCategory*100)/totalAmount
            rateArray.add(rate)
        }

        return rateArray
    }

    //get total amount of all incomings for this month and year
    private fun getTotalAmount(allItem: MutableList<DBHelper.ItemInfo>): Double{
        var total = 0.0
        for (item in allItem)
            total += item.price
        return total
    }

    //metodo che data una categoria e la lista di elementi totale mi restituisce un array di elementi per quella categoria
    private fun getCategoryWithOutflows(category: String, allItem: MutableList<DBHelper.ItemInfo>): ArrayList<DBHelper.ItemInfo> {
        var outflowsByCategory = ArrayList<DBHelper.ItemInfo>()
        for (item in allItem)
            if (item.category == category)
                outflowsByCategory.add(item)
        return outflowsByCategory
    }

    //metodo che restituisce un associazione "nome categoria"->"array di item di quella categoria"
    //se l'associazione contiene un arraylisst vuoto allora elimina l'associazione
    private fun getOutflowByCategory(categories: ArrayList<String>, allItem: MutableList<DBHelper.ItemInfo>): MutableMap<String, ArrayList<DBHelper.ItemInfo>> {
        var outflowByCategory = mutableMapOf<String, ArrayList<DBHelper.ItemInfo>>()
        for (element in categories)
            outflowByCategory.put(element, getCategoryWithOutflows(element, allItem))
        for (element in categories) {
            if (outflowByCategory.containsKey(element)) {
                val testList = outflowByCategory[element]
                if (testList != null)
                    if (testList.isEmpty())
                        outflowByCategory.remove(element)
            }
        }
        return outflowByCategory
    }

    private fun setColorCategory(categories: ArrayList<String>, itemByCategory: MutableMap<String, ArrayList<DBHelper.ItemInfo>>): ArrayList<Int> {
        var colorMap = mutableMapOf<String, Int>()
        var colorArray = context?.resources?.getIntArray(R.array.color_array)
            ?: throw java.lang.IllegalStateException()
        for (i in categories.indices)
            colorMap[categories[i]] = colorArray[i]

        var colorsByCategory = ArrayList<Int>()
        for (item in itemByCategory.keys) {
            val color = colorMap[item]
            if (color != null)
                colorsByCategory.add(color)
        }
        return colorsByCategory
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var allItemOutflow = db.getItemsByType("Uscita", month, year)
        var allCategories = db.getCategoryName()
        var itemByCategory = getOutflowByCategory(allCategories, allItemOutflow)

        //add MenuProvider
        activity?.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        //imposto le textview di mese e anno nel mese e anno che mi vengono passati dalla schermata home
        monthTextView.text = month
        yearTextView.text = year.toString()

        createPieChart(view, itemByCategory, allCategories)
        if (itemByCategory.isEmpty())
            pieChart.visibility = View.GONE
        else
            pieChart.visibility = View.VISIBLE

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

    private fun catchKeys(itemByCategory: MutableMap<String, ArrayList<DBHelper.ItemInfo>>): ArrayList<String>{
        return ArrayList(itemByCategory.keys)
    }

    private fun getTotalCategory(category: String, itemByCategory: MutableMap<String, ArrayList<DBHelper.ItemInfo>>): Double{
        var total = 0.0
        var itemsOfThisCategory = itemByCategory[category]
        if (itemsOfThisCategory != null)
            for (item in itemsOfThisCategory)
                total = total + item.price
        return total
    }

    private fun createPieChart(view: View, itemByCategory: MutableMap<String, ArrayList<DBHelper.ItemInfo>>, categories: ArrayList<String>) {
        pieChart = view.findViewById(R.id.chart_outflow)

        val categoriesWithOutflow = catchKeys(itemByCategory)
        val colors = setColorCategory(categories, itemByCategory)

        entries = ArrayList()
        for (element in categoriesWithOutflow) {
            var total = getTotalCategory(element, itemByCategory)
            entries.add(PieEntry(total.toFloat(), element))
        }

        set = PieDataSet(entries, "")
        data = PieData(set)
        pieChart.data = data
        set.colors = colors

        //legend of the pieChart invisible
        pieChart.legend.isEnabled = false
        //not draw labels
        pieChart.setDrawEntryLabels(false)
        //enable percent values
        pieChart.setUsePercentValues(true)
        //set Entry label's color (temporally dis-activated)
        pieChart.data.setValueTextColor(Color.rgb(0, 0, 0))
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
            holeColor = ContextCompat.getColor(this.requireContext(), R.color.indaco)
        }
        pieChart.setHoleColor(holeColor)

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

    private fun updatePieChart(itemByCategory: MutableMap<String, ArrayList<DBHelper.ItemInfo>>, categories: ArrayList<String>){
        val categoriesWithIncomings = catchKeys(itemByCategory)
        val colors = setColorCategory(categories, itemByCategory)
        entries.clear()

        entries = ArrayList()
        for (element in categoriesWithIncomings) {
            var total = getTotalCategory(element, itemByCategory)
            entries.add(PieEntry(total.toFloat(), element))
        }

        set = PieDataSet(entries, "")
        data = PieData(set)
        pieChart.data =  data
        set.colors = colors
        pieChart.data.setValueTextSize(0f)
        data.notifyDataChanged()
        set.notifyDataSetChanged()
        pieChart.notifyDataSetChanged()
        pieChart.invalidate()

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

    private fun updateAll(month: String, year: Int) {
        var allItemOutflow = db.getItemsByType("Uscita", month, year)
        var allCategories = db.getCategoryName()
        var itemByCategory = getOutflowByCategory(allCategories, allItemOutflow)
        var colorMap = setColorCategory(allCategories, itemByCategory)
        var totalAmount = getTotalAmount(allItemOutflow)
        var rateArray = getRateByCategory(itemByCategory, totalAmount)

        //aggiorna la visibilità del bottone nextFab
        if (!(month == getCurrentMonth() && year == getCurrentYear()))
            fabNext.show()
        else fabNext.hide()

        if (itemByCategory.isEmpty())
            pieChart.visibility = View.GONE
        else
            pieChart.visibility = View.VISIBLE

        updatePieChart(itemByCategory, allCategories)
        //aggiornamento recyclerView
        recyclerView.adapter = OutflowListAdapter(itemByCategory, colorMap, rateArray)
    }
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_empty, menu)
    }

    //action for every menuItem selected
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) {
            val action = OutflowFragmentDirections.actionOutflowFragmentToHomeFragment()
            view?.findNavController()?.navigate(action)
        }
        return true
    }

}