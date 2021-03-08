package com.reshuege

import android.content.Context
import android.os.AsyncTask
import androidx.appcompat.app.AlertDialog
import android.util.Log
import com.reshuege.backend.ApiRequestsImp

import org.json.JSONObject

import java.net.URL
import java.util.Date

import javax.net.ssl.HttpsURLConnection

import com.reshuege.utils.StreamReader
import kotlinx.coroutines.*
import retrofit2.awaitResponse
import java.io.*
import javax.inject.Inject

class SubjInfo(var context: Context? = null) {

    private val filename = "subjects_info"
    private var mTask: OnlineSubjInfo? = null
    private var serverError = false

    @Inject
    lateinit var apiRequests: ApiRequestsImp

    val _subj_data: JSONObject?
        get() {
            if (subj_data == null)
                check_subject_data()
            return subj_data
        }

    fun check_subject_data() {
        if (!this.load_file()) {
            Log.d("LOAD_ONLINE", "YEP")
            this.load_online()
        }
    }

    private fun load_file(): Boolean {
        try {
            val inputStream = context!!.openFileInput(this.filename)

            val buffer = ByteArray(8192) // Такого вот размера буфер
            // Далее, например, вот так читаем ответ
            var bytesRead: Int
            var data: ByteArray? = null
            val baos = ByteArrayOutputStream()
            while (inputStream.read(buffer).let { bytesRead = it; it != -1 }) {
                baos.write(buffer, 0, bytesRead)
            }
            data = baos.toByteArray()

            subj_data = JSONObject(String(data, charset("UTF-8")))
            var stamp = subj_data!!.getLong("stamp")
            stamp *= 1000
            val time = Date().time
            if (time - stamp > 1000 * 60 * 60 * 24) {
                Log.d("myLogs", "subjinfo is_old")
                return false
            }
            return true
        } catch (e: Exception) {
            Log.d("ERROR_info_subj", "subjinfo load fails")
            Log.d("ERROR_info_subj", e.toString())
            return false
        }

    }

    private fun load_online(): Boolean {
        CoroutineScope(Dispatchers.IO).launch {
            getInfo()
        }
        return true
    }

    suspend fun getInfo() = coroutineScope {
        //val url = URL("https://ege.sdamgia.ru/api?type=subjs_info" + "&" + Protocol.protocolVersion)
        val res = apiRequests.getInfo(
            "https://ege.sdamgia.ru/api?type=subjs_info" +
                    "&" + Protocol.protocolVersion
        ).awaitResponse()
        //res.body()?.byteStream()?.bufferedReader().use { it?.readText() }
        res.body()?.byteStream().let { body ->
            FileOutputStream(
                File(
                    context!!.filesDir.absolutePath,
                    filename
                )
            ).use {
                    body?.copyTo(it)
                }

        }
        return@coroutineScope
    }


    inner class OnlineSubjInfo : AsyncTask<Void, Void, String>() {

        override fun doInBackground(vararg params: Void): String? {
            // TODO: attempt authentication against a network service.

            Log.d("SubjInfoTask", "get subj data")
            try {
                val data: ByteArray? = null
                val `is`: InputStream? = null
                val parameters = Protocol.protocolVersion
                val url =
                    URL("https://ege.sdamgia.ru/api?type=subjs_info" + "&" + Protocol.protocolVersion)
                val urlConnection = url.openConnection() as HttpsURLConnection

                val str: String?
                val `in` = BufferedReader(InputStreamReader(urlConnection.inputStream))
                str = StreamReader().readLines(`in`)

                Log.d("DOWNLOADED_info", str.toString())
                return str

            } catch (e: Exception) {
                Log.d("myLogs", "subjinfo fail load")
                Log.d("myLogs", e.toString())
                serverError = true
                return ""
            }

        }

        override fun onPostExecute(session: String?) {
            mTask = null

            if (!Connection.hasConnectionMain(context!!, false)) {

            } else if (session != "") {
                try {
                    subj_data = JSONObject(session)
                    Log.d("myLogs", "subjinfo done")
                    val outputStream: FileOutputStream

                    Log.d("myLogs", "subjinfo writing file")
                    outputStream = context!!.openFileOutput(filename, Context.MODE_PRIVATE)
                    outputStream.write(session?.toByteArray())
                    outputStream.close()
                } catch (e: Exception) {
                    Log.d("myLogs", "subjinfo fail file")
                    Log.d("myLogs", e.toString())
                }

            } else {
                showMessage("Сервер РЕШУ ЕГЭ временно недоступен..", "")
            }
            Log.d("SubjInfoTask", "task done")
        }

        override fun onCancelled() {
            mTask = null
        }

        fun showMessage(title: String, message: String) {

            val ad = AlertDialog.Builder(context!!)
            ad.setTitle(title)
            ad.setMessage(message)

            ad.setPositiveButton("Продолжить") { dialog, arg1 -> }

            ad.setCancelable(true)
            ad.setOnCancelListener { }

            ad.show()
        }
    }

    companion object {


        private var subj_data: JSONObject? = null
    }
}