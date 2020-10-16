package com.ilnur.backend

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ilnur.DataBase.AppDatabase
import com.ilnur.DataBase.ShowProg
import com.ilnur.DataBase.SubjectMain
import com.ilnur.DataBase.Task
import com.ilnur.R
import com.ilnur.service.DownloadForeground
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
    lateinit var notifBuilder: NotificationCompat.Builder
    lateinit var notifManager: NotificationManagerCompat
    lateinit var name: String
    //val CHANNEL_ID = "FOREGROUND"

    val progress = ShowProg(0, 0, 0, 0, 0)


    fun getSubjectService(
        intent1: Intent,
        href: String, name: String,
        notifManager1: NotificationManagerCompat
    ) {
        this.name = name
        val intent = intent1
        Log.d("downloadersss", "getSubjSertv")
        notifManager = notifManager1
        notifBuilder = NotificationCompat.Builder(context, "77").apply {
            setContentTitle("$name: загрузка")
            setSmallIcon(R.mipmap.ic_launcher)
            setStyle(NotificationCompat.BigTextStyle())
            setAutoCancel(false)
        }
        CoroutineScope(Dispatchers.IO).launch {
            getSubject(href)
            Log.d("dwn","dwn getSubjectServ")
            CoroutineScope(Dispatchers.Main).launch{
                notifManager.cancel(33)
                notifBuilder = NotificationCompat.Builder(context.applicationContext, "77").apply {
                    //setDefaults(Notification.DEFAULT_)
                    setContentTitle(name + ": загрузка")

                    setContentText("Загрузка завершена")
                    setSmallIcon(R.mipmap.ic_launcher)
                    setStyle(NotificationCompat.BigTextStyle())
                    setAutoCancel(true)
                }
                intent.action = "done"
                notifManager.notify(77, notifBuilder.build())
                LocalBroadcastManager.getInstance(context.applicationContext)
                    .sendBroadcast(intent.putExtra("broadcastMessage", true))
                DownloadForeground.stopService(context.applicationContext)
            }
        }

    }

    suspend fun getSubject(href: String) = coroutineScope {
        Log.d("dwn", "dwn getSubj: $href started")
        notifManager.cancel(77)
        notifBuilder = NotificationCompat.Builder(context, "33").apply {
            setProgress(0, 0, true)
            setContentTitle("$name: загрузка")
            setSmallIcon(R.mipmap.ic_launcher)
            setStyle(NotificationCompat.BigTextStyle())
            setAutoCancel(false)
        }
        with(NotificationManagerCompat.from(context)) {
            notifManager.notify(33, notifBuilder.build())
        }

        val key = async { apiRequests.getPredefTests(href) }
        val subjKey = key.await().body()?.data?.toInt() //10687168
        subjKey?.let {
            checkFolder(href, subjKey)
            Log.d("dwn logg", subjKey.toString())
        }

        for (i in 1..15) {
            val getVar = async {
                getVariant(
                    href = href,
                    id = subjKey!! + i - 1,
                    varNum = i,
                    subjKey = subjKey.toString()
                )
            }

            Log.d("dwn", "dwn getVariant finished getSubj ${getVar.await().toString()}").let {
                //progress.varDone = progress.varDone + 1
                //publishProgress(false)
            }
        }
       /* notifBuilder = NotificationCompat.Builder(context.applicationContext, "77").apply {
            //setDefaults(Notification.DEFAULT_)
            setContentTitle(name + ": загрузка")

            setContentText(res)
            setSmallIcon(R.mipmap.ic_launcher)
            setStyle(NotificationCompat.BigTextStyle())
            setAutoCancel(true)
        }
        intent.action = "done"
        notificationManager.notify(77, notificationBuilder.build())
        LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(intent.putExtra("broadcastMessage", true))*/
        Log.d("dwn", "dwn getVariant DONE")

    }

    //2020-10-16 11:48:29.407
    //2020-10-16 11:49:36.645


    fun publishProgress(isDone: Boolean) {
        if (isDone) {
        } else {
            notifBuilder
                .setStyle(
                    NotificationCompat
                        .BigTextStyle()
                        .bigText(
                            "${progress.varDone} из 15 вариантов загружены\n ${progress.taskDone} заданий загружено\n ${progress.imgDone} изображений загружено"
                        )
                )

            notifManager.notify(33, notifBuilder.build())

        }

    }

    fun checkFolder(href: String, key: Int) {
        Log.d("dwn", "dwn checkFolders")
        val dir = File(context.filesDir.absolutePath, "/pictures/${href}_$key")
        if (dir.exists()) {
            if (dir.deleteRecursively()) dir.mkdirs()
        } else dir.mkdirs()
    }

    // id = key + i
    suspend fun getVariant(href: String, id: Int, varNum: Int, subjKey: String) = coroutineScope {
        Log.d("dwn", "dwn getVariant started")
        val tasksIds = async { apiRequests.getTestKeys(href, id) }.await().body()?.data
        Log.d("dwn logg tasks", tasksIds.toString())
        //val taskLoader = TaskLoader(varNum)
        tasksIds?.map {
            async {
                Log.d("dwn", "dwn getTask started var:$varNum ;id$it")
                getTask(href, it, subjKey, varNum)
            }
        }.let {
            it?.size
            Log.d("dwn", "dwn it:${it.toString()}")
        }
        Log.d("dwn", "dwn getVariant finished").let {
            progress.varDone = progress.varDone + 1
            publishProgress(false)
        }
    }


    suspend fun getTask(href: String, id: Int, subjKey: String, variant: Int) = coroutineScope {
        Log.d("start to load", "task$href $id $subjKey $variant")
        val task = async { apiRequests.getTask(href, id) }.await().body()
        task?.data?.let {
            async { downloadImgs(it, href, subjKey, variant) }.await()
            Log.d("dwn", "dwn getTask finished var:$variant ;id$id")
            progress.taskDone = progress.taskDone + 1
            publishProgress(false)
        }
    }

    //2020-10-16 05:48:53.722
    //2020-10-16 05:49:09.399


     fun downloadImgs(taskResp: TaskResp, href: String, subjKey: String, variant: Int) {

         val task = Task(
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

         db.taskDao().insert(task).also { Log.d("dwn", "subj: $href ;variant: $variant") }
         //Log.d("dwn", "subj: $href ;variant: $variant, task: ${task.await().subj}")
     }

    /*  suspend fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int){
          outputStream().use { out ->
              bitmap.compress(format, quality, out)
              bitmap.
              out.flush()
          }
      }*/

     fun downloadAndReplace(html: String, folderName: String): String {
        var temp = html
        Jsoup.parse(html).select("img").forEach {
            val link =
                it.attr("src") //https://ege.sdamgia.ru/formula/svg/af/afc77fc980d80c6b041f16ead4497d31.svg
            val name = link.replaceBeforeLast("/", "")
                .replace("/", "") //afc77fc980d80c6b041f16ead4497d31.svg
            temp = temp.replace(
                link,
                "file://${context.filesDir.absolutePath}/pictures/$folderName/$name"
            )
            val resp = apiRequests.getImage(link)
            Log.d("Img Response", "  " + resp.isSuccessful)
            //Log.d("Img Response", "  " + resp.isSuccessful)
            resp.body()?.byteStream().let { body ->
                FileOutputStream(
                    File(
                        "${context.filesDir.absolutePath}/pictures/$folderName/",
                        name
                    )
                ).use {
                    body?.copyTo(it)
                }
            }
            progress.imgDone = progress.imgDone + 1
            publishProgress(false)

            /*apiRequests.getImage(link, object : Callback<ResponseBody> {
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
                    progress.imgDone = progress.imgDone + 1
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    t.printStackTrace()
                }
            })*/
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