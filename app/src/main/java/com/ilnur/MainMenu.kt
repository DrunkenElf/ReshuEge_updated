package com.ilnur


//import vdd.test.Fragments.SubjectsFragment;
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteException
import android.graphics.Color
import android.net.Uri
import android.os.*
import android.transition.Slide
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
//import androidx.navigation.NavController
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController

import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.ilnur.DataBase.MyDB
//import com.ilnur.DataBase.MyDB1
import com.ilnur.DataBase.QuestionsDataBaseHelper
import com.ilnur.DownloadTasks.*
import com.ilnur.Fragments.*
import com.ilnur.Session.Session
import com.ilnur.Session.SessionState
import com.ilnur.Session.Settings
import com.ilnur.utils.MyNavController
import kotlinx.coroutines.*
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Runnable
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection
import kotlin.coroutines.CoroutineContext

class MainMenu : AppCompatActivity(), CoroutineScope, NavigationView.OnNavigationItemSelectedListener, SubjectsFragment.OnFragmentInteractionListener, StartPageFragment.OnFragmentInteractionListener, SubjectsTheoryFragment.OnFragmentInteractionListener, SubjectSearchFragment.OnFragmentInteractionListener, SubjectThemesFragment.OnFragmentInteractionListener {


    private lateinit var mJob: Job
    override val coroutineContext: CoroutineContext
        get() = mJob + Dispatchers.Main

    //internal lateinit var fManager: FragmentManager
    //internal lateinit var fTransaction: FragmentTransaction
    //internal var itemSelected: String? = "РЕШУ ЕГЭ"
    internal val itemSelected = Stack<String>()
    internal lateinit var context: Context
    private var mHandler: Handler? = null
    lateinit var fragment: Fragment

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

    override fun onStart() {
        super.onStart()
        Log.d("onStart", "sa")
        /*Log.d("sa",getNavController().currentDestination.toString())
        Log.d("sa",getNavController().currentDestination!!.parent.toString())
        //Log.d("sa",getNavController().popBackStack())
        Log.d("sa",""+getNavController().getBackStackEntry(getNavController().currentDestination!!.id).toString())
        Log.d("sa",""+getNavController().getBackStackEntry(getNavController().currentDestination!!.id).destination.parent.toString())*/
        //Log.d("sa",""+getNavController().graph.)
        //serReceiver()
    }

    override fun onResume() {
        super.onResume()
        Log.d("onRessume", "sa")

    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        Log.d("onSave", "sa")
        super.onSaveInstanceState(outState, outPersistentState)
        outState?.putAll(getNavController().saveState())
        //getNavController().navigateUp()
        //supportFragmentManager.putFragment(outState!!, "myFr", fragment)
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        Log.d("onSaveWITHOUT", "sa")

        outState.putString("itemSelected", itemSelected.peek())
        //getNavController().saveState()
        outState.putAll(getNavController().saveState())
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        Log.d("onRes", "sa")
        super.onRestoreInstanceState(savedInstanceState)
        getNavController().restoreState(savedInstanceState)
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var importance = NotificationManager.IMPORTANCE_HIGH
            var channel = NotificationChannel("77", "РЕШУ ЕГЭ", importance).apply {
                description = "Загрузка предмета"
                //enableLights(true)
                setSound(null, null)
                //lightColor = Color.RED
                setShowBadge(true)
            }
            // Register the channel with the system
            var notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            importance = NotificationManager.IMPORTANCE_LOW
            channel = NotificationChannel("33", "РЕШУ ЕГЭ", importance).apply {
                description = "Загрузка предмета"
                //enableLights(true)
                //lightColor = Color.RED
                setShowBadge(true)
            }
            // Register the channel with the system
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }






    //@SuppressLint("RestrictedApi")
    //@SuppressLint("RestrictedApi")
    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        instance = savedInstanceState
        setupAnim()
        setContentView(R.layout.activity_main_menu)
        mJob = Job()
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        if (itemSelected.isEmpty())
            itemSelected.push("РЕШУ ЕГЭ")
        Log.d("MAINACT", "onCreate")
        context = this

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        //container = findViewById<View>(R.id.container) as FrameLayout
        //fManager = supportFragmentManager


