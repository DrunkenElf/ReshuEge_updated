package com.ilnur.Fragments

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView

import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager


import com.ilnur.Adapters.SubjAdapter
import com.ilnur.R

class SubjectsFragment : Fragment() {
    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        try {
            if (this.context is AppCompatActivity)
                mListener = activity as OnFragmentInteractionListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$activity должен реализовывать интерфейс OnFragmentInteractionListener")
        }
        val subject = resources.getStringArray(R.array.subjects)
        val root = inflater.inflate(R.layout.fragment_subjs, container, false)
        val lw = root.findViewById<ListView>(R.id.subjs_list)
        val adapter = SubjAdapter(root.context, subject, mListener!!)
        serReceiver(adapter)
        lw.adapter = adapter

        return root
    }

    fun serReceiver(adapter: SubjAdapter) {
        val receiver = SubjAdapter.BroadIntReceiver(adapter)
        val intentFilter = IntentFilter()
        intentFilter.addAction("done")

        LocalBroadcastManager.getInstance(this.context!!).registerReceiver(receiver, intentFilter)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                val builder = androidx.appcompat.app.AlertDialog.Builder(activity!!)
                builder.setMessage("Для корректного отображения скачанных изображений требуется дать доступ к сохранению файлов.")
                        .setPositiveButton("Продолжить") { dialog, which ->
                            ActivityCompat.requestPermissions(activity!!,
                                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                                    1)
                        }
                builder.show()
            }
        }
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(link: String)
    }

    fun sendSubject(subject: String) {
        mListener!!.onFragmentInteraction(subject)
    }
}
