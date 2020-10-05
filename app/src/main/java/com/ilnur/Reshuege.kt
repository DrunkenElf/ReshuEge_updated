package com.ilnur

import android.app.Application
import android.database.sqlite.SQLiteException
import com.ilnur.DataBase.MyDB

public class Reshuege : Application(){
    lateinit var db: MyDB
    lateinit var user: User
    override fun onCreate() {
        super.onCreate()

        db = MyDB.getInstance(applicationContext)
        try {
            user = MyDB.getUser()
        } catch (e: SQLiteException){
            user = User(null, null, null)
        }

    }

     fun get_user(): User{
        return user
    }
}