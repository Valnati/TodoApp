package com.codinginflow.mvvmtodo

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

//need this to activate dagger hilt, from the top
@HiltAndroidApp
class ToDoApplication : Application() {
}