package com.reshuege.DownloadTasks

import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Environment
import android.preference.PreferenceManager
import androidx.appcompat.app.AlertDialog
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.reshuege.DataBase.DataPreferences

import org.json.JSONObject

import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.URL
import java.net.URLConnection
import java.util.ArrayList

import javax.net.ssl.HttpsURLConnection

import com.reshuege.DataBase.QuestionsDataBaseHelper
import com.reshuege.Protocol
import com.reshuege.R
import com.reshuege.utils.StreamReader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class DownloadAllThemesTasks(internal var context: Context, internal var subject_prefix: String, internal var themes: Array<String?>) : AsyncTask<String, Int, ArrayList<JSONObject>>() {

    internal lateinit var progress: ProgressDialog
    internal var serverError = false
    internal var download_pictures: Boolean = false
    private val instance: DownloadAllThemesTasks

    val prefs = DataPreferences(context)

    internal var pngCount = 1


    init {
        val settingsPref = PreferenceManager.getDefaultSharedPreferences(context)
        download_pictures = settingsPref.getBoolean("download_pictures", false)
        instance = this
    }


    override fun onPreExecute() {
        progress = ProgressDialog(context)
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progress.setMessage("Подключение к серверу")
        progress.setCancelable(true)
        progress.setOnCancelListener { instance.cancel(true) }
        try {
            progress.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun doInBackground(vararg strings: String): ArrayList<JSONObject>? {
        var themeCount = 0
        for (theme in themes) {
            publishProgress(themeCount + 1, themes.size)
            if (!isTableExist(subject_prefix + "_" + theme)) {
                val questions = ArrayList<JSONObject>()
                try {

                    val file = if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
                        File(context.getExternalFilesDir(null), "/RESHUEGE/Pictures/" + subject_prefix + theme)
                    } else {
                        File(context.filesDir, "/RESHUEGE/Pictures/" + subject_prefix + theme)
                    }

                    if (!file.exists()) {
                        file.mkdirs()
                    }
                    val themesTasks = loadAPI("get_theme_tasks&data=$theme", 0)
                    val themesTasksJSON = JSONObject(themesTasks)
                    val themesTasksArray = themesTasksJSON.getJSONArray("data")

                    for (i in 0 until themesTasksArray.length()) {
                        val taskString = loadAPI("get_task&data=", Integer.parseInt(themesTasksArray.getString(i)))
                        val data = JSONObject(taskString)
                        val task = JSONObject(data.getString("data"))
                        questions.add(task)
                        publishProgress(themeCount + 1, themes.size, i + 1, themesTasksArray.length())
                        if (isCancelled) break
                    }
                    CoroutineScope(Dispatchers.IO).launch{
                        prefs.saveQuestionsThemeCount(subject_prefix, theme!!, questions.size)
                    }


                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d("myLogs", e.toString())
                    serverError = true
                }

                addToDataBase(questions, theme!!)
            }
            themeCount++
            if (isCancelled) break
        }
        return null
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
        try {
            if (values.size == 2) {
                progress.setMessage("Проверка темы\nТема " + values[0] + " из " + values[1])
            }
            if (values.size == 4) {
                progress.setMessage("Загрузка заданий\nТема " + values[0] + " из " + values[1] + "\nЗадание " + values[2] + " из " + values[3])
            } else if (values[2] == -1) {
                if (!download_pictures) {
                    progress.setMessage("Загрузка изображений темы\nЗадание " + values[0] + " из " + values[1])
                } else {
                    progress.setMessage("Сохранение заданий\nЗадание " + values[0] + " из " + values[1])
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onPostExecute(questions: ArrayList<JSONObject>) {
        super.onPostExecute(questions)
        if (!serverError) {
            try {
                progress.dismiss()
                Toast.makeText(context, "Задания загружены", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else {
            progress.dismiss()
            showMessage("Сервер РЕШУ ЕГЭ временно недоступен.", context.getString(R.string.error))
        }
    }

    fun downloadPicture(url: String, path: String, name: String, format: String): String {

        pngCount++
        val input: InputStream
        val output: OutputStream
        val connection: URLConnection
        var extention = format
        try {
            connection = URL(url).openConnection()
            input = connection.getInputStream()
            val cont_type = connection.getHeaderField("content-type")
            Log.d("myLogs", cont_type)
            extention = "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(cont_type)!!
            if (extention === ".null") extention = format
            Log.d("myLogs", "assert extention is $extention")

            val file = File(path, name + extention)
            output = FileOutputStream(file, false)
            Log.d("myLogs", "$url $path/$name$extention")
            StreamReader().writeFile(input, output)
        } catch (e: Exception) {
            return "file://$path/$name$extention"
        }

        try {
            val bitmap = BitmapFactory.decodeFile("$path/$name$extention")
            val displaymetrics = context.resources.displayMetrics

            val width: Int = if (bitmap.width < (displaymetrics.widthPixels - 100) / displaymetrics.density) bitmap.width else ((displaymetrics.widthPixels - 100) / displaymetrics.density).toInt()
            return "file://" + path + "/" + name + extention + if ( bitmap.width <= width) "" else "\" style=\"width:" + width + "px"
        } catch (e: Exception) {
            e.printStackTrace()
            return "file://$path/$name$extention"
        }

    }

    fun loadAPI(APIRequest: String, id: Int): String? {

        var str: String? = null
        var tryesCount = 0
        while (str == null && tryesCount != 10)
            try {
                tryesCount++
                val url: URL
                url = if (id == 0)
                    URL("https://" + subject_prefix + "-ege.sdamgia.ru/api?type=" + APIRequest + "&" + Protocol.protocolVersion)
                else
                    URL("https://" + subject_prefix + "-ege.sdamgia.ru/api?type=" + APIRequest + id + "&" + Protocol.protocolVersion)
                val urlConnection = url.openConnection() as HttpsURLConnection
                val `in` = BufferedReader(InputStreamReader(urlConnection.inputStream))
                val sBuffer = StringBuffer("")
                var line = ""
                while (`in`.readLine().let { line = it; it != null }) {
                    sBuffer.append(line + "\n")

                }
                `in`.close()
                str = sBuffer.toString()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("myLogs", "$APIRequest $id $e")
            }

        return str
    }

    private fun isTableExist(tableName: String): Boolean {
        val qdbHelper = QuestionsDataBaseHelper(context, tableName)
        val isExist = qdbHelper.checkTable(tableName)
        qdbHelper.close()
        return isExist
    }

    private fun addToDataBase(questions: ArrayList<JSONObject>, theme: String) {
        val qdbHelper = QuestionsDataBaseHelper(context, subject_prefix + "_" + theme)
        val db = qdbHelper.writableDatabase
        if (qdbHelper.checkTable(subject_prefix + "_" + theme)) {
            qdbHelper.updateTable(subject_prefix + "_" + theme)
        } else
            qdbHelper.addTable(subject_prefix + "_" + theme)
        val cv = ContentValues()
        for (i in questions.indices) {
            try {
                val task = questions[i]
                publishProgress(i + 1, questions.size, -1)
                val question_name = "q" + (i + 1)
                cv.put("question_name", question_name)
                var body = task.getString("body")
                body = performPictures(body, theme)
                cv.put("body", body)
                Log.d("myLogs", body)
                var solution = task.getString("solution")
                solution = performPictures(solution, theme)
                cv.put("solution", solution)
                Log.d("myLogs", solution)
                val task_number = task.getString("task")
                cv.put("task", Integer.parseInt(task_number))
                Log.d("myLogs", task_number)
                try {
                    var the_text = task.getString("text")
                    the_text = performPictures(the_text, theme)
                    cv.put("the_text", the_text)
                    Log.d("myLogs", the_text)
                } catch (e: Exception) {
                    Log.d("myLogs", e.toString())
                    cv.put("the_text", " ")
                }

                try {
                    val answer = task.getString("answer")
                    cv.put("answer", answer)
                    Log.d("myLogs", answer)
                } catch (e: Exception) {
                    Log.d("myLogs", e.toString())
                    cv.put("answer", " ")
                }

                val type = task.getString("type")
                cv.put("type", Integer.parseInt(type))
                Log.d("myLogs", type)
                val question_id = task.getString("id")
                cv.put("question_id", Integer.parseInt(question_id))
                Log.d("myLogs", question_id)
                val cat = task.getString("category")
                cv.put("category", Integer.parseInt(cat))
                Log.d("myLogs", cat)
                val id = db.insert(subject_prefix + "_" + theme, null, cv)
                Log.d("myLogs", id.toString())
                cv.clear()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("myLogs", "Финальный лог = $e")
            }

        }
        qdbHelper.close()
    }


    fun performPictures(string: String, theme: String): String {
        var string = string
        if (!download_pictures) {
            while (string.indexOf("src=\"https://ege.sdamgia.ru") != -1) {

                val path = (context.getExternalFilesDir(null)?.absolutePath ?: context.filesDir.absolutePath) + "/RESHUEGE/Pictures/" + subject_prefix + theme

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

                string = string.replace(pictureUrl, downloadPicture(pictureUrl, path, name, format))
            }
        }
        return string
    }

    fun showMessage(title: String, message: String) {
        val ad = AlertDialog.Builder(context)
        ad.setTitle(title)
        ad.setMessage(message)
        ad.setPositiveButton("Продолжить") { dialog, arg1 -> }
        ad.setCancelable(true)
        ad.setOnCancelListener { }
        ad.show()
    }

}
