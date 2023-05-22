package unipd.dei.cashelper.ui

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import unipd.dei.cashelper.MainActivity
import unipd.dei.cashelper.R
import unipd.dei.cashelper.adapters.CategoryListAdapter
import unipd.dei.cashelper.adapters.HomeListAdapter
import unipd.dei.cashelper.helpers.DBHelper

class UpdateCategoryFragment: Fragment(), CategoryListAdapter.OnCategoryDeletedListener,
    MenuProvider {

    private lateinit var addCategoryButton: Button
    private lateinit var recyclerViewCategory: RecyclerView
    private lateinit var db: DBHelper

    //popup
    private var popup: PopupWindow? = null
    private var popupActive: Boolean = false  //At start the popup is not open

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
        reenterTransition = MaterialFadeThrough()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_category, container, false)

        //Set the title in the action bar in the specific fragment
        requireActivity().title = "Categorie"
        requireActivity().actionBar?.title = "Categorie"

        //enable backroll arrow
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //set icon backroll
        if ((activity as MainActivity).isDarkModeOn(requireContext()))
            (activity as MainActivity)?.supportActionBar?.setHomeAsUpIndicator(R.drawable.backroll_dark)
        else
            (activity as MainActivity)?.supportActionBar?.setHomeAsUpIndicator(R.drawable.backroll_light)

        db = DBHelper(context as Context)

        recyclerViewCategory = view.findViewById(R.id.recycler_view_category)
        recyclerViewCategory.adapter = CategoryListAdapter(db.getCategoryName(), this)
        recyclerViewCategory.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)


        addCategoryButton = view.findViewById(R.id.add_category)
        addCategoryButton.setOnClickListener {
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

        //add MenuProvider
        activity?.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        //popup visibility save instance
        if (savedInstanceState != null) {
            popupActive= savedInstanceState.getBoolean("popup_visibility")

            //If there was the popup when the activity was active, create it.
            if (popupActive) {
                createPopup()
            }
        }
    }

    override fun onCategoryDeleted(category: String) {
        // val contextView = (view as View).findViewById<View>(R.id.coordinator_layout_message)
        db.removeCategory(category)
        // Snackbar.make(contextView, "Categoria eliminata", Snackbar.LENGTH_SHORT).setAction("Chiudi") {}.show()
        recyclerViewCategory.adapter = CategoryListAdapter(db.getCategoryName(), this)
    }
    //inflate the correct menu for this fragment
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_empty, menu)
    }

    //action for every menuItem selected
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) {
            val action = UpdateCategoryFragmentDirections.actionCategoryFragmentToHomeFragment()
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

        //Set the container of the popup (the fragment that is in background of him)
        val popupContainerView = (view as View).findViewById<View>(R.id.Constraint_update_category)

        //if the user click outside the keyboard, it disappear
        popupView.setOnClickListener{
            hideKeyboard(popupView)
        }
        //when the popup is created automatic by the UpdateCategoryFragment.onViewCreated the activity is not created completely and return width = 0.
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


        val addButton = popupView.findViewById<Button>(R.id.add_button)
        addButton.isEnabled = false
        addButton.backgroundTintList =
            AppCompatResources.getColorStateList(requireContext(), R.color.Disable)


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
                        AppCompatResources.getColorStateList(requireContext(), R.color.confirm)
                } else {
                    addButton.isEnabled = false
                    addButton.backgroundTintList =
                        AppCompatResources.getColorStateList(requireContext(), R.color.Disable)
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
                    val contextView = (view as View).findViewById<View>(R.id.Constraint_update_category)
                    Snackbar.make(contextView, "Categoria già esistente", Snackbar.LENGTH_SHORT)
                        .setAction("Chiudi") {}
                        .show()
                } else {
                    //add category to database
                    db.addCategory(new_category.toString().trim())

                    //update recyclerView
                    recyclerViewCategory.adapter = CategoryListAdapter(db.getCategoryName(), this)

                    Toast.makeText(requireContext(), "Categoria aggiunta", Toast.LENGTH_SHORT).show()

                }
            } else
            //disable button
                addButton.isEnabled = false


            popup?.dismiss()

        }
    }

    private fun hideKeyboard(view: View) {
        val hide = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        hide.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
    }


}