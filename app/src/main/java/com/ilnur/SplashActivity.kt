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
import com.ilnur.Session.Session
import com.ilnur.Session.SessionState
import com.ilnur.Session.Settings
import kotlinx.coroutines.*
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.coroutines.CoroutineContext

public class SplashActivity : AppCompatActivity(), CoroutineScope {
    lateinit var user: User

    private lateinit var mJob: Job
    override val coroutineContext: CoroutineContext
        get() = mJob + Dispatchers.Main

    private fun setupAnim() {
        if (Build.VERSION.SDK_INT >= 21) {
            with(window) {
                requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
                val toRight = Slide()
                toRight.slideEdge = Gravity.RIGHT
                toRight.duration = 300

                val toLeft = Slide()
                toLeft.slideEdge = Gravity.LEFT
                toLeft.duration = 300

                //когда переходишь на новую
                exitTransition = toRight
                enterTransition = toRight
                allowEnterTransitionOverlap = true
                allowReturnTransitionOverlap = true

                //когда нажимаешь с другого назад и открываешь со старого
                returnTransition = toRight
                reenterTransition = toRight
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        //setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        //setupAnim()
        setContentView(R.layout.activity_splash)
        mJob = Job()
        val handler = Handler()

        user = (application as Reshuege).get_user()

        if (user.login == null || user.password == null) {
            val intent = Intent(this, LoginActivity::class.java)
            val runnable = Runnable {
                if (Build.VERSION.SDK_INT > 20) {
                    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this)
                    startActivity(intent, options.toBundle())
                } else {
                    startActivity(intent)
                }
                Log.d("splash noUser", "log or pas null")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    supportFinishAfterTransition()
                } else
                    finish()
            }
            handler.postDelayed(runnable, 2000)
        } else {
            /*
             * проверить на логирование
             * ЕСЛИ тру - продолжить, фолс - написать причину в тосте и продолжить
             * */
            if (user.session_id != null && user.login != null && user.password != null) {
                Log.d("apCont", applicationContext.toString())
                val intent = Intent(applicationContext, MainMenu::class.java)
                val runnable = Runnable {
                    if (Build.VERSION.SDK_INT > 20) {
                        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this)
                        Log.d("actOpt", options.toString())
                        startActivity(intent, options.toBundle())
                    } else {
                        startActivity(intent)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        supportFinishAfterTransition()
                    } else
                        finish()
                }
                handler.postDelayed(runnable, 2000)
            } else if (Connection.hasConnection(this)) {
                Log.d("main hascon", "has connection")

                launch(Dispatchers.Main) {
                        Log.d("before", "result")
                        //showSomeData(user.login!!, user.password!!)
                        //val result = checkLogin(user.login!!, user.password!!)
                    showSomeData(user.login!!, user.password!!)
                        Log.d("after", "result")

                       // onResponse(result)
                }
            } else {
                Log.d("main noCon", "no connection")
                val sessionObject = Session("", SessionState.anonymus)
                val settings = Settings()
                settings.setSession(sessionObject, applicationContext)
                settings.setLoginAndPassword("", "", applicationContext)
                //Toast.makeText(applicationContext, "Для авторизации необобходимо подключение к интернету", Toast.LENGTH_SHORT).show()
                val intent = Intent(applicationContext, LoginActivity::class.java)
                val runnable = Runnable {
                    if (Build.VERSION.SDK_INT > 20) {
                        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this)
                        startActivity(intent, options.toBundle())
                    } else {
                        startActivity(intent)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        supportFinishAfterTransition()
                    } else
                        finish()
                }
                handler.postDelayed(runnable, 2000)
            }
        }
    }



    override fun onDestroy() {
        Log.d("dest", "tyes")
        super.onDestroy()
    }

    /*fun checkLogin(mEmail: String, mPassword: String) : Deferred<Resp> = async {
         execLogin(mEmail, mPassword)
    }*/
    suspend fun showSomeData(mEmail: String, mPassword: String) = coroutineScope {
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
    }
}