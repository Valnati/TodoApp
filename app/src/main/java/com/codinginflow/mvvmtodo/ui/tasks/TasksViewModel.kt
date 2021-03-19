package com.codinginflow.mvvmtodo.ui.tasks

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.codinginflow.mvvmtodo.data.PreferencesManager
import com.codinginflow.mvvmtodo.data.SortOrder
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.data.TaskDao
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class TasksViewModel @ViewModelInject constructor(
//viewmodels have special inject but otherwise are the same
    private val taskDao: TaskDao,
    private val preferencesManager: PreferencesManager,
    @Assisted private val state: SavedStateHandle
): ViewModel() {
    //get flow of list of tasks - flow is reactive data source
    //so viewmodel doesn't need reference to fragment, it just holds data up
    //livedata is similar, but has latest data available, not a stream of all
    //livedata is lifecycle aware, so can sleep/wake with activity
    //no memory leaks or crashes
    //use flow below viewmodel, livedata at viewmodel for good results

    //this allows for searches to be custom, holds single value as a flow
    //was MutableStateFlow, now is liveData to be able to put in SavedStateHandle
    //no Type needed; compiler will intuit it
    //still get persistence, saved data will change as it does
    val searchQuery = state.getLiveData("searchQuery", "")

    //default state of list is defined here, then whatever user decides
    val preferencesFlow = preferencesManager.preferencesFlow

    //the variable for calling snackbar as an event
    //expose the flow, not the channel, so fragment can't input into the channel
    private val tasksEventChannel = Channel<TasksEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()

    //when a value changes, execute and use the given value
    //it will run the search query again and switch flows to new search, without
    //stopping observation
    //combine all flows with the combine keyword, then lambda to pass all values when one changes
    private val tasksFlow = combine(
        //turn liveData back into a flow
        searchQuery.asFlow(),
        preferencesFlow
    ) { query, filterPreferences ->
        //wrap into a single return value
        Pair(query, filterPreferences)
        //another lambda allows the vars to be accessed directly
    }.flatMapLatest { (query, filterPreferences) ->
        taskDao.getTasks(query, filterPreferences.sortOrder, filterPreferences.hideCompleted)
    }

    //now just watch that flow!
    val tasks = tasksFlow.asLiveData()

    //on any change, these functions can call the suspend functions in preferencesManager
    //they are actually called in the fragment
    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }

    fun onHideCompletedClick(hideCompleted: Boolean) = viewModelScope.launch {
        preferencesManager.updateHideCompleted(hideCompleted)
    }

    //this behavior is for choosing a single list item to edit
    fun onTaskSelected(task: Task) = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToEditTaskScreen(task))
    }

    //need coroutine, as Dao functions are suspended
    //must create copy of passed task, as Dao's properties are immutable
    fun onTaskCheckChanged(task: Task, isChecked: Boolean) = viewModelScope.launch {
        taskDao.update(task.copy(completed = isChecked))
        //isChecked must update, all else will be identical
    }

    //behavior for when an item is swiped on
    //delete is suspended function so need a coroutine
    //and taskDao has this behavior set up already
    fun onTaskSwiped(task: Task) = viewModelScope.launch {
        taskDao.delete(task)
        //and show snackbar in viewModel
        //but need to dispatch event, or snackbar would live longer than the fragment itself
        //butlivedata/mutablestateflow keep latest value
        //they will connect to old viewmodel and get newest livedata
        //they are not consumed once and gone, they stay around
        //so instead use channels - send data between coroutines
        //send objects (events) to fragment, which consumes them
        //channels are launched in coroutine either side, can suspend in background
        tasksEventChannel.send(TasksEvent.ShowUndoDeleteTaskMessage(task))

    }

    //if the snackbar's undo button is pressed
    fun onUndoDeleteClick(task: Task) = viewModelScope.launch {
        taskDao.insert(task)
    }

    fun onAddNewTaskClick() = viewModelScope.launch{
        //as with swipe, emit to the fragment so it knows what to do
        tasksEventChannel.send(TasksEvent.NavigateToAddTaskScreen)
    }

    //this will govern the different events that can be sent
    sealed class  TasksEvent {
        //data class extends sealed class, allows for nonexhaustive when statement later
        //tells compiler no other events except those defined here
        data class ShowUndoDeleteTaskMessage(val task: Task): TasksEvent()
        //singleton of item click to EditTask; with data to edit bundled
        data class NavigateToEditTaskScreen(val task: Task) : TasksEvent()
        //singleton of fab button click to newTask
        object NavigateToAddTaskScreen: TasksEvent()
    }
}