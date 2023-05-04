package unipd.dei.cashelper.ui

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import unipd.dei.cashelper.R
import unipd.dei.cashelper.helpers.DBHelper
import java.util.*
import kotlin.properties.Delegates

class UpdateItemFragment: Fragment() {
    private lateinit var db: DBHelper
    private lateinit var value : EditText
    private lateinit var date : Button
    private lateinit var update : Button
    private lateinit var selected_category : String
    private lateinit var switch: Switch
    private lateinit var description: EditText
    private lateinit var spinner : Spinner
    private lateinit var constraintLayout : ConstraintLayout
    private var idItem by Delegates.notNull<Int>()
    private lateinit var disable : Button

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
        val view = inflater.inflate(R.layout.fragment_update_item, container, false)
        idItem = UpdateItemFragmentArgs.fromBundle(requireArguments()).idItem
        constraintLayout = view.findViewById<ConstraintLayout>(R.id.constraint_update_item)

        db = DBHelper(this.requireContext() as Context)
        var itemInfo = db.getItemById(idItem)
        update = view.findViewById(R.id.update_button)
        disable = view.findViewById(R.id.disable_buttonFAKE_update)
        update.visibility = View.INVISIBLE
        spinner = view.findViewById<Spinner>(R.id.category_select_update)
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
        spinner.setSelection(categories.indexOf(itemInfo.category))

        //ediText value
        value = view.findViewById<EditText>(R.id.value_update_item)
        value.setText(itemInfo.price.toString())

        //button date
        date = view.findViewById(R.id.date_update_item)

        //contain the state of the switch
        switch = view.findViewById(R.id.Switch_update_item)
        if(itemInfo.type == "Entrata")
            switch.isChecked = true
        // default choose
        switch_choose = itemInfo.type

        // set current date as default
        year = itemInfo.year
        month = getIntMonth(itemInfo.month)
        day = itemInfo.day

        description = view.findViewById(R.id.description_update_item)
        description.setText(itemInfo.description)

        //Set date's text
        date.text = "$day/${month + 1}/$year"

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //hide keyboard when click anywhere in the screen
        constraintLayout.setOnClickListener {
            hideKeyboard(view)
        }

        var categoryCheck = true
        var valueCheck = true

        switch.setOnCheckedChangeListener { _, isChecked ->
            switch_choose = if(isChecked)
                "Entrata"
            else
                "Uscita"
        }

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
                if(valueCheck && categoryCheck) {
                    update.isEnabled = true
                    update.visibility = View.VISIBLE
                    disable.visibility = View.INVISIBLE
                    disable.isEnabled = false
                } else {
                    update.isEnabled = false
                    update.visibility = View.INVISIBLE
                    disable.visibility = View.VISIBLE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //nothing
            }
        }

        monthString = dateConverter(month)

        date.setOnClickListener{
            val datePickerDialog = DatePickerDialog(
                this.requireContext(),
                DatePickerDialog.OnDateSetListener { _, year, month, day ->
                    date.text = "$day/${month + 1}/$year"
                    //take date
                    this.year = year
                    this.day = day
                    monthString = dateConverter(month)
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

        value.addTextChangedListener( object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val stringPrice : String = value.text.toString()
                valueCheck = stringPrice.isNotEmpty()
                if(valueCheck && categoryCheck) {
                    update.isEnabled = true
                    update.visibility = View.VISIBLE
                    disable.visibility = View.INVISIBLE
                    disable.isEnabled = false
                } else {
                    update.isEnabled = false
                    update.visibility = View.INVISIBLE
                    disable.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(s: Editable?) {
                //nothing
            }

        })

        update.setOnClickListener{
            //picking the current values
            picker(view)
            //write in database
            onClickListener(idItem, switch_choose, selected_category, price, day, monthString, year, desc.trim())

            //back to HomeFragment
            val action = UpdateItemFragmentDirections.actionUpdateFragmentToHomeFragment()
            view.findNavController().navigate(action)
        }
    }

    private fun picker(view :View) {
        //take value
        val stringPrice : String = value.text.toString()
        price = stringPrice.toDouble()

        //take description
        description = view.findViewById(R.id.description_update_item)
        desc = description.text.toString()


    }

    private fun onClickListener(idItem: Int, type : String, category : String, value: Double, day: Int, month: String, year: Int, description : String){
        db.updateItem(idItem, description, value, type, category, day, month, year)
    }

    private fun getIntMonth(month: String): Int {
        return when(month) {
            "Gennaio" -> 0
            "Febbraio" -> 1
            "Marzo" -> 2
            "Aprile" -> 3
            "Maggio" -> 4
            "Giugno" -> 5
            "Luglio" -> 6
            "Agosto" -> 7
            "Settembre" -> 8
            "Ottobre" -> 9
            "Novembre" -> 10
            else -> 11
        }
    }

    private fun dateConverter(month :Int) : String {
        return when(month) {
            0 -> "Gennaio"
            1 -> "Febbraio"
            2 -> "Marzo"
            3 -> "Aprile"
            4 -> "Maggio"
            5 -> "Giugno"
            6 -> "Luglio"
            7 -> "Agosto"
            8 -> "Settembre"
            9 -> "Ottobre"
            10 -> "Novembre"
            11 -> "Dicembre"
            else -> return "Errore"
        }
    }

    private fun hideKeyboard(view: View) {
        val hide = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        hide.hideSoftInputFromWindow(view.windowToken, 0)
    }
}