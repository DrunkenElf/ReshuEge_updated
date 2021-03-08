package com.reshuege

import android.os.Build
import android.os.Bundle
import android.transition.Slide
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.webkit.WebView
import android.widget.Button

class SearchTaskResult : AppCompatActivity(), View.OnClickListener {

    internal lateinit var body: String
    internal lateinit var solution: String
    internal lateinit var task: String
    internal lateinit var type: String
    internal lateinit var id: String
    internal lateinit var text: String
    internal var theoryText: String? = null
    internal lateinit var showSearched: Button
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
        theoryText = intent.getStringExtra("theory_text")
        showSearched = findViewById<View>(R.id.showSearched) as Button

        if (theoryText == null) {
            body = intent.getStringExtra("body").toString()
            solution = intent.getStringExtra("solution").toString()
            id = intent.getStringExtra("id").toString()
            task = intent.getStringExtra("task").toString()
            type = intent.getStringExtra("type").toString()
            text = intent.getStringExtra("text").toString()

            theoryText = "<b>Задание № " + id + ". Часть " + (if (type.contentEquals("2")) "B" else "C") + ". Тип " + task + "</b><p>" +
                    body

            if (!text.contentEquals("null") && !text.contentEquals(" "))
                theoryText += "<b>Текст</b><p>$text"
            showSearched.setOnClickListener(this)
            showSearched.visibility = View.VISIBLE
        }
        Log.d("myLogs", theoryText!!)

        showTheory(theoryText)
    }

    private fun showTheory(theoryText: String?) {
        val showWV = findViewById<View>(R.id.theoryView) as WebView
        showWV.settings.builtInZoomControls = true
        showWV.settings.displayZoomControls = false
        showWV.loadDataWithBaseURL(null, theoryText.toString(), "text/html", "utf-8", null)
    }


    override fun onClick(v: View) {
        if (showSearched.text.toString().contentEquals("Решение")) {
            showTheory(solution)
            showSearched.text = "Назад"
        } else if (showSearched.text.toString().contentEquals("Назад")) {
            showTheory(theoryText)
            showSearched.text = "Решение"
        }
    }
}
