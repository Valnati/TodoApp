package com.codinginflow.mvvmtodo.di

import android.app.Application
import androidx.room.Room
import com.codinginflow.mvvmtodo.data.TaskDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

//object will make dagger's generated code more efficient than a class
//a module allows for instructions (functions) on making dependencies
//installIn will place the object with dependencies in the application, so it's easy to grab
@Module
@InstallIn(ApplicationComponent::class)
object AppModule {
    //provides is used here because room code is off limits
    // inject is used when you can access code directly and don't need to modify through method calls
    @Provides
    @Singleton
    fun provideDatabase(
        //android.app application
        app: Application,
        callback: TaskDatabase.Callback
    ) = Room.databaseBuilder(app, TaskDatabase::class.java, "task_database")
            .fallbackToDestructiveMigration()
            //callback is occurring in the TaskDatabase file, using argument above
        .addCallback(callback)
            .build()
        //will create a single database instance
        //app, class literal, name

    @Provides
    fun provideTaskDao(db: TaskDatabase) = db.taskDao()
    //note the = instead of an actual method body, for single line of code
    //this chains previous provides to make application, then database, then dao
    //and finally pass the Dao as needed (since one is needed, singleton is added
    //this second function is inherently a singleton

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())
    //this is custom coroutine scope needed in taskDatabase to do db queries
    //this scope won't be canceled because supervisorJob will continue
    //normally when child fails, coroutine stops; not now

}

//if more than one CoroutineScope is made, these will define where each is used
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope