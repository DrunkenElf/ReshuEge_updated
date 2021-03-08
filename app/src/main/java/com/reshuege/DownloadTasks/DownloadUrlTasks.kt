package com.reshuege.DownloadTasks

import android.app.*
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.database.sqlite.SQLiteDatabase
import android.graphics.BitmapFactory
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.reshuege.Adapters.SubjAdapter
import com.reshuege.DataBase.QuestionsDataBaseHelper
import com.reshuege.Protocol
import com.reshuege.R
import com.reshuege.utils.StreamReader
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import java.net.URLConnection
import java.util.*
import javax.net.ssl.HttpsURLConnection

class DownloadUrlTasks(var context: Context, internal var subject_prefix: String,
                       internal var adapter: SubjAdapter, var name: String) {

    internal var testID: String? = null
    internal var qCount = 0
    internal var counter = 0
    internal var serverError = false
    internal var download_pictures: Boolean = false
    internal var var_counter = 0
    internal var img_counter = 0
    internal var pngCount = 1
    internal var progressMsg = arrayOf(" из 15 Вариантов загружены\n", " Файлов загружено")
    var handler: Handler
    lateinit var disposable: Disposable


    init {
        val settingsPref = PreferenceManager.getDefaultSharedPreferences(context)
        download_pictures = settingsPref.getBoolean("download_pictures", false)
        handler = Handler(Looper.getMainLooper())
    }

    class myService(name: String) : IntentService(name){

        override fun onHandleIntent(intent: Intent?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

        }
    }

    fun loadVar(testId: String, countPrefs: SharedPreferences,
                db: SQLiteDatabase, questCount: String, i: Int): IndValue {
        //if (i == 0)
        val questionNumbersString = loadAPI("get_test&id=", Integer.parseInt(testId) + i)
        val variant = JSONObject(questionNumbersString)
        val questionNumbers = variant.getJSONArray("data")
        val variantArray = ArrayList<JSONObject>()
        val ed = countPrefs.edit()
        ed.putInt(questCount, questionNumbers.length())
        ed.apply()
        qCount = questionNumbers.length()
        if (i == 0)
            addThemes(qCount, db)
        for (j in 0 until questionNumbers.length()) {
            val taskString = loadAPI("get_task&data=", Integer.parseInt(questionNumbers.getString(j)))
            val data = JSONObject(taskString)
            val task = JSONObject(data.getString("data"))
            variantArray.add(task)
        }
        var_counter++
        publishProgress(intArrayOf(var_counter, questionNumbers.length(), 1))
        addToDataBase1(variantArray, db, i)

        return IndValue(i, variantArray)
    }

    class IndValue(var index: Int, var array: ArrayList<JSONObject>)

    lateinit var notificationBuilder: NotificationCompat.Builder
    val notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(context)
    val id = 77


    fun downloadTasks() {
        Log.d("Download", "downloadTasks")
        notificationBuilder = NotificationCompat.Builder(context, "77").apply {
            setContentTitle(name+": загрузка")
            setSmallIcon(R.mipmap.ic_launcher)
            setStyle(NotificationCompat.BigTextStyle())
            setAutoCancel(false)
        }
        notificationManager.notify(id, notificationBuilder.build())

        try {
            if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)

                (context as AppCompatActivity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            else
                (context as AppCompatActivity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val QUESTIONS_COUNT = subject_prefix + "_questions_count"
        val countPref = context.getSharedPreferences("subjects_questions_count", AppCompatActivity.MODE_PRIVATE)

        val questions = arrayOfNulls<ArrayList<*>>(15) as Array<ArrayList<JSONObject>>
        testID = loadAPI("predefined_tests", 0)
        try {
            val id = JSONObject(testID)
            testID = id.getString("data")
            val folder = File(Environment.getExternalStorageDirectory().toString() + "/RESHUEGE/Pictures/" + subject_prefix + testID)

            if (!folder.exists()) {
                folder.mkdirs()
            }
        } catch (e: Exception) {
        }

        val qdbHelper = QuestionsDataBaseHelper(context, subject_prefix)
        if (qdbHelper.checkTable(subject_prefix)) {
            qdbHelper.updateTable(subject_prefix)
        } else
            qdbHelper.addTable(subject_prefix)
        val db = qdbHelper.writableDatabase

        notificationManager.cancel(77)
        notificationBuilder = NotificationCompat.Builder(context, "33").apply {
            setProgress(0, 0, true)
            setContentTitle(name+": загрузка")
            setSmallIcon(R.mipmap.ic_launcher)
            setStyle(NotificationCompat.BigTextStyle())
            setAutoCancel(false)
            setOngoing(true)
        }
        notificationManager.notify(33, notificationBuilder.build())

        val vals = Observable.range(1, 15) as Observable<Int>
        disposable = vals.flatMap { mapper ->
            Observable.just(mapper).subscribeOn(Schedulers.io())
                    .map { i -> loadVar(testID!!, countPref, db, QUESTIONS_COUNT, i - 1) }
        }
                .observeOn(AndroidSchedulers.mainThread())
                //.retryWhen(error -> {})
                .retry(2)
                .subscribe(
                        { result -> print(result.index) },
                        { error -> Log.d("ERROR", error.message.toString()) },
                        { onLoadFinish() }
                )


    }

    fun publishProgress(values: IntArray) {
        val r = Runnable {
            try {
                notificationBuilder
                        .setStyle(NotificationCompat
                                .BigTextStyle()
                                .bigText(var_counter.toString() + progressMsg[0] + img_counter.toString() + progressMsg[1]))
                notificationManager.notify(33, notificationBuilder.build())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        handler.postDelayed(r, 10)
    }

    fun onLoadFinish() {
        Log.d("Download", "onLoadFinish")
        if (!disposable.isDisposed) disposable.dispose()
        if (!serverError) {
            try {
                val verPref = context.getSharedPreferences("version_tests", AppCompatActivity.MODE_PRIVATE)
                val directoryForDeletePath = (Environment.getExternalStorageDirectory().toString() + "/RESHUEGE/Pictures/"
                        + subject_prefix + verPref.getString(subject_prefix, ""))

                //Удаления папки со старыми картинками
                val folderForDelete = File(directoryForDeletePath)
                if (!testID!!.contentEquals(verPref.getString(subject_prefix, "")!!))
                    if (folderForDelete.exists()) deleteRecursive(folderForDelete)

                //Сохранение номера первого скаченного варианта
                val ed = verPref.edit()
                ed.putString(subject_prefix, testID)
                ed.apply()

                val verTheory = context.getSharedPreferences("version_theory", AppCompatActivity.MODE_PRIVATE)
                val edit = verTheory.edit()
                edit.putString(subject_prefix, testID)
                edit.apply()

                //При критическом обновлении (одноразовое действие)
                val criticalUpdate = context.getSharedPreferences("critical_update", AppCompatActivity.MODE_PRIVATE)
                val editor = criticalUpdate.edit()
                editor.putBoolean(subject_prefix, true)
                editor.apply()

                //progress.dismiss()
                Toast.makeText(context, "Задания загружены", Toast.LENGTH_SHORT).show()
                adapter.notifyDataSetChanged()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            //progress.dismiss()
            showMessage("Сервер РЕШУ ЕГЭ временно недоступен.", context.getString(R.string.error))
        }

        notificationManager.cancel(33)
        notificationBuilder = NotificationCompat.Builder(context, "77").apply {
            setContentTitle(name+": загрузка")
            setContentText("Загрузка завершена")
            setSmallIcon(R.mipmap.ic_launcher)
            setStyle(NotificationCompat.BigTextStyle())
            setAutoCancel(true)
        }
        notificationManager.notify(77, notificationBuilder.build())
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
            input = connection.getInputStream()

            val cont_type = connection.getHeaderField("content-type")

            extention = "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(cont_type)!!
            if (extention === ".null") extention = format

            val file = File(path, name + extention)
            Log.d("EXTESION", extention)
            output = FileOutputStream(file, false)
            StreamReader().writeFile(input, output)
        } catch (e: Exception) {
            return "file://$path/$name$extention"
        }

        try {
            val bitmap = BitmapFactory.decodeFile("$path/$name$extention")
            val displaymetrics = context.resources.displayMetrics

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
                urlConnection.disconnect()
            }

        return str
    }

    private fun addThemes(qCount: Int, db: SQLiteDatabase) {
        val themesArray = ArrayList<String>()
        //get themes
        try {
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
        }
    }

    private fun addThemesToDataBase(themes: ArrayList<String>, db: SQLiteDatabase) {
        val qdbHelper = QuestionsDataBaseHelper(context, subject_prefix)
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

    private fun addToDataBase1(questions: ArrayList<JSONObject>, db: SQLiteDatabase, i: Int) {
        val cv = ContentValues()
        Log.i("ADDtoDB", "size" + questions.size)
        for (j in questions.indices) {
            try {
                val task = questions[j]
                val question_name = "var" + (i + 1) + "q" + (j + 1)
                cv.put("question_name", question_name)

                var body = task.getString("body")
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

                val path = Environment.getExternalStorageDirectory().toString() + "/RESHUEGE/Pictures/" + subject_prefix + if (testID == null) " theory" else testID
                val i = string.indexOf("src=\"https://ege.sdamgia.ru")
                var j = i + 5

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

    fun showMessage(title: String, message: String) {

        val ad = androidx.appcompat.app.AlertDialog.Builder(context)
        ad.setTitle(title)
        ad.setMessage(message)

        ad.setPositiveButton("Продолжить") { dialog, arg1 -> }

        ad.setCancelable(true)
        ad.setOnCancelListener { }

        ad.show()
    }


}