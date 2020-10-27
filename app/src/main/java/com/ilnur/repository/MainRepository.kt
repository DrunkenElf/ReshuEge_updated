package com.ilnur.repository

import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.MutableLiveData
import com.ilnur.DataBase.AppDatabase
import com.ilnur.DataBase.SubjectMain
import com.ilnur.DataBase.User
import com.ilnur.R
import com.ilnur.backend.Downloaders
import com.ilnur.service.DownloadForeground
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


    /* fun getSubjectService(intent: Intent,href: String, notifManager: NotificationManagerCompat)
            = downloaders.getSubjectService(intent1 = intent, href, "name", notifManager)*/


    suspend fun getAllSubjects(){
        val prefs = context.resources.getStringArray(R.array.subjects_prefix)
        for (i in 1..15){
            val sub = downloaders.getSubject(prefs[i])
            print(sub.toString())
        }
    }

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