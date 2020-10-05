package com.ilnur.Fragments

import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.ListFragment
import com.ilnur.Adapters.VariantsArrayAdapter
import com.ilnur.R
import com.ilnur.TestsActivity


class VariantsFragment : ListFragment() {

    internal var subject_prefix: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        subject_prefix = arguments?.getString("subject_prefix")

        return inflater.inflate(R.layout.fragment_listview, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val variants = arrayOfNulls<String>(15)

        for (i in 0..14) {
            variants[i] = "Вариант № " + (i + 1)
        }

        val adapter = VariantsArrayAdapter(requireActivity(), variants, subject_prefix!!)
        listAdapter = adapter
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        val intent = Intent(requireActivity(), TestsActivity::class.java)
        intent.putExtra("subject_prefix", subject_prefix)
        intent.putExtra("variant_number", position + 1)
        intent.putExtra("section", "Варианты")
        if (Build.VERSION.SDK_INT > 20) {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity())
            startActivity(intent, options.toBundle())
        } else {
            startActivity(intent)
        }
        //startActivity(intent)
    }


    fun setSubject(subject: String) {
        subject_prefix = subject
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("subject_prefix", subject_prefix)
        super.onSaveInstanceState(outState)
    }
}
