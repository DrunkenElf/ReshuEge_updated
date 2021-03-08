package com.reshuege.Fragments

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.reshuege.Adapters.MenuRecAdapter

import com.reshuege.R
import com.reshuege.Session.Settings
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StartPageFragment : Fragment() {

    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        try {
            if (this.requireActivity() is AppCompatActivity)
                mListener = activity as OnFragmentInteractionListener
        } catch (e: ClassCastException) {
            Log.d("error rrrrrrr", "startPage")
            throw ClassCastException("$activity должен реализовывать интерфейс OnFragmentInteractionListener")
        }
       val view =  inflater.inflate(R.layout.fragment_startpage, container, false)

        Log.d("start", mListener.toString())
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rw = view.findViewById<RecyclerView>(R.id.startpage_recycler)
            .also { it.layoutManager = LinearLayoutManager(requireContext()) }
        val mas = view.resources.getStringArray(R.array.menu_array)

        val adapter = MenuRecAdapter(view.context, mas){selectItem(it)}
        rw.adapter = adapter
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(itemSelected: String?, i: Int)
    }

    fun selectItem(itemSelecte: String) {
        Log.d("startPageFr", "onItemSelected $itemSelecte")
        var itemSelected: String
        when (itemSelecte) {
            "Варианты" -> {
                itemSelected = "Варианты"
                val settings = Settings()
                Log.d("StartPage", "varian")
                mListener!!.onFragmentInteraction(itemSelected, 1)
            }
            "Каталог заданий" -> {
                itemSelected = "Каталог заданий"
                val settings = Settings()
                Log.d("StartPage", "katal")

                mListener!!.onFragmentInteraction(itemSelected, 2)
            }
            "Режим экзамена" -> {
                itemSelected = "Режим экзамена"
                val settings = Settings()
                Log.d("StartPage", "rez_ekz")
                mListener!!.onFragmentInteraction(itemSelected, 3)
            }
            "Справочник" -> {
                itemSelected = "Теория"
                Log.d("StartPage", "sprav")
                mListener!!.onFragmentInteraction(itemSelected, 4)
            }
            "Поиск" -> {
                itemSelected = "Поиск"
                Log.d("StartPage", "poisk")
                mListener!!.onFragmentInteraction(itemSelected, 5)
            }
            "Учителю" -> {
                itemSelected = "Учителю"
                Log.d("StartPage", "teach")
                mListener!!.onFragmentInteraction(itemSelected, 6)
            }
            "Статистика" -> {
                itemSelected = "Статистика"
                mListener!!.onFragmentInteraction(itemSelected, 7)
            }
            "Об экзамене, шкала баллов" -> {
                itemSelected = "Об экзамене"
                mListener!!.onFragmentInteraction(itemSelected, 8)
            }
        }
        //mListener!!.onFragmentInteraction(itemSelecte, 2)
    }

}
