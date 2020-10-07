package com.ilnur.repository

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.ilnur.DataBase.AppDatabase
import com.ilnur.DataBase.SubjectMain
import com.ilnur.DataBase.User
import com.ilnur.backend.Downloaders
import javax.inject.Inject

class MainRepository @Inject constructor(
        var context: Context,
        var db: AppDatabase,
        var downloaders: Downloaders
) {


    val currentSubj = MutableLiveData<SubjectMain>()

    suspend fun launchCheck() = downloaders.getNewestTests()


    fun getSubjects(): List<SubjectMain> = db.subjectMainDao().getSubjects()

    /*fun updateSubjects(href: String): List<Subject> {
        re
    }*/

    fun selectSubject(subject: SubjectMain) {
        currentSubj.postValue(subject)
    }


    fun getUserDb(): User? {
        return db.userDao().getUserDb()
    }


}