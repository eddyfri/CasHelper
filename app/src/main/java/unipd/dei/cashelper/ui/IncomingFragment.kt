package unipd.dei.cashelper.ui


import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.widget.*
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
import android.view.*
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.google.android.material.transition.MaterialFadeThrough
import unipd.dei.cashelper.MainActivity
import unipd.dei.cashelper.adapters.CategoryDetailAdapter
import unipd.dei.cashelper.adapters.IncomingListAdapter


class IncomingFragment : Fragment(), MenuProvider {

    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewPopup: RecyclerView
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

    //popup
    private var popup: PopupWindow? = null
    private var popupActive: Boolean = false  //At start the popup is not open

    //restore popup after change UI mode (landscape/portrait)
    private lateinit var selectedItem : String
    private lateinit var itemByCategory : MutableMap<String, ArrayList<DBHelper.ItemInfo>>

    private lateinit var allItemIncoming : MutableList<DBHelper.ItemInfo>
    private lateinit var allCategories : ArrayList<String>



    private lateinit var month : String
    private var year by Delegates.notNull<Int>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
        reenterTransition = MaterialFadeThrough()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        //enable backroll arrow
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //set icon backroll
        if ((activity as MainActivity).isDarkModeOn(requireContext()))
            (activity as MainActivity)?.supportActionBar?.setHomeAsUpIndicator(R.drawable.backroll_dark)
        else
            (activity as MainActivity)?.supportActionBar?.setHomeAsUpIndicator(R.drawable.backroll_light)

        val view = inflater.inflate(R.layout.fragment_incoming, container,false)

        db = DBHelper(context as Context)

        month = IncomingFragmentArgs.fromBundle(requireArguments()).month
        year = IncomingFragmentArgs.fromBundle(requireArguments()).year

        //qui mi serve per passare all'adapter gli elementi
        allItemIncoming = db.getItemsByType("Entrata", month, year)
        allCategories = db.getCategoryName()
        itemByCategory = getIncomingByCategory(allCategories, allItemIncoming)
        var colorByCategory = setColorCategory(allCategories, itemByCategory)
        val totalAmount = getTotalAmount(allItemIncoming)
        val rateArray = getRateByCategory(itemByCategory, totalAmount)

