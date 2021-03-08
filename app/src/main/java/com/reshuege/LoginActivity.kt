package com.reshuege

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.transition.TransitionManager
import androidx.transition.Visibility

import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.constraintlayout.helper.widget.Layer
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope

import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputLayout

import com.reshuege.Session.Session
import com.reshuege.Session.SessionState
import com.reshuege.databinding.ActivityLoginBinding
import com.reshuege.utils.SettingsImp
import com.reshuege.viewModel.LoginFormState
import com.reshuege.viewModel.LoginState
import com.reshuege.viewModel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * A login screen that offers login via email/password.
 */
@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    val viewModel: LoginViewModel by viewModels()

    lateinit var binding: ActivityLoginBinding

    @Inject
    lateinit var settings: SettingsImp

    lateinit var mEmailView: AutoCompleteTextView
    private var mPasswordView: EditText? = null

    internal lateinit var input_layer: Layer

    fun setTrans(startState: Int, endState: Int, duration: Int = 500){
        binding.motionLayout.setTransition(startState, endState)
        binding.motionLayout.setTransitionDuration(duration)
        binding.motionLayout.transitionToEnd()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

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
                        setTrans(R.id.login_attempt_end, R.id.login_attempt_start)
                        lifecycleScope.launch(Dispatchers.Main) {
                            settings.setLogged(true)
                            setTrans(R.id.start, R.id.end, 1000)
                            delay(1250)
                            startActivity(Intent(this@LoginActivity, MainMenu::class.java))
                        }
                    }
                    LoginState.ERROR -> {

                    }
                    LoginState.NO_INTERNET -> {
                    }
                    LoginState.WRONG_LOG_OR_PAS -> {
                    }
                    LoginState.DEFAULT -> { //used to skip sign in
                        setTrans(R.id.login_attempt_end, R.id.login_attempt_start)
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
    }

    private val loginWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            viewModel.updateLoginState(
                (viewModel.loginFormState.value ?: LoginFormState()).copy(login = p0.toString()))
        }

        override fun afterTextChanged(p0: Editable?) = Unit

    }
    private val passwordWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            viewModel.updateLoginState(
                (viewModel.loginFormState.value ?: LoginFormState()).copy(password = p0.toString()))
        }

        override fun afterTextChanged(p0: Editable?) = Unit

    }

    fun setupViews() {
        input_layer = findViewById(R.id.layer_input)

        mEmailView = findViewById<View>(R.id.email) as AutoCompleteTextView

        mEmailView.addTextChangedListener(loginWatcher)

        val mEmailSignInButton = findViewById<View>(R.id.email_sign_in_button) as Button
        val mSkipButton = findViewById<View>(R.id.skip_button) as Button
        val signUpButton = findViewById<View>(R.id.sign_up) as Button
        mPasswordView = findViewById<View>(R.id.password) as EditText

        mPasswordView!!.addTextChangedListener(passwordWatcher)

        mEmailView.setText((viewModel.loginFormState.value ?: LoginFormState()).login)

        mPasswordView?.setText((viewModel.loginFormState.value ?: LoginFormState()).password)

        mEmailView.setOnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE)
                mEmailSignInButton.performClick()
            false
        }


        mEmailView.setOnKeyListener { v, id, event ->
            if (event.action == KeyEvent.ACTION_DOWN && id == KeyEvent.KEYCODE_ENTER) true
            false
        }

        mPasswordView!!.setOnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE)
                mEmailSignInButton.performClick()
            false
        }
        viewModel.loginFormState.observe(this, {
            when (it.checking) {
                true -> if (mEmailSignInButton.isEnabled) {
                    mEmailSignInButton.isEnabled = false
                    Log.d("login clicked", "isEnabled = " +
                            "${mEmailSignInButton.isEnabled}")
                }
                false -> {
                    if (!mEmailSignInButton.isEnabled)
                        mEmailSignInButton.isEnabled = true
                    Log.d("login clicked", "isEnabled = " +
                            "${mEmailSignInButton.isEnabled}")
                }
            }
        })
        mEmailSignInButton.setOnClickListener { view ->
            //setTrans(motion_layout.currentState, R.id.init_gone_launch)
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS)
            Log.d("emailSignBtn", "formData: ${viewModel.loginFormState.value.toString()}")
            ///mEmailSignInButton.isClickable = false //turned off button
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
            lifecycleScope.launch(Dispatchers.Main){
                setTrans(R.id.start, R.id.end, 1000)
                delay(1200)
                val intent = Intent(this@LoginActivity, MainMenu::class.java)
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this@LoginActivity)
                startActivity(intent, options.toBundle())
            }
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

        logg.error = null
        pass.error = null

        // Store values at the time of the login attempt.
        val email = mEmailView.text.toString()
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
            setTrans(R.id.login_attempt_start, R.id.login_attempt_end)
            viewModel.updateLoginState(viewModel.loginFormState.value!!.copy(checking = true))

            viewModel.login(viewModel.loginFormState.value!!.login, viewModel.loginFormState.value!!.password)

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
            //group_main.visibility = View.INVISIBLE
            //TransitionManager.beginDelayedTransition(view);
            TransitionManager.beginDelayedTransition(view, slideIn)
            //group_anim.visibility = View.VISIBLE
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

            TransitionManager.beginDelayedTransition(view, slideIn)
        }


        fun start_anim(){
            input_layer.translationY
        }

    }

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

