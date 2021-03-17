package com.codinginflow.mvvmtodo.ui.tasks

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.codinginflow.mvvmtodo.R
import com.codinginflow.mvvmtodo.databinding.FragmentTasksBinding
import dagger.hilt.android.AndroidEntryPoint

//unlike viewmodels or such, fragments use AndroidEntryPoint for injections
@AndroidEntryPoint
class TasksFragment : Fragment(R.layout.fragment_tasks) {
    //this will be included too
    //viewModel is not destroyed on layout change, unlike fragments
    private val viewModel : TasksViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //layout is already inflated through constructor
        val binding = FragmentTasksBinding.bind(view)
        val taskAdapter = TasksAdapter()

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
    }
}