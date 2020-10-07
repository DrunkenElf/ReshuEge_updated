package com.ilnur.Adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentManager
import com.ilnur.DownloadTasks.Update

import com.ilnur.Fragments.StartPageFragment
import com.ilnur.R
import com.ilnur.Session.Settings

class MenuAdapater(private val context: Context, private val mas: Array<String>, private val mListener: StartPageFragment.OnFragmentInteractionListener) : BaseAdapter() {
    private var itemSelected: String? = null
    private val manager: FragmentManager

    private val imgs = intArrayOf(R.drawable.ic_tests, R.drawable.ic_training, R.drawable.ico_exam, R.drawable.ic_theory, R.drawable.ic_search, R.drawable.teacher, R.drawable.ic_stats, R.drawable.ic_manual)

    init {
        manager = (context as AppCompatActivity).supportFragmentManager
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view: View? = convertView
        if (view == null)
            view = LayoutInflater.from(context).inflate(R.layout.startpage_item, parent, false)

        val card = view!!.findViewById<CardView>(R.id.start_card_item)
        card.setOnClickListener {
            when (mas[position]) {
                "Варианты" -> {
                    itemSelected = "Варианты"
                    val settings = Settings()
                    Log.d("StartPage", "varian")
                    /*if (!settings.getFirstStartFlag(context)) {
                        // Construct the LicenseCheckerCallback. The library calls this when done.
                        Log.d("HASconn", "update")
                        Update(context,false).doInBackground()
                        settings.setFirstStartFlag(true, context)
                    }*/
                    mListener.onFragmentInteraction(mas[position], 1)
                }
                "Каталог заданий" -> {
                    itemSelected = "Каталог заданий"
                    val settings = Settings()
                    Log.d("StartPage", "katal")
                    /*if (!settings.getFirstStartFlag(context)) {
                        // Construct the LicenseCheckerCallback. The library calls this when done.
                        Log.d("HASconn", "update")
                        Update(context,false).doInBackground()
                        settings.setFirstStartFlag(true, context)
                    }*/
                    mListener.onFragmentInteraction(mas[position], 2)
                }
                "Режим экзамена" -> {
                    itemSelected = "Режим экзамена"
                    val settings = Settings()
                    Log.d("StartPage", "rez_ekz")
                    /*if (!settings.getFirstStartFlag(context)) {
                        // Construct the LicenseCheckerCallback. The library calls this when done.
                        Log.d("HASconn", "update")
                        Update(context,false).doInBackground()
                        settings.setFirstStartFlag(true, context)
                    }*/
                    mListener.onFragmentInteraction(mas[position], 3)
                }
                "Справочник" -> {
                    itemSelected = "Теория"
                    Log.d("StartPage", "sprav")
                    mListener.onFragmentInteraction(itemSelected!!, 4)
                }
                "Поиск" -> {
                    itemSelected = "Поиск"
                    Log.d("StartPage", "poisk")
                    mListener.onFragmentInteraction(mas[position], 5)
                }
                "Учителю" -> {
                    itemSelected = "Учителю"
                    Log.d("StartPage", "teach")
                    mListener.onFragmentInteraction(mas[position], 6)
                }
                "Статистика" -> {
                    itemSelected = "Статистика"
                    mListener.onFragmentInteraction(mas[position], 7)
                }
                "Об экзамене, шкала баллов" -> {
                    itemSelected = "Об экзамене"
                    mListener.onFragmentInteraction(itemSelected, 8)
                }
            }
        }
        val img = view.findViewById<ImageView>(R.id.start_item_img)
        val text = view.findViewById<TextView>(R.id.start_item_text)

        img.setImageResource(imgs[position])
        text.text = mas[position]

        return view
    }

    override fun getCount(): Int {
        return mas.size
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}
