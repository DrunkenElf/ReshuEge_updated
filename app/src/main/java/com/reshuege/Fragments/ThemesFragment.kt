package com.reshuege.Fragments

import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.ListFragment

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat

import java.util.ArrayList

import com.reshuege.Adapters.ThemesArrayAdapter
import com.reshuege.Connection
import com.reshuege.DataBase.QuestionsDataBaseHelper
import com.reshuege.DownloadTasks.DownloadAllThemesTasks
import com.reshuege.DownloadTasks.DownloadThemesTasks
import com.reshuege.R
import com.reshuege.TestsActivity

class ThemesFragment : ListFragment() {

    internal var subject_prefix: String? = null
    internal var themes = ArrayList<String>()
    internal var themesId = ArrayList<Int>()
    internal var adapter: ThemesArrayAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

            subject_prefix = arguments?.getString("subject_prefix")


        return inflater.inflate(R.layout.fragment_listview, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            val qdbHelper = QuestionsDataBaseHelper(requireActivity(), subject_prefix!!)
            val db = qdbHelper.writableDatabase

            val cursor = db.query(subject_prefix!! + "_category_themes", null, null, null, null, null, null)

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        themes.add(cursor.getString(cursor.getColumnIndex("theme_name")))
                        themesId.add(cursor.getInt(cursor.getColumnIndex("theme_id")))
                    } while (cursor.moveToNext())
                }
            } else
                Log.d("myLogs", "Пустой курсор")

            cursor!!.close()
            db.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val themesArray = arrayOfNulls<String>(themes.size)
        for (i in themes.indices) {
            themesArray[i] = themes[i] + "$" + themesId[i]
        }

        adapter = ThemesArrayAdapter(requireActivity(), themesArray, subject_prefix!!)

        listAdapter = adapter
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                val builder = androidx.appcompat.app.AlertDialog.Builder(requireActivity())
                builder.setMessage("Для корректного отображения скачанных изображений требуется дать доступ к сохранению файлов.")
                        .setPositiveButton("Продолжить") { dialog, which ->
                            ActivityCompat.requestPermissions(requireActivity(),
                                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                                    1)
                        }
                builder.show()
            }
        }
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        val themeReloadImage = v.findViewById<View>(R.id.themeReloadIcon) as ImageView
        if (themesId[position] != 0) {
            if (themeReloadImage.visibility == View.GONE) {
                val themesArray = arrayOfNulls<String>(themesId.size)
                for (i in themesId.indices) {
                    themesArray[i] = themesId[i].toString()
                }
                showMessage("Отсутствуют задания", "Требуется загрузить задания. Скачать все темы сразу или отдельно выбранную?", subject_prefix, themesId[position].toString(), themesArray)
            } else {
                var pos = 0
                for (i in themesId.indices) {
                    if (themesId[i] == 0 || !themes[i].contains("⚫")) {
                        pos++
                    }
                    if (i == position) {
                        break
                    }
                }
                val intent = Intent(activity, TestsActivity::class.java)
                intent.putExtra("subject_prefix", subject_prefix)
                intent.putExtra("theme_number", themesId[position])
                intent.putExtra("position", pos)
                intent.putExtra("section", "Каталог заданий")
                if (Build.VERSION.SDK_INT > 20) {
                    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity())
                    startActivity(intent, options.toBundle())
                } else {
                    startActivity(intent)
                }
            }
        }
    }


    fun setSubject(subject: String) {
        subject_prefix = subject
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("subject_prefix", subject_prefix)
        super.onSaveInstanceState(outState)
    }

    fun showMessage(title: String, message: String, prefix: String?, theme: String, themes: Array<String?>) {
        val ad = androidx.appcompat.app.AlertDialog.Builder(requireActivity())
        ad.setTitle(title)
        ad.setMessage(message)


        ad.setNegativeButton("Все") { dialog, arg1 ->
            if (Connection.hasConnection(requireActivity())) {
                if (requireActivity().resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                } else
                    requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                val questions = DownloadAllThemesTasks(requireActivity(), prefix!!, themes)
                questions.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR)
                requireActivity().onBackPressed()
            }
        }

        ad.setNeutralButton("Выбранную") { dialog, which ->
            if (Connection.hasConnection(requireActivity())) {
                if (requireActivity().resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                } else
                    requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                val questions = DownloadThemesTasks(requireActivity(), prefix!!, theme)
                questions.adapter = adapter
                questions.execute()
                //activity!!.onBackPressed()
            }
        }

        ad.setPositiveButton("Отмена") { dialog, arg1 -> }

        ad.setCancelable(true)
        ad.setOnCancelListener { }

        ad.show()

    }

}
