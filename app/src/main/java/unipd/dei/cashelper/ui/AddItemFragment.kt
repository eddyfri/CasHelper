package unipd.dei.cashelper.ui



import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough
import unipd.dei.cashelper.R
import unipd.dei.cashelper.helpers.DBHelper
import java.util.*
import kotlin.properties.Delegates


class AddItemFragment : Fragment(), MenuProvider {

    private lateinit var db: DBHelper
    private lateinit var value : EditText
    private lateinit var date : Button
    private lateinit var add :Button
    private lateinit var selected_category : String
    private lateinit var switch: Switch
    private lateinit var description: EditText
    private lateinit var spinner : Spinner
    private lateinit var constraintLayout : ConstraintLayout
    private lateinit var delete : Button
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

        enterTransition = MaterialElevationScale(/* growing= */ true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_add_item, container, false)
        constraintLayout = view.findViewById<ConstraintLayout>(R.id.Constraint_add_item)

        db = DBHelper(this.requireContext() as Context)
        add = view.findViewById(R.id.confirm_button)
        disable = view.findViewById(R.id.disable_buttonFAKE)
        add.visibility = View.INVISIBLE
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

        //button delete_button
        delete = view.findViewById(R.id.delete_button)

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

        activity?.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)


        //hide keyboard when click anywhere in the screen
        constraintLayout.setOnClickListener {
            hideKeyboard(view)
        }


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
                if(valueCheck && categoryCheck) {
                    add.isEnabled = true
                    add.visibility = View.VISIBLE
                    disable.visibility = View.INVISIBLE
                    disable.isEnabled = false
                } else {
                    add.isEnabled = false
                    add.visibility = View.INVISIBLE
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

        delete.setOnClickListener{
            view.findNavController().navigate(R.id.action_addFragment_to_homeFragment)
        }

        //value must be not empty
        value.addTextChangedListener( object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val stringPrice : String = value.text.toString()
                valueCheck = stringPrice.isNotEmpty()
                if(valueCheck && categoryCheck) {
                    add.isEnabled = true
                    add.visibility = View.VISIBLE
                    disable.visibility = View.INVISIBLE
                    disable.isEnabled = false
                } else {
                    add.isEnabled = false
                    add.visibility = View.INVISIBLE
                    disable.visibility = View.VISIBLE
                }
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


    }

    //function that convert month from Int to String
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

    private fun onClickListener(type : String, category : String, value: Double, day: Int, month: String, year: Int, description : String ){
        db.addItem(description, value, type, category, day, month, year)
        Toast.makeText(requireContext(),"Elemento aggiunto", Toast.LENGTH_SHORT).show()
    }


    override fun onPause() {
        super.onPause()
        //when return to the home fragment show the app bar
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
    }

    private fun hideKeyboard(view: View) {
        val hide = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        hide.hideSoftInputFromWindow(view.windowToken, 0)
    }

    //inflate the correct menu for this fragment
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_empty, menu)
    }

    //action for every menuItem selected
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
       /* when(menuItem.itemId){
            R.id.Spese -> Toast.makeText(requireContext(),"Spese cliccato", Toast.LENGTH_SHORT).show()
        }*/
        return true
    }

}