        fabBack = view.findViewById<ExtendedFloatingActionButton>(R.id.back_month)
        fabNext = view.findViewById<ExtendedFloatingActionButton>(R.id.next_month)
        monthTextView = view.findViewById<TextView>(R.id.month_text)
        yearTextView = view.findViewById<TextView>(R.id.year_text)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.adapter = IncomingListAdapter(itemByCategory, colorByCategory, rateArray, this)
        recyclerView.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)


        //set selectedItem value because when we turn the screen the popup is created before the fragment
        if (savedInstanceState != null)
            selectedItem = savedInstanceState.getString("popupSelectedItem").toString()

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
    private fun getCategoryWithIncomings(category: String, allItem: MutableList<DBHelper.ItemInfo>): ArrayList<DBHelper.ItemInfo> {
        var incomingsByCategory = ArrayList<DBHelper.ItemInfo>()
        for (item in allItem)
            if (item.category == category)
                incomingsByCategory.add(item)
        return incomingsByCategory
    }

    //metodo che restituisce un associazione "nome categoria"->"array di item di quella categoria"
    //se l'associazione contiene un arraylist vuoto allora elimina l'associazione
    private fun getIncomingByCategory(categories: ArrayList<String>, allItem: MutableList<DBHelper.ItemInfo>): MutableMap<String, ArrayList<DBHelper.ItemInfo>> {
        var incomingByCategory = mutableMapOf<String, ArrayList<DBHelper.ItemInfo>>()
        for (element in categories)
            incomingByCategory.put(element, getCategoryWithIncomings(element, allItem))
        for (element in categories) {
            if (incomingByCategory.containsKey(element)) {
                val testList = incomingByCategory[element]
                if (testList != null)
                    if (testList.isEmpty())
                        incomingByCategory.remove(element)
            }
        }
        return incomingByCategory
    }

    //metodo per assegnare ad ogni categoria un colore, in modo tale che il colore sia sempre lo stesso per una categoria
    //indipendentemente dall'ordine con cui sono salvati gli item
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
        //add MenuProvider
        activity?.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        allItemIncoming = db.getItemsByType("Entrata", month, year)
        allCategories = db.getCategoryName()
        itemByCategory = getIncomingByCategory(allCategories, allItemIncoming)

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
                monthTextView.text = month
                yearTextView.text = year.toString()
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
                fabBack.startAnimation(anim)
                fabNext.startAnimation(anim)
                recyclerView.startAnimation(anim)
                pieChart.startAnimation(anim)
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
        //popup visibility save instance
        if (savedInstanceState != null) {
            popupActive= savedInstanceState.getBoolean("popup_visibility")
            selectedItem = savedInstanceState.getString("popupSelectedItem").toString()

            //If there was the popup when the activity was active, create it.
            if (popupActive) {
                createPopUp(selectedItem, itemByCategory)
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
                total += item.price
        return total
    }
    private fun createPieChart(view: View, itemByCategory: MutableMap<String, ArrayList<DBHelper.ItemInfo>>, categories: ArrayList<String>) {
        pieChart = view.findViewById(R.id.chart_incoming)

        val categoriesWithIncomings = catchKeys(itemByCategory)
        val colors = setColorCategory(categories, itemByCategory)

        entries = ArrayList()
        for (element in categoriesWithIncomings) {
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
        //animation
        pieChart.animateY(1500, Easing.EaseInOutCirc)

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
        //animation
        pieChart.animateY(1500, Easing.EaseInOutCirc)
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
        //aggiorna gli item in base al mese che è stato cambiato
        var allItemIncoming = db.getItemsByType("Entrata", month, year)
        var allCategories = db.getCategoryName()
        var itemByCategory = getIncomingByCategory(allCategories, allItemIncoming)
        var colorMap = setColorCategory(allCategories, itemByCategory)
        val totalAmount = getTotalAmount(allItemIncoming)
        val rateArray = getRateByCategory(itemByCategory, totalAmount)
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
        recyclerView.adapter = IncomingListAdapter(itemByCategory, colorMap, rateArray, this)
    }
    fun createPopUp(selectedCategory: String, itemByCategory: MutableMap<String, ArrayList<DBHelper.ItemInfo>>){
        //save the current category shown
        this.selectedItem = selectedItem
        this.itemByCategory = itemByCategory


        //dichiare l'inflater
        val inflater = LayoutInflater.from((view as View).context)
        //inserire nella view il popup
        val popupView = inflater.inflate(R.layout.popup_category_detail, view as ViewGroup, false)
        val popupTitle : TextView = popupView.findViewById(R.id.title_item)

        val arrayOfItem = getItemsList(selectedCategory, itemByCategory)

        popupTitle.text = selectedCategory
        recyclerViewPopup = popupView.findViewById(R.id.recycler_view_popup)
        recyclerViewPopup.adapter = CategoryDetailAdapter(arrayOfItem)
        recyclerViewPopup.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)

        //imposta la grandezza del popup all 85% della schermata su cui viene creato
        val width = ((view as View).width*0.85).toInt()
        //create the popup with specific size
        popup = PopupWindow(popupView, width, ViewGroup.LayoutParams.WRAP_CONTENT,true)

        //set the animation when the popup appear and disappear
        popup?.animationStyle = androidx.appcompat.R.style.Animation_AppCompat_DropDownUp
        //set the elevation of the popup view
        popup?.elevation = 100F
        //the popup can listen the touch outside his view
        popup?.isOutsideTouchable = true
        popupActive = true

        popup?.setOnDismissListener {
            popupActive = false
            popup = null
        }

        //Set the container of the popup (the fragment that is in background of him)
        val popupContainerView = (view as View).findViewById<View>(R.id.constraint_incoming)

        //when the popup is created automatic by the IncomingFragment.onViewCreated the activity is not created completely and return width = 0.
        //So, if the width is 0, this method slow the creation of the popup when width is initialized correctly
        if (width == 0) {
            popupContainerView.post {
                val updatedWidth = ((view as View).width*0.85).toInt()
                popup?.update(0,0, updatedWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
                popup?.showAtLocation(popupContainerView, Gravity.CENTER, 0, 0)
                popup?.dimBehind()
            }
        } else {
            popup?.showAtLocation(popupContainerView, Gravity.CENTER, 0, 0)
            popup?.dimBehind()
        }
    }

    private fun PopupWindow.dimBehind() {
        val container = contentView.rootView
        val context = contentView.context
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val p = container.layoutParams as WindowManager.LayoutParams
        p.flags = p.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
        p.dimAmount = 0.3f
        wm.updateViewLayout(container, p)
    }

    private fun getItemsList(category: String, itemByCategory: MutableMap<String, ArrayList<DBHelper.ItemInfo>>): ArrayList<DBHelper.ItemInfo> {
        var itemsOfThisCategory = itemByCategory[category]

        return sortByDate(itemsOfThisCategory!!)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_empty, menu)
    }

    //action for every menuItem selected
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) {
            val action = IncomingFragmentDirections.actionIncomingFragmentToHomeFragment()
            view?.findNavController()?.navigate(action)
        }
        return true
    }

    private fun sortByDate(itemInfo: ArrayList<DBHelper.ItemInfo>) : ArrayList<DBHelper.ItemInfo>{
        itemInfo.sortByDescending { it.day } //it is a lambda expression link to itemInfo
        return itemInfo
    }

    //Save the state of the popup when the activity is stopped
    override fun onStop() {
        super.onStop()
        val restore = popupActive
        popup?.dismiss()  //close popup
        popupActive = restore //save the state of the popup
    }

    fun setSelectedItem(selectedItem : String, itemByCategory: MutableMap<String, ArrayList<DBHelper.ItemInfo>>) {
        this.selectedItem = selectedItem
        this.itemByCategory = itemByCategory
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("popup_visibility", popupActive)
        outState.putString("popupSelectedItem", selectedItem)
    }

}