package com.reshuege.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

import com.reshuege.R

class SearchResultsAdapter(context: Context, private val values: Array<String?>) : ArrayAdapter<String>(context, R.layout.search_result_list_item, values) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView = inflater.inflate(R.layout.search_result_list_item, parent, false)
        val searchResult = rowView.findViewById<View>(R.id.search_res) as TextView
        searchResult.text = "Задание № " + values[position]
        return rowView
    }
}
