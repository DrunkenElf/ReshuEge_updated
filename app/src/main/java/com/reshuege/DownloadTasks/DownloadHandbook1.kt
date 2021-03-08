package com.reshuege.DownloadTasks

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.preference.PreferenceManager
import android.util.Log


import androidx.appcompat.app.AlertDialog

import com.google.gson.Gson
import com.google.gson.stream.JsonReader

import org.jsoup.Connection
import org.jsoup.Jsoup

import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.StringReader

import com.reshuege.Adapters.SubjectTheoryArrayAdapter
import com.reshuege.DataBase.MyDB
import com.reshuege.Json.Root
import com.reshuege.Json.Subject
import com.reshuege.R

class DownloadHandbook1(internal var context: Context, internal var subject_prefix: String, private val adapter: SubjectTheoryArrayAdapter) : AsyncTask<Void, Void, Root>() {
    internal lateinit var progress: ProgressDialog
    internal var serverError = false
    internal var download_pictures: Boolean = false
    private var isAdding = false

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

    override fun doInBackground(vararg voids: Void): Root? {
        progress.setMessage("Загрузка дерева тем")

        try {
            download()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    override fun onPostExecute(root: Root?) {
        super.onPostExecute(root)
        if (!isAdding) {
            progress.dismiss()
            val ad = AlertDialog.Builder(context)
            ad.setTitle("Раздел закрыт")
            ad.setMessage("Справочник по данному предмету находится в разработке")

            ad.setNegativeButton("Ок") { dialog, arg1 -> }
            ad.setCancelable(true)
            ad.setOnCancelListener { }

            ad.show()
            //adapter.notifyDataSetChanged();
        } else if (!serverError) {
            progress.dismiss()
            adapter.notifyDataSetChanged()
        } else {
            progress.dismiss()
            showMessage("Сервер РЕШУ ЕГЭ временно недоступен.", context.getString(R.string.error))
        }
    }

    @Throws(IOException::class)
    private fun download() {
        val link = "https://ege.sdamgia.ru/mobile_handbook/"
        var resp: Connection.Response
        var bis: BufferedInputStream?
        var isr: InputStreamReader
        var reader: JsonReader
        var root: Root

        try {
            val json = Jsoup.connect("https://ege.sdamgia.ru/mobile_handbook/subjects.json")
                    .ignoreContentType(true).get().select("body").text()

            val sr = StringReader(json)
            reader = JsonReader(sr)
            reader.isLenient = true
            val subjects = Gson().fromJson<Array<Subject>>(reader, Array<Subject>::class.java)
            for (s in subjects) {
                if (s.href == "$subject_prefix.json") {
                    isAdding = true
                    if (subject_prefix == "phys") {
                        Log.d("link", link + "phys.json?v=2")
                        resp = Jsoup.connect(link + "phys.json?v=2")
                                .ignoreContentType(true).maxBodySize(0).timeout(20000000).method(Connection.Method.GET).execute()
                        bis = resp.bodyStream()
                        isr = InputStreamReader(bis)
                    } else {
                        Log.d("link", "$link$subject_prefix.json?v=2")
                        resp = Jsoup.connect("$link$subject_prefix.json?v=2")
                                .ignoreContentType(true).maxBodySize(0).timeout(20000000).method(Connection.Method.GET).execute()
                        bis = resp.bodyStream()
                        isr = InputStreamReader(bis)
                    }
                    if (bis == null)
                        Log.d("bis", "null")

                    reader = JsonReader(isr)
                    reader.isLenient = true

                    root = Gson().fromJson(reader, Root::class.java)
                    bis?.close()
                    isr.close()
                    reader.close()
                    MyDB.updateDB(root)
                }
            }


        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

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
