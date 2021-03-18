package com.codinginflow.mvvmtodo.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.emptyPreferences
import androidx.datastore.preferences.preferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PreferencesManager"

//just need to define our terms for the when case in TaskDao
enum class SortOrder { BY_NAME, BY_DATE }
//wrapper value for below
data class FilterPreferences(val sortOrder: SortOrder, val hideCompleted: Boolean)

//as with room, want a singleton that dagger points to through appcontext
@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext context: Context){
    //this class is an abstraction layer to prepare data for the viewmodel

    private val dataStore = context.createDataStore("user_preferences")

    //instead of shared preferences, we are using a flow to keep small saves
    //this will never go on ui thread, unlike sharedprefs
    val preferencesFlow = dataStore.data
        //possible exception, theoretically - send off defaults if can't read
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading preferences", exception)
                emit(emptyPreferences())
            } else {
                //any other exception will be shown, and possibly crash app
                throw exception
            }
        }
        //and now prepare the data for viewModel
        .map { preferences ->
            //read preference, use enum's name value as default to cover nullable
            val sortOrder = SortOrder.valueOf(
                preferences[PreferencesKeys.SORT_ORDER] ?: SortOrder.BY_DATE.name
            )
            val hideCompleted = preferences[PreferencesKeys.HIDE_COMPLETED] ?: false
            FilterPreferences(sortOrder, hideCompleted)
        }

    //this will update sort order when it is changed
    suspend fun updateSortOrder(sortOrder: SortOrder) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_ORDER] = sortOrder.name
        }
    }

    suspend fun updateHideCompleted(hideCompleted: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HIDE_COMPLETED] = hideCompleted
        }
    }

    private object PreferencesKeys {
        val SORT_ORDER = preferencesKey<String>("sort_order")
        val HIDE_COMPLETED = preferencesKey<Boolean>("hide_completed")
    }
}