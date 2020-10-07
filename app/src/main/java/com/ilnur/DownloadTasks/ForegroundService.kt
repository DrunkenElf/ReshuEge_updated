package com.ilnur.DownloadTasks

import android.app.*
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.os.SystemClock
import android.preference.PreferenceManager
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.JsonObjectRequest
import com.ilnur.DataBase.QuestionsDataBaseHelper
import com.ilnur.Protocol
import com.ilnur.R
import com.ilnur.utils.StreamReader
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import java.net.URLConnection
import java.util.ArrayList
import javax.net.ssl.HttpsURLConnection

class ForegroundService : Service() {
    val CHANNEL_ID = "Foreground"
    internal var testID: String? = null
    internal var qCount = 0
    internal var counter = 0
    internal var serverError = false
    internal var download_pictures: Boolean = false
    internal var var_counter = 0
    internal var img_counter = 0
    internal var pngCount = 1
    internal var progressMsg = arrayOf(" из 15 вариантов загружены\n", " изображений загружено")
    internal lateinit var subject_prefix: String
    lateinit var name: String
    internal lateinit var cont: Context

    internal lateinit var queue: RequestQueue

    companion object {
        fun startService(context: Context, prefix: String, name: String) {
            val startIntent = Intent(context, ForegroundService1::class.java)
            startIntent.putExtra("prefix", prefix)
            startIntent.putExtra("name", name)
            ContextCompat.startForegroundService(context, startIntent)

        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, ForegroundService1::class.java)
            context.stopService(stopIntent)
        }
    }

    lateinit var notificationBuilder: NotificationCompat.Builder
    lateinit var notificationManager: NotificationManagerCompat

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val cache = DiskBasedCache(this.applicationContext.cacheDir, 1024 * 1024) // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        val network = BasicNetwork(HurlStack())

        // Instantiate the RequestQueue with the cache and network. Start the queue.
        queue = RequestQueue(cache, network).apply {
            start()
        }
        //return super.onStartCommand(intent, flags, startId)
        subject_prefix = intent!!.getStringExtra("prefix").toString()
        name = intent.getStringExtra("name").toString()
        val settingsPref = PreferenceManager.getDefaultSharedPreferences(this)
        download_pictures = settingsPref.getBoolean("download_pictures", false)

        notificationManager = NotificationManagerCompat.from(this.applicationContext)
        //progress = 0;
        //int id = 10;
        Log.d("Download", "downloadTasks")
        notificationBuilder = NotificationCompat.Builder(this.applicationContext, "77").apply {
            //setDefaults(Notification.DEFAULT_)
            setContentTitle(name + ": загрузка")
            setSmallIcon(R.mipmap.ic_launcher)
            setStyle(NotificationCompat.BigTextStyle())
            setAutoCancel(false)
        }
        startForeground(1, notificationBuilder.build())
        //notificationManager.notify(77, notificationBuilder.build())

        val QUESTIONS_COUNT = subject_prefix + "_questions_count"
        val countPref = this.getSharedPreferences("subjects_questions_count", AppCompatActivity.MODE_PRIVATE)

        val questions = arrayOfNulls<ArrayList<*>>(15) as Array<ArrayList<JSONObject>>
        testID = loadAPI("predefined_tests", 0)
        try {
            val id = JSONObject(testID)
            testID = id.getString("data")
            val file = if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
                File(this.getExternalFilesDir(null), "/RESHUEGE/Pictures/" + subject_prefix + testID)
            } else {
                File(this.filesDir, "/RESHUEGE/Pictures/" + subject_prefix + testID)
            }
            //val folder = File(Environment.getExternalStorageDirectory().toString() + "/RESHUEGE/Pictures/" + subject_prefix + testID)

            if (!file.exists()) {
                file.mkdirs()
            }
        } catch (e: Exception) {
        }

        val qdbHelper = QuestionsDataBaseHelper(this, subject_prefix)
        if (qdbHelper.checkTable(subject_prefix)) {
            qdbHelper.updateTable(subject_prefix)
        } else
            qdbHelper.addTable(subject_prefix)
        val db = qdbHelper.writableDatabase

        notificationManager.cancel(77)
        notificationBuilder = NotificationCompat.Builder(this.applicationContext, "33").apply {
            //setDefaults(Notification.DEFAULT_)
            setProgress(0, 0, true)
            setContentTitle(name + ": загрузка")
            setSmallIcon(R.mipmap.ic_launcher)
            setStyle(NotificationCompat.BigTextStyle())
            setAutoCancel(false)
            //setOngoing(true)
        }

        notificationManager.notify(33, notificationBuilder.build())

        val vals = Observable.range(1, 15) as Observable<Int>
        vals.subscribeOn(Schedulers.io())
                .map { i -> loadVar(testID!!, countPref, db, QUESTIONS_COUNT, i - 1) }
                .subscribe(
                        { result -> print(result.index) },
                        { error -> Log.d("ERROR", error.message.toString()) },
                        { onLoadFinish(intent) }
                )
        /*val vals = Observable.range(1, 15) as Observable<Int>
        val disposable = vals.flatMap { mapper ->
            Observable.just(mapper).retry(2).subscribeOn(Schedulers.io())
                    .map { i -> loadVar(testID!!, countPref, db, QUESTIONS_COUNT, i - 1) }
        }
                //.observeOn(AndroidSchedulers.from(this.applicationContext.mainLooper))
                //.retryWhen(error -> {})
                .retry(2)
                .subscribe(
                        { result -> print(result.index) },
                        { error -> Log.d("ERROR", error.message) },
                        { onLoadFinish(intent) }
                )*/

        /*Observable.just()
        for (i in 1 until 15){
            loadVar(testID!!, countPref, db, QUESTIONS_COUNT, i - 1)
        }
        onLoadFinish(intent)*/
        //db.close()

        return START_STICKY
    }

    fun publishProgress(values: IntArray) {
        //val r = Runnable {
        try {
            notificationBuilder
                    .setStyle(NotificationCompat
                            .BigTextStyle()
                            .bigText(var_counter.toString() + progressMsg[0] + img_counter.toString() + progressMsg[1]))
            //.setContentText(var_counter.toString() + progressMsg[0] + img_counter.toString() + progressMsg[1]);
            //notificationBuilder.setProgress(0, 0, true)
            notificationManager.notify(33, notificationBuilder.build())
            //progress.setMessage(var_counter.toString() + progressMsg[0] + img_counter.toString() + progressMsg[1])
            /*if (values.size != 4) {
                progress.setMessage(values[0].toString() + " из 15 Вариантов загружены")

            } else {
                if (!download_pictures) {
                    progress.setMessage(values[0].toString() + " из 15 Вариантов загружены\n" + values[3] + " Фотографий загружено")
                }
            }*/
        } catch (e: Exception) {
            e.printStackTrace()
        }
        /*}
        handler.postDelayed(r, 10)*/
    }

    fun onLoadFinish(intent: Intent) {
        Log.d("Download", "onLoadFinish")
        var res = "Загрузка завершена"
        //if (!disposable.isDisposed) disposable.dispose()
        if (!serverError) {
            try {
                val verPref = this.applicationContext.getSharedPreferences("version_tests", AppCompatActivity.MODE_PRIVATE)

                val directoryForDeletePath = ((this.getExternalFilesDir(null)?.absolutePath
                        ?: this.filesDir.absolutePath) + "/RESHUEGE/Pictures/"
                        + subject_prefix + verPref.getString(subject_prefix, ""))

                //Удаления папки со старыми картинками
                val folderForDelete = File(directoryForDeletePath)
                if (!testID!!.contentEquals(verPref.getString(subject_prefix, "")!!))
                    if (folderForDelete.exists()) deleteRecursive(folderForDelete)

                //Сохранение номера первого скаченного варианта
                val ed = verPref.edit()
                ed.putString(subject_prefix, testID)
                ed.apply()

                val verTheory = this.applicationContext.getSharedPreferences("version_theory", AppCompatActivity.MODE_PRIVATE)
                val edit = verTheory.edit()
                edit.putString(subject_prefix, testID)
                edit.apply()

                //При критическом обновлении (одноразовое действие)
                val criticalUpdate = this.applicationContext.getSharedPreferences("critical_update", AppCompatActivity.MODE_PRIVATE)
                val editor = criticalUpdate.edit()
                editor.putBoolean(subject_prefix, true)
                editor.apply()

                //progress.dismiss()
                //Toast.makeText(context, "Задания загружены", Toast.LENGTH_SHORT).show()

                //adapter.notifyDataSetChanged()

            } catch (e: Exception) {
                e.printStackTrace()
            }
            notificationManager.cancel(33)
        } else {
            //progress.dismiss()
            notificationManager.cancel(33)
            res = "Сервер РЕШУ ЕГЭ временно недоступен"
            //return false
            //showMessage("Сервер РЕШУ ЕГЭ временно недоступен.", context.getString(R.string.error))

        }
        /*notificationBuilder
                .setProgress(0,0, false)
                .setContentText("Загрузка завершена");*/
        //notificationManager.cancel(33)
        //notificationManager.notify(33, notificationBuilder.build())
        notificationBuilder = NotificationCompat.Builder(this.applicationContext, "77").apply {
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
                .sendBroadcast(intent.putExtra("broadcastMessage", true))
        stopSelf()
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        val restartServiceIntent = Intent(applicationContext, this.javaClass)
        restartServiceIntent.setPackage(packageName)

        val restartServicePendingIntent = PendingIntent.getService(applicationContext, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        val alarmService = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartServicePendingIntent)

        super.onTaskRemoved(rootIntent)
    }

    fun getUrl(APIRequest: String, id: Int): String {
        if (id == 0)
            return "https://" + subject_prefix + "-ege.sdamgia.ru/api?type=" + APIRequest + "&" + Protocol.protocolVersion
        else
            return "https://" + subject_prefix + "-ege.sdamgia.ru/api?type=" + APIRequest + id + "&" + Protocol.protocolVersion
    }

    fun loadAPI1(){}

    fun loadVar(testId: String, countPrefs: SharedPreferences,
                db: SQLiteDatabase, questCount: String, i: Int): IndValue {
        //if (i == 0)
        val url = getUrl("get_test&id=", Integer.parseInt(testId) + i)
        val questionNumbersString = loadAPI("get_test&id=", Integer.parseInt(testId) + i)
        val variant = JSONObject(questionNumbersString)
        val questionNumbers = variant.getJSONArray("data")
        //val variantArray = ArrayList<JSONObject>()
        val variantArray = arrayOfNulls<JSONObject>(questionNumbers.length())
        val varArray = arrayOfNulls<JSONObject>(questionNumbers.length())
        val ed = countPrefs.edit()
        ed.putInt(questCount, questionNumbers.length())
        ed.apply()
        qCount = questionNumbers.length()
        if (i == 0)
            addThemes(qCount, db)
        /*val disposable = loadTask(questionNumbers.getString(0),0).subscribeOn(Schedulers.io())
        for (j in 1 until questionNumbers.length()){
            disposable.mergeWith(loadTask(questionNumbers.getString(j), j).subscribeOn(Schedulers.io()))
        }
        disposable.observeOn(AndroidSchedulers.mainThread())
                .subscribe({ res -> varArray[res.index] = res.task},
                        {},
                        {
                            var_counter++
                            publishProgress(intArrayOf(var_counter, questionNumbers.length(), 1))
                            addToDataBase1(arrayToList(varArray), db, i)
                            //return@blockingSubscribe
                            //return@loadVar IndValue(i, arrayToList(varArray))
                        })*/
        /*.subscribe(
                { res -> varArray[res.index] = res.task},
                {},
                {
                    var_counter++
                    publishProgress(intArrayOf(var_counter, questionNumbers.length(), 1))
                    addToDataBase1(variantArray, db, i)
                    return@loadVar IndValue(i, arrayToList(varArray))
                }
        )*/

        for (j in 0 until questionNumbers.length()) {
            val taskString = loadAPI("get_task&data=", Integer.parseInt(questionNumbers.getString(j)))
            val data = JSONObject(taskString)
            val task = JSONObject(data.getString("data"))
            variantArray[j] = task
        }
        Log.d("VAR_COUNT", var_counter.toString())
        var_counter++
        publishProgress(intArrayOf(var_counter, questionNumbers.length(), 1))
        addToDataBase1(variantArray, db, i)

        return IndValue(i, arrayToList(varArray))
    }




    fun arrayToList(array: Array<JSONObject?>): ArrayList<JSONObject> {
        val list = ArrayList<JSONObject>()
        for (i in array) {
            if (i != null) list.add(i)
        }
        return list
    }

    fun loadTask(id: String, index: Int): Observable<IndTask> {
        val taskString = loadAPI("get_task&data=", id.toInt())
        val data = JSONObject(taskString)
        val task = JSONObject(data.getString("data"))
        return Observable.just(IndTask(index, task))
    }

    class IndTask(var index: Int, var task: JSONObject)

    class IndValue(var index: Int, var array: ArrayList<JSONObject>)

    fun File.copyInputStreamToFile(inputStream: InputStream) {
        this.outputStream().use { fileOut ->
            inputStream.copyTo(fileOut)
        }
    }

    private fun downloadPicture(url: String, path: String, name: String, format: String, size: Int): String {
        pngCount++
        img_counter++
        publishProgress(intArrayOf(var_counter, size, 1, img_counter))
        val input: InputStream
        val output: OutputStream
        val connection: URLConnection
        var extention = format
        try {
            connection = URL(url).openConnection()
            connection.useCaches = false
            input = connection.getInputStream()

            val cont_type = connection.getHeaderField("content-type")

            extention = "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(cont_type)!!
            if (extention === ".null") extention = format

            val file = File(path, name + extention)
            Log.d("EXTESION", extention)
           // output = FileOutputStream(file, false)
            file.copyInputStreamToFile(input)
            //StreamReader().writeFile(input, output)
        } catch (e: Exception) {
            return "file://$path/$name$extention"
        }

        try {
            val bitmap = BitmapFactory.decodeFile("$path/$name$extention")
            val displaymetrics = this.applicationContext.resources.displayMetrics

            val width: Int = if (bitmap.width < (displaymetrics.widthPixels - 100) / displaymetrics.density) bitmap.width else ((displaymetrics.widthPixels - 100) / displaymetrics.density).toInt()
            val finalImagePath = "file://" + path + "/" + name + extention + if (bitmap.width <= width) "" else "\" style=\"width:" + width + "px"

            return finalImagePath
        } catch (e: Exception) {
            return "file://$path/$name$extention"
        }

    }

    private fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory)
            for (child in fileOrDirectory.listFiles())
                deleteRecursive(child)
        Log.d("DELETING", fileOrDirectory.name)
        fileOrDirectory.delete()
    }


    fun loadAPI(APIRequest: String, id: Int): String? {
        var str: String? = null
        var tryesCount = 0
        val url: URL = if (id == 0)
            URL("https://" + subject_prefix + "-ege.sdamgia.ru/api?type=" + APIRequest + "&" + Protocol.protocolVersion)
        else
            URL("https://" + subject_prefix + "-ege.sdamgia.ru/api?type=" + APIRequest + id + "&" + Protocol.protocolVersion)
        val urlConnection = url.openConnection() as HttpsURLConnection
        while (str == null && tryesCount != 10)
            try {
                tryesCount++
                str = urlConnection.inputStream.bufferedReader().readText()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                urlConnection.inputStream.close()
                urlConnection.disconnect()
            }

        return str
    }

    private fun addThemes(qCount: Int, db: SQLiteDatabase) {
        val themesArray = ArrayList<String>()
        //get themes
        val url = getUrl("get_themes", 0)
        val request = JsonObjectRequest(Request.Method.GET, url, null, Response.Listener { response ->
            run {
                val array = response.getJSONArray("data")
                for (k in 0 until qCount) {
                    val jsonObject = array.getJSONObject(k)
                    val name = jsonObject.getString("name")
                    themesArray.add(name)
                }
                addThemesToDataBase(themesArray, db)
            }
        }, Response.ErrorListener {
            error -> error.printStackTrace()
        })
        queue.add(request)
       /* try {
            val themes = loadAPI("get_themes", 0)
            val themesJSON = JSONObject(themes)
            val array = themesJSON.getJSONArray("data")
            for (k in 0 until qCount) {
                val jsonObject = array.getJSONObject(k)
                val name = jsonObject.getString("name")
                themesArray.add(name)
            }
            addThemesToDataBase(themesArray, db)
        } catch (e: Exception) {
        }*/
    }

    private fun addThemesToDataBase(themes: ArrayList<String>, db: SQLiteDatabase) {
        val qdbHelper = QuestionsDataBaseHelper(this.applicationContext, subject_prefix)
        if (qdbHelper.checkTable(subject_prefix + "_themes")) {
            qdbHelper.updateTableThemes(subject_prefix + "_themes")
        } else
            qdbHelper.addTableThemes(subject_prefix + "_themes")
        val cv = ContentValues()

        for (i in themes.indices) {
            val theme = themes[i]
            cv.put("theme_name", theme)
            val id = db.insert(subject_prefix + "_themes", null, cv)
            cv.clear()

        }
    }

    private fun addToDataBase1(questions: Array<JSONObject?>, db: SQLiteDatabase, i: Int) {
        val cv = ContentValues()
        Log.i("ADDtoDB", "size" + questions.size)
        for (j in questions.indices) {
            try {
                val task = questions[j]
                val question_name = "var" + (i + 1) + "q" + (j + 1)
                cv.put("question_name", question_name)

                var body = task!!.getString("body")
                body = performPictures(body, questions.size)
                cv.put("body", body)

                var solution = task.getString("solution")
                solution = performPictures(solution, questions.size)
                cv.put("solution", solution)

                val task_number = task.getString("task")
                cv.put("task", Integer.parseInt(task_number))

                try {
                    var the_text = task.getString("text")
                    the_text = performPictures(the_text, questions.size)
                    cv.put("the_text", the_text)
                } catch (e: Exception) {
                    cv.put("the_text", " ")
                }

                try {
                    val answer = task.getString("answer")
                    cv.put("answer", answer)
                } catch (e: Exception) {
                    cv.put("answer", " ")
                }

                val type = task.getString("type")
                cv.put("type", Integer.parseInt(type))

                val question_id = task.getString("id")
                cv.put("question_id", Integer.parseInt(question_id))

                val category_id = task.getString("category")
                cv.put("category", Integer.parseInt(category_id))

                val id = db.insert(subject_prefix, null, cv)
                cv.clear()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        //db.close();
    }


    private fun performPictures(string: String, size: Int): String {
        var string = string

        if (!download_pictures) {
            while (string.indexOf("src=\"https://ege.sdamgia.ru") != -1) {

                val path = (this.getExternalFilesDir(null)?.absolutePath
                        ?: this.filesDir.absolutePath) + "/RESHUEGE/Pictures/" + subject_prefix + if (testID == null) " theory" else testID
                val i = string.indexOf("src=\"https://ege.sdamgia.ru")
                var j = i + 5

                Log.d("Path", path)
                while (string[j] != '\"') j++

                val pictureUrl = string.substring(i + 5, j)
                val end = pictureUrl.length

                val format = if (pictureUrl.get(end - 4) == '.') pictureUrl.substring(end - 4, end) else ".png"
                val endName = if (pictureUrl.get(end - 4) == '.') end - 4 else end - 1
                var startName = endName
                while (pictureUrl.get(startName) != '/') startName--
                val name = if (format.contentEquals(".png")) Integer.toString(pngCount) else pictureUrl.substring(startName, endName)

                string = string.replace(pictureUrl, downloadPicture(pictureUrl, path, name, format, size))
            }
        }
        return string
    }

    private fun createNotificationChannel() {
        createNotificationChannel()
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var importance = NotificationManager.IMPORTANCE_HIGH
            var channel = NotificationChannel("77", "РЕШУ ЕГЭ", importance).apply {
                description = "Загрузка предмета"
                //enableLights(true)
                setSound(null, null)
                //lightColor = Color.RED
                setShowBadge(true)
            }
            // Register the channel with the system
            var notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            importance = NotificationManager.IMPORTANCE_LOW
            channel = NotificationChannel("33", "РЕШУ ЕГЭ", importance).apply {
                description = "Загрузка предмета"
                //enableLights(true)
                //lightColor = Color.RED
                setShowBadge(true)
            }
            // Register the channel with the system
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


}