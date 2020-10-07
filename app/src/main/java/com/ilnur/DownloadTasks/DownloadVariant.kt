package com.ilnur.DownloadTasks

import android.app.ActivityOptions
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AlertDialog
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat

import org.json.JSONObject

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.ArrayList

import javax.net.ssl.HttpsURLConnection

import com.ilnur.Protocol
import com.ilnur.Session.Session
import com.ilnur.Session.Settings
import com.ilnur.TestsActivity
import com.ilnur.utils.StreamReader
import org.jsoup.Connection
import org.jsoup.Jsoup

class DownloadVariant(private val context: Context, private val subject_prefix: String, private val testID: String, private val section: String, internal var isTeacherTest: Boolean) : AsyncTask<String, Int, DownloadVariant.Response>() {

    private var progress: ProgressDialog? = null
    internal var serverError = false
    internal var token = ""

    override fun onPreExecute() {
        progress = ProgressDialog(context)
        progress!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)

        progress!!.setMessage("Подключение к серверу")
        try {
            progress!!.show()
            progress!!.setCancelable(false)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun doInBackground(vararg strings: String): Response? {
        val questions = ArrayList<JSONObject>()

        var publicType = 0
        var teacherId = 0

        try {
            if (isTeacherTest){
                val settings = Settings()
                token = Jsoup.connect("https://ege.sdamgia.ru/v1.0/auth/login?body={%22deviceGuid%22:%200,%20%22login%22:%22"+
                        settings.getLogin(context)+"%22,%20%22password%22:%22"+settings.getPassword(context)+"%22}&jwt=1")
                        .get().html().split("sessionId\": \"")[1].split("\"}}")[0]
                Log.d("TOKEN", token)
            }
            val questionNumbersString = loadAPI("get_test&id=", Integer.parseInt(testID))
            Log.d("TASK: Variant: ", questionNumbersString.toString())
            val variant = JSONObject(questionNumbersString)
            publicType = variant.optInt("public")
            teacherId = variant.optInt("teacher")
            val questionNumbers = variant.getJSONArray("data")

            for (j in 0 until questionNumbers.length()) {
                val taskString = loadAPI("get_task&data=", Integer.parseInt(questionNumbers.getString(j)))
                //Log.d("TASK: Task", taskString)
                val data = JSONObject(taskString)
                val task = JSONObject(data.getString("data"))
                questions.add(task)
                publishProgress(j + 1, questionNumbers.length())
            }

        } catch (e: NumberFormatException) {
            return null
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("myLogs", e.toString())
            serverError = true
        }

        return Response(questions, teacherId, publicType)
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
        try {
            progress!!.setMessage("Задание: " + values[0] + " из " + values[1])
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onPostExecute(response: Response?) {
        super.onPostExecute(response)
        if (!serverError) {
            if (response != null) {
                val questions = response.questions
                if (questions != null) {
                    val questionName = ArrayList<String>()
                    val body = ArrayList<String>()
                    val solution = ArrayList<String>()
                    val task = ArrayList<String>()
                    val theText = ArrayList<String>()
                    val answer = ArrayList<String>()
                    val type = ArrayList<String>()
                    val questionId = ArrayList<String>()
                    val categoryId = ArrayList<String>()

                    for (j in questions.indices) {
                        try {
                            val JSONtask = questions[j]

                            val _question_name = "q" + (j + 1)
                            questionName.add(_question_name)

                            val _body = JSONtask.getString("body")
                            body.add(_body)

                            val _solution = JSONtask.getString("solution")
                            solution.add(_solution)

                            val _task_number = JSONtask.getString("task")
                            task.add(_task_number)

                            try {
                                val _the_text = JSONtask.getString("text")
                                theText.add(_the_text)
                            } catch (e: Exception) {
                                theText.add(" ")
                            }


                            try {
                                val _answer = JSONtask.getString("answer")
                                answer.add(_answer)
                            } catch (e: Exception) {
                                answer.add(" ")
                            }

                            val _type = JSONtask.getString("type")
                            type.add(_type)

                            val _question_id = JSONtask.getString("id")
                            questionId.add(_question_id)
                            val _cat = JSONtask.getString("category")
                            categoryId.add(_cat)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Log.d("myLogs", "Финальный лог = $e")
                        }

                    }
                    val publicType = response.publicType
                    val teacherId = response.teacherId
                    val intent = Intent(context, TestsActivity::class.java)
                    intent.putExtra("subject_prefix", subject_prefix)
                    intent.putExtra("variant_number", Integer.valueOf(testID))
                    intent.putExtra("section", section)
                    intent.putExtra("count", questions.size)
                    intent.putExtra("question_name", questionName)
                    intent.putExtra("body", body)
                    intent.putExtra("the_text", theText)
                    intent.putExtra("solution", solution)
                    intent.putExtra("task", task)
                    intent.putExtra("answer", answer)
                    intent.putExtra("type", type)
                    intent.putExtra("question_id", questionId)
                    intent.putExtra("category_id", categoryId)
                    intent.putExtra("public", publicType)
                    intent.putExtra("teacherId", teacherId)
                    intent.putExtra("isTeach", isTeacherTest)
                    intent.putExtra("token", token)
                    if (Build.VERSION.SDK_INT > 20) {
                        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(context as AppCompatActivity)
                        context.startActivity(intent, options.toBundle())
                    } else {
                        context.startActivity(intent)
                    }
                }
            } else {
                showMessage("Ошибка", "Вариант не найден.")
            }
        } else {
            showMessage("Ошибка", "Вариант не найден.")
        }
        progress!!.dismiss()
    }


    fun loadAPI(APIRequest: String, id: Int): String? {

        var str: String? = null
        var tryesCount = 0
        while (str == null && tryesCount != 10)
            try {
                tryesCount++
                val url: URL = if (id == 0)
                    URL("https://" + subject_prefix + "-ege.sdamgia.ru/api?type=" + APIRequest + "&" + Protocol.protocolVersion + if (isTeacherTest) "&public=1" else "")
                else
                    URL("https://" + subject_prefix + "-ege.sdamgia.ru/api?type=" + APIRequest + id + "&" + Protocol.protocolVersion + if (isTeacherTest) "&public=1" else "")
                val urlConnection = url.openConnection() as HttpsURLConnection
                val `in` = BufferedReader(InputStreamReader(urlConnection.inputStream))
                str = StreamReader().readLines(`in`)


            } catch (e: Exception) {
                e.printStackTrace()
            }

        return str
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

    inner class Response(internal var questions: ArrayList<JSONObject>?, internal var teacherId: Int, internal var publicType: Int)


}
