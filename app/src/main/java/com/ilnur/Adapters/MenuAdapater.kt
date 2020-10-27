package com.ilnur.Adapters

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.text.CaseMap
import android.media.Image
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.ilnur.DataBase.SubjectMain

import com.ilnur.Fragments.StartPageFragment
import com.ilnur.R
import com.ilnur.Session.Settings
import com.ilnur.viewModel.MainViewModel

class MenuRecAdapter(
    val context: Context, val mas: Array<String>,
    val listener: (String) -> Unit,
) :
    RecyclerView.Adapter<MenuRecAdapter.MenuViewHolder>() {

    private var itemSelected: String? = null
    private val imgs = intArrayOf(
        R.drawable.ic_tests, R.drawable.ic_training,
        R.drawable.ico_exam, R.drawable.ic_theory, R.drawable.ic_search,
        R.drawable.teacher, R.drawable.ic_stats, R.drawable.ic_manual
    )


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        return MenuViewHolder(
            LayoutInflater.from(this.context).inflate(R.layout.startpage_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bindItems()
        holder.itemView.setOnClickListener {
            listener(mas[position])
        }

    }

    override fun getItemCount(): Int {
        return imgs.size
    }

    inner class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card = itemView.findViewById<CardView>(R.id.start_card_item)
        val img = itemView.findViewById<ImageView>(R.id.start_item_img)
        val title = itemView.findViewById<TextView>(R.id.start_item_text)


        fun bindItems() {
            title.text = mas[adapterPosition]
            img.setImageResource(imgs[adapterPosition])
        }

    }
}

/*
class MenuAdapater(
    private val context: Context,
    private val mas: Array<String>,
    private val mListener: StartPageFragment.OnFragmentInteractionListener,
    private val manager: FragmentManager
) : BaseAdapter() {
    private var itemSelected: String? = null
    //private val manager: FragmentManager

    private val imgs = intArrayOf(
        R.drawable.ic_tests,
        R.drawable.ic_training,
        R.drawable.ico_exam,
        R.drawable.ic_theory,
        R.drawable.ic_search,
        R.drawable.teacher,
        R.drawable.ic_stats,
        R.drawable.ic_manual
    )

    *//*init {
        manager = context.applicationContext as FragmentManager
    }*//*

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
                    *//*if (!settings.getFirstStartFlag(context)) {
                        // Construct the LicenseCheckerCallback. The library calls this when done.
                        Log.d("HASconn", "update")
                        Update(context,false).doInBackground()
                        settings.setFirstStartFlag(true, context)
                    }*//*
                    mListener.onFragmentInteraction(mas[position], 1)
                }
                "Каталог заданий" -> {
                    itemSelected = "Каталог заданий"
                    val settings = Settings()
                    Log.d("StartPage", "katal")
                    *//*if (!settings.getFirstStartFlag(context)) {
                        // Construct the LicenseCheckerCallback. The library calls this when done.
                        Log.d("HASconn", "update")
                        Update(context,false).doInBackground()
                        settings.setFirstStartFlag(true, context)
                    }*//*
                    mListener.onFragmentInteraction(mas[position], 2)
                }
                "Режим экзамена" -> {
                    itemSelected = "Режим экзамена"
                    val settings = Settings()
                    Log.d("StartPage", "rez_ekz")
                    *//*if (!settings.getFirstStartFlag(context)) {
                        // Construct the LicenseCheckerCallback. The library calls this when done.
                        Log.d("HASconn", "update")
                        Update(context,false).doInBackground()
                        settings.setFirstStartFlag(true, context)
                    }*//*
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
}*/
