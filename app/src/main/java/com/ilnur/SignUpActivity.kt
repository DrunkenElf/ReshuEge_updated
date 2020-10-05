package com.ilnur

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.transition.Slide
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText

import org.json.JSONObject

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.math.BigInteger
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.regex.Pattern

import javax.net.ssl.HttpsURLConnection

import com.ilnur.utils.DatePickerUtils
import com.ilnur.utils.GuiUtils

class SignUpActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {
    internal lateinit var email: EditText
    internal lateinit var password: EditText
    internal lateinit var confirmPassword: EditText
    internal lateinit var name: EditText
    internal lateinit var surname: EditText
    internal lateinit var birthdayEdit: EditText
    internal var status: EditText? = null
    internal var statusType: String? = null
    private var birthday: Date? = null
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

    private val isAllValid: Boolean
        get() {
            var result = true
            if (email.text.toString().isEmpty()) {
                setError(email, getString(R.string.email_error))
                result = false
            } else if (!validate(email.text.toString(), EMAIL_PATTERN)) {
                setError(email, getString(R.string.email_validate_error))
                result = false
            }
            if (password.text.toString().isEmpty()) {
                setError(password, getString(R.string.password_error))
                result = false
            }
            if (confirmPassword.text.toString().isEmpty()) {
                setError(confirmPassword, getString(R.string.confirm_password_error))
                result = false
            }
            if (password.text.toString() != confirmPassword.text.toString()) {
                setError(confirmPassword, getString(R.string.compare_password_error))
                result = false
            }
            if (name.text.toString().isEmpty()) {
                setError(name, getString(R.string.name_error))
                result = false
            }
            if (surname.text.toString().isEmpty()) {
                setError(surname, getString(R.string.surname_error))
                result = false
            }
            return result
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupAnim()
        setContentView(R.layout.activity_sign_up)
        statusType = null
        email = findViewById<View>(R.id.email) as EditText
        password = findViewById<View>(R.id.password) as EditText
        confirmPassword = findViewById<View>(R.id.confirm_password) as EditText
        name = findViewById<View>(R.id.name) as EditText
        surname = findViewById<View>(R.id.surname) as EditText
        birthdayEdit = findViewById<View>(R.id.birthday) as EditText
        birthdayEdit.setOnClickListener {
            showDatePickerDialog()
            birthdayEdit.requestFocus()
        }
        birthdayEdit.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                showDatePickerDialog()
                birthdayEdit.requestFocus()
            }
        }
        status = findViewById<View>(R.id.status) as EditText

        val questionsAdapter = ArrayAdapter(this, R.layout.dialog_item, R.id.item, resources.getStringArray(R.array.state_array))
        status!!.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                setDialog(questionsAdapter)
                status!!.requestFocus()
            }
        }
        status!!.setOnClickListener {
            setDialog(questionsAdapter)
            status!!.requestFocus()
        }
        val signUp = findViewById<View>(R.id.sign_up) as Button
        signUp.setOnClickListener { v ->
            GuiUtils.hideKeyboard(v)
            if (isAllValid) {
                val signUpTask = SignUpTask(email.text.toString().toLowerCase(),
                        password.text.toString(),
                        name.text.toString(),
                        surname.text.toString(),
                        birthdayEdit.text.toString(),
                        statusType,
                        MD5(hash + email.text.toString()))
                signUpTask.execute()
            } else {
                GuiUtils.displayOkMessage(this@SignUpActivity, "Ошибка",
                        "Проверьте заполненность всех полей", null)
            }
        }
    }

    protected fun showDatePickerDialog() {
        val date = Calendar.getInstance()
        if (birthday != null) {
            date.time = birthday
        }
        DatePickerUtils.showCommonDateDialog(this, this, date)
    }

    override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val c = Calendar.getInstance()
        c.set(year, monthOfYear, dayOfMonth)
        birthday = c.time
        birthdayEdit.setText(SimpleDateFormat("yyyy-MM-dd").format(c.time))
        birthdayEdit.error = null
    }

    private fun setDialog(adapter: ArrayAdapter<String>) {
        androidx.appcompat.app.AlertDialog.Builder(this).setAdapter(adapter) { dialog, which ->
            status!!.setText(adapter.getItem(which))
            val statuses = resources.getStringArray(R.array.state_query)
            statusType = statuses[which]
            dialog.dismiss()
        }.setCancelable(false).create().show()
    }

    private fun setError(editText: EditText, error: String) {
        editText.error = error
    }

    private fun validate(target: String, pattern: String): Boolean {
        return Pattern
                .compile(pattern)
                .matcher(target)
                .matches()
    }

    fun MD5(md5: String): String {
        var messageDigest: MessageDigest? = null
        var digest = ByteArray(0)

        try {
            messageDigest = MessageDigest.getInstance("MD5")
            messageDigest!!.reset()
            messageDigest.update(md5.toByteArray())
            digest = messageDigest.digest()
        } catch (e: NoSuchAlgorithmException) {
            // тут можно обработать ошибку
            // возникает она если в передаваемый алгоритм в getInstance(,,,) не существует
            e.printStackTrace()
        }

        val bigInt = BigInteger(1, digest)
        var md5Hex = bigInt.toString(16)

        while (md5Hex.length < 32) {
            md5Hex = "0$md5Hex"
        }

        return md5Hex
    }

    inner class SignUpTask internal constructor(private val mEmail: String, private val mPassword: String, private val mName: String, private val mSurname: String, private val mBirthday: String, private val mStatus: String?, private val mHash: String) : AsyncTask<Void, Void, String>() {
        private var serverError = false
        internal lateinit var progressDialog: ProgressDialog

        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog = ProgressDialog(this@SignUpActivity)
            progressDialog.setMessage("Регистрация...")
            progressDialog.setCancelable(false)
            progressDialog.show()
        }

        override fun doInBackground(vararg params: Void): String {
            // TODO: attempt authentication against a network service.

            try {
                var data: ByteArray? = null
                var `is`: InputStream? = null
                var parameters = "username=" + mEmail + "&password=" + mPassword + "&name=" + URLEncoder.encode(mName, "UTF-8") + "&sname=" + URLEncoder.encode(mSurname, "UTF-8") + "&hash=" + mHash + "&" + Protocol.protocolVersion
                if (birthday != null) {
                    parameters = "$parameters&birthdate=$mBirthday"
                }
                if (status != null) {
                    parameters = "$parameters&status=$mStatus"
                }
                val url = URL("https://ege.sdamgia.ru/api?type=register")

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

                    //val buffer = ByteArray(8192) // Такого вот размера буфер
                    // Далее, например, вот так читаем ответ
                    //var bytesRead: Int
                    //while (`is`.read(data).let { bytesRead = it; it != -1 }) {
                   //     baos.write(buffer, 0, bytesRead)
                    //}
                    //data = baos.toByteArray()
                    str = `is`.bufferedReader().readText()
                    //str = String(data, charset("UTF-8"))
                } else {
                }

                val jObject = JSONObject(str)
                try {
                    val jObject2 = jObject.getJSONObject("data")
                    return "success"
                } catch (e: Exception) {
                    return jObject.getString("error")
                }

            } catch (e: Exception) {
                e.printStackTrace()
                serverError = true
                return ""
            }

        }

        override fun onPostExecute(result: String) {

            progressDialog.cancel()

            if (result == "duplicate email") {
                GuiUtils.displayOkMessage(this@SignUpActivity, "Данный e-mail уже занят.", "Ошибка при регистрации", null)
            } else if (serverError) {
                showMessage("Сервер РЕШУ ЕГЭ временно недоступен.", getString(R.string.error))
            } else if (result == "success") {

                GuiUtils.displayOkMessage(this@SignUpActivity, "Регистрация прошла успешно.", "Завершено",
                //object : DialogInterface.OnClickListener{ dialog, which -> this@SignUpActivity.onBackPressed() })
                object : DialogInterface.OnClickListener{
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        this@SignUpActivity.supportFinishAfterTransition()
                    }
                })
            }
        }

        override fun onCancelled() {}

        fun showMessage(title: String, message: String) {

            val ad = AlertDialog.Builder(this@SignUpActivity)
            ad.setTitle(title)
            ad.setMessage(message)

            ad.setPositiveButton("Продолжить") { dialog, arg1 -> }

            ad.setCancelable(true)
            ad.setOnCancelListener { }

            ad.show()
        }
    }

    companion object {

        val EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
        val hash = "quaeweSio7aingoo6wa1xoochethieJ6eishieph1eishai6Gi"
    }
}
