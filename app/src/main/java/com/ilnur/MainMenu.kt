package com.ilnur


//import vdd.test.Fragments.SubjectsFragment;
import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.*
import android.transition.Slide
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
//import androidx.navigation.NavController
import androidx.navigation.NavController
import androidx.navigation.findNavController

import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.ilnur.Adapters.SubjAdapter
//import com.ilnur.DataBase.MyDB1
import com.ilnur.DataBase.QuestionsDataBaseHelper
import com.ilnur.DownloadTasks.*
import com.ilnur.Fragments.*
import com.ilnur.Session.Session
import com.ilnur.Session.SessionState
import com.ilnur.Session.Settings
import com.ilnur.backend.Downloaders
import com.ilnur.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.lang.Runnable
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

@AndroidEntryPoint
class MainMenu : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, SubjectsFragment.OnFragmentInteractionListener, StartPageFragment.OnFragmentInteractionListener, SubjectsTheoryFragment.OnFragmentInteractionListener, SubjectSearchFragment.OnFragmentInteractionListener, SubjectThemesFragment.OnFragmentInteractionListener {

    val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var downloader: Downloaders
    //internal val viewModel = Stack<String>()
    //internal lateinit var context: Context
    private var mHandler: Handler? = null
    lateinit var fragment: Fragment

    private fun setupAnim() {
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

    private fun createNotificationChannel() {
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
            //startForegroundService()
        }
    }

    fun observeTitle(){
        viewModel.title.observe(this, {
            supportActionBar!!.title = it
        })
    }

