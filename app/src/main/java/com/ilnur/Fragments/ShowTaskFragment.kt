package com.ilnur.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.TextView

import com.ilnur.R

class ShowTaskFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_show_task, container, false)
    }

    fun setTask(task: String, questionNumber: Int) {
        try {

            val line = view!!.findViewById<View>(R.id.line) as TextView
            line.text = Integer.toString(questionNumber + 1)

            val showWV = view!!.findViewById<View>(R.id.taskWebView) as WebView
            showWV.settings.builtInZoomControls = true
            showWV.settings.displayZoomControls = false
            showWV.settings.domStorageEnabled = true
            showWV.settings.javaScriptEnabled = true
            showWV.settings.setAppCacheEnabled(true)
            showWV.settings.loadsImagesAutomatically = true
            showWV.settings.allowFileAccessFromFileURLs = true
            showWV.loadDataWithBaseURL(null, task, "text/html", "utf-8", null)


        } catch (e: Exception) {
            Log.d("myLogs", e.toString())
        }

    }
}
