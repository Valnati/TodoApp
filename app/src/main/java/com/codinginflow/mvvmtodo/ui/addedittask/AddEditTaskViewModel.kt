package com.codinginflow.mvvmtodo.ui.addedittask

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codinginflow.mvvmtodo.R
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.data.TaskDao
import com.codinginflow.mvvmtodo.ui.ADD_TASK_RESULT_OK
import com.codinginflow.mvvmtodo.ui.EDIT_TASK_RESULT_OK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

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

    //values to send snackbar info back to previous screen
    private val addEditTaskEventChannel = Channel<AddEditTaskEvent>()
    val addEditTaskEvent = addEditTaskEventChannel.receiveAsFlow()

    //validate input and save if valid, or inform if not
    fun onSaveClick() {
        //.isBlank checks if a line is only blank spaces, or null
        if (taskName.isBlank()) {
            showInvalidInputMessage("Name cannot be empty")
            return
        }
        if (task != null) {
            //copy task to use a mutable version
            val updatedTask = task.copy(name = taskName, important = taskImportance)
            updateTask(updatedTask)
        } else {
            val newTask = Task(name = taskName, important = taskImportance)
            createTask(newTask)
        }
    }

    private fun showInvalidInputMessage(text: String) = viewModelScope.launch {
        addEditTaskEventChannel.send(AddEditTaskEvent.ShowInvalidInputMessage(text))
    }

    private fun createTask(newTask: Task) = viewModelScope.launch{
        taskDao.insert(newTask)
        //navigate back, using custom constants to avoid -1/1 clashing
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(ADD_TASK_RESULT_OK))
    }

    private fun updateTask(updatedTask: Task) = viewModelScope.launch {
        taskDao.insert(updatedTask)
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(EDIT_TASK_RESULT_OK))
    }
    //TODO: ctl+shift+enter will autocomplete a line in a lot of helpful ways

    sealed class AddEditTaskEvent {
        //send result of add/edit to previous screen, which will handle snackbar
        //if shown with fragment, it is stopped immediately as we navigate back
        data class ShowInvalidInputMessage(val msg: String) : AddEditTaskEvent()
        data class NavigateBackWithResult(val result: Int) : AddEditTaskEvent()
    }
}