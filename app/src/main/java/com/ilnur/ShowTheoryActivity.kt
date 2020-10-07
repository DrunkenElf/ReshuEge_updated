package com.ilnur

import android.app.Activity
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.Slide
import android.view.Gravity
import android.view.View
import android.view.Window
import android.webkit.WebView
import com.ilnur.DataBase.MyDB

//import com.ilnur.DataBase.MyDB1

class ShowTheoryActivity : AppCompatActivity() {
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
        setContentView(R.layout.activity_show_theory)
        val subj_pref = intent.getStringExtra("subj_pref")
        val id = intent.getStringExtra("id")
        //Log.d("myLogs", theoryText);
        showTheory(subj_pref.toString(), id!!)
    }

    private fun showTheory(table: String, id: String) {
        val data = MyDB.getTemasData(table, id)
        val showWV = findViewById<View>(R.id.theoryView) as WebView
        showWV.settings.builtInZoomControls = true
        showWV.settings.displayZoomControls = false
        showWV.loadDataWithBaseURL(null, data, "text/html", "utf-8", null)
    }


}
