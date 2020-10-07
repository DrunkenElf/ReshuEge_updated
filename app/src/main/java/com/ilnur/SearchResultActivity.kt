package com.ilnur

import android.app.ListActivity
import android.os.Build
import android.os.Bundle
import android.transition.Slide
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

import com.ilnur.Adapters.SearchResultsAdapter

import com.ilnur.DownloadTasks.OpenTask1

class SearchResultActivity : AppCompatActivity() {

    internal lateinit var subject_prefix: String
    internal lateinit var results: IntArray
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
        setContentView(R.layout.activity_search_results)
        val lv = findViewById<ListView>(R.id.list_results)
        results = intent.getIntArrayExtra("results")!!
        subject_prefix = intent.getStringExtra("subject_prefix").toString()
        val resultsString = arrayOfNulls<String?>(results.size)
        for (i in results.indices) {
            resultsString[i] = results[i].toString()
        }
        val adapter = SearchResultsAdapter(this, resultsString)
        lv.adapter = adapter
        lv.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            if (Connection.hasConnection(this)) {
                val openTask = OpenTask1(this, subject_prefix, results[position].toString())
                openTask.search()
            }
        }
    }


}
