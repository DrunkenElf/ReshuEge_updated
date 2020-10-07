package com.ilnur

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import android.text.Spannable
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.transition.Slide
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.webkit.WebView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView

import org.json.JSONObject

import java.io.UnsupportedEncodingException
import java.net.URLEncoder

import com.ilnur.Session.Session
import com.ilnur.Session.SessionState
import com.ilnur.Session.Settings
import com.ilnur.utils.Results
import com.ilnur.utils.TaskImage
import kotlin.collections.ArrayList

class ResultsActivity : AppCompatActivity() {

    internal var body = ArrayList<String>()
    internal var solution = ArrayList<String>()
    internal var task = ArrayList<String>()
    internal var the_text = ArrayList<String>()
    internal var answer = ArrayList<String>()
    internal var type = ArrayList<String>()
    internal var question_id = ArrayList<String>()
    internal lateinit var subject_prefix: String
    internal lateinit var section: String
    internal lateinit var yourAnswerArray: Array<String>
    internal lateinit var points: Array<String>
    internal lateinit var stateAnswerArray: Array<String>
    internal lateinit var colorArray: Array<String>
    internal var rightAnswers: Int = 0
    internal var maxPoints: Int = 0
    internal var variantNumber: Int = 0
    internal var questionsCount: Int = 0
    internal var publicType: Int = 0
    internal var teacherId: Int = 0
    internal lateinit var context: Context
    internal lateinit var subj_data: JSONObject
    internal lateinit var images: ArrayList<Results>

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
        setContentView(R.layout.activity_results)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        context = this

        getIntentExtras()

        try {
            subj_data = SubjInfo()._subj_data!!.getJSONObject(subject_prefix)
            Log.d("myLogs", subj_data.toString())
        } catch (e: Exception) {
            Log.d("myLogs", "subjinfo problem")
            Log.d("myLogs", e.message + "")
        }


        initializeTable()
        initializePointsTextView()
        if (publicType != 2) {
            addComments()
        }
        supportActionBar!!.title = "Ваш результат: $rightAnswers из $questionsCount"

        val buttonContinue = findViewById<View>(R.id.buttonContinue) as Button
        buttonContinue.setOnClickListener { onBackPressed() }

        val buttonShow = findViewById<View>(R.id.buttonShow) as Button
        buttonShow.setOnClickListener { v ->
            val containerLayout = findViewById<View>(R.id.containerLayout) as LinearLayout
            containerLayout.visibility = View.VISIBLE
            v.visibility = View.GONE
        }

        val session = Settings().getSession(this)
        val buttonSaveStats = findViewById<View>(R.id.buttonSaveStats) as Button
        if (session.sessionState == SessionState.authorized) {
            buttonSaveStats.visibility = View.VISIBLE
        } else {
            buttonSaveStats.visibility = View.GONE
        }
        buttonSaveStats.setOnClickListener { saveStats(session) }
        Log.d("PubType", " " + publicType)
        if (publicType != 0) {
            buttonSaveStats.visibility = View.GONE
            saveStats(session)
            Log.d("pType!=0", "saveStat")
        }

