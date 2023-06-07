package unipd.dei.cashelper.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import com.google.android.material.transition.MaterialFadeThrough
import unipd.dei.cashelper.MainActivity
import unipd.dei.cashelper.R


class CreditsFragment: Fragment(), MenuProvider {

    private lateinit var creditsTextView: TextView
    private lateinit var reportButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
        reenterTransition = MaterialFadeThrough()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        //Set the title in the action bar in the specific fragment
        requireActivity().title = "Crediti"
        requireActivity().actionBar?.title = "Crediti"

        //enable backroll arrow
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //set icon backroll
        if ((activity as MainActivity).isDarkModeOn(requireContext()))
            (activity as MainActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.backroll_dark)
        else
            (activity as MainActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.backroll_light)

        val view = inflater.inflate(R.layout.fragment_credits, container,false)

        creditsTextView = view.findViewById(R.id.credits_text_view)
        creditsTextView.text = "CasHelper è un applicazione di aiuto per la gestione delle finanze sviluppata da " +
                "Eddy Frighetto, Matteo Manuzzato e Simone Corrò durante il corso di Programmazione di Sistemi Embedded, " +
                "valido per la laurea triennale in Ingegneria Informatica all'Università di Padova. " +
                "Eventuali informazioni sulle funzionalità e le caratteristiche sono scritte nel report, per aprilo basta " +
                "premere il pulsante qui sotto."

        reportButton = view.findViewById(R.id.report_button)
        reportButton.setOnClickListener {
            // inserire link report
            val link = "https://drive.google.com/file/d/1M7m-aQyPJVuY1szIaqFmgJtp4dcPHwHH/view?usp=drive_link"
            val uri: Uri = Uri.parse(link)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //add MenuProvider
        activity?.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_empty, menu)
    }

    //action for every menuItem selected
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) {
            val action = CreditsFragmentDirections.actionCredtisFragmentToHomeFragment()
            view?.findNavController()?.navigate(action)
        }
        return true
    }
}