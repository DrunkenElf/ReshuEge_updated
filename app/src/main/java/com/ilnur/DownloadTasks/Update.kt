package com.ilnur.DownloadTasks

import android.app.ActivityOptions
import androidx.appcompat.app.AppCompatActivity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AlertDialog
import android.util.Log
import androidx.core.app.ActivityOptionsCompat
import com.ilnur.LoginActivity
import com.ilnur.MainMenu

import org.json.JSONObject

import java.net.URL

import javax.net.ssl.HttpsURLConnection

import com.ilnur.Protocol
import com.ilnur.R
import com.ilnur.Session.Settings
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class Update(internal var context: Context, var isLogin: Boolean) {

    internal lateinit var progress: ProgressDialog
    internal lateinit var subject_prefix: Array<String>
    internal var counter = 0

    internal var serverError = false

     fun onPreExecute() {
        subject_prefix = context.resources.getStringArray(R.array.subjects_prefix)
        progress = ProgressDialog(context)
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER)

        progress.setMessage("Проверка обновлений")

        try {

            progress.show()
            progress.setCancelable(false)

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    fun load(ind: Int): pair?{
        try {
            var testID = loadAPI("predefined_tests", subject_prefix[ind])
            val id = JSONObject(testID)
            testID = id.getString("data")
            //predefined_variant[i] = testID
            counter++
            return  pair(ind,testID)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("myLogs", javaClass.getName() + " " + e.toString())
            serverError = true
            return null
        }
    }

    class pair(var ind: Int, var id: String)

    fun doInBackground() {
        onPreExecute()
        counter = 0
        val predefined_variant = arrayOfNulls<String>(subject_prefix.size)

        Log.d("UPDATE", "STARTE")
        val vals = Observable.range(0, subject_prefix.size) as Observable<Int>
        val disposable = vals.flatMap { mapper ->
            Observable.just(mapper).subscribeOn(Schedulers.io())
                    .map { i -> load(i) }
        }
                .observeOn(AndroidSchedulers.from(context.mainLooper, true))
                //.retryWhen(error -> {})
                .retry(2)
                .subscribe(
                        { result ->
                            run {
                                if (result != null)
                                    predefined_variant[result.ind] = result.id
                            }
                        },
                        { error -> Log.d("ERROR", error.message.toString())},
                        { onPostExecute(predefined_variant) }
                )

       /* for (i in subject_prefix.indices) {
            //val thread = Thread(Runnable {
                try {
                    var testID = loadAPI("predefined_tests", subject_prefix[i])
                    val id = JSONObject(testID)
                    testID = id.getString("data")
                    predefined_variant[i] = testID

                    counter++
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d("myLogs", javaClass.getName() + " " + e.toString())
                    serverError = true
                }
            //})
            //thread.start()


        }*/

        /*while (counter <= 14 && !serverError) {
            try {
                Log.i("COUNTER", "" + counter)
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

        }*/

    }


     fun onPostExecute(predefined_variant: Array<String?>) {
        if (!serverError) {
            val verPref = context.getSharedPreferences("latest_version_tests", AppCompatActivity.MODE_PRIVATE)

            for (i in subject_prefix.indices) {
                try {

                    val ed = verPref.edit()
                    ed.putString(subject_prefix[i], predefined_variant[i])
                    Log.d("myLogs", subject_prefix[i] + " = " + predefined_variant[i])
                    ed.apply()

                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d("myLogs", e.toString())
                }

            }
            progress.dismiss()
        } else {
            progress.dismiss()
            showMessage("Сервер РЕШУ ЕГЭ временно недоступен.", context.getString(R.string.error))
        }

        if (isLogin){
            val settings = Settings()
            if (!settings.getFirstStartFlag(context)) {
                // Construct the LicenseCheckerCallback. The library calls this when done.
                Log.d("HASconn", "update")
                settings.setFirstStartFlag(true, context)
            }
            val intent = Intent(context, MainMenu::class.java)
            if (Build.VERSION.SDK_INT > 20) {
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(context as AppCompatActivity)
                context.startActivity(intent, options.toBundle())
            } else {
                context.startActivity(intent)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                (context as LoginActivity).finishAfterTransition()
            } else
                (context as LoginActivity).finish()
        }
    }

    fun loadAPI(APIRequest: String, subject_prefix: String): String? {

        var str: String? = null
        var triesCount = 0
        var urlConnection: HttpsURLConnection? = null
        try {
            triesCount++
            Log.d("myLogs", "loadApi $subject_prefix")
            val url = URL("https://" + subject_prefix + "-ege.sdamgia.ru/api?type=" + APIRequest + "&" + Protocol.protocolVersion)
            urlConnection = url.openConnection() as HttpsURLConnection
            //val `in` = BufferedReader(InputStreamReader(urlConnection.inputStream))
            str = urlConnection.inputStream.bufferedReader().readText()

        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("myLogs", "loadApi$e")
        } finally {
            if (urlConnection!=null) {
                urlConnection.inputStream.close()
                urlConnection.disconnect()
            }
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
