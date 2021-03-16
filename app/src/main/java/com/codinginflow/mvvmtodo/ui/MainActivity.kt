package com.codinginflow.mvvmtodo.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.codinginflow.mvvmtodo.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    //navgraph xml is taking other xmls and adding them to the single activity
    //seems to grab daggerhilt activity from ToDoApplication?
}