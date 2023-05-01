package unipd.dei.cashelper.ui



import android.app.DatePickerDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import unipd.dei.cashelper.R
import unipd.dei.cashelper.helpers.DBHelper
import java.util.*
import kotlin.properties.Delegates


class AddItemFragment : Fragment() {

    private lateinit var db: DBHelper
    private lateinit var value : EditText
    private lateinit var date : Button
    private lateinit var add :Button
    private lateinit var selected_category : String
    private lateinit var switch: Switch
    private lateinit var description: EditText
    private lateinit var spinner : Spinner

    //variables for picking
    private lateinit var switch_choose : String
    private var price by Delegates.notNull<Double>()
    private  lateinit var desc : String
    private var day by Delegates.notNull<Int>()
    private var month by Delegates.notNull<Int>()
    private var year by Delegates.notNull<Int>()
    private lateinit var monthString :String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_add_item, container, false)


        //hide the app_bar in this fragment
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()

        db = DBHelper(this.requireContext() as Context)
        add = view.findViewById(R.id.confirm_button)
        spinner = view.findViewById<Spinner>(R.id.category_select)
        val categories = db.getCategoryName()
        //add first element the default string
        categories.add(0, getString(R.string.category_spinner))
        val adapter = ArrayAdapter<String>(
            this.requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        //set the default string as default value of the spinner
        spinner.setSelection(0)

        //ediText value
        value = view.findViewById<EditText>(R.id.value_add_item)

        //button date
        date = view.findViewById(R.id.date_add_item)

        //contain the state of the switch
        switch = view.findViewById(R.id.Switch_add_item)
        // default choose
        switch_choose = "Uscita"

        // set current date as default
        val calendar = Calendar.getInstance()
        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH)
        day = calendar.get(Calendar.DAY_OF_MONTH)

        //Set date's text
        date.text = "$day/${month + 1}/$year"

        return view

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //variables for checking that the category and value fields are not empty
        var categoryCheck = false
        var valueCheck = false

        switch.setOnCheckedChangeListener { _, isChecked ->
            switch_choose = if(isChecked)
                "Entrata"
            else
                "Uscita"
        }

        //take category
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                //give the category selected
                selected_category = parent.getItemAtPosition(position).toString()
                //confirm button disabled if the selected category is "Seleziona una categoria" (position = 0)
                categoryCheck = selected_category != getString(R.string.category_spinner)
                add.isEnabled = (valueCheck && categoryCheck)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //nothing
            }

        }

        date.setOnClickListener{
            showDatePickerDialog(day, month + 1, year)
        }

        //value must be not empty
        value.addTextChangedListener( object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val stringPrice : String = value.text.toString()
                valueCheck = stringPrice.isNotEmpty()
                add.isEnabled = (valueCheck && categoryCheck)
            }

            override fun afterTextChanged(s: Editable?) {
                //nothing
            }

        })

        add.setOnClickListener{
            //picking the current values
            picker(view)
            //write in database
            onClickListener(switch_choose, selected_category, price, day, monthString, year, desc.trim())

            //back to HomeFragment
            val action = AddItemFragmentDirections.actionAddFragmentToHomeFragment()
            view.findNavController().navigate(action)
        }
    }


    //function that pick the values when the user press the confirm button
    private fun picker(view :View) {
        //take value
        val stringPrice : String = value.text.toString()
        price = stringPrice.toDouble()

        //take description
        description = view.findViewById(R.id.description_add_item)
        desc = description.text.toString()

        //take date
        monthString = dateConverter(month)
    }

    //function that convert month from Int to String
    private fun dateConverter(month :Int) : String {
        when {
            month == 0 -> return "Gennaio"
            month == 1 -> return "Febbraio"
            month == 2 -> return "Marzo"
            month == 3 -> return "Aprile"
            month == 4 -> return "Maggio"
            month == 5 -> return "Giugno"
            month == 6 -> return "Luglio"
            month == 7 -> return "Agosto"
            month == 8 -> return "Settembre"
            month == 9 -> return "Ottobre"
            month == 10 -> return "Novembre"
            month == 11 -> return "Dicembre"
            else -> return "Errore"
        }
    }


    private fun onCheckedChanged(buttonView: CompoundButton, isChecked : Boolean) : String {
        if(isChecked) {
            return "Entrata"
        }
        else {
            return "Uscita"
        }
    }


    private fun onClickListener(type : String, category : String, value: Double, day: Int, month: String, year: Int, description : String ){
        db.addItem(description, value, type, category, day, month, year)


    }

    private fun showDatePickerDialog(day : Int, month: Int, year : Int) {
        val datePickerDialog = DatePickerDialog(
            this.requireContext(),
            DatePickerDialog.OnDateSetListener { view, year, month, day ->
                date.text = "$day/${month + 1}/$year"
            },
            year,
            month,
            day
        )
        val calendar = Calendar.getInstance()
        //set max date = today
        datePickerDialog.datePicker.maxDate = calendar.timeInMillis
        datePickerDialog.show()
    }

    override fun onPause() {
        super.onPause()
        //when return to the home fragment show the app bar
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
    }



}