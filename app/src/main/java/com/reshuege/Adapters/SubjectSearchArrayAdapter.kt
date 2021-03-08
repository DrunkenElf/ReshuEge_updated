package com.reshuege.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

import androidx.cardview.widget.CardView

import com.reshuege.Fragments.SubjectSearchFragment
import com.reshuege.R

class SubjectSearchArrayAdapter(context: Context, private val values: Array<String>,
                                private val mListener: SubjectSearchFragment.OnFragmentInteractionListener,
                                private val subj_prefs: Array<String>) : ArrayAdapter<String>(context, R.layout.subjects_list_item, values) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView = inflater.inflate(R.layout.subjects_list_item, parent, false)
        val subListItem = rowView.findViewById<TextView>(R.id.name)
        val subjectAccess = rowView.findViewById<TextView>(R.id.checker)
        val subjectIcon = rowView.findViewById<ImageView>(R.id.subjectIcon)
        val updateIcon = rowView.findViewById<ImageView>(R.id.updateIcon)
        updateIcon.visibility = View.GONE
        subListItem.text = values[position]

        val s = values[position]
        val subjects = context.resources.getStringArray(R.array.subjects)
        val subjects_prefix = context.resources.getStringArray(R.array.subjects_prefix)

        for (i in subjects.indices) {
            if (s.contentEquals(subjects[i])) {
                subjectIcon.setImageResource(context.resources.getIdentifier(
                        "ico_" + subjects_prefix[i], "drawable", context.packageName))
                subjectAccess.text = "true"
            }
        }
        val card = rowView.findViewById<CardView>(R.id.subjs_card)
        card.setOnClickListener { mListener.onFragmentInteraction(subj_prefs[position]) }

        return rowView
    }

}
