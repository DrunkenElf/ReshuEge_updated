package com.ilnur

import android.app.Application
import android.database.sqlite.SQLiteException
import com.ilnur.DataBase.MyDB
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Reshuege : Application(){

    override fun onCreate() {
        super.onCreate()

    }

}