package unipd.dei.cashelper


import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import unipd.dei.cashelper.ui.AddItemFragment
import unipd.dei.cashelper.ui.HomeFragmentDirections


class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController


    //function that verify if we are in dark mode
    fun isDarkModeOn(context: Context): Boolean{
        val currentNightMode = context.resources.configuration.uiMode and  Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        if (intent.action == "OPEN_ADD_ITEM_FRAGMENT") {
            val action = HomeFragmentDirections.actionHomeFragmentToAddFragment()
            navController.navigate(action)
        }
    }

    //animation when change theme mode
    override fun recreate() {
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        super.recreate()
    }
}