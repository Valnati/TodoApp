package com.codinginflow.mvvmtodo.ui.tasks

import android.bluetooth.BluetoothHidDevice
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.codinginflow.mvvmtodo.data.PreferencesManager
import com.codinginflow.mvvmtodo.data.SortOrder
import com.codinginflow.mvvmtodo.data.TaskDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class TasksViewModel @ViewModelInject constructor(
//viewmodels have special inject but otherwise are the same
    private val taskDao: TaskDao,
    private val preferencesManager: PreferencesManager
): ViewModel() {
    //get flow of list of tasks - flow is reactive data source
    //so viewmodel doesn't need reference to fragment, it just holds data up
    //livedata is similar, but has latest data available, not a stream of all
    //livedata is lifecycle aware, so can sleep/wake with activity
    //no memory leaks or crashes
    //use flow below viewmodel, livedata at viewmodel for good results

    //this allows for searches to be custom, holds single value as a flow
    val searchQuery = MutableStateFlow("")

    //default state of list is defined here, then whatever user decides
    val preferencesFlow = preferencesManager.preferencesFlow

    //when a value changes, execute and use the given value
    //it will run the search query again and switch flows to new search, without
    //stopping observation
    //combine all flows with the combine keyword, then lambda to pass all values when one changes
    private val tasksFlow = combine(
        searchQuery,
        preferencesFlow
    ) { query, filterPreferences ->
        //wrap into a single return value
        Pair(query, filterPreferences)
        //another lambda allows the vars to be accessed directly
    }.flatMapLatest { (query, filterPreferences) ->
        taskDao.getTasks(query, filterPreferences.sortOrder, filterPreferences.hideCompleted)
    }

    //on any change, these function can call the suspend functions in preferencesManager
    //they are actually called in the fragment
    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }

    fun onHideCompletedClick(hideCompleted: Boolean) = viewModelScope.launch {
        preferencesManager.updateHideCompleted(hideCompleted)
    }

    //now just watch that flow!
    val tasks = tasksFlow.asLiveData()
}