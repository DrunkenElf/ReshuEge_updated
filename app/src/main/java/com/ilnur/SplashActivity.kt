package com.ilnur

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.transition.Slide
import android.util.Log
import android.view.Gravity
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.ilnur.DataBase.AppDatabase
import com.ilnur.DataBase.User
import com.ilnur.Session.Session
import com.ilnur.Session.SessionState
import com.ilnur.Session.Settings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import javax.inject.Inject
import javax.net.ssl.HttpsURLConnection
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
 class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var db: AppDatabase

    //@Inject lateinit var settings: SettingsImp

    fun isLogged() {
        //settings.isLogged.asLiveData().observe()
        lifecycleScope.launch(Dispatchers.IO){
            _user.postValue(db.userDao().getUserList().firstOrNull())

        }
        user.observe(this) {
            val tmp = it
            if (tmp == null) {
                startLoginAct()
            } else if (tmp.session_id != null && tmp.password != null && tmp.logged) {
                Log.d("Used data exist", tmp.toString())
                //start main
                startMainActivity(tmp)
            } else
                startLoginAct()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        isLogged()
    }


    private fun startMainActivity(user: User){
        Log.d("startMain", user.toString())
        val startupIntent = Intent(this, MainMenu::class.java)
        startActivity(startupIntent)
        this.finish()
    }

    private fun startLoginAct() {
        Log.d("startLogin", "no user")
        val startupIntent = Intent(this, LoginActivity::class.java)
        startActivity(startupIntent)
        this.finish()
    }

    private val _user = MutableLiveData<User?>()

    val user: LiveData<User?> get() = _user


    /*fun checkLogin(mEmail: String, mPassword: String) : Deferred<Resp> = async {
         execLogin(mEmail, mPassword)
    }*/
    /*suspend fun showSomeData(mEmail: String, mPassword: String) = coroutineScope {
        val data = async(Dispatchers.IO) { // <- extension on current scope
            execLogin(mEmail, mPassword)
               }

        withContext(Dispatchers.Main){
            val res = data.await()
            onResponse(res)
        }
    }

    suspend fun checkLogin(mEmail: String, mPassword: String): Resp {
        return withContext(Dispatchers.IO) {
            execLogin(mEmail, mPassword)
        }
    }

    data class Resp(
            val serverErro: Boolean,
            val result: String
    )

    private fun onResponse(res: Resp) {
        Log.d("onResp", res.result)
        val handler = Handler()
        val runnable = Runnable {
            if (res.result != "") {
                val sessionObject = Session(res.result, SessionState.authorized)
                val settings = Settings()
                Log.d("SESSION:", res.result)
                settings.setSession(sessionObject, applicationContext)
                Log.d("apCont", applicationContext.toString())

                settings.setLoginAndPassword(user.login!!, user.password!!, applicationContext)
                val intent = Intent(applicationContext, MainMenu::class.java)

                if (Build.VERSION.SDK_INT > 20) {
                    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this)
                    startActivity(intent, options.toBundle())
                } else {
                    startActivity(intent)
                }


                supportFinishAfterTransition()
            } else {
                if (res.serverErro)
                    Toast.makeText(applicationContext, "Что-то пошло не так", Toast.LENGTH_SHORT).show()

                val intent = Intent(applicationContext, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                if (Build.VERSION.SDK_INT > 20) {
                    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this)
                    startActivity(intent, options.toBundle())
                } else {
                    startActivity(intent)
                }



                supportFinishAfterTransition()
            }
        }
        handler.postDelayed(runnable, 2000)
    }

    private fun execLogin(mEmail: String?, mPassword: String?): Resp {
        try {
            var data: ByteArray? = null
            var `is`: InputStream? = null
            val parameters = "user=" + mEmail + "&password=" + mPassword + "&" + Protocol.protocolVersion
            val url = URL("https://ege.sdamgia.ru/api?type=login")

            val urlConnection = url.openConnection() as HttpsURLConnection
            urlConnection.requestMethod = "POST"
            urlConnection.doInput = true
            urlConnection.doOutput = true
            urlConnection.setRequestProperty("Content-Length", "" + Integer.toString(parameters.toByteArray().size))
            val os = urlConnection.outputStream
            data = parameters.toByteArray(charset("UTF-8"))
            os.write(data)
            data = null
            urlConnection.connect()
            val responseCode = urlConnection.responseCode

            val baos = ByteArrayOutputStream()
            var str: String? = null
            if (responseCode == 200) {
                val inputstream = urlConnection.inputStream

                data = inputstream.readBytes()
                str = String(data, charset("UTF-8"))
            } else {
            }

            val jObject = JSONObject(str)
            try {
                val jObject2 = jObject.getJSONObject("data")
                return Resp(false, jObject2.getString("session"))
            } catch (e: Exception) {
                val jObject2 = jObject.getString("error")
                return Resp(false, "")
            }

        } catch (ioe: IOException) {
            Log.i("errorLOG", ioe.message)
            return Resp(true, "")
        } catch (jse: JSONException) {
            Log.i("errorLOG", jse.message)
            return Resp(false, "")
        } catch (np: NullPointerException) {
            return Resp(false, "")
        }
    }*/
}