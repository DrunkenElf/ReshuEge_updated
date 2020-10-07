package com.ilnur.backend

import android.content.Context
import android.util.Log
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


class Downloaders @Inject constructor(context: Context, var  apiRequests: ApiRequestsImp, var db: AppDatabase) {


    //@Inject lateinit var db: AppDatabase

    val subjects = MutableLiveData<List<SubjectMain>>()

    suspend fun getNewestTests() = coroutineScope {
        val subjects = async {  db.subjectMainDao().getSubjects()}
        subjects.await().map {
            Log.d("getNewestTests", "${it.href} started")
            async { isUpdateAvail(it)}
        }.awaitAll()

        /*subjects.await().forEach {
            Log.d("getNewestTests", "${it.href} started")
            async { isUpdateAvail(it)}
        }*/
        Log.d("getNewestTests", "all Done")
    }

    suspend fun isUpdateAvail(subject: SubjectMain) = coroutineScope {
        val key = async { apiRequests.getPredefTests(subject.href) }
        key.await().body()?.let {
            it.data?.let {
                Log.d("isUpdateAvail", "$it loaded")
                if (subject.testsKey != it.toInt() && subject.isAdded) {
                    db.subjectMainDao().insert(subject.apply {
                        testsKey = it.toInt()
                        isNeedToUpd = true })
                }
            }
        }

    }
}