package com.reshuege

import android.os.Build
import android.os.Bundle
import android.transition.Slide
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.webkit.*
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

import com.bumptech.glide.Glide

class TeacherActivity : AppCompatActivity() {
    private var pref: String? = null
    private var web: WebView? = null
    private var url: String? = null
    private var manager: CookieManager? = null
    private lateinit var cookies: MutableMap<String, String>
    private lateinit var webClient: com.reshuege.WebViewClient

    private fun setupAnim() {
        if (Build.VERSION.SDK_INT >= 21) {
            with(window) {
                requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
                val toRight = Slide()
                toRight.slideEdge = Gravity.RIGHT
                toRight.duration = 300

                val toLeft = Slide()
                toLeft.slideEdge = Gravity.LEFT
                toLeft.duration = 300

                //когда переходишь на новую
                exitTransition = toRight
                enterTransition = toRight

                //когда нажимаешь с другого назад и открываешь со старого
                returnTransition = toRight
                reenterTransition = toRight
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupAnim()
        setContentView(R.layout.activity_teacher)


        Glide.with(this)
                .load(R.drawable.ball)
                .timeout(100)
                .into(findViewById<View>(R.id.gif_load) as ImageView)

        web = findViewById(R.id.teacher)
        pref = intent.getStringExtra("pref")
        //cont = this;
        url = intent.getStringExtra("url")
        cookies = listToMap(intent.getStringArrayListExtra("keySet")!!, intent.getStringArrayListExtra("values")!!)

        if (cookies == null) {
            showMessage("Что-то не так", "Попробуйте зайти снова")
            finish()
        }



        val settings = web!!.settings
        settings.javaScriptEnabled = true
        settings.setAppCacheEnabled(true)
        settings.databaseEnabled = true
        settings.domStorageEnabled = true
        settings.saveFormData = true
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS
        settings.setSupportMultipleWindows(true)
        settings.allowContentAccess = true
        settings.allowFileAccess = true
        settings.allowFileAccessFromFileURLs = true
        settings.allowUniversalAccessFromFileURLs = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                settings.disabledActionModeMenuItems = WebSettings.MENU_ITEM_NONE
            }
        }

        settings.loadWithOverviewMode = false
        settings.useWideViewPort = false
        settings.userAgentString = "Mozilla/5.0 (Linux; Android 9; Pixel 2 XL Build/OPD1.170816.004)" + " AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/80.0.3987.149 Mobile Safari/537.36"
        settings.pluginState = WebSettings.PluginState.ON
        settings.loadsImagesAutomatically = true


        val syncManager = CookieSyncManager.createInstance(web!!.context)
        manager = CookieManager.getInstance()
        manager!!.setAcceptCookie(true)
        manager!!.removeSessionCookie()
        Log.d("syncMan", syncManager.toString())
        val gif = findViewById<ImageView>(R.id.gif_load)
        webClient = WebViewClient(web, gif, cookies, this)
        web!!.webViewClient = webClient
        //web!!.webChromeClient = WebChromeClient()

        for ((key, value) in cookies!!) {
            manager!!.setCookie(url,
                    "$key=$value; ")
        }
        CookieSyncManager.getInstance().sync()
        if (!webClient.isFinished)
            web!!.loadUrl(url.toString(), cookies)

    }

    override fun onDestroy() {
        webClient.dismissProgressDialog()
        super.onDestroy()
    }




    fun listToMap(keys: List<String>, values: List<String>): MutableMap<String, String>{
        val map: MutableMap<String, String> = mutableMapOf()
        val i = keys.iterator()
        val j = values.iterator()

        while (i.hasNext() || j.hasNext()) map.put(i.next(), j.next())
        return map
    }



    override fun onBackPressed() {
        if (web!!.canGoBack()) {
            web!!.goBack()
        } else {
            //super.onBackPressed()
            this.supportFinishAfterTransition()
        }
    }

    fun showMessage(title: String, message: String) {
        val ad = AlertDialog.Builder(this)
        ad.setTitle(title)
        ad.setMessage(message)

        ad.setNegativeButton("Да") { dialog, arg1 -> }


        ad.setCancelable(true)
        ad.setOnCancelListener { }

        ad.show()
    }
}
