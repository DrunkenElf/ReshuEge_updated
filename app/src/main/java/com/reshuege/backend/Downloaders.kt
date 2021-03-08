package com.reshuege.backend

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.reshuege.DataBase.*
import com.reshuege.R
import com.reshuege.service.DownloadForeground
import com.reshuege.utils.SettingsImp
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import retrofit2.Response
import retrofit2.awaitResponse
import java.io.File
import java.io.FileOutputStream
import java.net.UnknownHostException
import javax.inject.Inject


class Downloaders @Inject constructor(
    var context: Context,
    var apiRequests: ApiRequestsImp,
    var db: AppDatabase
) {
    val subjects = MutableLiveData<List<SubjectMain>>()

    val subjsPrefs = DataPreferences(context)

    lateinit var notifBuilder: NotificationCompat.Builder
    lateinit var notifManager: NotificationManagerCompat
    lateinit var name: String

    val progress = ShowProg(0, 0, 0, 0, 0)


    fun getSubjectService(
        intent1: Intent,
        href: String, name: String,
        notifManager1: NotificationManagerCompat, position: Int,
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
            Log.d("dwn", "dwn getSubjectServ")
            db.subjectMainDao()
                .update(SubjectMain(title = name, href = href, isAdded = true, isNeedToUpd = false))
            CoroutineScope(Dispatchers.Main).launch {
                notifManager.cancel(33)
                notifBuilder = NotificationCompat.Builder(context.applicationContext, "77").apply {
                    setContentTitle(name + ": загрузка")

                    setContentText("Загрузка завершена")
                    setSmallIcon(R.mipmap.ic_launcher)
                    setStyle(NotificationCompat.BigTextStyle())
                    setAutoCancel(true)
                }
                intent.action = "done"
                notifManager.notify(77, notifBuilder.build())
                LocalBroadcastManager.getInstance(context.applicationContext)
                    .sendBroadcast(
                        intent.putExtra("broadcastMessage", true).putExtra("position", position)
                    )
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
        Log.d("dwn", "dwn getVariant DONE")
        subjKey?.let {
            val prefs = SettingsImp(context).also {
                it.saveSubjVersion(href, subjKey)
                it.saveSubjTheorVersion(href, subjKey)
                it.saveSubjTestCrit(href, true)
            }
            Log.d(
                "dwn",
                "prefs: ${prefs.getSubjVersion(href)}"
            )
        }
        Log.d("tasks count",
            "${subjsPrefs.questionsCount(href)} - total tasks for $href")
    }


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
        subjsPrefs.saveQuestionsCount(href, tasksIds?.size)
        tasksIds?.map {
            async(context = Dispatchers.IO) {
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
        val task = apiRequests.getTask(href, id).body()
        task?.data?.let {
            downloadImgs(it, href, subjKey, variant)
            Log.d("dwn", "dwn getTask finished var:$variant ;id$id")
            progress.taskDone = progress.taskDone + 1
            publishProgress(false)
        }
        Log.d("finish to load", "task$href $id $subjKey $variant")
    }


    suspend fun downloadImgs(
        taskResp: TaskResp, href: String,
        subjKey: String, variant: Int
    ) = coroutineScope {
        val task = Task(
            variant = "$variant",
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
    }

    suspend fun downloadAndReplace(html: String, folderName: String): String = coroutineScope {
        var temp = html.replace("https://math-oge.sdamgia.ru","")
        Jsoup.parse(temp).select("img").forEach {
            //https://ege.sdamgia.ru/formula/svg/af/afc77fc980d80c6b041f16ead4497d31.svg
            val link = it.attr("src")

            val name = link.replaceBeforeLast("/", "")
                .replace("/", "") //afc77fc980d80c6b041f16ead4497d31.svg
            temp = temp.replace(
                link,
                "file://${context.filesDir.absolutePath}/pictures/$folderName/$name"
            )
            var resp: Response<ResponseBody>? = null
            try {
                resp = apiRequests.getImage(link).awaitResponse()
            } catch (error: UnknownHostException) {
                error.printStackTrace()
                Log.e(
                    "error",
                    "link:$link ; ${apiRequests.getImage(link).request().url}"
                )
                Log.e(
                    "error element",
                    "link:$link ;\n ${it.html()}"
                )
                Log.e(
                    "error html",
                    "link:$link ;\n ${html}"
                )
            }

            Log.d("Img Response", "  " + resp?.isSuccessful)

            resp?.body()?.byteStream().let { body ->
                FileOutputStream(
                    File(
                        "${context.filesDir.absolutePath}/pictures/$folderName/",
                        name
                    )
                ).use {
                    body?.copyTo(it)
                }
            }
            Log.d("dwn body:", "is null: ${resp?.body() == null}")
            progress.imgDone = progress.imgDone + 1
            publishProgress(false)

        }
        return@coroutineScope temp
    }

    suspend fun getNewestTests() = coroutineScope {
        val subjects = async { db.subjectMainDao().getSubjects() }
        subjects.await().map {
            Log.d("getNewestTests", "${it.href} started")
            async { isUpdateAvail(it) }
        }.awaitAll()

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