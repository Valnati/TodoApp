package com.codinginflow.mvvmtodo.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    //suspend is part of coroutines - allows for thread switching
    //all databases operations must be off main thread
    //thread will autosuspend as needed; no need to wait for insert to happen
    //suspend can be called from coroutine, or another suspend

    //gotta mark the default query, which now uses the searchQuery argument
    @Query("SELECT * FROM task_table WHERE name LIKE '%' || :searchQuery || '%' ORDER BY important DESC")
    // the || is an append in SQLite, so if query must be somewhere in the name

    //flow will be updated on each database change, and flow is itself suspended
    //make sure you get Kotlin.coroutines.Flow
    //livedata is another option, but for now this
    fun getTasks(searchQuery: String): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)


}