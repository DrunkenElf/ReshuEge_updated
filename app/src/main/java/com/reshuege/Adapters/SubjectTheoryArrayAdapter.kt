package com.reshuege.Adapters

import androidx.appcompat.app.AppCompatActivity
import android.content.Context

import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

import com.reshuege.Connection
import com.reshuege.DownloadTasks.DownloadHandbook1
import com.reshuege.Fragments.SubjectsTheoryFragment
import com.reshuege.R


class SubjectTheoryArrayAdapter(context: Context, private val values: Array<String>,
                                private val mListener: SubjectsTheoryFragment.OnFragmentInteractionListener) : ArrayAdapter<String>(context, R.layout.subjects_list_item, values) {
    private val ada: SubjectTheoryArrayAdapter = this

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView = inflater.inflate(R.layout.subjects_list_item, parent, false)
        val subListItem = rowView.findViewById<View>(R.id.name) as TextView
        val subjectAccess = rowView.findViewById<View>(R.id.checker) as TextView
        val subjectPrefix = rowView.findViewById<View>(R.id.prefix) as TextView
        val subjectIcon = rowView.findViewById<View>(R.id.subjectIcon) as ImageView
        val updateIcon = rowView.findViewById<View>(R.id.updateIcon) as ImageView
        updateIcon.visibility = View.GONE
        subListItem.text = values[position]


        // делаем проверку предмета на наличие
        val subjects_prefix = context.resources.getStringArray(R.array.subjects_prefix_handbook)
        val verTheory = context.getSharedPreferences("version_theory", AppCompatActivity.MODE_PRIVATE)
        subjectPrefix.text = subjects_prefix[position]
        try {
            subjectIcon.setImageResource(context.resources.getIdentifier(
                    "ico_" + subjects_prefix[position], "drawable", context.packageName))
            subjectAccess.text = "true"

            val sub_pref = subjects_prefix[position]
            updateIcon.visibility = View.VISIBLE
            updateIcon.setImageResource(R.drawable.ico_reload)
            updateIcon.setOnClickListener { v ->
                Log.d("sub_pref", sub_pref)
                showMessage("Что-то пошло не так?", "Скачать материалы справочника заново?", sub_pref)
                v.visibility = View.GONE
            }

            if (verTheory.getString(subjects_prefix[position], "")!!.contentEquals("0")) {
                subjectIcon.setImageResource(R.drawable.ico_download)
                subjectAccess.text = "false"
            }

        } catch (e: IndexOutOfBoundsException) {
            subjectIcon.setImageResource(R.drawable.ico_download)
            subjectAccess.text = "false"
        }

        val card = rowView.findViewById<CardView>(R.id.subjs_card)
        card.setOnClickListener {
            if (subjectAccess.text.toString().contentEquals("false")) {
                showMessage("Отсутствуют материалы", "Требуется загрузить материалы справочника. Начать загрузку?",
                        subjectPrefix.text.toString())
            } else if (subjectAccess.text.toString().contentEquals("blocked")) {
                val ad = AlertDialog.Builder(rowView.context)
                ad.setTitle("Раздел закрыт")
                ad.setMessage("Справочник по данному предмету находится в разработке")

                ad.setNegativeButton("Ок") { dialog, arg1 -> notifyDataSetChanged() }

                ad.setCancelable(true)
                ad.setOnCancelListener { }

                ad.show()
            } else {
                Log.d("OnClickTheor", subListItem.text.toString())
                mListener.onFragmentInteraction(subListItem.text.toString())
            }
        }

        return rowView
    }

    fun showMessage(title: String, message: String, prefix: String) {
        val ad = AlertDialog.Builder(context)
        ad.setTitle(title)
        ad.setMessage(message)

        ad.setNegativeButton("Да") { dialog, arg1 ->
            if (Connection.hasConnection(context)) {
                val handbook = DownloadHandbook1(context, prefix, ada)
                handbook.execute()
            }
        }

        ad.setPositiveButton("Нет") { dialog, arg1 -> }

        ad.setCancelable(true)
        ad.setOnCancelListener { }

        ad.show()

    }


}
