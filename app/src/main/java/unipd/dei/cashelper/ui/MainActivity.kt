package unipd.dei.cashelper.ui


import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import unipd.dei.cashelper.R


class MainActivity : AppCompatActivity() {
    //function that verify if we are in dark mode
    fun isDarkModeOn(context: Context): Boolean{
        val currentNightMode = context.resources.configuration.uiMode and  Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.fragment_home)








        //PieChart
        val pieChart = findViewById<PieChart>(R.id.chart)

        //entries values
        val entries: MutableList<PieEntry> = ArrayList()
        entries.add(PieEntry(80f, "Entrate"))
        entries.add(PieEntry(40f, "Uscite"))
        val set = PieDataSet(entries, "")
        val data = PieData(set)
        pieChart.data = data

        //setting PieChart
        //color's Entry
        val colors: MutableList<Int> = ArrayList()
        val exitColor = ContextCompat.getColor(this, R.color.Exits)
        colors.add(exitColor)
        val entriesColor = ContextCompat.getColor(this, R.color.Entries)
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
        if(isDarkModeOn(this)) {
            holeColor = ContextCompat.getColor(this, R.color.pink_salomon)

        } else {
            holeColor = ContextCompat.getColor(this, R.color.violet)

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
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.app_bar, menu)


        return true
    }

}