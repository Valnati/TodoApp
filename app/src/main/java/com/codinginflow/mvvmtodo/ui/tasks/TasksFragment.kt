package com.codinginflow.mvvmtodo.ui.tasks

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.codinginflow.mvvmtodo.R
import com.codinginflow.mvvmtodo.data.SortOrder
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.databinding.FragmentTasksBinding
import com.codinginflow.mvvmtodo.util.onQueryTextChanged
import dagger.hilt.android.AndroidEntryPoint
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
        }

        //when lambda is last argument, can use trailing lambda syntax, like here
        viewModel.tasks.observe(viewLifecycleOwner) {
            //send updated data to the adapter (it is the thing in question it seems)
            taskAdapter.submitList(it)
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

        //for checking hide completed status, usecoroutine scope, lives as long as fragment
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

                true
            }
            //click is not handled; required
            else -> super.onOptionsItemSelected(item)
        }
    }
}