    fun serReceiver() {
        val receiver = BroadIntReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction("done")

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter)
    }


    class BroadIntReceiver(): BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val res = intent!!.getBooleanExtra("broadcastMessage", false)
            Log.d("BROAD", res.toString())
            //adapter.notifyDataSetChanged()
        }
    }

    //@SuppressLint("RestrictedApi")
    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        setupAnim()

         serReceiver()
        setContentView(R.layout.activity_main_menu)
        //serReceiver()
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        viewModel.launchCheck()
        //viewModel.getAllSubjects()

        observeTitle()

        if (viewModel.peek().isNullOrBlank())
            viewModel.push("РЕШУ ЕГЭ")
        Log.d("MAINACT", "onCreate")


        /*val si = SubjInfo()
        si.context = context
        si.check_subject_data()*/


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
            supportActionBar!!.setTitlected)e(itemSel
        } else {
            val startFragment = StartPageFragment()
            setFragment(startFragment, "START", savedInstanceState)
        }*/


        val updatePref = getSharedPreferences("updateShown", AppCompatActivity.MODE_PRIVATE)

        val ed = updatePref.edit()
        ed.putBoolean("shown", false)
        ed.apply()
        toggle.syncState()

    }


    override fun onBackPressed() {
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)

        } else if (getNavController().currentDestination != null && getNavController().currentDestination!!.id != R.id.nav_start_page) {
            Log.d("myLogs", getNavController().currentDestination.toString())
            getNavController().popBackStack()
            viewModel.pop()

            if (viewModel.peek() == null){
                viewModel.push("РЕШУ ЕГЭ")
            }

            viewModel.setTitle(viewModel.peek().toString())
            //supportActionBar!!.title = viewModel.peek()
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
        val id = item.itemId

        if (id == R.id.action_settings) {
            hideStartPage()
            getNavController().navigate(R.id.action_global_settingsFragment3)
            viewModel.push("Настройки")
            viewModel.setTitle("Настройки")
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
                if (getNavController().currentDestination?.id != R.id.nav_subj_search && viewModel.peek() != "Поиск") {
                    getNavController().navigate(R.id.action_global_nav_subj_search2) //
                    viewModel.setTitle("Поиск")
                    viewModel.push("Поиск")
                } else if (getNavController().currentDestination?.id == R.id.nav_subj_search && viewModel.peek() != "Поиск") {
                    viewModel.push("Поиск")
                    viewModel.setTitle("Поиск")
                    getNavController().navigate(R.id.action_nav_subj_search_self)
                }

                hideStartPage()
            }
            R.id.nav_news -> {
                if (Connection.hasConnection(this)) {
                    val downloadNews = DownloadNews(this)
                    downloadNews.execute()
                }
            }
            R.id.nav_stats -> {
                if (getNavController().currentDestination?.id != R.id.nav_subj_search && viewModel.peek() != "Статистика") {
                    viewModel.push("Статистика")
                    viewModel.setTitle("Статистика")
                    getNavController().navigate(R.id.action_global_nav_subj_search2) //
                    Log.d("Stats Nav1", viewModel.peek() + " " + getNavController().currentDestination?.toString())
                } else if (getNavController().currentDestination?.id == R.id.nav_subj_search && viewModel.peek() != "Статистика"){
                    viewModel.push("Статистика")
                    viewModel.setTitle("Статистика")
                    getNavController().navigate(R.id.action_nav_subj_search_self)
                    Log.d("Stats Nav3", viewModel.peek() + " " + getNavController().currentDestination?.toString())
                }
                hideStartPage()
            }
            R.id.nav_manual -> {
                if (getNavController().currentDestination?.id != R.id.nav_subj_search && viewModel.peek() != "Об экзамене") {
                    viewModel.push("Об экзамене")
                    viewModel.setTitle("Об экзамене")
                    getNavController().navigate(R.id.action_global_nav_subj_search2) //
                    Log.d("Stats Nav1", viewModel.peek() + " " + getNavController().currentDestination?.toString())
                } else if (getNavController().currentDestination?.id == R.id.nav_subj_search && viewModel.peek() != "Об экзамене"){
                    viewModel.push("Об экзамене")
                    viewModel.setTitle("Об экзамене")
                    getNavController().navigate(R.id.action_nav_subj_search_self)
                    Log.d("Stats Nav3", viewModel.peek() + " " + getNavController().currentDestination?.toString())
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
                viewModel.push("Настройки")
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

        Log.d("title", viewModel.getTitle().toString())
        //supportActionBar?.title = viewModel.peek()


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

    //bot done yet
    override fun onFragmentInteraction(subject: String) {//subj as href
        Log.d("onFrInter", "first")
        Log.d("onFrInter", subject)
        Log.d("onFrInter", viewModel.peek().toString())

        when (viewModel.peek()) {
            "Варианты" -> { //+
                val action = SubjectsFragmentDirections.actionNavSubjVarsToVariantsFragment(subject)
                getNavController().navigate(action)
                viewModel.setTitle("${href_to_subj(subject)}.${viewModel.peek()}")
                viewModel.push("Варианты")
            }
            "Каталог заданий" -> { //+
                val action = SubjectThemesFragmentDirections.actionNavSubjThemesToThemesFragment(subject)
                getNavController().navigate(action)
                viewModel.setTitle("${href_to_subj(subject)}.${viewModel.peek()}")
                viewModel.push("Каталог заданий")
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
            "Теория" -> {//hz
                Log.d("teor", href_to_subj(subject))
                Log.d("teor1", subject)
                val action = SubjectsTheoryFragmentDirections.actionNavSubjTheoryToTheoryFragment(subject)
                viewModel.setTitle("${href_to_subj(subject)}.${viewModel.peek()}")
                //viewModel.push("$subject.${viewModel.peek()}")
                viewModel.push("Теория" )
                getNavController().navigate(action)
            }
            "Поиск" -> { // doene
                val intent = Intent(this, SearchTypeActivity::class.java)
                intent.putExtra("subject_prefix", subject)
                intent.putExtra("subject_name", href_to_subj(subject))
                intent.putExtra("section", "Поиск")
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this)
                startActivity(intent, options.toBundle())
            }
            "Учителю" -> { // donei
                val session = Settings().getSession(this)
                if (session.sessionState == SessionState.anonymus) {
                    showAuthorizeMessage("Требуется авторизация", "Для просмотра статистики нужно " + "авторизоваться. Перейти к авторизации?", false)
                } else {
                    if (Connection.hasConnection(this)) {
                        val action = SubjectSearchFragmentDirections.actionNavTeacher1ToNavTeacher2(subject)
                        getNavController().navigate(action)
                        viewModel.setTitle("${href_to_subj(subject)}.${viewModel.peek()}")
                        viewModel.push("Учителю")
                    }
                }
            }
            "Статистика" -> { //done
                val session = Settings().getSession(this)
                if (session.sessionState == SessionState.anonymus) {
                    showAuthorizeMessage("Требуется авторизация", "Для просмотра статистики нужно " + "авторизоваться. Перейти к авторизации?", false)
                } else {
                    if (Connection.hasConnection(this)) {
                        val downloadStatistics = DownloadStatistics(this, subject, "session=" + session.session)
                        downloadStatistics.execute()
                    }
                }
            }
            "Об экзамене" -> { // done
                if (Connection.hasConnection(this)) {
                    val downloadManual = DownloadAboutExam(this, subject, href_to_subj(subject)) ///
                    downloadManual.execute()
                }
            }
            "О проекте" -> {//done
                if (Connection.hasConnection(this)) {
                    val downloadManual = DownloadAboutProject(this) ////
                    downloadManual.execute()
                }
            }
        }

        //supportActionBar?.title = subject+"."+itemSelected.peek()

    }

    fun href_to_subj(href: String): String {
        val subjectsArray = this.resources.getStringArray(R.array.subjects)
        val prefixArray = this.resources.getStringArray(R.array.subjects_prefix)
        for (i in prefixArray.indices) {
            if (prefixArray[i] == href) {
                return subjectsArray[i]
            }
        }
        return ""
    }

    //пока что сделано
    //фрагменты
    override fun onFragmentInteraction(itemSelected: String?, number: Int) {
        //hideStartPage();
        Log.d("onFrInter Int", itemSelected.toString())
        when(itemSelected){
            "Учителю" -> getNavController().navigate(StartPageFragmentDirections.actionNavStartPageToNavTeacher1())
            "Варианты" -> getNavController().navigate(StartPageFragmentDirections.actionNavStartPageToNavSubjVars())
            "Каталог заданий" -> getNavController().navigate(StartPageFragmentDirections.actionNavStartPageToNavSubjThemes())
            "Режим экзамена" -> getNavController().navigate(StartPageFragmentDirections.actionNavStartPageToNavSubj())
            "Теория" -> getNavController().navigate(StartPageFragmentDirections.actionNavStartPageToNavSubjTheory())
            "Поиск" -> getNavController().navigate(R.id.action_global_nav_subj_search2)
            "Статистика" -> getNavController().navigate(R.id.action_global_nav_subj_search2)
            "Об экзамене" -> getNavController().navigate(R.id.action_global_nav_subj_search2)
            "Настройки" -> getNavController().navigate(R.id.action_global_settingsFragment3)
        }
        val newSelected = if (itemSelected.isNullOrBlank()) "РЕШУ ЕГЭ" else itemSelected

        viewModel.push(newSelected)
        viewModel.setTitle(newSelected)
        hideStartPage()
        supportActionBar?.title = itemSelected //change title

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
                //MyDB.removeUser(MyDB.getUser().login)
                val sessionObject = Session("", SessionState.anonymus)
                val settings = Settings()
                settings.setSession(sessionObject, this)
                settings.setLoginAndPassword("", "", this)
            }
            val intent = Intent(this, LoginActivity::class.java)
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

        val ad = AlertDialog.Builder(this)
        ad.setTitle(title)
        ad.setMessage(message)

        ad.setNegativeButton("Да") { dialog, arg1 ->
            if (Connection.hasConnection(this)) {
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


        //lateinit var user: User

        private val SALT = byteArrayOf(-46, 65, 30, -128, -113, -103, -57, 74, -64, 51, 88, -95, -45, 77, -117, -36, -11, 32, -64, 89)
    }
}
