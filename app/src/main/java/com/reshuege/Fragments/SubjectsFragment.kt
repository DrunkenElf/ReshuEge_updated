package com.reshuege.Fragments

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.reshuege.Adapters.SubjRecAdapter
import com.reshuege.DataBase.SubjectMain
import com.reshuege.R
import com.reshuege.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubjectsFragment : Fragment() {
    val sharedModel: MainViewModel by activityViewModels()

    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d("onCreate: ${this.javaClass.name}", "created")
        try {
            if (this.requireActivity() is AppCompatActivity)
                mListener = activity as OnFragmentInteractionListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$activity должен реализовывать интерфейс OnFragmentInteractionListener")
        }
        return  inflater.inflate(R.layout.fragment_subjs_recycler, container, false)
    }

    fun serReceiver(adapter: SubjRecAdapter) {
        val receiver = SubjRecAdapter.BroadIntReceiver(adapter)
        val intentFilter = IntentFilter()
        intentFilter.addAction("done")
        LocalBroadcastManager.getInstance(this.requireContext()).registerReceiver(receiver, intentFilter)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rw = view.findViewById<RecyclerView>(R.id.recycler_subj)
            .also { it.layoutManager = LinearLayoutManager(requireContext()) }

        val adapter = SubjRecAdapter(view.context, sharedModel, object : SubjRecAdapter.ItemSelectedListener {
            override fun onItemSelected(item: SubjectMain) {
                if (sharedModel.subjects.value!!.find { it.href == item.href}!!.isAdded
                    && !sharedModel.subjects.value!!.find { it.href == item.href}!!.isNeedToUpd
                ) {
                    Log.d("itemSelect", item.toString() + "selected Recycler")
                    sendSubject(item.href)
                    sharedModel.selectSubject(item)
                } else {
                    Toast.makeText(context, "Этот предмет еще не загружен", Toast.LENGTH_SHORT).show()
                }
            }
        })

        serReceiver(adapter)
        rw.adapter = adapter

        sharedModel.subjects.observe(viewLifecycleOwner, {
            adapter.notifyDataSetChanged()
        })

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

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(link: String)
    }

    fun sendSubject(subject: String) {
        mListener!!.onFragmentInteraction(subject)
    }
}
