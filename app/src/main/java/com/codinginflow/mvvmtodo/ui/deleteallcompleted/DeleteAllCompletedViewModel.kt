package com.codinginflow.mvvmtodo.ui.deleteallcompleted

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.codinginflow.mvvmtodo.data.TaskDao
import com.codinginflow.mvvmtodo.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

//need application scope in case viewModel is removed from memory
//a smaller scope could remove this viewModel and its command before
// it calls for an update to the database
class DeleteAllCompletedViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    @ApplicationScope private val applicationScope: CoroutineScope
) : ViewModel() {

    fun onConfirmClicked() = applicationScope.launch {
        taskDao.deleteCompletedTasks()
    }
}