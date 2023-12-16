package com.bignerdranch.android.learngestures

import android.app.Application
import android.util.Log
import com.bignerdranch.android.learngestures.db.data.MainDb

class App : Application() {
    val database by lazy {
        MainDb.getDataBase(this)
    }
}