package unipd.dei.cashelper.ui

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.content.res.AppCompatResources.getColorStateList
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import unipd.dei.cashelper.MainActivity
import com.google.android.material.transition.MaterialFadeThrough
import unipd.dei.cashelper.R
import unipd.dei.cashelper.helpers.DBHelper
import java.util.*
import kotlin.properties.Delegates

class UpdateItemFragment: Fragment(), MenuProvider {
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
    private lateinit var delete : Button
    private lateinit var add_category : Button

    //variables for picking
    private lateinit var switch_choose : String
    private var price by Delegates.notNull<Double>()
    private  lateinit var desc : String
    private var day by Delegates.notNull<Int>()
    private var month by Delegates.notNull<Int>()
    private var year by Delegates.notNull<Int>()
    private lateinit var monthString :String

    //popup
    private var popup: PopupWindow? = null
    private var popupActive: Boolean = false  //At start the popup is not open

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialFadeThrough()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_update_item, container, false)

        //set the menu'current fragment
        activity?.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        //enable backroll arrow
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //set icon backroll
        if ((activity as MainActivity).isDarkModeOn(requireContext()))
            (activity as MainActivity)?.supportActionBar?.setHomeAsUpIndicator(R.drawable.backroll_dark)
        else
            (activity as MainActivity)?.supportActionBar?.setHomeAsUpIndicator(R.drawable.backroll_light)

        idItem = UpdateItemFragmentArgs.fromBundle(requireArguments()).idItem
        constraintLayout = view.findViewById<ConstraintLayout>(R.id.constraint_update_item)

        db = DBHelper(this.requireContext())
        var itemInfo = db.getItemById(idItem)
        update = view.findViewById(R.id.update_button)
        update.isEnabled = false
        update.backgroundTintList = getColorStateList(requireContext(), R.color.Disable)
        delete = view.findViewById((R.id.delete_button))
        spinner = view.findViewById<Spinner>(R.id.category_select_update)
        val categories = db.getCategoryName()

        //set alphabetic order of category list
        categories.sortWith(String.CASE_INSENSITIVE_ORDER)


        //set category "Altro" as last category of the spinner
        categories.remove("Altro")
        categories.add("Altro")

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

        //popup
        add_category = view.findViewById<Button>(R.id.update_category)
        add_category.setOnClickListener {
            //check, if we have already 30 categories (max possible), show a message
            if((db.getCategoryName()).size == 30){
                Toast.makeText(requireContext(), "Limite massimo categorie raggiunto", Toast.LENGTH_SHORT).show()
            }
            else
                createPopup()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                    update.backgroundTintList = getColorStateList(requireContext(), R.color.confirm)
                } else {
                    update.isEnabled = false
                    update.backgroundTintList = getColorStateList(requireContext(), R.color.Disable)
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
                    update.backgroundTintList = getColorStateList(requireContext(), R.color.confirm)
                } else {
                    update.isEnabled = false
                    update.backgroundTintList = getColorStateList(requireContext(), R.color.Disable)
                }
            }

            override fun afterTextChanged(s: Editable?) {
                //nothing
            }

        })

        //when clicked return to the HomeFragment
        delete.setOnClickListener{
            val action = UpdateItemFragmentDirections.actionUpdateFragmentToHomeFragment(monthString, year)
            view.findNavController().navigate(action)
        }

        //when clicked add the item and return to the HomeFragment
        update.setOnClickListener{
            //picking the current values
            picker(view)
            //write in database
            onClickListener(idItem, switch_choose, selected_category, price, day, monthString, year, desc.trim())

            //back to HomeFragment
            val action = UpdateItemFragmentDirections.actionUpdateFragmentToHomeFragment(monthString, year)
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
        view.clearFocus()
    }

    //inflate the correct menu for this fragment
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_update_item, menu)
    }

    //action for every menuItem selected
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.remove_item -> {
                val builder = AlertDialog.Builder(view?.context)
                builder.setMessage("Sei sicuro di voler eliminare questo elemento?")
                    .setPositiveButton("Elimina") { _, _ ->
                        db.removeItem(idItem)
                        Toast.makeText(requireContext(), "Elemento eliminato", Toast.LENGTH_SHORT)
                            .show()
                        val action = UpdateItemFragmentDirections.actionUpdateFragmentToHomeFragment(monthString, year)
                        view?.findNavController()?.navigate(action)
                    }
                    .setNegativeButton("Annulla") { dialog, _ ->
                        dialog.cancel()
                    }
                    .show()
            }
            android.R.id.home -> {
                val action = UpdateItemFragmentDirections.actionUpdateFragmentToHomeFragment(monthString, year)
                view?.findNavController()?.navigate(action)
            }
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

        val width = ((view as View).width * 0.85).toInt()
        //create the popup with specific size
        popup = PopupWindow(popupView, width, ViewGroup.LayoutParams.WRAP_CONTENT, true)

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
        val popupContainerView = (view as View).findViewById<View>(R.id.constraint_update_item)

        //if the user click outside the keyboard, it disappear
        popupView.setOnClickListener {
            hideKeyboard(popupView)
        }

        popup?.showAtLocation(popupContainerView, Gravity.CENTER, 0, 0)
        popup?.dimBehind()

        val addButton = popupView.findViewById<Button>(R.id.add_button)
        addButton.isEnabled = false
        addButton.backgroundTintList = getColorStateList(requireContext(), R.color.Disable)


        //enable/disable add_button
        val popupTextView = popupView.findViewById<EditText>(R.id.text_category)
        popupTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (popupTextView.text.isNotEmpty()) {
                    addButton.isEnabled = true
                    addButton.backgroundTintList =
                        getColorStateList(requireContext(), R.color.confirm)
                } else {
                    addButton.isEnabled = false
                    addButton.backgroundTintList =
                        getColorStateList(requireContext(), R.color.Disable)
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
                    val contextView = (view as View).findViewById<View>(R.id.constraint_update_item)
                    Snackbar.make(contextView, "Categoria già esistente", Snackbar.LENGTH_SHORT)
                        .setAction("Chiudi") {}
                        .show()
                } else {
                    //add category to database
                    db.addCategory(new_category.toString().trim())

                    //update the spinner
                    val categories = db.getCategoryName()
                    val adapter = ArrayAdapter<String>(
                        this.requireContext(),
                        android.R.layout.simple_spinner_item,
                        categories
                    )
                    //set alphabetic order of category list
                    categories.sortWith(String.CASE_INSENSITIVE_ORDER)

                    //set category "Altro" as last category of the spinner
                    categories.remove("Altro")
                    categories.add("Altro")
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter
                    //set the default string as the new category added
                    val posNew = categories.indexOf(new_category.toString())
                    spinner.setSelection(posNew)

                    Toast.makeText(requireContext(), "Categoria aggiunta", Toast.LENGTH_SHORT)
                        .show()

                }
            } else
            //disable button
                addButton.isEnabled = false


            popup?.dismiss()

        }
    }
}
