package com.ilnur.backend

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.lifecycle.MutableLiveData
import coil.Coil
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.DownloadListener
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.ilnur.DataBase.AppDatabase
import com.ilnur.DataBase.SubjectMain
import com.ilnur.DataBase.Task
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.suspendCoroutine


class Downloaders @Inject constructor(
    var context: Context,
    var apiRequests: ApiRequestsImp,
    var db: AppDatabase
) {


    //@Inject lateinit var db: AppDatabase

    val subjects = MutableLiveData<List<SubjectMain>>()

    //var subjKey: Int? = null

    suspend fun getSubject(href: String) = coroutineScope {
        val key = async { apiRequests.getPredefTests(href) }
        val subjKey = key.await().body()?.data?.toInt() //10687168
        subjKey?.let {
            checkFolder(href, subjKey)
            Log.d("logg", subjKey.toString())
        }

        for (i in 1..15) {
            async { getVariant(href, subjKey!! + i - 1, i, subjKey.toString()) }.await()
        }

    }

    fun checkFolder(href: String, key: Int) {
        val dir = File(context.filesDir.absolutePath, "/pictures/${href}_$key")
        if (dir.exists()) {
            if (dir.deleteRecursively()) dir.mkdirs()
        } else dir.mkdirs()
    }

    // id = key + i
    suspend fun getVariant(href: String, id: Int, varNum: Int, subjKey: String) = coroutineScope {
        val tasksIds = async { apiRequests.getTestKeys(href, id) }.await().body()?.data
        Log.d("logg tasks", tasksIds.toString())
        //val taskLoader = TaskLoader(varNum)
        tasksIds?.map {
            async { getTask(href, it, subjKey, varNum) }
        }.let {

        }
    }


    suspend fun getTask(href: String, id: Int, subjKey: String, variant: Int) = coroutineScope {
        Log.d("start to load", "task$href $id $subjKey $variant")
        val task = async { apiRequests.getTask(href, id) }.await().body()
        task?.data?.let {
            async { downloadImgs(it, href, subjKey, variant) }
        }
    }

    suspend fun downloadImgs(taskResp: TaskResp, href: String, subjKey: String, variant: Int) =
        coroutineScope {
            val task = async {
                Task(
                    variant = "${variant}_${taskResp.task}",
                    subj = href,
                    stamp = taskResp.stamp,
                    id = taskResp.id,
                    type = taskResp.type,
                    task = taskResp.task,
                    category = taskResp.category,
                    body = downloadAndReplace(taskResp.body.toString(), "${href}_$subjKey"),
                    solution = downloadAndReplace(
                        taskResp.solution.toString(),
                        "${href}_$subjKey"
                    ),
                    base_id = taskResp.base_id,
                    answer = downloadAndReplace(taskResp.answer.toString(), "${href}_$subjKey"),
                    likes = convertLikes(taskResp.likes!!)
                )
            }
            db.taskDao().insert(task.await())
        }

    /*  suspend fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int){
          outputStream().use { out ->
              bitmap.compress(format, quality, out)
              bitmap.
              out.flush()
          }
      }*/

    fun downloadAndReplace(html: String, folderName: String): String {
        //val imageLoader= Coil.imageLoader(context)
        var temp = html
        Jsoup.parse(html).select("img").forEach {
            val link =
                it.attr("src") //https://ege.sdamgia.ru/formula/svg/af/afc77fc980d80c6b041f16ead4497d31.svg
            val name = link.replaceBeforeLast("/", "")
                .replace("/", "") //afc77fc980d80c6b041f16ead4497d31.svg
            val type = name.split('.')[1] //svg
            temp = temp.replace(
                link,
                "file://${context.filesDir.absolutePath}/pictures/$folderName/$name"
            )
            //GlobalScope.launch {

            val saveToStr = apiRequests.getImage(link, object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    Log.d("Img Response", "  " + response.isSuccessful)
                    response.body()?.byteStream().let { body ->
                        FileOutputStream(
                            File(
                                "${context.filesDir.absolutePath}/pictures/$folderName/",
                                name
                            )
                        ).use {
                            body?.copyTo(it)
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    t.printStackTrace()
                }
            })
            //}
        }
        /*val saveToStr = apiRequests.getImage(link, object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                Log.d("Img Response", "  " + response.isSuccessful)
                response.body()?.byteStream().let { body ->
                    FileOutputStream(
                        File(
                            "${context.filesDir.absolutePath}/pictures/$folderName/",
                            name
                        )
                    ).use {
                        body?.copyTo(it)
                    }
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }
        })*/

        return temp


    }

    suspend fun getNewestTests() = coroutineScope {
        val subjects = async { db.subjectMainDao().getSubjects() }
        subjects.await().map {
            Log.d("getNewestTests", "${it.href} started")
            async { isUpdateAvail(it) }
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
                        isNeedToUpd = true
                    })
                }
            }
        }

    }
}


/*AndroidNetworking.download(
                    link,
                    "${context.filesDir.absolutePath}/pictures/$folderName/",
                    name
                )
                    .setTag("downloadImg")
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .startDownload(object : DownloadListener {
                        override fun onDownloadComplete() {
                            Log.d("Img Response", " done")
                        }

                        override fun onError(anError: ANError?) {
                            anError.toString()
                            anError?.printStackTrace()
                        }

                    })*/