package com.reshuege.DownloadTasks

import androidx.appcompat.app.AppCompatActivity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Environment
import android.preference.PreferenceManager
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.reshuege.Adapters.ThemesArrayAdapter

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

class DownloadThemesTasks(internal var context: Context, internal var subject_prefix: String,
                          internal var theme: String) : AsyncTask<String, Int, ArrayList<JSONObject>>() {

    internal lateinit var progress: ProgressDialog
    internal var serverError = false
    internal var download_pictures: Boolean = false
    internal var adapter: ThemesArrayAdapter? = null

    internal var pngCount = 1

    init {
        val settingsPref = PreferenceManager.getDefaultSharedPreferences(context)
        download_pictures = settingsPref.getBoolean("download_pictures", false)
    }



    override fun onPreExecute() {
        progress = ProgressDialog(context)
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progress.setMessage("Подключение к серверу")
        try {
            progress.show()
            progress.setCancelable(false)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun doInBackground(vararg strings: String): ArrayList<JSONObject> {
        val questions = ArrayList<JSONObject>()
        try {

            val QUESTIONS_COUNT = subject_prefix + "_" + theme + "_questions_count"
            val countPref = context.getSharedPreferences("subjects_questions_count", AppCompatActivity.MODE_PRIVATE)

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
                publishProgress(i + 1, themesTasksArray.length())
            }

            val ed = countPref.edit()
            ed.putInt(QUESTIONS_COUNT, questions.size)
            ed.apply()

        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("myLogs", e.toString())
            serverError = true
        }

        addToDataBase(questions)
        return questions
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
        try {
            if (values.size == 2) {
                progress.setMessage("Загрузка заданий\nЗадание " + values[0] + " из " + values[1])
            } else if (values[2] == -1) {
                if (!download_pictures) {
                    progress.setMessage("Загрузка изображений\nЗадание " + values[0] + " из " + values[1])
                } else {
                    progress.setMessage("Сохранение заданий\nЗадание " + values[0] + " из " + values[1])
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("myLogs", e.toString())
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
                Log.d("myLogs", e.toString())
            }
            adapter?.notifyDataSetChanged()
        } else {
            progress.dismiss()
            showMessage("Сервер РЕШУ ЕГЭ временно недоступен.", context.getString(R.string.error))
        }
    }

    private fun downloadPicture(url: String, path: String, name: String, format: String): String {

        pngCount++
        val input: InputStream
        val output: OutputStream
        val connection: URLConnection
        var extention = format
        try {
            connection = URL(url).openConnection()
            input = connection.getInputStream()
            val data = ByteArray(4096)
            var count: Int
            val cont_type = connection.getHeaderField("content-type")
            Log.d("myLogs", cont_type)
            extention = "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(cont_type)!!
            if (extention === ".null") extention = format
            Log.d("myLogs", "assert extention is $extention")

            val file = File(path, name + extention)
            output = FileOutputStream(file, false)
            Log.d("myLogs", "$url $path/$name$extention")
            StreamReader().writeFile(input, output)
            Log.d("myLogs", "Done")
        } catch (e: Exception) {
            return "file://$path/$name$extention"
        }

        return try {
            val bitmap = BitmapFactory.decodeFile("$path/$name$extention")
            val displaymetrics = context.resources.displayMetrics

            Log.d("myLogs", displaymetrics.widthPixels.toString() + "")
            val width: Int = if (bitmap.width < (displaymetrics.widthPixels - 100) / displaymetrics.density) bitmap.width else ((displaymetrics.widthPixels - 100) / displaymetrics.density).toInt()
            "file://" + path + "/" + name + extention + if (bitmap.width <= width) "" else "\" style=\"width:" + width + "px"
        } catch (e: Exception) {
            Log.d("myLogs", e.toString())
            "file://$path/$name$extention"
        }

    }

    fun loadAPI(APIRequest: String, id: Int): String? {

        var str: String? = null
        var tryesCount = 0
        while (str == null && tryesCount != 10)
            try {
                tryesCount++
                val url: URL = if (id == 0)
                    URL("https://" + subject_prefix + "-ege.sdamgia.ru/api?type=" + APIRequest + "&" + Protocol.protocolVersion)
                else
                    URL("https://" + subject_prefix + "-ege.sdamgia.ru/api?type=" + APIRequest + id + "&" + Protocol.protocolVersion)
                val urlConnection = url.openConnection() as HttpsURLConnection
                val `in` = BufferedReader(InputStreamReader(urlConnection.inputStream))
                str = StreamReader().readLines(`in`)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("myLogs", "$APIRequest $id $e")
            }

        return str
    }


    private fun addToDataBase(questions: ArrayList<JSONObject>) {
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
                Log.d("myLogs", question_name)
                var body = task.getString("body")
                body = performPictures(body)
                cv.put("body", body)
                Log.d("myLogs", body)
                var solution = task.getString("solution")
                solution = performPictures(solution)
                cv.put("solution", solution)
                Log.d("myLogs", solution)
                val task_number = task.getString("task")
                cv.put("task", Integer.parseInt(task_number))
                Log.d("myLogs", task_number)
                try {
                    var the_text = task.getString("text")
                    the_text = performPictures(the_text)
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
                val questionId = task.getString("id")
                cv.put("question_id", Integer.parseInt(questionId))
                Log.d("myLogs", questionId)
                val categoryId = task.getString("category")
                cv.put("category", Integer.parseInt(categoryId))
                Log.d("myLogs", categoryId)
                val id = db.insert(subject_prefix + "_" + theme, null, cv)
                Log.d("myLogs", java.lang.Long.toString(id))
                cv.clear()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("myLogs", "Финальный лог = $e")
            }

        }
        qdbHelper.close()
    }


    private fun performPictures(string: String): String {
        var string = string
        if (!download_pictures) {
            while (string.indexOf("src=\"https://ege.sdamgia.ru") != -1) {

                val path = (context.getExternalFilesDir(null)?.absolutePath ?: context.filesDir.absolutePath)  + "/RESHUEGE/Pictures/" + subject_prefix + theme

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

        val ad = androidx.appcompat.app.AlertDialog.Builder(context)
        ad.setTitle(title)
        ad.setMessage(message)

        ad.setPositiveButton("Продолжить") { dialog, arg1 -> }

        ad.setCancelable(true)
        ad.setOnCancelListener { }

        ad.show()
    }

}