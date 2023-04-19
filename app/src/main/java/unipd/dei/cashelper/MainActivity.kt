package unipd.dei.cashelper

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import android.graphics.Color


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //PieChart
        val pieChart = findViewById<PieChart>(R.id.chart)

        //entries values
        val entries: MutableList<PieEntry> = ArrayList()
        entries.add(PieEntry(80f, "Entrate"))
        entries.add(PieEntry(40f, "Uscite"))
        val set: PieDataSet = PieDataSet(entries, "")
        val data = PieData(set)
        pieChart.data = data

        //setting PieChart
        //color's Entry
        val colors: MutableList<Int> = ArrayList()
        colors.add(Color.rgb(250, 16,32))
        colors.add(Color.rgb(48, 149,14))
        set.setColors(colors)
        //not draw labels
        pieChart.setDrawEntryLabels(false)
        //enable percent values
        pieChart.setUsePercentValues(true)
        //set background color
        pieChart.setBackgroundColor(Color.YELLOW)
        //set Entry label's color
        pieChart.data.setValueTextColor(Color.rgb(255, 255, 255))
        //set Entry label's color
        pieChart.data.setValueTextSize(36f)
        //delete description
        pieChart.description.text = ""
        //hole
        pieChart.holeRadius = 40f
        pieChart.setTransparentCircleAlpha(0)
        pieChart.setHoleColor(Color.YELLOW)
        //Border
        

        //Legend
        pieChart.legend.textSize = 20f
        // refresh
        pieChart.invalidate()


    }
}