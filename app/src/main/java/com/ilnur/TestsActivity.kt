package com.ilnur

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.transition.Slide
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.util.Log
import android.view.*
import android.webkit.WebView
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat

import java.util.ArrayList
import java.util.Random

import com.ilnur.DataBase.QuestionsDataBaseHelper
//import com.ilnur.Fragments.AnswerFragment
import com.ilnur.Fragments.AnswerFragment1
import com.ilnur.Fragments.QuestionNumbersFragment
import com.ilnur.Fragments.ShowTaskFragment
import com.ilnur.utils.Results
import com.ilnur.utils.TaskImage

class TestsActivity : AppCompatActivity(), QuestionNumbersFragment.OnFragmentInteractionListener, AnswerFragment1.OnFragmentInteractionListener {

    internal var questions = ArrayList<ArrayList<String>>()
    internal var question_name = ArrayList<String>()
    internal var body = ArrayList<String>()
    internal var solution = ArrayList<String>()
    internal var task = ArrayList<String>()
    internal var the_text = ArrayList<String>()
    internal var answer = ArrayList<String>()
    internal var type = ArrayList<String>()
    internal var question_id = ArrayList<String>()
    internal var category_id = ArrayList<String>()
    internal lateinit var yourAnswerArray: Array<String?>
    internal lateinit var pointsArray: Array<String?>
    internal lateinit var colorArray: Array<String?>
    internal lateinit var plusesArray: Array<String?>
    internal lateinit var stateAnswersArray: Array<String?>
    internal var questionsCount: Int = 0
    internal var variantNumber: Int = 0
    internal var questionNumber: Int = 0
    internal var rightAnswers: Int = 0
    internal val QUESTION_NAME_ROW = 0
    internal val BODY_ROW = 1
    internal val SOLUTION_ROW = 2
    internal val TASK_ROW = 3
    internal val THE_TEXT_ROW = 4
    internal val ANSWER_ROW = 5
    internal val TYPE_ROW = 6
    internal val QUESTION_ID_ROW = 7
    internal val CATEGORY_ROW = 8
    internal var questionsDone = 0
    internal lateinit var qFragment: QuestionNumbersFragment
    internal lateinit var sFragment: ShowTaskFragment
    internal lateinit var aFragment: AnswerFragment1
    internal lateinit var section: String
    internal var themeNumber = 0
    internal var themePosition = 0
    internal lateinit var subject_prefix: String
    internal var publicType: Int = 0
    internal var teacherId: Int = 0
    var images = HashMap<Int, TaskImage>()
    internal var b = "11100110111011111100"
    internal var masked = ""
    public var token = ""
    internal var isTeach = false

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
        setContentView(R.layout.activity_tests)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar

        setSupportActionBar(toolbar)
        isTeach = intent.getBooleanExtra("isTeach", false)
        if (isTeach) {
            token = intent.getStringExtra("token")
        }

        publicType = intent.getIntExtra("public", 0)
        teacherId = intent.getIntExtra("teacherId", 0)

        initializeSettings()
        initializeFragments()
        initializeArrays()

