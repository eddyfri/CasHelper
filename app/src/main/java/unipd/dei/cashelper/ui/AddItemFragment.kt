package unipd.dei.cashelper.ui



import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import unipd.dei.cashelper.MainActivity
import unipd.dei.cashelper.R
import unipd.dei.cashelper.helpers.DBHelper
import java.util.*
import kotlin.properties.Delegates


class AddItemFragment : Fragment(), MenuProvider {

    private lateinit var db: DBHelper
    private lateinit var value: EditText
    private lateinit var date: Button
    private lateinit var add: Button
    private lateinit var selected_category: String
    private lateinit var switch: Switch
    private lateinit var description: EditText
    private lateinit var spinner: Spinner
    private lateinit var constraintLayout: ConstraintLayout
    private lateinit var delete: Button
    private lateinit var disable: Button
    private lateinit var add_category : Button

    //variables for picking
    private lateinit var switch_choose: String
    private var price by Delegates.notNull<Double>()
    private lateinit var desc: String
    private var day by Delegates.notNull<Int>()
    private var month by Delegates.notNull<Int>()
    private var year by Delegates.notNull<Int>()
    private lateinit var monthString: String

    //popup
    private var popup: PopupWindow? = null
    private var popupActive: Boolean = false  //At start the popup is not open


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //enable backroll arrow
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //set icon backroll
        if ((activity as MainActivity).isDarkModeOn(requireContext()))
            (activity as MainActivity)?.supportActionBar?.setHomeAsUpIndicator(R.drawable.backroll_dark)
        else
            (activity as MainActivity)?.supportActionBar?.setHomeAsUpIndicator(R.drawable.backroll_light)

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


        //popup
        add_category = view.findViewById<Button>(R.id.add_category)
        add_category.setOnClickListener {
            createPopup()
        }

        return view

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //add MenuProvider
        activity?.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        //hide keyboard when click anywhere in the screen
        constraintLayout.setOnClickListener {
            hideKeyboard(view)
        }

        //popup visibility save instance
        if (savedInstanceState != null) {
            popupActive= savedInstanceState.getBoolean("popup_visibility")

            //If there was the popup when the activity was active, create it.
            if (popupActive) {
                createPopup()
            }
        }



        //variables for checking that the category and value fields are not empty
        var categoryCheck = false
        var valueCheck = false

