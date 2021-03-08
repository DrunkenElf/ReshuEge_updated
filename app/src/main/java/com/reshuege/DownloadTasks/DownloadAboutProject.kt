package com.reshuege.DownloadTasks

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AlertDialog
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat

import org.json.JSONObject

import java.net.URL

import javax.net.ssl.HttpsURLConnection

import com.reshuege.AboutExamActivity
import com.reshuege.Protocol
import com.reshuege.R

class DownloadAboutProject(internal var context: Context) : AsyncTask<String, Int, String>() {
    internal lateinit var progress: ProgressDialog
    internal var serverError = false

    override fun onPreExecute() {
        progress = ProgressDialog(context)
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER)

        progress.setMessage("Загрузка информации о проекте")

        try {
            progress.show()
            progress.setCancelable(true)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun doInBackground(vararg strings: String): String? {

        try {
            val response = loadAPI("page&id=about")

            return JSONObject(response).getString("data")

        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("logs", e.toString())
            serverError = true
        }

        return null
    }

    override fun onPostExecute(manual: String?) {
        super.onPostExecute(manual)
        if (!serverError) {
            try {

                progress.dismiss()
                if (manual == null) {
                    Toast.makeText(context, "Произошла ошибка, попробуйте позже.", Toast.LENGTH_SHORT).show()
                } else {
                    val intent = Intent(context, AboutExamActivity::class.java)
                    intent.putExtra("manual", manual)
                    intent.putExtra("title", "О проекте")
                    if (Build.VERSION.SDK_INT > 20) {
                        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(context as AppCompatActivity)
                        context.startActivity(intent, options.toBundle())
                    } else {
                        context.startActivity(intent)
                    }
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

    fun loadAPI(APIRequest: String): String? {

        var str: String? = null
        var tryesCount = 0
        while (str == null && tryesCount != 10)
            try {
                tryesCount++
                val url = URL("https://ege.sdamgia.ru/api?type=" + APIRequest + "&" + Protocol.protocolVersion)
                val urlConnection = url.openConnection() as HttpsURLConnection

                str = String(urlConnection.inputStream.readBytes(), charset("UTF-8"))


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

}
