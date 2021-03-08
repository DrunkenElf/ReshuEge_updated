package com.reshuege.DownloadTasks

import android.app.ProgressDialog
import android.content.ContentValues
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

import com.reshuege.Adapters.SubjectThemesArrayAdapter
import com.reshuege.DataBase.QuestionsDataBaseHelper
import com.reshuege.Protocol
import com.reshuege.R
import com.reshuege.utils.StreamReader

class DownloadThemes(internal var context: Context, internal var subject_prefix: String, internal var adapter: SubjectThemesArrayAdapter) : AsyncTask<String, Int, ArrayList<JSONObject>>() {

    internal lateinit var progress: ProgressDialog
    internal var serverError = false


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

    override fun doInBackground(vararg strings: String): ArrayList<JSONObject>? {


        val themesArray = ArrayList<String>()
        val themesId = ArrayList<Int>()
        progress.setMessage("Загрузка списка тем")
        try {

            val themes = loadAPI("get_themes", 0)
            val themesJSON = JSONObject(themes)
            val array = themesJSON.getJSONArray("data")
            for (k in 0 until array.length()) {
                val jsonObject = array.getJSONObject(k)
                val name = jsonObject.getString("name")
                themesArray.add(name)
                var id: Int? = 0
                try {
                    id = array.getJSONObject(k).getInt("id")
                } catch (e: Exception) {

                } finally {
                    themesId.add(id!!)
                }
                var hasChilds = true
                var childsThemes: JSONArray? = null
                try {
                    childsThemes = array.getJSONObject(k).getJSONArray("childs")
                } catch (e: Exception) {
                    hasChilds = false
                }

                if (hasChilds) {
                    for (i in 0 until childsThemes!!.length()) {
                        val jObject = childsThemes!!.getJSONObject(i)
                        val child_name = "⚫ " + jObject.getString("name")
                        themesArray.add(child_name)
                        val child_id = jObject.getInt("id")
                        themesId.add(child_id)
                    }
                }
            }
            addThemesToDataBase(themesArray, themesId)
        } catch (e: Exception) {
            Log.d("myLogs", e.toString())
            serverError = true
        }

        return null
    }

    override fun onPostExecute(questions: ArrayList<JSONObject>?) {
        super.onPostExecute(questions)
        if (!serverError) {
            try {

                progress.dismiss()
                Toast.makeText(context, "Список тем загружен", Toast.LENGTH_SHORT).show()
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

    private fun addThemesToDataBase(themes: ArrayList<String>, themesId: ArrayList<Int>) {

        val qdbHelper = QuestionsDataBaseHelper(context, subject_prefix)
        val db = qdbHelper.writableDatabase
        if (qdbHelper.checkTable(subject_prefix + "_category_themes")) {
            qdbHelper.updateTableThemes(subject_prefix + "_category_themes")
        } else
            qdbHelper.addTableThemes(subject_prefix + "_category_themes")
        val cv = ContentValues()

        for (i in themes.indices) {
            val theme = themes[i]
            cv.put("theme_name", theme)

            val themeId = themesId[i]
            cv.put("theme_id", themeId)

            var childs = false
            if (themeId == 0) {
                childs = true
            }
            cv.put("childs", childs)

            val id = db.insert(subject_prefix + "_category_themes", null, cv)
            Log.d("myLogs", java.lang.Long.toString(id))
            cv.clear()

        }
        qdbHelper.close()

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
