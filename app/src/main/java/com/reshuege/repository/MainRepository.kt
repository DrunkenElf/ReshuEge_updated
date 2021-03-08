package com.reshuege.repository

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.reshuege.DataBase.AppDatabase
import com.reshuege.DataBase.SubjectMain
import com.reshuege.DataBase.User
import com.reshuege.R
import com.reshuege.backend.Downloaders
import com.reshuege.service.DownloadForeground
import javax.inject.Inject

class MainRepository @Inject constructor(
        var context: Context,
        var db: AppDatabase,
        var downloaders: Downloaders,
) {



    val currentSubj = MutableLiveData<SubjectMain>()

    suspend fun launchCheck() = downloaders.getNewestTests()

    fun getSubject(href: String, name: String) =
        DownloadForeground.startService(context, href, name, downloaders)


    suspend fun getAllSubjects(){
        val prefs = context.resources.getStringArray(R.array.subjects_prefix)
        for (i in 1..15){
            val sub = downloaders.getSubject(prefs[i])
            print(sub.toString())
        }
    }

    fun getSubjects(): List<SubjectMain> = db.subjectMainDao().getSubjects()


    fun selectSubject(subject: SubjectMain) {
        currentSubj.postValue(subject)
    }


    fun getUserDb(): User? {
        return db.userDao().getUserDb()
    }


}