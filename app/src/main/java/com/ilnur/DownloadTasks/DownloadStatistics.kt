package com.ilnur.DownloadTasks

import android.app.Activity
import android.app.ActivityOptions
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat

import org.json.JSONObject

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.ArrayList

import javax.net.ssl.HttpsURLConnection

import com.ilnur.Protocol
import com.ilnur.R
import com.ilnur.StatisticsActivity
import com.ilnur.utils.StreamReader
import org.json.JSONException

class DownloadStatistics(internal var context: Context, internal var subject_prefix: String, internal var query: String) : AsyncTask<String, Int, JSONObject>() {

    internal lateinit var progress: ProgressDialog
    internal lateinit var subject_name: String
    internal var serverError = false


    init {
        val subjectsArray = context.resources.getStringArray(R.array.subjects)
        val prefixArray = context.resources.getStringArray(R.array.subjects_prefix)
        for (i in prefixArray.indices) {
            if (prefixArray[i].contentEquals(subject_prefix)) {
                this.subject_name = subjectsArray[i]
                break
            }
        }
    }

    override fun onPreExecute() {
        progress = ProgressDialog(context)
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER)

        progress.setMessage("Загрузка статистики")

        try {
            progress.show()
            progress.setCancelable(true)

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun doInBackground(vararg strings: String): JSONObject? {


        try {
            val response = loadAPI("get_stat&", query)

            return JSONObject(response).getJSONObject("data")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("myLogs", e.toString())
            serverError = true
        } catch (j: JSONException){
            j.printStackTrace()
            serverError = true

        }

        return null
    }

    override fun onPostExecute(results: JSONObject) {
        super.onPostExecute(results)
        if (!serverError) {
            try {

                val question_name = ArrayList<String>()
                val solved = ArrayList<Int>()
                val rightSolved = ArrayList<Int>()
                val percents = ArrayList<Int>()

                val bArray = results.getJSONArray("B")
                val cArray = results.getJSONArray("C")

                for (i in 0 until bArray.length()) {
                    val questionStats = bArray.getJSONArray(i)
                    question_name.add("B" + (i + 1))
                    val solv = questionStats.getInt(0)
                    val rightSolv = questionStats.getInt(1)
                    var perc: Int? = 0
                    if (solv > 0) {
                        perc = rightSolv * 100 / solv
                    }
                    solved.add(solv)
                    rightSolved.add(rightSolv)
                    percents.add(perc!!)
                }

                for (i in 0 until cArray.length()) {
                    val questionStats = cArray.getJSONArray(i)
                    question_name.add("C" + (i + 1))
                    val solv = questionStats.getInt(0)
                    val rightSolv = questionStats.getInt(1)
                    var perc: Int? = 0
                    if (solv > 0) {
                        perc = rightSolv * 100 / solv
                    }
                    solved.add(solv)
                    rightSolved.add(rightSolv)
                    percents.add(perc!!)
                }

                val intent = Intent(context, StatisticsActivity::class.java)
                intent.putExtra("question_number", question_name)
                intent.putExtra("solved", solved)
                intent.putExtra("right_solved", rightSolved)
                intent.putExtra("percents", percents)
                intent.putExtra("subject_name", subject_name)
                if (Build.VERSION.SDK_INT > 20) {
                    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(context as AppCompatActivity)
                    context.startActivity(intent, options.toBundle())
                } else {
                    context.startActivity(intent)
                }
                progress.dismiss()

            } catch (e: Exception) {
                Toast.makeText(context, "Произошла ошибка. Повторите позже.", Toast.LENGTH_SHORT).show()
                progress.dismiss()
                Log.d("myLogs", e.toString())
            }

        } else {
            progress.dismiss()
            showMessage("Сервер РЕШУ ЕГЭ временно недоступен.", context.getString(R.string.error))
        }
    }

    fun loadAPI(APIRequest: String, query: String): String? {

        var str: String? = null
        var tryesCount = 0
        while (str == null && tryesCount != 10)
            try {
                tryesCount++
                val url = URL("https://" + subject_prefix + "-ege.sdamgia.ru/api?type=" + APIRequest + query + "&" + Protocol.protocolVersion)
                val urlConnection = url.openConnection() as HttpsURLConnection
                val `in` = BufferedReader(InputStreamReader(urlConnection.inputStream))
                str = StreamReader().readLines(`in`)


            } catch (e: Exception) {
                e.printStackTrace()
            }

        return str
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