        val si = SubjInfo()
        si.context = context
        si.check_subject_data()

        val fab = findViewById<View>(R.id.fab) as FloatingActionButton
        fab.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ege.sdamgia.ru/"))
            startActivity(browserIntent)
        }

        setVersionPreferences()
        val content: CoordinatorLayout = findViewById(R.id.coordinator_layout)

        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        //drawer.setDrawerListener(toggle)
        drawer.setScrimColor(Color.TRANSPARENT)
        val drawerToggle: ActionBarDrawerToggle = object : ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        ) {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                super.onDrawerSlide(drawerView, slideOffset)
                val slideX = drawerView.width * slideOffset
                content.translationX = slideX
            }

        }
        drawer.drawerElevation = 4f
        drawer.addDrawerListener(drawerToggle)
        toggle.syncState()


        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)
        //navigationView.setupWithNavController(findNavController(R.id.container))

        /*if (savedInstanceState != null) {
            itemSelected = savedInstanceState.getString("itemSelected")
            supportActionBar!!.setTitle(itemSelected)
        } else {
            val startFragment = StartPageFragment()
            setFragment(startFragment, "START", savedInstanceState)
        }*/

        if (!itemSelected.peek()!!.contentEquals(getString(R.string.app_name))) {
            fab.visibility = View.GONE
        }

        val updatePref = getSharedPreferences("updateShown", AppCompatActivity.MODE_PRIVATE)

        val ed = updatePref.edit()
        ed.putBoolean("shown", false)
        ed.apply()
        toggle.syncState()


        instance = savedInstanceState
        //val call = OnBackPressedCallback()
    }


    override fun onBackPressed() {
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)

        } else if (getNavController().currentDestination != null && getNavController().currentDestination!!.id != R.id.nav_start_page) {
            Log.d("myLogs", getNavController().currentDestination.toString())
            getNavController().popBackStack()
            itemSelected.pop()

            supportActionBar!!.title = itemSelected.peek()
        } else
            showMessage("Выход", "Вы уверены, что хотите выйти?")

        createDataBaseIfNotExist()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_settings) {
            hideStartPage()
            getNavController().navigate(R.id.action_global_settingsFragment3)
            itemSelected.push("Настройки")
            supportActionBar?.title = "Настройки"
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    fun getNavController(): NavController {
        return this.findNavController(R.id.container)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        Log.d("navItem", "sel")
        val id = item.itemId

        //val sFragment = SubjectsFragment()
        //clearBackStack()
        val handler = Handler()
        val r = Runnable {
            val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
            drawer.closeDrawer(GravityCompat.START)
        }
        handler.postDelayed(r, 200)
        when(id) {
            R.id.nav_search -> {
                if (getNavController().currentDestination?.id != R.id.nav_subj_search && itemSelected.peek() != "Поиск") {
                    getNavController().navigate(R.id.action_global_nav_subj_search2) //
                    itemSelected.push("Поиск")
                } else if (getNavController().currentDestination?.id == R.id.nav_subj_search && itemSelected.peek() != "Поиск") {
                    itemSelected.push("Поиск")
                    getNavController().navigate(R.id.action_nav_subj_search_self)
                }

                hideStartPage()
            }
            R.id.nav_news -> {
                if (Connection.hasConnection(context)) {
                    val downloadNews = DownloadNews(this)
                    downloadNews.execute()
                }
            }
            R.id.nav_stats -> {
                if (getNavController().currentDestination?.id != R.id.nav_subj_search && itemSelected.peek() != "Статистика") {
                    itemSelected.push("Статистика")
                    getNavController().navigate(R.id.action_global_nav_subj_search2) //
                    Log.d("Stats Nav1", itemSelected.peek() + " " + getNavController().currentDestination?.toString())
                } else if (getNavController().currentDestination?.id == R.id.nav_subj_search && itemSelected.peek() != "Статистика"){
                    itemSelected.push("Статистика")
                    getNavController().navigate(R.id.action_nav_subj_search_self)
                    Log.d("Stats Nav3", itemSelected.peek() + " " + getNavController().currentDestination?.toString())
                }
                hideStartPage()
            }
            R.id.nav_manual -> {
                if (getNavController().currentDestination?.id != R.id.nav_subj_search && itemSelected.peek() != "Об экзамене") {
                    itemSelected.push("Об экзамене")
                    getNavController().navigate(R.id.action_global_nav_subj_search2) //
                    Log.d("Stats Nav1", itemSelected.peek() + " " + getNavController().currentDestination?.toString())
                } else if (getNavController().currentDestination?.id == R.id.nav_subj_search && itemSelected.peek() != "Об экзамене"){
                    itemSelected.push("Об экзамене")
                    getNavController().navigate(R.id.action_nav_subj_search_self)
                    Log.d("Stats Nav3", itemSelected.peek() + " " + getNavController().currentDestination?.toString())
                }
                hideStartPage()
            }
            R.id.nav_about -> {
                DownloadAboutProject(this).execute()
            }
            R.id.nav_check_update -> {
                showUpdateMessage("Проверка обновлений", "Проверка обновлений может занять несколько минут. Продолжить?")
            }
            R.id.nav_settings -> {
                itemSelected.push("Настройки")
                getNavController().navigate(R.id.action_global_settingsFragment3) //
                hideStartPage()
            }
            R.id.nav_change_user -> {
                showAuthorizeMessage("Смена учетной записи", "Вы уверены, что хотите сменить учетную запись?", true)
            }
            R.id.nav_exit -> {
                showMessage("Выход", "Вы уверены, что хотите выйти?")
            }
        }

        Log.d("naV", itemSelected.peek())
        supportActionBar?.title = itemSelected.peek()


        return true
    }

    override fun onDestroy() {
        super.onDestroy()
    }


    /* private fun clearBackStack() {
         if (fManager.backStackEntryCount > 0) {
             val first = fManager.getBackStackEntryAt(0)
             //fManager.popBackStack("START", FragmentManager.POP_BACK_STACK_INCLUSIVE)
         }
     }*/


    override fun onFragmentInteraction(subject: String) {
        Log.d("onFrInter", "first")
        Log.d("onFrInter", subject)
        Log.d("onFrInter", itemSelected.peek())
        Log.d("onFrInter2", itemSelected.peek())
        when (itemSelected.peek()) {
            "Варианты" -> { //+
                val action = SubjectsFragmentDirections.actionNavSubjVarsToVariantsFragment(subject)
                getNavController().navigate(action)
                supportActionBar?.title = "${href_to_subj(subject)}.${itemSelected.peek()}"
                itemSelected.push("${href_to_subj(subject)}.${itemSelected.peek()}")
            }
            "Каталог заданий" -> { //+
                val action = SubjectThemesFragmentDirections.actionNavSubjThemesToThemesFragment(subject)
                getNavController().navigate(action)
                supportActionBar?.title = "${href_to_subj(subject)}.${itemSelected.peek()}"
                itemSelected.push("${href_to_subj(subject)}.${itemSelected.peek()}")
            }
            "Режим экзамена" -> { //+
                val intent = Intent(this, TestsActivity::class.java)
                intent.putExtra("subject_prefix", subject)
                intent.putExtra("subject_name", href_to_subj(subject))
                intent.putExtra("section", "Режим экзамена")
                if (Build.VERSION.SDK_INT > 20) {
                    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this)
                    startActivity(intent, options.toBundle())
                } else {
                    startActivity(intent)
                }
            }
            "Теория" -> {
                Log.d("teor", href_to_subj(subject))
                Log.d("teor1", subject)
                val action = SubjectsTheoryFragmentDirections.actionNavSubjTheoryToTheoryFragment(subject)
                supportActionBar?.title = "$subject.${itemSelected.peek()}"
                itemSelected.push("$subject.${itemSelected.peek()}")
                getNavController().navigate(action)
            }
            "Поиск" -> { // doene
                val intent = Intent(this, SearchTypeActivity::class.java)
                intent.putExtra("subject_prefix", subject)
                intent.putExtra("subject_name", href_to_subj(subject))
                intent.putExtra("section", "Поиск")
                if (Build.VERSION.SDK_INT > 20) {
                    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this)
                    startActivity(intent, options.toBundle())
                } else {
                    startActivity(intent)
                }
            }
            "Учителю" -> { // donei
                val session = Settings().getSession(this)
                if (session.sessionState == SessionState.anonymus) {
                    showAuthorizeMessage("Требуется авторизация", "Для просмотра статистики нужно " + "авторизоваться. Перейти к авторизации?", false)
                } else {
                    if (Connection.hasConnection(context)) {
                        val action = SubjectSearchFragmentDirections.actionNavTeacher1ToNavTeacher2(subject)
                        getNavController().navigate(action)
                        supportActionBar?.title = "${href_to_subj(subject)}.${itemSelected.peek()}"
                        itemSelected.push("${href_to_subj(subject)}.${itemSelected.peek()}")
                    }
                }
            }
            "Статистика" -> { //done
                val session = Settings().getSession(this)
                if (session.sessionState == SessionState.anonymus) {
                    showAuthorizeMessage("Требуется авторизация", "Для просмотра статистики нужно " + "авторизоваться. Перейти к авторизации?", false)
                } else {
                    if (Connection.hasConnection(context)) {
                        val downloadStatistics = DownloadStatistics(this, subject, "session=" + session.session)
                        downloadStatistics.execute()
                    }
                }
            }
            "Об экзамене" -> { // done
                if (Connection.hasConnection(context)) {
                    val downloadManual = DownloadAboutExam(this, subject, href_to_subj(subject)) ///
                    downloadManual.execute()
                }
            }
            "О проекте" -> {//done
                if (Connection.hasConnection(context)) {
                    val downloadManual = DownloadAboutProject(this) ////
                    downloadManual.execute()
                }
            }
        }

        //supportActionBar?.title = subject+"."+itemSelected.peek()

    }

    fun href_to_subj(href: String): String {
        val subjectsArray = context.resources.getStringArray(R.array.subjects)
        val prefixArray = context.resources.getStringArray(R.array.subjects_prefix)
        for (i in prefixArray.indices) {
            if (prefixArray[i] == href) {
                return subjectsArray[i]
            }
        }
        return ""
    }

    override fun onFragmentInteraction(itemSelected: String, number: Int) {
        //hideStartPage();
        Log.d("onFrInter", "second")
        Log.d("onFrInter", itemSelected)
        if (itemSelected.contentEquals("Учителю")) {
            getNavController().navigate(StartPageFragmentDirections.actionNavStartPageToNavTeacher1())
        }
        if (itemSelected.contentEquals("Варианты")) {
            getNavController().navigate(StartPageFragmentDirections.actionNavStartPageToNavSubjVars())
        }
        if (itemSelected.contentEquals("Каталог заданий")) {
            getNavController().navigate(StartPageFragmentDirections.actionNavStartPageToNavSubjThemes())
        }
        if (itemSelected.contentEquals("Режим экзамена")) {
            getNavController().navigate(StartPageFragmentDirections.actionNavStartPageToNavSubj())
        }
        if (itemSelected.contentEquals("Теория")) {
            getNavController().navigate(StartPageFragmentDirections.actionNavStartPageToNavSubjTheory())
        }
        if (itemSelected.contentEquals("Поиск")) {
            getNavController().navigate(R.id.action_global_nav_subj_search2)
        }
        if (itemSelected.contentEquals("Статистика")) {
            getNavController().navigate(R.id.action_global_nav_subj_search2) //
        }
        if (itemSelected.contentEquals("Об экзамене")) {
            getNavController().navigate(R.id.action_global_nav_subj_search2) //
        }
        if (itemSelected.contentEquals("Настройки")) {
            getNavController().navigate(R.id.action_global_settingsFragment3)
        }

        this.itemSelected.push(itemSelected)
        hideStartPage()
        supportActionBar?.title = itemSelected

    }


    fun createDataBaseIfNotExist() {
        val qdbHelper = QuestionsDataBaseHelper(this, "start_table")
        val db = qdbHelper.writableDatabase
        qdbHelper.close()
    }

    fun hideStartPage() {
        val fab = findViewById<View>(R.id.fab) as FloatingActionButton
        if (fab.visibility != View.GONE) {
            val animation = AnimationUtils.loadAnimation(this, R.anim.slide_out_down)
            fab.startAnimation(animation)
            fab.hide()
        }
    }

    override fun finish() {

        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)

    }

    fun checkUpdate() {
        val update = Update(this, false)
        update.doInBackground()
    }

    fun setVersionPreferences() {
        val verPref = getSharedPreferences("version_tests", AppCompatActivity.MODE_PRIVATE)
        val verTheory = getSharedPreferences("theory_tests", AppCompatActivity.MODE_PRIVATE)

        val subject_prefix = resources.getStringArray(R.array.subjects_prefix)
        for (i in subject_prefix.indices) {
            var check = verPref.getString(subject_prefix[i], "null")
            if (check!!.contentEquals("null")) {
                val ed = verPref.edit()
                ed.putString(subject_prefix[i], "0")
                ed.commit()
                Log.d("myLog", "Версия предмета " + subject_prefix[i] + " = null")
            } else
                Log.d("myLog", "Версия предмета " + subject_prefix[i] + " = " + check)
            check = verTheory.getString(subject_prefix[i], "null")
            if (check!!.contentEquals("null")) {
                val ed = verTheory.edit()
                ed.putString(subject_prefix[i], "0")
                ed.commit()
                Log.d("myLog", "Версия предмета " + subject_prefix[i] + " = null")
            }
        }

    }

    fun showMessage(title: String, message: String) {

        val ad = AlertDialog.Builder(this)
        ad.setTitle(title)
        ad.setMessage(message)

        ad.setNegativeButton("Да") { dialog, arg1 -> finish() }

        ad.setPositiveButton("Нет") { dialog, arg1 -> }

        ad.setCancelable(true)
        ad.setOnCancelListener { }

        ad.show()
    }

    fun showAuthorizeMessage(title: String, message: String, change_user: Boolean) {

        val ad = AlertDialog.Builder(this)
        ad.setTitle(title)
        ad.setMessage(message)

        ad.setNegativeButton("Да") { dialog, arg1 ->
            if (change_user) {
                MyDB.removeUser(MyDB.getUser().login)
                val sessionObject = Session("", SessionState.anonymus)
                val settings = Settings()
                settings.setSession(sessionObject, context)
                settings.setLoginAndPassword("", "", context)
            }
            val intent = Intent(context, LoginActivity::class.java)
            if (Build.VERSION.SDK_INT > 20) {
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this)
                startActivity(intent, options.toBundle())
            } else {
                startActivity(intent)
            }

            //startActivity(intent)
        }

        ad.setPositiveButton("Нет") { dialog, arg1 -> }

        ad.setCancelable(true)
        ad.setOnCancelListener { }

        ad.show()
    }

    fun showUpdateMessage(title: String, message: String) {

        val ad = AlertDialog.Builder(context)
        ad.setTitle(title)
        ad.setMessage(message)

        ad.setNegativeButton("Да") { dialog, arg1 ->
            if (Connection.hasConnection(context)) {
                val update = Update(this, false)
                update.doInBackground()
            }
        }

        ad.setPositiveButton("Нет") { dialog, arg1 -> }

        ad.setCancelable(true)
        ad.setOnCancelListener { }

        ad.show()
    }

    companion object {
        internal var instance: Bundle? = null


        lateinit var user: User

        private val SALT = byteArrayOf(-46, 65, 30, -128, -113, -103, -57, 74, -64, 51, 88, -95, -45, 77, -117, -36, -11, 32, -64, 89)
    }
}