        /*interstitial = new InterstitialAd(this);
        interstitial.setAdUnitId("ca-app-pub-4003037296923580/3256710080");
        AdRequest adRequest = new AdRequest.Builder().build();
        interstitial.loadAd(adRequest);*/
    }

    private fun tryToSaveStats(session: Session) {
        if (Connection.hasConnection(context)) {
            saveStats(session)
        } else {
            val ad = androidx.appcompat.app.AlertDialog.Builder(this)
            ad.setTitle("Для продолжения требуется интернет")
            ad.setMessage("Для сохранения результатов необходимо подключение к интернет. Проверьте подключение и попробуйте снова.")

            ad.setNegativeButton("Повторить попытку") { dialog, arg1 -> tryToSaveStats(session) }

            ad.setPositiveButton("Отмена") { dialog, arg1 ->
                val intent = Intent(this@ResultsActivity, MainMenu::class.java)
                val updatePref = getSharedPreferences("updateShown", AppCompatActivity.MODE_PRIVATE)
                val ed = updatePref.edit()
                ed.putBoolean("shown", true)
                ed.commit()
                startActivity(intent)
            }

            ad.setCancelable(false)
            ad.show()
        }
    }

    fun saveStats(session: Session) {
        if (Connection.hasConnection(context)) {

            val sharedPreferences = getSharedPreferences("version_tests", AppCompatActivity.MODE_PRIVATE)
            var variant = 0
            var query: String? = null
            if (section.contentEquals("Варианты")) {
                variant = Integer.valueOf(sharedPreferences.getString(subject_prefix, "-1")!!) + variantNumber - 1
                query = "test_id=$variant"
            } else if (section.contentEquals("Поиск")) {
                variant = variantNumber
                query = "test_id=$variant"
            }
            query += "&session=" + session.session
            for (i in question_id.indices) {
                /*query = query + ("&id=" + question_id[i])
                try {
                    query = query + ("&data=" + URLEncoder.encode(yourAnswerArray[i], "UTF-8"))
                } catch (e: UnsupportedEncodingException) {
                    query = query + ("&data=" + yourAnswerArray[i])
                }*/
                /*if (!yourAnswerArray[i].contentEquals("Часть С")) {
                    query = query + ("&id=" + question_id[i])
                    try {
                        query = query + ("&data=" + URLEncoder.encode(yourAnswerArray[i], "UTF-8"))
                    } catch (e: UnsupportedEncodingException) {
                        query = query + ("&data=" + yourAnswerArray[i])
                    }
                } else*/
                if (!yourAnswerArray[i].contains("Часть С")) {
                    query = query + ("&id=" + question_id[i])
                    try {
                        query = query + ("&data=" + URLEncoder.encode(yourAnswerArray[i], "UTF-8"))
                    } catch (e: UnsupportedEncodingException) {
                        query = query + ("&data=" + yourAnswerArray[i])
                    }
                } else {
                    query = query + ("&id=" + question_id[i])
                    try {
                        query = query + ("&data=" + URLEncoder.encode(points[i], "UTF-8"))
                    } catch (e: UnsupportedEncodingException) {
                        query = query + ("&data=" + points[i])
                    }
                    /*if (yourAnswerArray[i].contains("true")){
                        try {
                            query = query + ("&data=" + URLEncoder.encode("Загружено", "UTF-8"))
                        } catch (e: UnsupportedEncodingException) {
                            query = query + ("&data=" + "Загружено")
                        }
                    }else {
                        try {
                            query = query + ("&data=" + URLEncoder.encode("Не загружено", "UTF-8"))
                        } catch (e: UnsupportedEncodingException) {
                            query = query + ("&data=" + "Не загружено")
                        }
                    }*/
                }
            }
            Log.d("QUERY", query.toString())
            val save = SaveStatistics(context, subject_prefix, query!!)
            save.execute()
        }
    }

    fun getIntentExtras() {
        val intent = intent
        variantNumber = intent.getIntExtra("variant", 0)
        body = intent.getStringArrayListExtra("body") as ArrayList<String>
        solution = intent.getStringArrayListExtra("solution") as ArrayList<String>
        task = intent.getStringArrayListExtra("task") as ArrayList<String>
        the_text = intent.getStringArrayListExtra("the_text") as ArrayList<String>
        answer = intent.getStringArrayListExtra("answer") as ArrayList<String>
        type = intent.getStringArrayListExtra("type") as ArrayList<String>
        question_id = intent.getStringArrayListExtra("question_id") as ArrayList<String>
        yourAnswerArray = intent.getStringArrayExtra("your_answer") as Array<String>
        points = intent.getStringArrayExtra("points") as Array<String>
        rightAnswers = intent.getIntExtra("right_answers", 0)
        stateAnswerArray = intent.getStringArrayExtra("state_answer") as Array<String>
        colorArray = intent.getStringArrayExtra("color") as Array<String>
        questionsCount = intent.getIntExtra("questions_count", 0)
        maxPoints = intent.getIntExtra("max_points", 0)
        subject_prefix = intent.getStringExtra("subject_prefix").toString()
        section = intent.getStringExtra("section").toString()
        publicType = intent.getIntExtra("public", 0)
        teacherId = intent.getIntExtra("teacherId", 0)
        images = intent.getParcelableArrayListExtra("images")!!
    }

    internal fun initializePointsTextView() {

        val firstPoints = findViewById<View>(R.id.firstPoints) as TextView
        val finalPoints = findViewById<View>(R.id.finalPoints) as TextView
        val minPoints = findViewById<View>(R.id.minPoints) as TextView
        if (!section.contentEquals("Каталог заданий") || publicType != 2) {
            var firstPointSum = 0

            for (i in points.indices) {
                Log.d("myLogs", points[i])
                firstPointSum += Integer.parseInt(points[i])
            }

            firstPoints.text = "Первичный балл: $firstPointSum  из $maxPoints"


            //String[] finalPointsArray = getResources().getStringArray(getResources().getIdentifier(subject_prefix + "_final_points", "array", getPackageName()));

            var finalPointSum: String
            var min: String
            var sum = 0
            //try {
            //    finalPointSum = finalPointsArray[firstPointSum];
            //} catch (Exception e) {
            //    finalPointSum = "100";
            //}
            try {
                finalPointSum = subj_data.getJSONObject("perevod").getInt(firstPointSum.toString()).toString()
                min = subj_data.getInt("border").toString()
                val tasks = subj_data.getJSONArray("tasks")
                for (i in 0 until tasks.length()) {
                    sum += tasks.getJSONObject(i).getInt("max")
                }
            } catch (e: Exception) {
                min = ""
                finalPointSum = "100"
                Log.d("myLogs", e.toString())
            }



            finalPoints.text = "Тестовый балл: $finalPointSum"


            minPoints.text = "Минимальный балл для сдачи ЕГЭ: $min"

        } else {
            firstPoints.visibility = View.GONE
            finalPoints.visibility = View.GONE
            minPoints.visibility = View.GONE
        }

    }

    fun initializeTable() {

        val resultTable = findViewById<View>(R.id.resultTable) as TableLayout

        val titleRow = TableRow(this)

        val params = TableRow.LayoutParams()

        val displaymetrics = resources.displayMetrics

        params.width = displaymetrics.widthPixels / 4
        params.height = TableRow.LayoutParams.MATCH_PARENT
        params.weight = 1f

        titleRow.addView(initializeTextView("Задание", R.color.colorAccent), params)
        titleRow.addView(initializeTextView("Ваш ответ", R.color.colorAccent), params)
        titleRow.addView(initializeTextView("Правильный ответ", R.color.colorAccent), params)
        titleRow.addView(initializeTextView("Первичный балл", R.color.colorAccent), params)

        resultTable.addView(titleRow)

        for (i in body.indices) {

            val row = TableRow(this)
            val number = TextView(this)
            val pos = i + 1

            if (stateAnswerArray[i].contentEquals("wrong") && publicType != 2) {
                val scroll = findViewById<View>(R.id.commentScroll) as ScrollView
                number.setOnClickListener {
                    val containerLayout = findViewById<View>(R.id.containerLayout) as LinearLayout
                    val firstPoints = findViewById<View>(R.id.firstPoints) as TextView
                    val finalPoints = findViewById<View>(R.id.finalPoints) as TextView
                    val minPoints = findViewById<View>(R.id.minPoints) as TextView
                    val wv = containerLayout.findViewById<View>(pos) as WebView
                    if (containerLayout.visibility == View.VISIBLE)
                        scroll.scrollTo(wv.left, wv.top + resultTable.height + firstPoints.height +
                                finalPoints.height + minPoints.height)
                }
                val str = Integer.toString(pos)
                val ss = SpannableString(str)
                ss.setSpan(UnderlineSpan(), 0, str.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                number.text = ss
            } else {
                number.text = Integer.toString(pos)
                number.isClickable = false
            }
            number.setBackgroundResource(R.drawable.white)
            number.setTextColor(Color.BLACK)
            number.gravity = Gravity.CENTER

            row.addView(number, params)
            row.addView(initializeTextView(yourAnswerArray[i],
                    resources.getIdentifier(colorArray[i], "drawable", packageName)), params)
            row.addView(initializeTextView(if (publicType != 2) performAnswer(answer[i]) else "-", R.drawable.white), params)
            row.addView(initializeTextView(if (publicType != 2) points[i] else "-", R.drawable.white), params)

            resultTable.addView(row)
        }

    }

    fun initializeTextView(text: String, background: Int): TextView {
        val textView = TextView(this)
        textView.text = text
        textView.setTextColor(Color.BLACK)
        if (publicType != 2) {
            textView.setBackgroundResource(background)
        } else {
            textView.setBackgroundResource(R.drawable.white)
        }
        textView.gravity = Gravity.CENTER
        return textView
    }

    fun addComments() {

        val containerLayout = findViewById<View>(R.id.containerLayout) as LinearLayout
        for (i in body.indices) {

            if (stateAnswerArray[i].contentEquals("wrong")) {

                val comment = WebView(this)
                comment.settings.javaScriptEnabled = true
                comment.id = Integer.valueOf(i + 1)

                val type_task: String
                val answerComment: String
                if (type[i].contentEquals("2")) {
                    type_task = task[i]
                    val yourAnswer: String
                    if (yourAnswerArray[i].contentEquals("Не решено"))
                        yourAnswer = "<i>нет ответа</i>"
                    else
                        yourAnswer = yourAnswerArray[i]
                    answerComment = "Ваш ответ: " + yourAnswer + ". Правильный ответ: " + answer[i]
                } else {
                    type_task = "C" + task[i]
                    answerComment = ""
                }

                val text: String
                if (!the_text[i].contentEquals("null") && !the_text[i].contentEquals(" "))
                    text = "<b>Текст</b><p>" + the_text[i]
                else
                    text = ""

                var commentText = "<b>Задание " + (i + 1) + " № " + question_id[i] + " тип " + type_task + "</b><p>" +
                        body[i] + text + "<b>Пояснение</b><p>" + solution[i] + "<p>" + answerComment

                images.forEach { t: Results ->
                    run {
                        if (t.path != null)
                            Log.d("img path", t.path.toString())
                        Log.d("indi file", t.filename + " " + t.indice)
                        if (t != null && t.filename != null && t.indice != null && t.indice == i && t.path != null)
                            commentText += "<p><img src=\"file://" + t.path + "\"/>"
                    }
                }
                Log.d("web ", "file://" + commentText)
                comment.loadDataWithBaseURL(null, commentText, "text/html", "utf-8", null)

                containerLayout.addView(comment)
            }
        }

    }

    fun performAnswer(string: String): String {
        var string = string

        for (i in 0 until string.length) {
            if (string[i] == '|') {
                val substr = string.substring(i, string.length)
                string = string.replace(substr, "")
                break
            }
        }
        return string

    }

    override fun onBackPressed() {
        val ad = androidx.appcompat.app.AlertDialog.Builder(this)
        ad.setTitle("Выход в главное меню")
        ad.setMessage("Закончить просмотр результатов?")

        ad.setNegativeButton("Да") { dialog, arg1 ->
            //val intent = Intent(this@ResultsActivity, MainMenu::class.java)

            val updatePref = getSharedPreferences("updateShown", AppCompatActivity.MODE_PRIVATE)
            val ed = updatePref.edit()
            ed.putBoolean("shown", true)
            ed.commit()
            this@ResultsActivity.supportFinishAfterTransition()
            //startActivity(intent)
            /*if (interstitial.isLoaded()) {
                    interstitial.show();
                }*/
        }

        ad.setPositiveButton("Нет") { dialog, arg1 -> }

        ad.setCancelable(true)
        ad.setOnCancelListener { }

        ad.show()
    }
}
