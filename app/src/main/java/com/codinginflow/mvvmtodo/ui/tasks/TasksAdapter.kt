package com.codinginflow.mvvmtodo.ui.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.databinding.ItemTaskBinding

class TasksAdapter (private val listener: OnItemClickListener): ListAdapter<Task, TasksAdapter.TasksViewHolder>(DiffCallback()) {
    //ListAdapter is equipped for getting new lists each time, calculate differences
    //so pass new list and define an item callback

    //auto uses the right viewholder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasksViewHolder {
        //use parent var to get context here
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TasksViewHolder(binding)
    }

    //using bind function defined in the ViewHolder itself
    override fun onBindViewHolder(holder: TasksViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    //inner keyword to avoid it being a static class
    //viewholder is tightly coupled to adapter now; fine, cause that's where it'll be forever
    inner class TasksViewHolder(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        //set listeners on checkboxes and items, in init to execute on instantiation
        //don't do this in onBindViewHolder, or it will do this every time item is called to screen
        init {
            binding.apply {
                //root is the base layout
                root.setOnClickListener {
                    val position = adapterPosition
                    //constant of -1, make sure item is ignored during animation before deletion
                    if (position != RecyclerView.NO_POSITION) {
                        val task = getItem(position)
                        listener.onItemClick(task)
                    }
                }
                checkBoxCompleted.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val task = getItem(position)
                        listener.onCheckBoxClick(task, checkBoxCompleted.isChecked)
                    }
                }
            }
        }
        //no need for findviewbyid, thanks to viewholder
        //item_task xml is found through the binding variable
        //root is necessary as well
        fun bind(task: Task) {
            //as before, ids get converted automatically, can just find with binding
            //apply allows them to act as local variables
            binding.apply {
                //check booleans for whether it should apply
                checkBoxCompleted.isChecked = task.completed
                //grab the name from task and use it
                textViewName.text = task.name
                textViewName.paint.isStrikeThruText = task.completed
                labelPriority.isVisible = task.important
            }
        }
    }

    //fragment will implement the interface to save list states
    //decoupled from fragment to allow multiple uses of adapter
    interface OnItemClickListener {
        fun onItemClick(task: Task)
        fun onCheckBoxClick(task: Task, isChecked: Boolean)
    }

    //here's where the differences are calculated
    class DiffCallback: DiffUtil.ItemCallback<Task>() {
        //do these items represent the same logical item?
        override fun areItemsTheSame(oldItem: Task, newItem: Task) =
            oldItem.id == newItem.id
        //note the smaller function, with = instead of : Boolean {return stuff}

        //are these items equivalent?
        override fun areContentsTheSame(oldItem: Task, newItem: Task) =
            oldItem == newItem
            //task has equals method inherently, thanks to kotlin's data class


    }
}