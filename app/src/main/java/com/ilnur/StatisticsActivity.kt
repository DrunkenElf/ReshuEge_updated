package com.ilnur

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.transition.Slide
import androidx.appcompat.app.AlertDialog
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton

import java.util.ArrayList

class StatisticsActivity : AppCompatActivity() {

    internal var question_number = ArrayList<String>()
    internal var solved = ArrayList<Int>()
    internal var rightSolved = ArrayList<Int>()
    internal var percents = ArrayList<Int>()
    internal lateinit var subject_name: String
    internal lateinit var context: Context
    //private InterstitialAd interstitial;
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
        setContentView(R.layout.activity_statistics)
        context = this

        getIntentExtras()
        initializeTable()

        supportActionBar!!.title = "$subject_name. Статистика"

        val buttonContinue = findViewById<View>(R.id.buttonContinue) as AppCompatButton
        buttonContinue.setOnClickListener { onBackPressed() }
    }

    fun getIntentExtras() {
        val intent = intent
        question_number = intent.getStringArrayListExtra("question_number")
        solved = intent.getIntegerArrayListExtra("solved")
        rightSolved = intent.getIntegerArrayListExtra("right_solved")
        percents = intent.getIntegerArrayListExtra("percents")
        subject_name = intent.getStringExtra("subject_name")
    }

    fun initializeTable() {

        val resultTable = findViewById<View>(R.id.statsTable) as TableLayout

        val titleRow = TableRow(this)

        val params = TableRow.LayoutParams()

        val displaymetrics = resources.displayMetrics

        params.width = displaymetrics.widthPixels / 4
        params.height = TableRow.LayoutParams.MATCH_PARENT
        params.weight = 1f

        titleRow.addView(initializeTextView("Номер задания", R.drawable.white), params)
        titleRow.addView(initializeTextView("Всего решалось", R.drawable.white), params)
        titleRow.addView(initializeTextView("Решено верно", R.drawable.white), params)
        titleRow.addView(initializeTextView("Статистика", R.drawable.white), params)

        resultTable.addView(titleRow)

        for (i in solved.indices) {

            val row = TableRow(this)

            row.addView(initializeTextView(question_number[i], R.drawable.white), params)
            row.addView(initializeTextView(solved[i].toString(), R.drawable.white), params)
            row.addView(initializeTextView(rightSolved[i].toString(), R.drawable.white), params)
            row.addView(initializeTextView(percents[i].toString() + "%", R.drawable.white), params)

            resultTable.addView(row)
        }

    }

    fun initializeTextView(text: String, background: Int): TextView {
        val textView = TextView(this)
        textView.text = text
        textView.setTextColor(Color.BLACK)
        textView.setBackgroundResource(background)
        textView.gravity = Gravity.CENTER
        return textView
    }

    override fun onBackPressed() {
        val ad = AlertDialog.Builder(this)
        ad.setTitle("Выход в главное меню")
        ad.setMessage("Закончить просмотр статистики?")

        ad.setNegativeButton("Да") { dialog, arg1 ->
            //val intent = Intent(this@StatisticsActivity, MainMenu::class.java)

            val updatePref = getSharedPreferences("updateShown", AppCompatActivity.MODE_PRIVATE)
            val ed = updatePref.edit()
            ed.putBoolean("shown", true)
            ed.commit()

            //startActivity(intent)
            this.supportFinishAfterTransition()
        }

        ad.setPositiveButton("Нет") { dialog, arg1 -> }

        ad.setCancelable(true)
        ad.setOnCancelListener { }

        ad.show()
    }
}
