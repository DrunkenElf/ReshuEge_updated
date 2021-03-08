package com.reshuege

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.reshuege.DataBase.AppDatabase
import com.reshuege.DataBase.User
import com.reshuege.databinding.ActivityLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
 class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var db: AppDatabase

    lateinit var binding: ActivityLoginBinding


    fun isLogged() {
        lifecycleScope.launch(Dispatchers.IO){
            _user.postValue(db.userDao().getUserList().firstOrNull())

        }

        user.observe(this) {
            lifecycleScope.launch(Dispatchers.Main) {
                delay(1900)
                val tmp = it
                if (tmp == null) {
                    startLoginAct()
                } else if (tmp.session_id != null && tmp.password != null && tmp.logged) {
                    Log.d("Used data exist", tmp.toString())
                    //start main
                    startMainActivity(tmp)
                } else
                    startLoginAct()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        lifecycleScope.launch(Dispatchers.Main){ isLogged() }
    }

    override fun onResume() {
        lifecycleScope.launch(Dispatchers.Main){ isLogged() }
        super.onResume()
    }



    private fun startMainActivity(user: User){
        Log.d("startMain", user.toString())
        val startupIntent = Intent(this, MainMenu::class.java)
        startActivity(startupIntent)
        //this.finish()
    }

    private fun startLoginAct() {
        Log.d("startLogin", "no user")
        val startupIntent = Intent(this, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(startupIntent)
    }

    private val _user = MutableLiveData<User?>()

    val user: LiveData<User?> get() = _user
}