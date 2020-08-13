package com.bounce.app.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.bounce.app.R

class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {

    private lateinit var mNavControllerMain: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mNavControllerMain = findNavController(R.id.fragmentNavHost)
        NavigationUI.setupActionBarWithNavController(this@MainActivity, mNavControllerMain)
        mNavControllerMain.addOnDestinationChangedListener(this@MainActivity)
    }

    override fun onSupportNavigateUp(): Boolean {
        return mNavControllerMain.navigateUp()
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        when (destination.id) {
            R.id.mainFragment -> supportActionBar?.hide()
            else -> supportActionBar?.show()
        }
    }
}