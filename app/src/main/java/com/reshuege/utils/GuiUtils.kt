package com.reshuege.utils


import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AlertDialog
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast


/**
 * Набор вспомогательных методов для UI
 *
 */
object GuiUtils {

    fun setTextAndVisibility(textView: TextView, text: String) {
        if (AppTextUtils.isEmpty(text)) {
            textView.visibility = View.GONE
        } else {
            textView.text = text
            textView.visibility = View.VISIBLE
        }
    }

    /**
     * Скрываем клавиатуру
     *
     * @param v любая [View]
     */
    fun hideKeyboard(v: View?) {
        if (v != null && v.context != null) {
            if (!v.isFocused) {
                v.requestFocus()
            }
            val imm = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    fun displayKeyboard(v: View?) {
        if (v != null && v.context != null) {
            if (!v.isFocused) {
                v.requestFocus()
            }
            val imm = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }
    }

    fun toDp(context: Context, px: Int): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                px.toFloat(),
                context.resources.displayMetrics)
    }

    /**
     * Добавление слушателя [OnSoftInputStateListener] на состояние клавиатуры<br></br>
     *
     * @param screenRootView           корневой [View] в разметке экрана
     * @param onSoftInputStateListener callback [OnSoftInputStateListener]
     */
    fun addSoftInputStateListener(screenRootView: View, onSoftInputStateListener: OnSoftInputStateListener?) {
        screenRootView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            private val HEIGHT_ROOT_THRESHOLD = 100

            override fun onGlobalLayout() {
                val thresold = screenRootView.height / 3
                val rootViewHeight = screenRootView.rootView.height
                val viewHeight = screenRootView.height
                val heightDiff = rootViewHeight - viewHeight
                if (heightDiff > thresold) {
                    onSoftInputStateListener?.onOpened()
                } else {
                    onSoftInputStateListener?.onClosed()
                }
            }
        })
    }

    fun openEmailForFeedback(context: Context) {
        val i = Intent()
        i.action = Intent.ACTION_SEND
        i.type = "message/rfc822"
        i.putExtra(Intent.EXTRA_EMAIL, arrayOf("recipient@example.com"))
        i.putExtra(Intent.EXTRA_SUBJECT, "Обращение в службу технической поддержки приложения")
        i.type = "text/plain"
        try {
            context.startActivity(Intent.createChooser(i, "Выберите приложение.."))
        } catch (ex: android.content.ActivityNotFoundException) {
            Toast.makeText(context, "Для обращения в службу технической поддержки необходимо исопльзовать E-mail клиент, установите любой E-mail клиент", Toast.LENGTH_SHORT).show()
        }

    }

    fun displayUnknownError(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("Произошла непредвиденная ошибка, попробуйте повторить операцию позже")
        builder.setPositiveButton("OK") { dialog, which -> }
        try {
            builder.show()
        } catch (ignored: WindowManager.BadTokenException) {
        }

    }

    fun displayWebViewDialog(context: Context, message: String) {
        val builder = AlertDialog.Builder(context)
        val view = WebView(context)
        view.webViewClient = WebViewClient()
        view.loadDataWithBaseURL(null, message, "text/html", "utf-8", null)
        val padding = DisplayMetricUtils.convertDpToPixel(16f, context).toInt()
        view.setPadding(padding, padding, padding, padding)
        builder.setView(view)
        builder.setPositiveButton("OK") { dialog, which -> }
        try {
            builder.show()
        } catch (ignored: WindowManager.BadTokenException) {
        }

    }

    fun displayUnknownError(context: Context, okListener: DialogInterface.OnClickListener) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("Произошла непредвиденная ошибка, попробуйте повторить операцию позже")
        builder.setPositiveButton("OK", okListener)
        try {
            builder.show()
        } catch (ignored: WindowManager.BadTokenException) {
        }

    }

    fun displayOkMessage(context: Context, message: String, listener: DialogInterface.OnClickListener, cancelable: Boolean) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(message)
        builder.setPositiveButton("OK", listener)
        builder.setCancelable(cancelable)
        try {
            builder.show()
        } catch (ignored: WindowManager.BadTokenException) {
            ignored.printStackTrace()
        }

    }

    fun displayOkMessage(context: Context, message: Int, okListener: DialogInterface.OnClickListener) {
        displayOkMessage(context, context.getString(message), okListener)
    }

    fun displayOkMessage(context: Context, message: Int, title: Int, okListener: DialogInterface.OnClickListener) {
        displayOkMessage(context, context.getString(message), context.getString(title), okListener)
    }

    fun displayOkMessage(context: Context, message: String, okListener: DialogInterface.OnClickListener) {
        displayOkMessage(context, message, okListener, true)
    }


    fun displayOkMessage(context: Context, message: String, title: String, okListener: DialogInterface.OnClickListener?) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(message)
        builder.setTitle(title)
        builder.setPositiveButton("OK", okListener)
        try {
            builder.show()
        } catch (ignored: WindowManager.BadTokenException) {
        }

    }

    fun checkEditTextFieldsIsEmpty(vararg views: EditText): Boolean {
        for (view in views) {
            if (view.text.toString().trim { it <= ' ' }.isEmpty()) return true
        }
        return false
    }

    fun showError(context: Context, view: EditText, messageId: Int) {
        view.error = context.getString(messageId)
        view.requestFocus()
    }

    fun openSystemBrowser(context: Context, url: String) {
        try {
            val browser = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(browser)
        } catch (ignored: ActivityNotFoundException) {
            Toast.makeText(context, "Приложение для просмотра ссылки не обнаружено", Toast.LENGTH_SHORT).show()
        }

    }

    interface OnSoftInputStateListener {

        fun onOpened()

        fun onClosed()
    }

    fun getStatusBarHeight(context: Context): Int {
        var result = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = context.resources.getDimensionPixelSize(resourceId)
            }
        }
        return result
    }

    fun browseAppInGooglePlayMarket(context: Context) {
        val appPackageName = context.packageName
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
        } catch (anfe: android.content.ActivityNotFoundException) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
        }

    }

}
