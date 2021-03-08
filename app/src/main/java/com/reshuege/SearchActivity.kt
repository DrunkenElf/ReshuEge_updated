package com.reshuege

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import android.transition.Slide
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import com.reshuege.DownloadTasks.*


class SearchActivity : AppCompatActivity(), View.OnClickListener {

    private var subject_prefix: String? = null
    private var section: String? = null
    private var searchButton: Button? = null
    private var searchEdit: EditText? = null
    private var type: Int = 0
    private var hint: String? = null
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
        super.onCreate(savedInstanceState)
        setupAnim()
        setContentView(R.layout.activity_search)

        subject_prefix = intent.getStringExtra("subject_prefix")
        section = intent.getStringExtra("section")
        hint = intent.getStringExtra("hint")
        type = intent.getIntExtra("type", 1)

        val subject = resources.getStringArray(R.array.subjects)
        val subjects = resources.getStringArray(R.array.subjects_prefix)
        for (i in subjects.indices) {
            if (subjects[i].contentEquals(subject_prefix!!)) {
                actionBar!!.setTitle(subject[i] + ". " + section)
            }
        }

        searchEdit = findViewById<View>(R.id.search_edit) as EditText
        searchEdit!!.hint = hint
        searchButton = findViewById<View>(R.id.searchButton) as Button
        searchButton!!.setOnClickListener(this)
    }

    override fun onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //this.finishAfterTransition()
            this.supportFinishAfterTransition()
        } else{
            this.finish()
        }
    }

    override fun onClick(v: View) {
        val checkbox = findViewById<View>(R.id.teacher_test) as CheckBox
        if (Connection.hasConnection(this)) {
            when (type) {
                1 -> {
                    val downloadVariant = DownloadVariant(this, subject_prefix!!, searchEdit!!.text.toString(), section!!, checkbox.isChecked)
                    downloadVariant.execute()
                }
                2 -> {
                    val openTask = OpenTask1(this, subject_prefix!!, searchEdit!!.text.toString())
                    openTask.search()
                }
                3 -> {
                    //val searchTask = SearchTask(this, subject_prefix!!, searchEdit!!.text.toString())
                    //searchTask.execute()
                    Searchtask1(this, subject_prefix!!, searchEdit!!.text.toString()).search()
                }
            }
        }
    }
}
