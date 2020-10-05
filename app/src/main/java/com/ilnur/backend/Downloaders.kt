package com.ilnur.backend

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.ilnur.DataBase.AppDatabase
import com.ilnur.DataBase.SubjectMain
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

class Downloaders(context: Context) {

    @Inject
    lateinit var apiRequests: ApiRequestsImp

    @Inject
    lateinit var db: AppDatabase

    val subjects = MutableLiveData<List<SubjectMain>>()

    suspend fun getNewestTests() = coroutineScope {
        val subjects = db.subjectMainDao().getSubjects()
        subjects.forEach {
            isUpdateAvail(it)
        }
    }

    suspend fun isUpdateAvail(subject: SubjectMain) = coroutineScope {
        val key = async { apiRequests.getPredefTests(subject.href) }
        key.await().body()?.let {
            it.data?.let {
                if (subject.testsKey != it.toInt() && subject.isAdded) {
                    db.subjectMainDao().insert(subject.apply {
                        testsKey = it.toInt()
                        isNeedToUpd = true })
                }
            }
        }

    }
}