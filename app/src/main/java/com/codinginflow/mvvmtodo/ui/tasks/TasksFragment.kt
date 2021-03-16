package com.codinginflow.mvvmtodo.ui.tasks

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.codinginflow.mvvmtodo.R
import dagger.hilt.android.AndroidEntryPoint

//unlike viewmodels or such, fragments use AndroidEntryPoint for injections
@AndroidEntryPoint
class TasksFragment : Fragment(R.layout.fragment_tasks) {
    //this will be included too
    private val viewModel : TasksViewModel by viewModels()
}