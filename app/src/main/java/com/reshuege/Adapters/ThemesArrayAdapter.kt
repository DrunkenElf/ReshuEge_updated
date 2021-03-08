package com.reshuege.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

import com.reshuege.Connection
import com.reshuege.DataBase.AppDatabase
import com.reshuege.DownloadTasks.DownloadThemesTasks
import com.reshuege.R

class ThemesArrayAdapter(context: Context,
                         private val values: Array<String?>,
                         private val subject_prefix: String)
    : ArrayAdapter<String>(context, R.layout.themes_list_item, values) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView = inflater.inflate(R.layout.themes_list_item, parent, false)
        val theme = rowView.findViewById<View>(R.id.theme) as TextView
        val themeIcon = rowView.findViewById<View>(R.id.themeIcon) as ImageView
        val themeReloadIcon = rowView.findViewById<View>(R.id.themeReloadIcon) as ImageView

        val themeId = Integer.valueOf(values[position]!!.substring(values[position]!!.indexOf("$") + 1))
        val themeName = values[position]!!.replace(values[position]!!.substring(values[position]!!.indexOf("$")), "")
        theme.setText(themeName)
        if (values[position]!!.contains("⚫")) {
            themeIcon.visibility = View.GONE
        } else {
            themeIcon.visibility = View.VISIBLE
            themeReloadIcon.visibility = View.GONE
        }

        val displaymetrics = context.resources.displayMetrics

        val themeDao = AppDatabase(context).themeDao()

        if (themeDao.getTheme(subject_prefix).isNotEmpty()) {
            themeReloadIcon.visibility = View.VISIBLE
            if (values[position]!!.contains("⚫")) {
                theme.setPadding((theme.paddingLeft - 25 * displaymetrics.density).toInt(), 0, (40 * displaymetrics.density).toInt(), 0)
            } else {
                theme.setPadding(theme.paddingLeft, 0, (40 * displaymetrics.density).toInt(), 0)
            }
        } else {
            themeReloadIcon.visibility = View.GONE
            if (values[position]!!.contains("⚫")) {
                theme.setPadding((theme.paddingLeft - 25 * displaymetrics.density).toInt(), 0, 0, 0)
            }
        }

        themeReloadIcon.setOnClickListener { showMessage("Что-то пошло не так?", "Загрузить задания заново?", subject_prefix, themeId.toString()) }

        return rowView
    }

    fun showMessage(title: String, message: String, prefix: String, theme: String) {
        val ad = androidx.appcompat.app.AlertDialog.Builder(context)
        ad.setTitle(title)
        ad.setMessage(message)

        ad.setNegativeButton("Да") { dialog, arg1 ->
            if (Connection.hasConnection(context)) {
                val questions = DownloadThemesTasks(context, prefix, theme)
                questions.execute()
            }
        }

        ad.setPositiveButton("Нет") { dialog, arg1 -> }

        ad.setCancelable(true)
        ad.setOnCancelListener { }

        ad.show()

    }
}
