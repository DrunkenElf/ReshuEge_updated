package com.reshuege.DataBase

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.reshuege.R
import com.reshuege.utils.subjects
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Task::class, User::class, Category::class,
    Card::class, Subject::class,
    SubjectMain::class, Theme::class], version = 6)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    abstract fun categoryDao(): CategoryDao

    abstract fun cardDao(): CardDao

    abstract fun subjectDao(): SubjectDao


    abstract fun subjectMainDao(): SubjectMainDao

    abstract fun taskDao(): TaskDao

    abstract fun themeDao(): ThemeDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also { instance = it }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(context,
                AppDatabase::class.java, "database.db")
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            subjects.forEach { instance?.subjectDao()?.insert(it) }
                            val titles = context.resources.getStringArray(R.array.subjects)
                            val hrefs = context.resources.getStringArray(R.array.subjects_prefix)
                            titles.zip(hrefs).forEach {
                                Log.d("first   second", it.first +  " "+it.second)
                                instance?.subjectMainDao()?.insert(SubjectMain(it.first, it.second))
                            }
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                //.allowMainThreadQueries()
                .build()
    }
}