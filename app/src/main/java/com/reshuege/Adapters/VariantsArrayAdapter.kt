package com.reshuege.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.reshuege.DataBase.DataPreferences

import com.reshuege.R
import kotlinx.coroutines.*


class VariantsArrayAdapter(context: Context, private val values: Array<String?>,
                           private val subject_prefix: String) : ArrayAdapter<String>(context, R.layout.variant_list_item, values) {

    val prefs = DataPreferences(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView = inflater.inflate(R.layout.variant_list_item, parent, false)
        val variant = rowView.findViewById<View>(R.id.variant) as TextView
        val lastResult = rowView.findViewById<View>(R.id.lastResult) as TextView

        variant.text = values[position]

        CoroutineScope(Dispatchers.IO).launch {
            val count = prefs.questionsCount(subject_prefix)
            val result = prefs.resultsCount(subject_prefix, position+1)
            withContext(Dispatchers.Main){
                if (result == -1)
                    lastResult.text = "0 из $count"
                else if (result >= count / 2) {
                    lastResult.text = "$result из $count"
                    lastResult.setBackgroundResource(R.drawable.button_right)
                } else {
                    lastResult.text = "$result из $count"
                    lastResult.setBackgroundResource(R.drawable.button_wrong)
                }
            }
        }


        return rowView
    }
}
