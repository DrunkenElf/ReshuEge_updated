package com.ilnur.DownloadTasks

import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Environment
import android.preference.PreferenceManager
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ilnur.Adapters.SubjAdapter
import com.ilnur.DataBase.QuestionsDataBaseHelper
import com.ilnur.Protocol
import com.ilnur.R
import com.ilnur.utils.StreamReader
import io.reactivex.BackpressureStrategy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import java.net.URLConnection
import java.util.*
import javax.net.ssl.HttpsURLConnection

class ParserUrlTasks(internal var context: Context, internal var subject_prefix: String, internal var adapter: SubjAdapter) : AsyncTask<String, Int, Array<ArrayList<JSONObject>>>() {

    internal lateinit var progress: ProgressDialog
    internal var testID: String? = null
    internal var qCount = 0
    internal var counter = 0
    internal var serverError = false
    internal var download_pictures: Boolean = false

    internal var var_counter = 0
    internal var img_counter = 0

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

    fun loadVar(testId: String, countPrefs: SharedPreferences,
                db: SQLiteDatabase, questCount: String, i: Int): IndValue{
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
        //Log.d("myLogs", "Массив заданий длина " + questionNumbers.length());
        for (j in 0 until questionNumbers.length()) {
            val taskString = loadAPI("get_task&data=", Integer.parseInt(questionNumbers.getString(j)))
            val data = JSONObject(taskString)
            val task = JSONObject(data.getString("data"))
            variantArray.add(task)
        }
        var_counter++
        publishProgress(var_counter, questionNumbers.length(), 1)

        //questions.add(variantArray);
        addToDataBase1(variantArray, db, i)
        return IndValue(i, variantArray)
    }

    //fun dowload()

    class IndValue(var index: Int, var array: ArrayList<JSONObject>)

    override fun doInBackground(vararg strings: String): Array<ArrayList<JSONObject>> {

        val QUESTIONS_COUNT = subject_prefix + "_questions_count"
        val countPref = context.getSharedPreferences("subjects_questions_count", AppCompatActivity.MODE_PRIVATE)

        //ArrayList<ArrayList<JSONObject>> questions = new ArrayList<ArrayList<JSONObject>>();
        val questions = arrayOfNulls<ArrayList<*>>(15) as Array<ArrayList<JSONObject>>
        testID = loadAPI("predefined_tests", 0)
        Log.d("myLogs", testID.toString())
        try {
            val id = JSONObject(testID)
            testID = id.getString("data")
            val file = if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
                File(context.getExternalFilesDir(null), "/RESHUEGE/Pictures/" + subject_prefix + testID)
            } else {
                File(context.filesDir, "/RESHUEGE/Pictures/" + subject_prefix + testID)
            }

            val folder = File(context.filesDir,  "/RESHUEGE/Pictures/" + subject_prefix + testID)

            if (!folder.exists()) {
                folder.mkdirs()
            }
            Log.d("folderesdir",file.absolutePath)
            Log.d("filesdir",folder.absolutePath)
        } catch (e: Exception) {
            Log.d("error create folder", e.toString())
        }

        val qdbHelper = QuestionsDataBaseHelper(context, subject_prefix)
        if (qdbHelper.checkTable(subject_prefix)) {
            qdbHelper.updateTable(subject_prefix)
        } else
            qdbHelper.addTable(subject_prefix)
        //progress.setMessage("Загрузка вариантов");
        val db = qdbHelper.writableDatabase

        val tasks = PublishSubject.create<Int>()
        tasks.toFlowable(BackpressureStrategy.MISSING)
                .parallel()
                .runOn(Schedulers.io())
                .map { i -> loadVar(testID!!, countPref, db, QUESTIONS_COUNT, i) }
                .sequential()
                .retry()
                .subscribe(
                        {result -> questions[result.index] = result.array},
                        {error -> Log.d("ERROR", error.message.toString())},
                        {onLoadFinish(questions)}
                )




                //loadVar(testID, countPref, db, QUESTIONS_COUNT,)

