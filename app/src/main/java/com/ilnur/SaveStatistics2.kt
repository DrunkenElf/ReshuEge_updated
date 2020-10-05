package com.ilnur

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast

import org.json.JSONArray
import org.json.JSONObject

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.ArrayList

import javax.net.ssl.HttpsURLConnection


class SaveStatistics2(internal var context: Context, internal var subject_prefix: String, internal var query: String) : AsyncTask<String, Int, JSONArray>() {

    internal lateinit var progress: ProgressDialog
    internal var serverError = false

    override fun onPreExecute() {
        progress = ProgressDialog(context)
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER)

        progress.setMessage("Сохранение статистики")

        try {
            progress.show()
            progress.setCancelable(false)

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun doInBackground(vararg strings: String): JSONArray? {


        val questions = ArrayList<Int>()

        try {
            val performedQuery = query.toByteArray(charset("UTF-8"))
            query = String(performedQuery, charset("UTF-8"))
            val response = loadAPI("check_answer&", query)

            return JSONObject(response).getJSONArray("data")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("myLogs", e.toString())
            serverError = true
        }

        return null
    }

    override fun onPostExecute(results: JSONArray) {
        super.onPostExecute(results)
        if (!serverError) {
            try {

                progress.dismiss()
                if (results.length() == 0) {
                    Toast.makeText(context, "Произошла ошибка, повторите позже", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Статистика успешно сохранена", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
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
                val url: URL
                url = URL("https://" + subject_prefix + "-ege.sdamgia.ru/api?type=" + APIRequest + query + "&" + Protocol.protocolVersion)//URLEncoder.encode(query, "UTF-8"));
                val urlConnection = url.openConnection() as HttpsURLConnection
                val `in` = BufferedReader(InputStreamReader(urlConnection.inputStream))
                val sBuffer = StringBuffer("")
                /*var line = ""
                while (true) {
                    line = `in`.readline()
                    if (line != null)
                        sbuffer.append(line + "\n")
                    else
                        break
                }*/
                /*for (line in `in`.lines()){
                    sBuffer.append(line)
                }*/
                str = `in`.use{BufferedReader::readText}.toString()

                `in`.close()
                //str = sBuffer.toString()


            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("myLogs", "$APIRequest $query $e")
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
