package com.ilnur

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import androidx.transition.TransitionManager
import androidx.transition.Visibility

import android.os.Handler
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.transition.Slide
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.lifecycleScope

import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputLayout
import com.ilnur.DataBase.MyDB

import org.json.JSONException
import org.json.JSONObject

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date

import javax.net.ssl.HttpsURLConnection

//import com.ilnur.DataBase.MyDB1
import com.ilnur.DownloadTasks.Update
import com.ilnur.Session.Session
import com.ilnur.Session.SessionState
import com.ilnur.Session.Settings
import com.ilnur.utils.SettingsImp
import com.ilnur.viewModel.LoginState
import com.ilnur.viewModel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * A login screen that offers login via email/password.
 */
@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    //internal lateinit var context: Context
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    val viewModel: LoginViewModel by viewModels()

    @Inject
    lateinit var settings: SettingsImp
    //private var mAuthTask: UserLoginTask? = null

    // UI references.
    lateinit var mEmailView: AutoCompleteTextView
    private var mPasswordView: EditText? = null

    //Placeholder holder;
    internal lateinit var group_main: Group
    internal lateinit var group_anim: Group

    //private View mProgressView;
    //private View mLoginFormView;
    private fun setupAnim() {

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupAnim()
        setContentView(R.layout.activity_login)
        // Set up the login form.

        //viewModel.addMainSubjs()

        //context = this

        Glide.with(this)
                .load(R.drawable.ball)
                .timeout(10)
                //.apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                .into(findViewById<View>(R.id.logo) as ImageView)
        Glide.with(this)
                .load(R.drawable.ball1)
                .timeout(10)
                //.apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                .into(findViewById<View>(R.id.packman) as ImageView)


        setupViews()

        viewModel.loginResult.observe(this, {
            it?.let {
                Log.d("loginRES OBS", it.toString())
                when (it) {
                    LoginState.SUCCESS -> {
                        lifecycleScope.launch { settings.setLogged(true) }
                        startActivity(Intent(this, MainMenu::class.java))
                    }
                    LoginState.ERROR -> {
                    }
                    LoginState.NO_INTERNET -> {
                    }
                    LoginState.WRONG_LOG_OR_PAS -> {
                    }
                    else -> Log.d("Nothing has happened", "уляля")
                }
                Log.d("THe last", it.toString())
            }
        })

        viewModel.loginFormState.observe(this, {
            it?.let {
                Log.d("loginFORM OBS", it.toString())
            }
        })
        /* mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });*/


        //mLoginFormView = findViewById(R.id.login_form);
        //mProgressView = findViewById(R.id.login_progress);
        /*SubjInfo si = new SubjInfo();
        si.context = context;
        si.check_subject_data();*/
    }

    private val loginWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            viewModel.updateLoginState(viewModel.loginFormState.value!!.copy(login = p0.toString()))
        }

        override fun afterTextChanged(p0: Editable?) = Unit

    }
    private val passwordWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            viewModel.updateLoginState(viewModel.loginFormState.value!!.copy(password = p0.toString()))
        }

        override fun afterTextChanged(p0: Editable?) = Unit

    }

    fun setupViews() {
        group_main = findViewById(R.id.group_main)
        group_anim = findViewById(R.id.group_anim)
        mEmailView = findViewById<View>(R.id.email) as AutoCompleteTextView

        mEmailView.addTextChangedListener(loginWatcher)

        val mEmailSignInButton = findViewById<View>(R.id.email_sign_in_button) as Button
        val mSkipButton = findViewById<View>(R.id.skip_button) as Button
        val signUpButton = findViewById<View>(R.id.sign_up) as Button
        mPasswordView = findViewById<View>(R.id.password) as EditText

        mPasswordView!!.addTextChangedListener(passwordWatcher)

        mEmailView.setText(viewModel.loginFormState.value!!.login)

        mPasswordView!!.setText(viewModel.loginFormState.value!!.password)

        mPasswordView!!.setOnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE) {
                mEmailSignInButton.performClick()
                true
            }
            false
        }
        viewModel.loginFormState.observe(this, {
            when (it.checking) {
                true -> if (mEmailSignInButton.isEnabled) {
                    mEmailSignInButton.isEnabled = false
                }
                false -> {
                    if (!mEmailSignInButton.isEnabled)
                        mEmailSignInButton.isEnabled = true
                }
            }
        })
        //mEmailSignInButton.
        mEmailSignInButton.setOnClickListener { view ->
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS)

            ///mEmailSignInButton.isClickable = false //turned off button
            viewModel.updateLoginState(viewModel.loginFormState.value!!.copy(checking = true))

            viewModel.login(viewModel.loginFormState.value!!.login, viewModel.loginFormState.value!!.password)
            attemptLogin()
        }

        mSkipButton.setOnClickListener {
            val sessionObject = Session("", SessionState.anonymus)


            try {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                if (currentFocus != null)
                    imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val intent = Intent(this, MainMenu::class.java)
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this)
            startActivity(intent, options.toBundle())

            //this.finishAfterTransition()
            //Update(context,true).doInBackground()
            //finish()
        }

        signUpButton.setOnClickListener {
            /*val intent = Intent(this, SignUpActivity::class.java)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this)
            startActivity(intent, options.toBundle())*/
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {


        val logg = findViewById<TextInputLayout>(R.id.input_email)
        val pass = findViewById<TextInputLayout>(R.id.input_password)

        // Reset errors.
        //mEmailView.setError(null);
        //mPasswordView.setError(null);
        logg.error = null
        pass.error = null

        // Store values at the time of the login attempt.
        val email = mEmailView!!.text.toString()
        val password = mPasswordView!!.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            pass.error = getString(R.string.error_field_required)
            focusView = mPasswordView
            cancel = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            logg.error = getString(R.string.error_field_required)
            focusView = mEmailView
            cancel = true
        } else if (!isEmailValid(email)) {
            logg.error = getString(R.string.error_invalid_email)
            focusView = mEmailView
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView!!.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            try {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                if (currentFocus != null)
                    imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            //logg.setVisibility(View.GONE);
            //pass.setVisibility(View.GONE);

            showProgress(true, false)
            //mAuthTask = UserLoginTask(email, password)
            //mAuthTask!!.execute(null as Void?)
        }
    }

    private fun isEmailValid(email: String): Boolean {
        //TODO: Replace this with your own logic
        return email.contains("@")
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    //@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean, is_Canceled: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        /* TextInputLayout logg = findViewById(R.id.input_email);
        TextInputLayout pass = findViewById(R.id.input_password);
        ImageView logo = findViewById(R.id.logo);
        TextView title = findViewById(R.id.textView2);*/
        val view = findViewById<View>(android.R.id.content) as ViewGroup
        val slideIn = androidx.transition.Slide()
        if (show && !is_Canceled) {
            /*logg.setVisibility(View.GONE);
            pass.setVisibility(View.GONE);
            logo.setVisibility(View.GONE);
            title.setVisibility(View.GONE);*/
            //holder.setContentId(R.id.packman);
            //ViewGroup vg1 = ViewGroup(group_main);
            //TransitionManager.beginDelayedTransition(view);
            //TransitionManager.beginDelayedTransition(view, slideIn);
            slideIn.mode = Visibility.MODE_OUT
            slideIn.slideEdge = Gravity.LEFT
            group_main.visibility = View.INVISIBLE
            //TransitionManager.beginDelayedTransition(view);
            TransitionManager.beginDelayedTransition(view, slideIn)
            group_anim.visibility = View.VISIBLE
            //group_main.requestLayout();
            //ImageView gif = findViewById(R.id.packman);
            //gif.setVisibility(View.VISIBLE);
            //TransitionManager.beginDelayedTransition(view);
            /*Glide.with(this)
                    .load(R.drawable.ball)
                    .into((ImageView) findViewById(R.id.packman));*/
            //group_anim.requestLayout();
        } else if (!show && is_Canceled) {

        } else {
            slideIn.mode = Visibility.MODE_IN
            slideIn.slideEdge = Gravity.RIGHT
            //TransitionManager.beginDelayedTransition(view);
            group_anim.visibility = View.GONE
            TransitionManager.beginDelayedTransition(view, slideIn)
            group_main.visibility = View.VISIBLE

            //group_main.requestLayout();
            //group_anim.requestLayout();
        }
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

           *//* mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });*//*
            Glide.with(this)
                    .load(R.drawable.ball)
                    .timeout(100)
                    .into((ImageView) findViewById(R.id.packman));
            *//*mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });*//*
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            //mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            //mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }*/
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     *//*
    inner class UserLoginTask internal constructor(private val mEmail: String, private val mPassword: String) : AsyncTask<Void, Void, String>() {
        private var serverError = false

        override fun doInBackground(vararg params: Void): String {
            // TODO: attempt authentication against a network service.

            try {
                var data: ByteArray? = null
                var `is`: InputStream? = null
                val parameters = "user=" + mEmail + "&password=" + mPassword + "&" + Protocol.protocolVersion
                val url = URL("https://ege.sdamgia.ru/api?type=login")

                val urlConnection = url.openConnection() as HttpsURLConnection
                urlConnection.requestMethod = "POST"
                urlConnection.doInput = true
                urlConnection.doOutput = true
                urlConnection.setRequestProperty("Content-Length", "" + Integer.toString(parameters.toByteArray().size))
                val os = urlConnection.outputStream
                data = parameters.toByteArray(charset("UTF-8"))
                os.write(data)
                data = null
                urlConnection.connect()
                val responseCode = urlConnection.responseCode

                val baos = ByteArrayOutputStream()
                var str: String? = null
                if (responseCode == 200) {
                    `is` = urlConnection.inputStream

                    val buffer = ByteArray(8192) // Такого вот размера буфер
                    // Далее, например, вот так читаем ответ
                    var bytesRead: Int
                    while (`is`.read(buffer).let { bytesRead = it; it != -1 }) {
                        baos.write(buffer, 0, bytesRead)
                    }
                    data = baos.toByteArray()
                    str = String(data, charset("UTF-8"))
                } else {
                }

                val jObject = JSONObject(str)
                try {
                    val jObject2 = jObject.getJSONObject("data")
                    return jObject2.getString("session")
                } catch (e: Exception) {
                    val jObject2 = jObject.getString("error")
                    return ""
                }

            } catch (ioe: IOException) {
                Log.i("errorLOG", ioe.message.toString())
                serverError = true
                return ""
            } catch (jse: JSONException) {
                Log.i("errorLOG", jse.message.toString())
                serverError = false
                return ""
            } catch (e: Exception) {
                Log.i("errorLOG", e.message.toString())
                serverError = true
                return ""
            }
        }

        override fun onPostExecute(session: String) {
            mAuthTask = null
            val handler = Handler()
            val sdf = SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z")
            val d = sdf.format(Date())
            //Log.i("date", "_"+d);
            //Log.i("date", d.split(" ")[0].split("\\.")[2]);
            //showProgress(false);
            val r = Runnable {
                showProgress(false, false)
                if (!Connection.hasConnectionMain(context, true)) {
                    *//*Session sessionObject = new Session("", SessionState.anonymus);
                            Settings settings = new Settings();
                            settings.setSession(sessionObject, context);
                            settings.setLoginAndPassword("", "", context);*//*
                    Toast.makeText(context, "Для авторизации необобходимо подключение к интернету", Toast.LENGTH_SHORT).show()
                } else
                //what ever you do here will be done after 3 seconds delay.
                    if (session != "") {
                        val sessionObject = Session(session, SessionState.authorized)
                        val settings = Settings()
                        settings.setSession(sessionObject, this)
                        settings.setLoginAndPassword(mEmail, mPassword, context)
                        MyDB.updateUser(mEmail, mPassword, session)
                        Update(context,true).doInBackground()
                        //finish()
                    } else {
                        if (serverError) {
                            showMessage("Сервер РЕШУ ЕГЭ временно недоступен", getString(R.string.error))

                        } else {
                            mPasswordView!!.error = getString(R.string.error_incorrect_password)
                            mPasswordView!!.requestFocus()
                        }
                    }
            }
            handler.postDelayed(r, 2200)


            *//* if (!session.equals("")) {
                Session sessionObject = new Session(session, SessionState.authorized);
                Settings settings = new Settings();
                settings.setSession(sessionObject, context);
                settings.setLoginAndPassword(mEmail, mPassword, context);
                Intent intent = new Intent(context, MainMenu.class);
                startActivity(intent);

            } else {
                if (serverError) {
                    showMessage("Сервер РЕШУ ЕГЭ временно недоступен.", getString(R.string.error));
                } else {
                    mPasswordView.setError(getString(R.string.error_incorrect_password));
                    mPasswordView.requestFocus();
                }
            }*//*
        }

        override fun onCancelled() {
            mAuthTask = null
            showProgress(false, true)
        }

        fun showMessage(title: String, message: String) {

            val ad = AlertDialog.Builder(context)
            ad.setTitle(title)
            ad.setMessage(message)

            ad.setPositiveButton("Продолжить") { dialog, arg1 -> }

            ad.setCancelable(true)
            ad.setOnCancelListener { }

            ad.show()
        }
    }
*/
    companion object {

        /**
         * Id to identity READ_CONTACTS permission request.
         */
        private val REQUEST_READ_CONTACTS = 0

        /**
         * A dummy authentication store containing known user names and passwords.
         * TODO: remove after connecting to a real authentication system.
         */
        private val DUMMY_CREDENTIALS = arrayOf("foo@example.com:hello", "bar@example.com:world")
    }
}

