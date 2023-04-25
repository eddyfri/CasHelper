package unipd.dei.cashelper.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import android.graphics.Color
import androidx.core.content.ContextCompat
import unipd.dei.cashelper.R


class MainActivity : AppCompatActivity() {

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
        //set background color

        //set Entry label's color
        pieChart.data.setValueTextColor(Color.rgb(255, 255, 255))
        //set Entry label's color
        pieChart.data.setValueTextSize(20f)
        //delete description
        pieChart.description.text = ""
        //hole
        pieChart.holeRadius = 30f
        pieChart.setTransparentCircleAlpha(0)

        //Border


        //Legend
        pieChart.legend.textSize = 20f
        // refresh
        pieChart.invalidate()
    }
}