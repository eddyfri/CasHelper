package unipd.dei.cashelper.ui



import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import unipd.dei.cashelper.R
import unipd.dei.cashelper.helpers.DBHelper
import java.util.*


class AdapterFragmentAddItem : AppCompatActivity() {

    private lateinit var db: DBHelper
    private lateinit var value : EditText
    private lateinit var date : Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.fragment_add_item)

        db = DBHelper(this as Context)
        val spinner = findViewById<Spinner>(R.id.category_select)
        val categories = db.getCategoryName()
        //add first element the default string
        categories.add(0, getString(R.string.category_spinner))
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        //set the default string as default value of the spinner
        spinner.setSelection(0)
        //ediText value
        value = findViewById<EditText>(R.id.value_add_item)

        //button date
        date = findViewById(R.id.date_add_item)

        // set current date as default
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        //Set date's text
        date.text = "$day/${month + 1}/$year"
        date.setOnClickListener{
            showDatePickerDialog(day, month + 1, year)
        }

    }
    private fun showDatePickerDialog(day : Int, month: Int, year : Int) {
        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view, year, month, day ->
                date.text = "$day/${month + 1}/$year"
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }



}