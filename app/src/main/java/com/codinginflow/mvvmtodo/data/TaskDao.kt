package com.codinginflow.mvvmtodo.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    fun getTasks(query: String, sortOrder: SortOrder, hideCompleted: Boolean): Flow<List<Task>> =
        when(sortOrder) {
            SortOrder.BY_DATE -> getTasksSortedByDateCreated(query, hideCompleted)
            SortOrder.BY_NAME -> getTasksSortedByName(query, hideCompleted)
        }

    //gotta mark the default query, which now uses the searchQuery argument
    //inner parentheses shows all tasks when hideCompleted isn't checked; only uncompleted when is
    //parentheses for giving OR precedence over AND
    @Query("SELECT * FROM task_table WHERE (completed != :hideCompleted OR completed = 0) AND name LIKE '%' || :searchQuery || '%' ORDER BY important DESC, name")
    // the || is an append in SQLite, so searchQuery must be somewhere in the name
    fun getTasksSortedByName(searchQuery: String, hideCompleted: Boolean): Flow<List<Task>>

    @Query("SELECT * FROM task_table WHERE (completed != :hideCompleted OR completed = 0) AND name LIKE '%' || :searchQuery || '%' ORDER BY important DESC, created")
    fun getTasksSortedByDateCreated(searchQuery: String, hideCompleted: Boolean): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    //custom query for deleting everything
    @Query( "DELETE FROM task_table WHERE completed = 1")
    suspend fun deleteCompletedTasks()
}

//suspend is part of coroutines - allows for thread switching
//all databases operations must be off main thread
//thread will autosuspend as needed; no need to wait for insert to happen
//suspend can be called from coroutine, or another suspend

//flow will be updated on each database change, and flow is itself suspended
//make sure you get Kotlin.coroutines.Flow
//livedata is another option, but for now this