package com.codinginflow.mvvmtodo.ui.tasks

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codinginflow.mvvmtodo.R
import com.codinginflow.mvvmtodo.data.SortOrder
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.databinding.FragmentTasksBinding
import com.codinginflow.mvvmtodo.util.exhaustive
import com.codinginflow.mvvmtodo.util.onQueryTextChanged
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

//unlike viewmodels or such, fragments use AndroidEntryPoint for injections
@AndroidEntryPoint
class TasksFragment : Fragment(R.layout.fragment_tasks), TasksAdapter.OnItemClickListener {
    //this will be included too
    //viewModel is not destroyed on layout change, unlike fragments
    private val viewModel: TasksViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //layout is already inflated through constructor
        val binding = FragmentTasksBinding.bind(view)
        //pass the fragment itself to the adapter for watching
        val taskAdapter = TasksAdapter(this)

        //nested applies for the recyclerView!
        binding.apply {
            recyclerViewTasks.apply {
                adapter = taskAdapter
                layoutManager = LinearLayoutManager(requireContext())
                //get layout, and use fragment method??
                setHasFixedSize(true)
            }

            //built in package to help with swiping
            //swipe only is dragDirs, which isn't used
            ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                //unnecessary method here, for drag and dropping
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val task = taskAdapter.currentList[viewHolder.adapterPosition]
                    viewModel.onTaskSwiped(task)
                }
            }).attachToRecyclerView(recyclerViewTasks)

            fabAddTask.setOnClickListener {
                //as always, viewModel gets the behavior
                viewModel.onAddNewTaskClick()
            }
        }

        setFragmentResultListener("add_edit_request") { _, bundle ->
            val result = bundle.getInt("add_edit_result")
            viewModel.onAddEditResult(result)
        }

        //when lambda is last argument, can use trailing lambda syntax, like here
        viewModel.tasks.observe(viewLifecycleOwner) {
            //send updated data to the adapter (it is the thing in question it seems)
            taskAdapter.submitList(it)
        }

        //for catching and consuming snackbar events
        //launchWhenStarted will remove this coroutine with onStop/onStart, limiting lifecycle
        //don't listen to events while in the background
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            //possible to name the returned object, so event instead of it here
            viewModel.tasksEvent.collect { event ->
                //with .exhaustive (from Utils), when is now expression,
                //so it won't compile without all possible events covered
                when (event) {
                    //make snackbar only when catching/consuming this event!
                    is TasksViewModel.TasksEvent.ShowUndoDeleteTaskMessage -> {
                        Snackbar.make(requireView(), "Task deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                //and have viewModel actually conduct the behavior
                                //smart cast to get task sent from viewModel, send back to viewModel
                                viewModel.onUndoDeleteClick(event.task)
                            }.show()
                    }
                    //here's our navigations to other fragments
                    TasksViewModel.TasksEvent.NavigateToAddTaskScreen -> {
                        //could just navigate to specific layer, but action var forces compile time safety
                        //title is hardcoded in for the activity name, depending on how we got there
                        val action = TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(null, getString(
                                                    R.string.fragment_title_new_task))
                        findNavController().navigate(action)
                    }
                    is TasksViewModel.TasksEvent.NavigateToEditTaskScreen -> {
                        //just send in the clicked task, otherwise the same
                        val action = TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(event.task, getString(
                                                    R.string.fragment_title_edit_task))
                        findNavController().navigate(action)
                    }
                    is TasksViewModel.TasksEvent.ShowTaskSavedConfirmationMessage -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_SHORT).show()
                    }
                    TasksViewModel.TasksEvent.NavigateToDeleteAllCompletedScreen -> {
                        val action = TasksFragmentDirections.actionGlobalDeleteAllCompletedDialogFragment()
                        findNavController().navigate(action)
                    }
                }.exhaustive
            }
        }

        setHasOptionsMenu(true)
    }

    //as before, give the work to the viewmodel
    override fun onItemClick(task: Task) {
        viewModel.onTaskSelected(task)
    }

    override fun onCheckBoxClick(task: Task, isChecked: Boolean) {
        viewModel.onTaskCheckChanged(task, isChecked)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_tasks, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        //must cast this normal view into a searchView for androidx

        //kotlin extension instead of loooong function
        searchView.onQueryTextChanged {
            viewModel.searchQuery.value = it
        }

        //for checking hide completed status, uses coroutine scope, lives as long as fragment
        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.action_hide_completed_tasks).isChecked =
                    //look in viewModel once to get status, it will update itself after
                    //first reads one view then cancels coroutine
                    //collect will constantly gather in a living coroutine
                viewModel.preferencesFlow.first().hideCompleted
        }
    }

    //case statement of when must return true, though it's not used
    //tells viewModel, which decides, and tells PrefManager, which decides to save
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort_by_name -> {
                viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                true
            }
            R.id.action_sort_by_date_created -> {
                //this behavior is defined in tasksviewmodel file
                viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                true
            }
            R.id.action_hide_completed_tasks -> {
                //flip the completed tasks with a logic toggle
                item.isChecked = !item.isChecked
                viewModel.onHideCompletedClick(item.isChecked)
                true
            }
            R.id.action_delete_all_completed_tasks -> {
                viewModel.onDeleteAllCompletedClick()

                true
            }
            //click is somehow not handled; required
            else -> super.onOptionsItemSelected(item)
        }
    }
}