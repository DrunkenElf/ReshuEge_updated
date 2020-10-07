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

class AboutExamActivity : AppCompatActivity() {
    private fun setupAnim() {
        if (Build.VERSION.SDK_INT >= 21) {
            with(window) {
                requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
                val toRight = Slide()
                toRight.slideEdge = Gravity.RIGHT
                toRight.duration = 400

                val toLeft = Slide()
                toLeft.slideEdge = Gravity.LEFT
                toLeft.duration = 400

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
        super.onCreate(savedInstanceState)
        setupAnim()
        setContentView(R.layout.activity_about_exam)

        val manual = intent.getStringExtra("manual")
        val title = intent.getStringExtra("title")

        supportActionBar?.title = title

        showTheory(manual.toString())
    }

    private fun showTheory(theoryText: String) {
        val showWV = findViewById<View>(R.id.manualView) as WebView
        showWV.settings.builtInZoomControls = true
        showWV.settings.displayZoomControls = false
        showWV.loadDataWithBaseURL(null, theoryText, "text/html", "utf-8", null)
    }
}
