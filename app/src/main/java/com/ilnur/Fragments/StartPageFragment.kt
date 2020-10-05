package com.ilnur.Fragments

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView

import com.ilnur.Adapters.MenuAdapater
import com.ilnur.Connection
import com.ilnur.DownloadTasks.Update
import com.ilnur.R


class StartPageFragment : Fragment() {

    internal var itemSelected: String? = null
    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        try {
            if (this.context is AppCompatActivity)
                mListener = activity as OnFragmentInteractionListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$activity должен реализовывать интерфейс OnFragmentInteractionListener")
        }


        val root = inflater.inflate(R.layout.fragment_startpage, container, false)
        val lw = root.findViewById<ListView>(R.id.startpage_list)
        val mas = root.context.resources.getStringArray(R.array.menu_array)
        val adapter = MenuAdapater(root.context, mas, mListener!!)

        lw.adapter = adapter

        return root
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(itemSelected: String, i: Int)
    }

    fun sendItemSelected(itemSelected: String, i: Int) {
        mListener!!.onFragmentInteraction(itemSelected, i)
    }



    fun showUpdateMessage(title: String, message: String) {
        val ad = androidx.appcompat.app.AlertDialog.Builder(requireActivity())
        ad.setTitle(title)
        ad.setMessage(message)

        ad.setNegativeButton("Да") { dialog, arg1 ->
            if (Connection.hasConnection(requireActivity())) {
                val update = Update(requireActivity(), false)
                update.doInBackground()
            }
        }

        ad.setPositiveButton("Нет") { dialog, arg1 -> }

        ad.setCancelable(true)
        ad.setOnCancelListener { }

        ad.show()
    }
}
