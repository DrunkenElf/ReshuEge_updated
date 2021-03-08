package com.reshuege.DownloadTasks

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

import org.json.JSONException
import org.json.JSONObject

import com.reshuege.Protocol
import com.reshuege.SearchTaskResult


class OpenTask1(internal var context: Context, internal var subject_prefix: String, internal var taskNumber: String) {
    internal lateinit var progress: ProgressDialog
    internal var serverError = false
    val queue: RequestQueue = Volley.newRequestQueue(context)

    fun search() {
        initProgress("Загрузка задания")

        val listener = RequestQueue.RequestFinishedListener<JsonObjectRequest>() { request ->
            run {
                Log.d("urlFinished", request.url)
            }
        }
        queue.addRequestFinishedListener(listener)
        val url = getUrl("get_task&data=" + Integer.parseInt(taskNumber), 0)
        val request = JsonObjectRequest(Request.Method.GET, url, null,
                Response.Listener { response ->
                    try {
                        val task = response.getJSONObject("data")
                        onResult(task)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        progress.dismiss()
                        showMessage("Ошибка", "Задание не найдено.")
                    }
                }, Response.ErrorListener { error ->
            run {
                error.printStackTrace()
                serverError = true
                progress.dismiss()
                showMessage("Ошибка", "Задание не найдено.")
            }
        })
        queue.add(request)
    }

    fun onResult(task: JSONObject) {
        Log.d("RESULT1", task.toString())
        if (!serverError && task.toString() != "{}") {
            try {
                progress.dismiss()

                val intent = Intent(context, SearchTaskResult::class.java)
                intent.putExtra("body", task.getString("body"))
                var text = " "
                try {
                    text = task.getString("text")
                } catch (e: Exception) {

                }

                intent.putExtra("text", text)
                intent.putExtra("solution", task.getString("solution"))
                intent.putExtra("id", task.getInt("id").toString())
                intent.putExtra("task", task.getString("task"))
                intent.putExtra("type", task.getString("type"))

                if (Build.VERSION.SDK_INT > 20) {
                    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(context as AppCompatActivity)
                    context.startActivity(intent, options.toBundle())
                } else {
                    context.startActivity(intent)
                }


            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("myLogs", e.toString())
            }

        } else {
            progress.dismiss()
            showMessage("Ошибка", "Задание не найдено.")
        }
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

    fun initProgress(title: String) {
        progress = ProgressDialog(context)
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progress.setMessage(title)
        try {
            progress.show()
            progress.setCancelable(false)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getUrl(APIRequest: String, id: Int): String {
        if (id == 0)
            return "https://" + subject_prefix + "-ege.sdamgia.ru/api?type=" + APIRequest + "&" + Protocol.protocolVersion
        else
            return "https://" + subject_prefix + "-ege.sdamgia.ru/api?type=" + APIRequest + id + "&" + Protocol.protocolVersion
    }
}


