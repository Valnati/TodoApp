package com.codinginflow.mvvmtodo.util

import androidx.appcompat.widget.SearchView

//this extension function subs for the longer one that is stock
//passing another function in this custom one, which calls our below onQueryTextChange
inline fun SearchView.onQueryTextChanged(crossinline listener: (String) -> Unit) {
    this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        //this happens only on clicking submit button
        //this code isn't relevant, so hide it in this file
        override fun onQueryTextSubmit(query: String?): Boolean {
            return true
        }

        //on any list change, refill in real time
        override fun onQueryTextChange(newText: String?): Boolean {
            //return whatever was inputted, or null if nothing
            listener(newText.orEmpty())
            return true
        }
    })
}

//the lambda is copied directly into the relevant function call on compile
//so more efficient
//crossinline is forcing that inner function to return, normally it's not allowed