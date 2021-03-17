package com.codinginflow.mvvmtodo.ui.tasks

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.codinginflow.mvvmtodo.data.TaskDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest

class TasksViewModel @ViewModelInject constructor(
//viewmodels have special inject but otherwise are the same
private val taskDao: TaskDao): ViewModel() {
    //get flow of list of tasks - flow is reactive data source
    //so viewmodel doesn't need reference to fragment, it just holds data up
    //livedata is similar, but has latest data available, not a stream of all
    //livedata is lifecycle aware, so can sleep/wake with activity
    //no memory leaks or crashes
    //use flow below viewmodel, livedata at viewmodel for good results

    //this allows for searches to be custom, holds single value as a flow
    val searchQuery = MutableStateFlow("")

    //when a value changes, execute and use the given value
    //it will run the search query again and switch flows to new search, without
    //stopping ovservation
    private val tasksFlow = searchQuery.flatMapLatest {
        taskDao.getTasks(it)
    }

    //now just watch that flow!
    val tasks = tasksFlow.asLiveData()


}