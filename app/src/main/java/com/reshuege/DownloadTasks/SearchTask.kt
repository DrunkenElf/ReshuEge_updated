package com.reshuege.DownloadTasks

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AlertDialog
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.JsonObjectRequest

import java.net.URLEncoder
import java.util.ArrayList

import com.reshuege.Protocol
import com.reshuege.R
import com.reshuege.SearchResultActivity
import org.json.JSONException

class Searchtask1(internal var context: Context, internal var subject_prefix: String, internal var query: String) {
    internal lateinit var progress: ProgressDialog
    internal var serverError = false
    val requestQueue: RequestQueue

    init {
        val cache = DiskBasedCache(context.cacheDir, 1024 * 1024) // 1MB cap
        val network = BasicNetwork(HurlStack())
        requestQueue = RequestQueue(cache, network).apply {
            start()
        }
    }

    fun search() {
        Log.d("SEARCH", query)
        progress = ProgressDialog(context)
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER)

        progress.setMessage("Поиск заданий")

        try {
            progress.show()
            progress.setCancelable(false)

        } catch (e: Exception) {
            e.printStackTrace()
        }

        val questions = ArrayList<Int>()

        val performedQuery = query.toByteArray(charset("UTF-8"))
        query = String(performedQuery, charset("UTF-8"))

        val url = getUrl("search&query=", query)
        Log.d("URL", url)
        val request = JsonObjectRequest(Request.Method.GET, url, null,
                Response.Listener { response ->
                    try {
                        Log.d("RESPONSE", "assd")
                        val taskNumbers = response.getJSONArray("data")
                        for (i in 0 until taskNumbers.length()) {
                            questions.add(taskNumbers.getInt(i))
                        }
                        onResult(questions)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        serverError = true
                        progress.dismiss()
                    }

                },
                Response.ErrorListener { error ->
                    {
                        error.printStackTrace()
                        serverError = true
                        progress.dismiss()
                    }
                }
        )
        requestQueue.add(request)
    }

    fun onResult(questions: ArrayList<Int>){
        if (!serverError) {
            try {

                var result = arrayOfNulls<Int>(questions.size)
                result = questions.toTypedArray<Int?>()

                progress.dismiss()
                if (result.size == 0) {
                    Toast.makeText(context, "Ничего не найдено", Toast.LENGTH_SHORT).show()
                } else {
                    val array = IntArray(result.size)
                    for (i in result.indices) {
                        array[i] = result[i]!!
                    }
                    val intent = Intent(context, SearchResultActivity::class.java)
                    intent.putExtra("results", array)
                    intent.putExtra("subject_prefix", subject_prefix)
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

    fun getUrl(APIRequest: String, query: String): String {
        return "https://" + subject_prefix + "-ege.sdamgia.ru/api?type=" + APIRequest + URLEncoder.encode(query, "UTF-8") + "&" + Protocol.protocolVersion
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
