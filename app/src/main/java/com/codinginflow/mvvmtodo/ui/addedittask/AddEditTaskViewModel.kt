package com.codinginflow.mvvmtodo.ui.addedittask

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.data.TaskDao

class AddEditTaskViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {
    //can get navigation arguments through savedStateHandle
    //in the middle of editing/add task, viewmodel will be lost if app is stopped
    //can cause misbehavior, 'invalid' input
    //savedInstanceState will hold this data safe, savedStateHandle can expose it directly
    //here's it in action, with same name as in navgraph
    val task = state.get<Task>("task")
    //the task values are immutable, so need new resources to pass along
    //if none, use the default task name, which might be null, so then use empty string
    var taskName = state.get<String>("taskName") ?: task?.name ?: ""
        //override the set method, where the field becomes the value (normal behavior)
        set(value) {
            field = value
            //but then also save the key value to savedState
            state.set("taskname", value)
        }
    var taskImportance = state.get<Boolean>("taskImportance") ?: task?.important ?: false
        //override the set method, where the field becomes the value (normal behavior)
        set(value) {
            field = value
            //but then also save the key value to savedState
            state.set("taskImportance", value)
        }
}