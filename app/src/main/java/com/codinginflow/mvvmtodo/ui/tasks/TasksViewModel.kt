package com.codinginflow.mvvmtodo.ui.tasks

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.codinginflow.mvvmtodo.data.TaskDao

class TasksViewModel @ViewModelInject constructor(
    //viewmodels have special inject but otherwise are the same
    private val taskDao: TaskDao
        ): ViewModel() {

}