        /*for (i in 0..14){
            try {
                val questionNumbersString = loadAPI("get_test&id=", Integer.parseInt(testID!!) + i)
                val variant = JSONObject(questionNumbersString)
                val questionNumbers = variant.getJSONArray("data")
                val variantArray = ArrayList<JSONObject>()

                val ed = countPref.edit()
                ed.putInt(QUESTIONS_COUNT, questionNumbers.length())
                ed.apply()
                qCount = questionNumbers.length()
                if (i == 0)
                    addThemes(qCount)
                //Log.d("myLogs", "Массив заданий длина " + questionNumbers.length());
                for (j in 0 until questionNumbers.length()) {
                    val taskString = loadAPI("get_task&data=", Integer.parseInt(questionNumbers.getString(j)))
                    val data = JSONObject(taskString)
                    val task = JSONObject(data.getString("data"))
                    variantArray.add(task)
                }
                var_counter++
                publishProgress(var_counter, questionNumbers.length(), 1)

                //questions.add(variantArray);
                questions[i] = variantArray
                addToDataBase1(variantArray, qdbHelper, i)
                counter++
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("error ", "run() called")
                Log.d("EXCEPTIONinTHREAD", "YEAS")
                serverError = true
            }
        }*/
        /*for (i in 0..14) {
            val thread = Thread(Runnable {
                try {
                    val questionNumbersString = loadAPI("get_test&id=", Integer.parseInt(testID!!) + i)
                    val variant = JSONObject(questionNumbersString)
                    val questionNumbers = variant.getJSONArray("data")
                    val variantArray = ArrayList<JSONObject>()

                    val ed = countPref.edit()
                    ed.putInt(QUESTIONS_COUNT, questionNumbers.length())
                    ed.apply()
                    qCount = questionNumbers.length()
                    if (i == 0)
                        addThemes(qCount, db)
                    //Log.d("myLogs", "Массив заданий длина " + questionNumbers.length());
                    for (j in 0 until questionNumbers.length()) {
                        val taskString = loadAPI("get_task&data=", Integer.parseInt(questionNumbers.getString(j)))
                        val data = JSONObject(taskString)
                        val task = JSONObject(data.getString("data"))
                        variantArray.add(task)
                    }
                    var_counter++
                    publishProgress(var_counter, questionNumbers.length(), 1)

                    //questions.add(variantArray);
                    questions[i] = variantArray
                    addToDataBase1(variantArray, db, i)
                    counter++
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d("error ", "run() called")
                    Log.d("EXCEPTIONinTHREAD", "YEAS")
                    serverError = true
                }
            })
            thread.start()
        }

        while (counter <= 14 && !serverError) {
            try {
                Log.i("COUNTER", "" + counter)
                Thread.sleep(100)
                Log.i("serverError", serverError.toString())
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

        }*/
        //addThemes(qCount);
        //addToDataBase(questions);
        return questions
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
        //addThemes(qCount);
        try {
            if (values.size != 4) {
                progress.setMessage(values[0].toString() + " из 15 Вариантов загружены")

            } else {
                if (!download_pictures) {
                    progress.setMessage(values[0].toString() + " из 15 Вариантов загружены\n" + values[3] + " Фотографий загружено")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("myLogs", e.toString())
        }

    }

    //protected void onPostExecute(ArrayList<ArrayList<JSONObject>> questions) {
    override fun onPostExecute(questions: Array<ArrayList<JSONObject>>) {
        super.onPostExecute(questions)
        //addToDataBase(questions);
        //addThemes(qCount);
        if (!serverError) {
            try {

                val verPref = context.getSharedPreferences("version_tests", AppCompatActivity.MODE_PRIVATE)

                val directoryForDeletePath = (context.getExternalFilesDir(null)!!.absolutePath + "/RESHUEGE/Pictures/"
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

                progress.dismiss()
                Toast.makeText(context, "Задания загружены", Toast.LENGTH_SHORT).show()
                adapter.notifyDataSetChanged()

            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("myLogs", e.toString())
            }

        } else {
            progress.dismiss()
            showMessage("Сервер РЕШУ ЕГЭ временно недоступен.", context.getString(R.string.error))
        }
    }

    fun onLoadFinish(questions: Array<ArrayList<JSONObject>>){
        if (!serverError) {
            try {

                val verPref = context.getSharedPreferences("version_tests", AppCompatActivity.MODE_PRIVATE)

                val directoryForDeletePath = (context.getExternalFilesDir(null)!!.absolutePath  + "/RESHUEGE/Pictures/"
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

                progress.dismiss()
                Toast.makeText(context, "Задания загружены", Toast.LENGTH_SHORT).show()
                adapter.notifyDataSetChanged()

            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("myLogs", e.toString())
            }

        } else {
            progress.dismiss()
            showMessage("Сервер РЕШУ ЕГЭ временно недоступен.", context.getString(R.string.error))
        }
    }

    private fun  downloadPicture(url: String, path: String, name: String, format: String, size: Int): String {
        //Log
        pngCount++
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
            output = FileOutputStream(file, false)
            StreamReader().writeFile(input, output)
        } catch (e: Exception) {
            Log.d("error conn", "downloadPicture() called with: url = [$url], path = [$path], name = [$name], format = [$format], size = [$size]")
            return "file://$path/$name$extention"
        }

        try {
            val bitmap = BitmapFactory.decodeFile("$path/$name$extention")
            val displaymetrics = context.resources.displayMetrics

            val width: Int = if (bitmap.width < (displaymetrics.widthPixels - 100) / displaymetrics.density) bitmap.width else ((displaymetrics.widthPixels - 100) / displaymetrics.density).toInt()
            val finalImagePath = "file://" + path + "/" + name + extention + if (bitmap.width <= width) "" else "\" style=\"width:" + width + "px"
            img_counter++
            publishProgress(var_counter, size, 1, img_counter)
            return finalImagePath
        } catch (e: Exception) {
            Log.d("error bit", "downloadPicture() called with: url = [$url], path = [$path], name = [$name], format = [$format], size = [$size]")
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
                //val `in` = BufferedReader(InputStreamReader(urlConnection.inputStream))
                //str = StreamReader().readLines(`in`)
                str = urlConnection.inputStream.bufferedReader().readText()
                Log.i("LOADAPIOR@", str)

            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("error ", "loadAPI() called with: APIRequest = [$APIRequest], id = [$id]")
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
                Log.d("myLogs", "№$k $name")
            }
            addThemesToDataBase(themesArray, db)
        } catch (e: Exception) {
            Log.d("myLogs", e.toString())
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
            Log.d("myLogs", java.lang.Long.toString(id))
            cv.clear()

        }
        //qdbHelper.close()

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

                val file = if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
                    File(context.getExternalFilesDir(null), "/RESHUEGE/Pictures/" + subject_prefix + testID)
                } else {
                    File(context.filesDir, "/RESHUEGE/Pictures/" + subject_prefix + testID)
                }
                val path = file.absolutePath + "/RESHUEGE/Pictures/" + subject_prefix + if (testID == null) " theory" else testID
                //Log.i("LINK",string);
                val i = string.indexOf("src=\"https://ege.sdamgia.ru")
                var j = i + 5

                Log.d("path", path)

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