        switch.setOnCheckedChangeListener { _, isChecked ->
            switch_choose = if (isChecked)
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
                if (valueCheck && categoryCheck) {
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

        date.setOnClickListener {
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

        delete.setOnClickListener {
            val action = AddItemFragmentDirections.actionAddFragmentToHomeFragment(monthString, year)
            view?.findNavController()?.navigate(action)
        }

        //value must be not empty
        value.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val stringPrice: String = value.text.toString()
                valueCheck = stringPrice.isNotEmpty()
                if (valueCheck && categoryCheck) {
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

        add.setOnClickListener {
            //picking the current values
            picker(view)
            //write in database
            onClickListener(
                switch_choose,
                selected_category,
                price,
                day,
                monthString,
                year,
                desc.trim()
            )

            //back to HomeFragment
            val action = AddItemFragmentDirections.actionAddFragmentToHomeFragment(monthString, year)
            view.findNavController().navigate(action)
        }
    }


    //function that pick the values when the user press the confirm button
    private fun picker(view: View) {
        //take value
        val stringPrice: String = value.text.toString()
        price = stringPrice.toDouble()

        //take description
        description = view.findViewById(R.id.description_add_item)
        desc = description.text.toString()


    }

    //function that convert month from Int to String
    private fun dateConverter(month: Int): String {
        return when (month) {
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

    private fun onClickListener(
        type: String,
        category: String,
        value: Double,
        day: Int,
        month: String,
        year: Int,
        description: String
    ) {
        db.addItem(description, value, type, category, day, month, year)
        Toast.makeText(requireContext(), "Elemento aggiunto", Toast.LENGTH_SHORT).show()
    }


    override fun onPause() {
        super.onPause()
        //when return to the home fragment show the app bar
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
    }

    private fun hideKeyboard(view: View) {
        val hide = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        hide.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
    }

    //inflate the correct menu for this fragment
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_empty, menu)
    }

    //action for every menuItem selected
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) {
            val action = AddItemFragmentDirections.actionAddFragmentToHomeFragment(monthString, year)
            view?.findNavController()?.navigate(action)
        }
        return true
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("popup_visibility", popupActive)
    }

    //Save the state of the popup when the activity is stopped
    override fun onStop() {
        super.onStop()
        val restore = popupActive
        popup?.dismiss()  //close popup
        popupActive = restore //save the state of the popup
    }


    //Set the background view with the semitransparent effect behind the popup
    private fun PopupWindow.dimBehind() {
        val container = contentView.rootView
        val context = contentView.context
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val p = container.layoutParams as WindowManager.LayoutParams
        p.flags = p.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
        p.dimAmount = 0.3f
        wm.updateViewLayout(container, p)
    }

    //Create the popup view for add a category
    private fun createPopup() {
        val inflater = LayoutInflater.from((view as View).context)
        val popupView = inflater.inflate(R.layout.popup_add_category, view as ViewGroup, false)

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

        //Set the container of teh popup (the fragment that is in background of him)
        val popupContainerView = (view as View).findViewById<View>(R.id.Constraint_add_item)

        //if the user click outside the keyboard, it disappear
        popupView.setOnClickListener{
            hideKeyboard(popupView)
        }

        //Quando la PopupWindow viene ricreta in automatico da PlannerFragment.onViewCreated l'activity sottostante non è
        //ancora stata inizializzata completamente. View.width ritorna allora 0
        //Il metodo post "rallenta" la creazione dalla PopupWindow a quando questo parametro sarà stato inizializzato correttamente.
       /* if (width == 0) {
            popupContainerView.post {
                val updatedWidth = ((view as View).width*0.85).toInt()
                popupWindow?.update(0,0, updatedWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
                popupWindow?.showAtLocation(popupContainerView, Gravity.CENTER, 0, 0)
                popupWindow?.dimBehind()
            }
        } else {*/ //se non funziona correttamente togli i commenti (Test)
            popup?.showAtLocation(popupContainerView, Gravity.CENTER, 0, 0)
            popup?.dimBehind()
       // }

        val addButton = popupView.findViewById<Button>(R.id.add_button)
        val fake_add_button = popupView.findViewById<Button>(R.id.FAKE_add_button)

        //enable/disable add_button
        val popupTextView = popupView.findViewById<EditText>(R.id.text_category)
        popupTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val stringPrice: String = value.text.toString()
                if (popupTextView.text.isNotEmpty()) {
                    addButton.isEnabled = true
                    addButton.visibility = View.VISIBLE
                    fake_add_button.visibility = View.INVISIBLE
                    fake_add_button.isEnabled = false
                } else {
                    addButton.isEnabled = false
                    addButton.visibility = View.INVISIBLE
                    fake_add_button.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(s: Editable?) {
                //nothing
            }

        })
        popupTextView.setOnEditorActionListener { _, actionId, _ ->
            //If the user click enter the keyboard disappear
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                popupTextView.clearFocus()
                //Nascondo la tastiera.
               hideKeyboard(popupView)

                return@setOnEditorActionListener true
            }

            return@setOnEditorActionListener false
        }

        //exit_button's on Click close the popup
        val cancelButton = popupView.findViewById<Button>(R.id.exit_button)
        cancelButton.setOnClickListener {
            popup?.dismiss()
        }

        //enable/disable the add button
        addButton.isEnabled = popupTextView.text.isNotEmpty()


        addButton.setOnClickListener {
            if (popupTextView.text.isNotEmpty()) {
                //Hide keyboard
                hideKeyboard(popupView)
                //enable button
                addButton.isEnabled = true

                //check if the category already exist
                val categories = db.getCategoryName()
                val new_category = popupTextView.text
                if (categories.contains(new_category.toString())) {
                    val contextView = (view as View).findViewById<View>(R.id.Constraint_add_item)
                    Snackbar.make(contextView, "Categoria già esistente", Snackbar.LENGTH_SHORT)
                        .setAction("Chiudi") {}
                        .show()
                } else {
                    //add category to database
                    db.addCategory(new_category.toString())

                    //update the spinner
                    val categories = db.getCategoryName()
                    val adapter = ArrayAdapter<String>(
                        this.requireContext(),
                        android.R.layout.simple_spinner_item,
                        categories
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter
                    //set the default string as the new category added
                    spinner.setSelection(categories.size - 1)


                }
            } else
                //disable button
                addButton.isEnabled = false


                popup?.dismiss()

        }
    }



}
