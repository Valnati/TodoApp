package com.codinginflow.mvvmtodo.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.codinginflow.mvvmtodo.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

//pass our only database as array literal, with version number
//after testing should update with each database structure change
@Database(entities = [Task::class], version = 1)
abstract class TaskDatabase : RoomDatabase() {

    //dependency injection: classes that use other classes are not responsible for creating or obtaining the class
    //the class will be classed with the constructor
    //use dagger for this; construct, hold, inject from one repository
    //use dagger hilt in android to make dagger easy to use
    abstract fun taskDao(): TaskDao

    //we are injecting through dagger, with whatever is provided to constructor
    class Callback @Inject constructor(
        //provider needed to avoid circular dependency; wait until databasebuilder is done
        private val database: Provider<TaskDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope //note scope annotation
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            //will run the first time database is opened, to create it
            //this will call after databasebuilder is completed
            val dao = database.get().taskDao()

            //globalscope will run as long as the app is running; little control
            //instead use coroutine custom scope
            applicationScope.launch {
                //finally, insert dummy data
                dao.insert(Task("Wash the dishes"))
                dao.insert(Task("Do the laundry"))
                dao.insert(Task("Buy groceries", important = true))
                dao.insert(Task("Prepare food", completed = true))
                dao.insert(Task("Call mom"))
                dao.insert(Task("Repair my bike", completed = true, important = true))

            }
        }
    }
}