        aFragment.setToken(token, subject_prefix)
        if (!section.contentEquals("Поиск")) {
            val loader = TasksLoader(this, subject_prefix)
            loader.execute()
        } else {
            fillTasks()
            qFragment.doClick(1)
        }

    }


    private fun fillTasks() {
        question_name = intent.getStringArrayListExtra("question_name")
        body = intent.getStringArrayListExtra("body")
        solution = intent.getStringArrayListExtra("solution")
        task = intent.getStringArrayListExtra("task")
        the_text = intent.getStringArrayListExtra("the_text")
        answer = intent.getStringArrayListExtra("answer")
        type = intent.getStringArrayListExtra("type")
        question_id = intent.getStringArrayListExtra("question_id")
        category_id = intent.getStringArrayListExtra("category_id")
        addToArray()
    }

    fun addToArray() {
        questions.add(question_name)
        questions.add(body)
        questions.add(solution)
        questions.add(task)
        questions.add(the_text)
        questions.add(answer)
        questions.add(type)
        questions.add(question_id)
        questions.add(category_id)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("onRes", "act")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.tests_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == R.id.end_tests) {

            endTest()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onFragmentInteraction(questionNumber: Int) {
        var questionNumber = questionNumber
        questionNumber -= 1
        this.questionNumber = questionNumber
        var pos = questionNumber
        sFragment.setTask(setTask(questionNumber), questionNumber)
        if (section.contentEquals("Каталог заданий")) {
            pos = themePosition - 1
        }
        Log.d("myLogs", questions[ANSWER_ROW].toString() + " " + questionNumber.toString())
        Log.d("myLogs", questions.size.toString())
        Log.d("myLogs", questions[CATEGORY_ROW].toString() + " " + questionNumber.toString())
        Log.d("SET_ANS", questions[CATEGORY_ROW][questionNumber])
        aFragment.setAnswer(performAnswer(questions[ANSWER_ROW][questionNumber]),
                Integer.parseInt(questions[TYPE_ROW][questionNumber]), pos,
                Integer.parseInt(questions[CATEGORY_ROW][questionNumber]), question_id.get(questionNumber))
        Log.d("myLogs", "Ss")
        aFragment.setButtonsVisibility(Integer.parseInt(questions[TYPE_ROW][questionNumber]))
        Log.d("myLogs", "aa")
        if (yourAnswerArray[questionNumber] != null) {
            aFragment.setYourAnswer(yourAnswerArray[questionNumber]!!)
            qFragment.setClickedWithColor(questionNumber)
        }

    }

    override fun onFragmentInteraction(answer: Boolean) {
        qFragment.setButtonColor(answer)
        if (answer) {
            rightAnswers++
            stateAnswersArray[questionNumber] = "right"
        } else {
            stateAnswersArray[questionNumber] = "wrong"
        }
    }

    override fun onFragmentInteraction(command: Byte) {
        if (command.toInt() == 1)
            clickNextQuestion()
        else if (command.toInt() == 2) showComment(questionNumber)
    }

    override fun onFragmentInteraction(yourAnswer: String, point: String, pluses: String, color: String, taskImage: TaskImage?) {
        yourAnswerArray[questionNumber] = yourAnswer
        pointsArray[questionNumber] = point
        plusesArray[questionNumber] = pluses
        colorArray[questionNumber] = color
        if (taskImage != null && taskImage.id_orig != null)
            images.put(questionNumber, taskImage)
        questionsDone++
        if (questionsDone == questionsCount) endTest()
        clickNextQuestion()
    }

    fun showComment(question: Int) {
        val commentDialog = androidx.appcompat.app.AlertDialog.Builder(this)


        val dialogLayout = layoutInflater.inflate(R.layout.comment_dialog, null)
        commentDialog.setView(dialogLayout)

        val comment = dialogLayout.findViewById<View>(R.id.comment) as WebView
        //val debug = dialogLayout.findViewById<View>(R.id.debug) as TextView

        commentDialog.setTitle("Комментарий")
        val text = questions[SOLUTION_ROW][question]
        //text = performHTMLString(text)
        //Log.d("COMMENT", text)
        //val uri = text.substringAfter("<img src=\"").split("\" ").first()
        //debug.text = uri
        comment.loadDataWithBaseURL(null, text, "text/html", "utf-8", null)

        commentDialog.setNegativeButton("Ок") { dialog, which -> dialog.cancel() }

        commentDialog.create()
        commentDialog.show()
    }

    fun clickNextQuestion() {
        var checker = false
        for (i in questionNumber + 1 until questionsCount) {
            if (yourAnswerArray[i] == null) {
                Log.d("myLogs", "null")
                qFragment.doClick(i + 1)
                checker = true
                break
            }
        }
        if (!checker)
            for (i in 0 until questionNumber) {
                if (yourAnswerArray[i] == null) {
                    qFragment.doClick(i + 1)
                    break
                }
            }
    }

    fun setTask(questionNumber: Int): String {
        var task = "<b>Задание № " + (if (publicType != 2) questions[QUESTION_ID_ROW][questionNumber] else questionNumber) + "</b><p>" +
                questions[BODY_ROW][questionNumber]

        if (!questions[THE_TEXT_ROW][questionNumber].contentEquals("null") && !questions[THE_TEXT_ROW][questionNumber].contentEquals(" "))
            task += "<b>Текст</b><p>" + questions[THE_TEXT_ROW][questionNumber]
        task = performHTMLString(task)


        Log.d("myLogs", task)
        return task
    }

    fun performHTMLString(string: String): String {
        /*string = string.replace("/files", "https://ege.sdamgia.ru/files");
        string = string.replace("https://ege.sdamgia.ruhttps://ege.sdamgia.ru/", "https://ege.sdamgia.ru/");*/
        return string
    }

    fun performAnswer(string: String): String {

        /*for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == '|') {
                String substr = string.substring(i, string.length());
                string = string.replace(substr, "");
                break;
            }
        }*/
        return string

    }

    override fun onBackPressed() {
        val ad = AlertDialog.Builder(this)
        ad.setTitle("Выйти из теста?")
        ad.setMessage("Ваши результаты не будут сохранены. Продолжить?")

        ad.setNegativeButton("Да") { dialog, arg1 ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //this.finishAfterTransition()
                this.supportFinishAfterTransition()
            } else{
                this.finish()
            }
            /*if (section.contentEquals("Поиск")) {
                val intent = Intent(this@TestsActivity, MainMenu::class.java)
                startActivity(intent)
            } else {
                super@TestsActivity.onBackPressed()
            }*/
            /*if (interstitial.isLoaded()) {
                    interstitial.show();
                }*/
        }

        ad.setPositiveButton("Нет") { dialog, arg1 -> }

        ad.setCancelable(true)
        ad.setOnCancelListener { }

        ad.show()
    }

    fun endTest() {
        for (i in 0 until questionsCount) {

            if (yourAnswerArray[i] == null) {
                if (type[i].contentEquals("3")) {
                    yourAnswerArray[i] = "Часть С"
                } else
                    yourAnswerArray[i] = "Не решено"
            }

            if (pointsArray[i] == null) {
                pointsArray[i] = "0"
            }

            if (stateAnswersArray[i] == null) {
                stateAnswersArray[i] = "wrong"
            }

            if (colorArray[i] == null) {
                colorArray[i] = "white"
            }
        }

        if (section.contentEquals("Варианты")) {
            val resultPref = getSharedPreferences(subject_prefix + "_results", AppCompatActivity.MODE_PRIVATE)
            val ed = resultPref.edit()
            ed.putInt("var$variantNumber", rightAnswers)
            ed.commit()
        }

        val maxPoints = aFragment.maxPoints

        val intent = Intent(this, ResultsActivity::class.java)
        intent.putExtra("variant", variantNumber)
        intent.putExtra("body", body)
        intent.putExtra("solution", solution)
        intent.putExtra("task", task)
        intent.putExtra("the_text", the_text)
        intent.putExtra("answer", answer)
        intent.putExtra("type", type)
        intent.putExtra("question_id", question_id)
        intent.putExtra("your_answer", yourAnswerArray)
        intent.putExtra("right_answers", rightAnswers)
        intent.putExtra("points", pointsArray)
        intent.putExtra("state_answer", stateAnswersArray)
        intent.putExtra("color", colorArray)
        intent.putExtra("questions_count", questionsCount)
        intent.putExtra("max_points", maxPoints)
        intent.putExtra("subject_prefix", subject_prefix)
        intent.putExtra("section", section)
        intent.putExtra("public", publicType)
        intent.putExtra("teacherId", teacherId)
        val list = ArrayList<Results>(50)
        for (entry in images.entries) {
            val task = Results(entry.value.id_orig, entry.value.id_masked, entry.value.filename,
                    entry.value.path, entry.key)
            list.add(task)
        }
        intent.putParcelableArrayListExtra("images", list)
        if (Build.VERSION.SDK_INT > 20) {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this)
            startActivity(intent, options.toBundle())
        } else {
            startActivity(intent)
        }
        supportFinishAfterTransition()
    }

    internal fun initializeArrays() {
        yourAnswerArray = arrayOfNulls(questionsCount)
        pointsArray = arrayOfNulls(questionsCount)
        plusesArray = arrayOfNulls(questionsCount)
        colorArray = arrayOfNulls(questionsCount)
        stateAnswersArray = arrayOfNulls(questionsCount)
    }

    internal fun initializeFragments() {
        qFragment = (supportFragmentManager.findFragmentById(R.id.fragment_questions_buttons) as QuestionNumbersFragment?)!!
        sFragment = (supportFragmentManager.findFragmentById(R.id.fragment_show_task) as ShowTaskFragment?)!!
        aFragment = (supportFragmentManager.findFragmentById(R.id.fragment_answer) as AnswerFragment1?)!!

        val e = if (true)
            " "
        else "sad"
        if (section.contentEquals("Поиск")) {
            if (publicType == 2) {
                qFragment.setColorAnswer(false)
            }
        }
    }

    internal fun initializeSettings() {
        subject_prefix = intent.getStringExtra("subject_prefix")

        section = intent.getStringExtra("section")

        val countPref = getSharedPreferences("subjects_questions_count", AppCompatActivity.MODE_PRIVATE)
        questionsCount = countPref.getInt(subject_prefix + "_questions_count", 0)

        if (section.contentEquals("Варианты") || section.contentEquals("Поиск")) {
            variantNumber = intent.getIntExtra("variant_number", 1)
            supportActionBar?.setTitle("Вариант №$variantNumber")
        } else if (section.contentEquals("Каталог заданий")) {
            themeNumber = intent.getIntExtra("theme_number", 1)
            themePosition = intent.getIntExtra("position", 1)
            questionsCount = countPref.getInt(subject_prefix + "_" + themeNumber + "_questions_count", 0)
            supportActionBar?.setTitle("Задание №$themePosition")
        } else if (section.contentEquals("Режим экзамена")) {
            var title = intent.getStringExtra("subject_name")
            title = if (title.contains(":")) title.split(": ")[0] else title.split(" ")[0]
            supportActionBar?.title = "Режим экзамена.$title"
        }

        if (section.contentEquals("Поиск")) {
            questionsCount = intent.getIntExtra("count", 0)
        }
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    inner class TasksLoader(internal var context: Context, internal var subject_prefix: String) : AsyncTask<String, Int, ArrayList<ArrayList<String>>>() {

        internal lateinit var progress: ProgressDialog
        internal val LOG_TAG = "myLogs"


        init {
            Log.d("myLogs", subject_prefix)
            Log.d("myLogs", questionsCount.toString() + "")
        }

        override fun onPreExecute() {
            progress = ProgressDialog(context)
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            progress.setMessage("Загрузка заданий")

            try {

                progress.show()
                progress.setCancelable(false)
            } catch (e: Exception) {
                e.printStackTrace()
            }


        }

        override fun doInBackground(vararg strings: String): ArrayList<ArrayList<String>> {


            try {
                var tableName = subject_prefix
                if (section.contentEquals("Каталог заданий")) {
                    tableName += "_$themeNumber"
                }
                val qdbHelper = QuestionsDataBaseHelper(context, tableName)
                val db = qdbHelper.writableDatabase


                val cursor = db.query(tableName, null, null, null, null, null, null)
                val request = setRequest()


                if (cursor != null) {
                    if (section.contentEquals("Каталог заданий")) {
                        while (cursor.moveToNext()) {
                            question_name.add(cursor.getString(cursor.getColumnIndex("question_name")))
                            body.add(cursor.getString(cursor.getColumnIndex("body")))
                            solution.add(cursor.getString(cursor.getColumnIndex("solution")))
                            task.add(cursor.getString(cursor.getColumnIndex("task")))
                            the_text.add(cursor.getString(cursor.getColumnIndex("the_text")))
                            answer.add(cursor.getString(cursor.getColumnIndex("answer")))
                            type.add(cursor.getString(cursor.getColumnIndex("type")))
                            question_id.add(cursor.getString(cursor.getColumnIndex("question_id")))
                            category_id.add(cursor.getString(cursor.getColumnIndex("category")))
                            Log.d("myLogs", "Записалось")
                        }
                    } else {
                        var counter = 1
                        while (counter != request!!.size) {
                            if (cursor.moveToFirst()) {
                                do {
                                    if (cursor.getString(cursor.getColumnIndex("question_name")).contentEquals(request[counter]!!)) {
                                        question_name.add(cursor.getString(cursor.getColumnIndex("question_name")))
                                        body.add(cursor.getString(cursor.getColumnIndex("body")))
                                        solution.add(cursor.getString(cursor.getColumnIndex("solution")))
                                        task.add(cursor.getString(cursor.getColumnIndex("task")))
                                        the_text.add(cursor.getString(cursor.getColumnIndex("the_text")))
                                        answer.add(cursor.getString(cursor.getColumnIndex("answer")))
                                        type.add(cursor.getString(cursor.getColumnIndex("type")))
                                        question_id.add(cursor.getString(cursor.getColumnIndex("question_id")))
                                        category_id.add(cursor.getString(cursor.getColumnIndex("category")))
                                        Log.d("myLogs", "Записалось")
                                    }
                                } while (cursor.moveToNext())
                                counter++
                            }
                        }
                    }
                } else
                    Log.d("myLogs", "Пустой курсор")
                addToArray()

                cursor!!.close()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d(LOG_TAG, e.toString())
            }

            return questions
        }


        override fun onPostExecute(questions: ArrayList<ArrayList<String>>) {
            super.onPostExecute(questions)
            try {
                progress.dismiss()

                qFragment.doClick(1)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        fun addToArray() {
            questions.add(question_name)
            questions.add(body)
            questions.add(solution)
            questions.add(task)
            questions.add(the_text)
            questions.add(answer)
            questions.add(type)
            questions.add(question_id)
            questions.add(category_id)
        }

        internal fun setRequest(): Array<String?> {
            var request: Array<String?> = emptyArray()
            if (section.contentEquals("Варианты")) {
                request = arrayOfNulls(questionsCount + 1)
                for (i in 1..questionsCount) {
                    request[i] = "var" + variantNumber + "q" + i
                }
            }
            if (section.contentEquals("Режим экзамена")) {
                request = arrayOfNulls(questionsCount + 1)
                val isNotRandom = setDoNotRandomize()
                var varNumber = 1
                val random = Random()
                for (i in 1..questionsCount) {
                    if (!isNotRandom[i]) varNumber = random.nextInt(14) + 1
                    request[i] = "var" + varNumber + "q" + i
                }
            }

            return request
        }

        internal fun setDoNotRandomize(): BooleanArray {
            val array = BooleanArray(questionsCount + 1)

            for (i in 1..questionsCount)
                when (subject_prefix) {
                    "rus" -> if (i == 2 || i == 3 || i >= 21 && i <= 25)
                        array[i] = true
                    else
                        array[i] = false
                    "math" -> array[i] = false
                    "mathb" -> array[i] = false
                    "inf" -> array[i] = false
                    "phys" -> array[i] = false
                    "chem" -> array[i] = false
                    "bio" -> array[i] = false
                    "geo" -> if (i == 27 || i == 34)
                        array[i] = true
                    else
                        array[i] = false
                    "hist" -> if (i >= 14 && i <= 16 || i == 19)
                        array[i] = true
                    else
                        array[i] = false
                    "soc" -> if (i >= 22 && i <= 24)
                        array[i] = true
                    else
                        array[i] = false
                    "lit" -> if (i >= 2 && i <= 9 || i >= 11 && i <= 16)
                        array[i] = true
                    else
                        array[i] = false
                    "en" -> if (i >= 13 && i <= 18 || i >= 33 && i <= 38)
                        array[i] = true
                    else
                        array[i] = false
                }

            return array
        }
    }

}


