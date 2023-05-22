package unipd.dei.cashelper.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import unipd.dei.cashelper.R
import unipd.dei.cashelper.adapters.CategoryListAdapter
import unipd.dei.cashelper.adapters.HomeListAdapter
import unipd.dei.cashelper.helpers.DBHelper

class UpdateCategoryFragment: Fragment(), CategoryListAdapter.OnCategoryDeletedListener {

    private lateinit var addCategoryButton: Button
    private lateinit var recyclerViewCategory: RecyclerView
    private lateinit var db: DBHelper

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

        db = DBHelper(context as Context)

        recyclerViewCategory = view.findViewById(R.id.recycler_view_category)
        recyclerViewCategory.adapter = CategoryListAdapter(db.getCategoryName(), this)
        recyclerViewCategory.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)

        return view
    }

    override fun onCategoryDeleted(category: String) {
        // val contextView = (view as View).findViewById<View>(R.id.coordinator_layout_message)
        db.removeCategory(category)
        // Snackbar.make(contextView, "Categoria eliminata con successo", Snackbar.LENGTH_SHORT).setAction("Chiudi") {}.show()
        recyclerViewCategory.adapter = CategoryListAdapter(db.getCategoryName(), this)
    }


}