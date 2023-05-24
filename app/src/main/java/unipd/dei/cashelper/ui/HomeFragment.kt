package unipd.dei.cashelper.ui

import android.content.ContentValues.TAG
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import unipd.dei.cashelper.R
import unipd.dei.cashelper.adapters.HomeListAdapter
import unipd.dei.cashelper.helpers.DBHelper
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.github.mikephil.charting.animation.Easing
import com.google.android.material.transition.MaterialFadeThrough
import unipd.dei.cashelper.MainActivity
import java.text.DecimalFormat


class HomeFragment: Fragment(), MenuProvider, HomeListAdapter.OnItemDeletedListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabBack: ExtendedFloatingActionButton
    private lateinit var fabNext: ExtendedFloatingActionButton
    private lateinit var monthTextView: TextView
    private lateinit var yearTextView: TextView
    private lateinit var totIncomingTextView: TextView
    private lateinit var totExitsTextView: TextView
    private lateinit var totalTextView: TextView
    private lateinit var addButton: ExtendedFloatingActionButton
    private lateinit var constraintLayoutEmptyList: ConstraintLayout
    private lateinit var emptyIcon: ImageView
    private lateinit var emptyText: TextView
    private lateinit var emptyChartText: TextView

    private lateinit var db: DBHelper

    // variabili per il chart
    private lateinit var pieChart: PieChart
    private lateinit var entries: MutableList<PieEntry>
    private lateinit var set: PieDataSet
    private lateinit var data: PieData

    private lateinit var month : String
    private var year by Delegates.notNull<Int>()

    //light/dark theme
    private var theme = false //false = light



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // transizioni ingresso e uscita schermata
        // exitTransition = MaterialElevationScale(/* growing = */ false)
        // reenterTransition = MaterialElevationScale(/* growing = */ true)
        if(savedInstanceState != null) {
            month = savedInstanceState.getString("month").toString()
            year = savedInstanceState.getInt("year")
        }
        else if(HomeFragmentArgs.fromBundle(requireArguments()).month != " " && HomeFragmentArgs.fromBundle(requireArguments()).year != -1) {
            month = HomeFragmentArgs.fromBundle(requireArguments()).month
            year = HomeFragmentArgs.fromBundle(requireArguments()).year
        }
        else {
            month = getCurrentMonth()
            year = getCurrentYear()
        }
        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
        reenterTransition = MaterialFadeThrough()

        //set theme
        val preferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        //first time preferences -> false so set the light mode
        theme = preferences.getBoolean("selectedTheme", theme)
        if(theme)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        //Set the title in the action bar in the specific fragment
        requireActivity().title = "CasHelper"
        requireActivity().actionBar?.title = "CasHelper"





        //disable the backroll arrow --> done because the arrow stay in the home fragment after return on it
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)

        db = DBHelper(context as Context)

        val itemInfo: MutableList<DBHelper.ItemInfo>
        itemInfo = db.getItem(month, year)

        fabBack = view.findViewById<ExtendedFloatingActionButton>(R.id.back_month)
        fabNext = view.findViewById<ExtendedFloatingActionButton>(R.id.next_month)
        monthTextView = view.findViewById<TextView>(R.id.month_text)
        yearTextView = view.findViewById<TextView>(R.id.year_text)
        totIncomingTextView = view.findViewById<TextView>(R.id.entries_label_home)
        totExitsTextView = view.findViewById<TextView>(R.id.exits_label_home)
        totalTextView = view.findViewById<TextView>(R.id.total_label_home)
        addButton = view.findViewById<ExtendedFloatingActionButton>(R.id.add_item)
        constraintLayoutEmptyList = view.findViewById(R.id.constraint_empty_list)
        emptyIcon = view.findViewById(R.id.empty_icon)
        emptyText = view.findViewById(R.id.empty_text)
        emptyChartText = view.findViewById(R.id.empty_chart_text)

        recyclerView = view.findViewById(R.id.recycler_view)
        var itemInfoSorted = sortByDate(itemInfo)
        recyclerView.adapter = HomeListAdapter(itemInfoSorted, this)
        recyclerView.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)


        //when the user use the back pressed gesture the app is stopped
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finish()
        }



        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        val itemInfo: MutableList<DBHelper.ItemInfo>
        itemInfo = db.getItem(month, year)

        monthTextView.text = month
        yearTextView.text = year.toString()

        var total = getIncoming(itemInfo)
        val decimalFormat = DecimalFormat("#.##")
        var totalString = decimalFormat.format(total)
        //replace "." instead of ","
        totalString = totalString.replace(",",".", true)
        totIncomingTextView.text = totalString + " €"

        total = getExits(itemInfo)
        totalString = decimalFormat.format(total)
        //replace "." instead of ","
        totalString = totalString.replace(",",".", true)
        totExitsTextView.text = totalString + " €"

        //set the total pattern like "#.##"
        total = getTotal(itemInfo)
        totalString = decimalFormat.format(total)
        //replace "." instead of ","
        totalString = totalString.replace(",",".", true)
        totalTextView.text = totalString + " €"
        constraintLayoutEmptyList.isVisible = itemInfo.isEmpty()
        emptyIcon.isVisible = itemInfo.isEmpty()
        emptyText.isVisible = itemInfo.isEmpty()
        emptyChartText.isVisible = itemInfo.isEmpty()

        createPieChart(view, itemInfo)
        if(itemInfo.isEmpty())
            pieChart.visibility = View.GONE
        else
            pieChart.visibility = View.VISIBLE
        if(month == getCurrentMonth() && year == getCurrentYear())
            fabNext.hide()

        fabBack.setOnClickListener {
            // cambia mese anno
            if(month == "Gennaio")
                year--
            month = backMonth(month)
            val anim = AlphaAnimation(1.0f, 0.0f)
            anim.duration = 200
            anim.repeatCount = 1
            anim.repeatMode = Animation.REVERSE
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationEnd(animation: Animation?) {}
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationRepeat(animation: Animation?) {
                    updateAll(month, year)
                }
            })
            monthTextView.startAnimation(anim)
            yearTextView.startAnimation(anim)
            totIncomingTextView.startAnimation(anim)
            totExitsTextView.startAnimation(anim)
            totalTextView.startAnimation(anim)
            fabBack.startAnimation(anim)
            fabNext.startAnimation(anim)
            recyclerView.startAnimation(anim)
            pieChart.startAnimation(anim)
        }
        fabNext.setOnClickListener {
            // non posso andare più avanti del mese corrente, no mesi futuri
            if(month != getCurrentMonth() || year != getCurrentYear()) {
                // cambia mese anno
                if(month == "Dicembre")
                    year++
                month = nextMonth(month)
                val anim = AlphaAnimation(1.0f, 0.0f)
                anim.duration = 200
                anim.repeatCount = 1
                anim.repeatMode = Animation.REVERSE
                anim.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationEnd(animation: Animation?) {}
                    override fun onAnimationStart(animation: Animation?) {}
                    override fun onAnimationRepeat(animation: Animation?) {
                        updateAll(month, year)
                    }
                })
                monthTextView.startAnimation(anim)
                yearTextView.startAnimation(anim)
                totIncomingTextView.startAnimation(anim)
                totExitsTextView.startAnimation(anim)
                totalTextView.startAnimation(anim)
                fabBack.startAnimation(anim)
                fabNext.startAnimation(anim)
                recyclerView.startAnimation(anim)
                pieChart.startAnimation(anim)
            }
            else
                fabNext.hide()
        }

        addButton.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToAddFragment()
            view.findNavController().navigate(action)
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle)
    {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putString("month", month)
        savedInstanceState.putInt("year", year)
    }

    override fun onItemDeleted() {
        // aggiorna tutta la schermata
        val contextView = (view as View).findViewById<View>(R.id.coordinator_layout_message)
        Snackbar.make(contextView, "Item eliminato con successo", Snackbar.LENGTH_SHORT).setAction("Chiudi") {}.show()
        updateAll(month, year)
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
            holeColor = ContextCompat.getColor(this.requireContext(), R.color.container_dark)
            pieChart.legend.textColor = ContextCompat.getColor(this.requireContext(), R.color.white)
        }
        else {
            holeColor = ContextCompat.getColor(this.requireContext(), R.color.indaco)
            pieChart.legend.textColor = ContextCompat.getColor(this.requireContext(), R.color.black)
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

        //animation
        pieChart.animateY(1500, Easing.EaseInOutCirc)

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
        //animation
        pieChart.animateY(1500, Easing.EaseInOutCirc)
        pieChart.invalidate()
    }

    private fun updateTotalTextViews(itemInfo: MutableList<DBHelper.ItemInfo>) {
        totIncomingTextView.text = getIncoming(itemInfo).toString() + " €"
        totExitsTextView.text = getExits(itemInfo).toString() + " €"
        var total = getTotal(itemInfo)
        val decimalFormat = DecimalFormat("#.##")
        var totalString = decimalFormat.format(total)
        //replace "." instead of ","
        totalString = totalString.replace(",",".", true)
        totalTextView.text = totalString + " €"
    }

    //inflate the correct menu for this fragment
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_home, menu)
    }

    //action for every menuItem selected
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when(menuItem.itemId){
            R.id.Spese -> {
                val action = HomeFragmentDirections.actionHomeFragmentToOutflowFragment(month, year)
                view?.findNavController()?.navigate(action)
            }
            R.id.Entrate -> {
                val action = HomeFragmentDirections.actionHomeFragmentToIncomingFragment(month, year)
                view?.findNavController()?.navigate(action)
            }
            R.id.themeMode -> {
                // Change the theme
                if (theme) {
                    //if dark set light
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    theme = false
                } else {
                    //if light set dark
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    theme = true
                }

                // Save the theme preference
                val preferences =
                    requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val editor = preferences.edit()
                editor.putBoolean("selectedTheme", theme)
                editor.apply()
            }
            R.id.Categorie -> {
                val action = HomeFragmentDirections.actionHomeFragmentToCategoryFragment()
                view?.findNavController()?.navigate(action)
            }
            R.id.Crediti -> {
                val action = HomeFragmentDirections.actionHomeFragmentToCreditsFragment()
                view?.findNavController()?.navigate(action)
            }
        }
        return true
    }

    private fun updateAll(month: String, year: Int) {
        val itemInfo: MutableList<DBHelper.ItemInfo>
        monthTextView.text = month
        yearTextView.text = year.toString()

        if(!(month == getCurrentMonth() && year == getCurrentYear()))
            fabNext.show()
        else fabNext.hide()
        // aggiorna dati
        itemInfo = db.getItem(month, year)
        constraintLayoutEmptyList.isVisible = itemInfo.isEmpty()
        emptyIcon.isVisible = itemInfo.isEmpty()
        emptyText.isVisible = itemInfo.isEmpty()
        emptyChartText.isVisible = itemInfo.isEmpty()
        pieChart.legend.isEnabled = itemInfo.isNotEmpty()
        if(itemInfo.isEmpty())
            pieChart.visibility = View.GONE
        else
            pieChart.visibility = View.VISIBLE
        // aggiorna pieChart
        updatePieChart(itemInfo)
        // aggiorna text view totali
        updateTotalTextViews(itemInfo)
        // aggiorna recyclerView
        var itemInfoSorted = sortByDate(itemInfo)
        recyclerView.adapter = HomeListAdapter(itemInfoSorted, this)
    }
    //function that order the itemList by date of the month
    private fun sortByDate(itemInfo: MutableList<DBHelper.ItemInfo>) : MutableList<DBHelper.ItemInfo>{
        itemInfo.sortByDescending { it.day } //it is a lambda expression link to itemInfo
        return itemInfo
    }

}