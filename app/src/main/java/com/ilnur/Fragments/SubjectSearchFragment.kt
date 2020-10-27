package com.ilnur.Fragments

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView

import com.ilnur.Adapters.SubjectSearchArrayAdapter
import com.ilnur.R

class SubjectSearchFragment : Fragment() {

    private var mListener: OnFragmentInteractionListener? = null
    internal lateinit var subject_prefix: Array<String>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        try {
            if (this.context is AppCompatActivity)
                mListener = activity as OnFragmentInteractionListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$activity должен реализовывать интерфейс OnFragmentInteractionListener")
        }
        val subject = resources.getStringArray(R.array.subjects)
        val root = inflater.inflate(R.layout.fragment_subjs, container, false)
        subject_prefix = resources.getStringArray(R.array.subjects_prefix)

        val lw = root.findViewById<ListView>(R.id.subjs_list)

        val adapter = SubjectSearchArrayAdapter(requireActivity(),
                subject, mListener!!, subject_prefix)
        lw.adapter = adapter
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
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



    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(link: String)
    }

    fun sendSubject(subject: String) {
        mListener!!.onFragmentInteraction(subject)
    }


}
