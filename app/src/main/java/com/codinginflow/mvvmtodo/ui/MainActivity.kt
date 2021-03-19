package com.codinginflow.mvvmtodo.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.codinginflow.mvvmtodo.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    //TODO: lateinit allows initializing a non-null property outside the constructor
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //can't get navController directly in onCreate
        //get fragment to access controller in activity's oncreate
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()

        setupActionBarWithNavController(navController)
        //note that fragment titles are coming from the labels in the nav graph
        //including dynamic arguments!
    }

    //go through backstack properly
    override fun onSupportNavigateUp(): Boolean {
        //or statement is in case button returns false somehow
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    //navgraph xml is taking other xmls and adding them to the single activity
    //seems to grab daggerhilt activity from ToDoApplication?
}