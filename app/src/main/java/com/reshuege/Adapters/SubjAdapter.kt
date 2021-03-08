package com.reshuege.Adapters

import androidx.appcompat.app.AppCompatActivity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.reshuege.Connection
import com.reshuege.DataBase.QuestionsDataBaseHelper
import com.reshuege.DataBase.SubjectMain
import com.reshuege.Fragments.SubjectsFragment
import com.reshuege.R
import com.reshuege.service.DownloadForeground
import com.reshuege.viewModel.MainViewModel


class SubjRecAdapter(val context: Context, val sharedModel: MainViewModel,
                     val itemSelectedListener: ItemSelectedListener
) :
    RecyclerView.Adapter<SubjRecAdapter.SubjViewHolder>() {

    class BroadIntReceiver(val adapter: SubjRecAdapter): BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val res = intent!!.getBooleanExtra("broadcastMessage", false)
            Log.d("BROAD", res.toString())
            adapter.sharedModel.updateSubjects()
            Log.d("BROAD", adapter.sharedModel.curr_download.value.toString())
            adapter.notifyItemChanged(adapter.sharedModel.curr_download.value!!.position)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjViewHolder {
        return SubjViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_subjects_item, parent, false))
    }

    override fun onBindViewHolder(holder: SubjViewHolder, position: Int) {
        holder.bindItems(sharedModel.subjects.value!![position])
    }

    override fun getItemCount(): Int {
        return sharedModel.subjects.value!!.size
    }

    inner class SubjViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val img_dwn: ImageView = itemView.findViewById(R.id.download_icon_subj)
        val title: TextView = itemView.findViewById(R.id.title_subj)
        val img_retry: ImageView = itemView.findViewById(R.id.redownload_icon_subj)

        init {
            itemView.setOnClickListener{
                when(sharedModel.subjects.value!![adapterPosition].isAdded){
                    true -> {
                        img_dwn.setImageResource(context.resources.getIdentifier("ico_${sharedModel.subjects.value!![adapterPosition].href}", "drawable", context.packageName))
                        when(sharedModel.subjects.value!![adapterPosition].isNeedToUpd){
                            true -> {
                                //show msg with reccomendation to update
                                img_retry.visibility = View.VISIBLE
                            }
                            else -> {
                                // everythings fine
                                img_retry.visibility = View.INVISIBLE
                            }
                        }
                    }
                    else -> {
                        //subj not added
                        img_dwn.setImageResource(R.drawable.ico_download)
                        showMessage("Отсутствуют задания", "Требуется загрузить задания. Начать загрузку?",
                            sharedModel.subjects.value!![adapterPosition].href, sharedModel.subjects.value!![adapterPosition].title,
                            adapterPosition)
                    }
                }
                itemSelectedListener.onItemSelected(sharedModel.subjects.value!![adapterPosition])
            }
        }
        fun bindItems(item: SubjectMain){
            title.text = item.title
            //image will be later
            when(sharedModel.subjects.value!![adapterPosition].isAdded){
                true -> {
                    img_dwn.setImageResource(context.resources.getIdentifier("ico_${sharedModel.subjects.value!![adapterPosition].href}", "drawable", context.packageName))
                    if (sharedModel.subjects.value!![adapterPosition].isNeedToUpd)
                        img_retry.visibility = View.VISIBLE
                    else
                        img_retry.visibility = View.INVISIBLE
                }
                else -> {
                    img_dwn.setImageResource(R.drawable.ico_download)
                }
            }
        }

    }

    interface ItemSelectedListener {
        fun onItemSelected(item: SubjectMain)
    }



    fun showMessage(title: String, message: String, prefix: String, name: String, position: Int) {
        val ad = AlertDialog.Builder(context)
        ad.setTitle(title)
        ad.setMessage(message)

        ad.setNegativeButton("Да") { dialog, arg1 ->
            if (Connection.hasConnection(context)) {
                sharedModel.updateCurrDwn(DwnCurr(title = name, href = prefix, position = position))
                val intent = Intent(context, DownloadForeground::class.java)
                intent.putExtra("prefix", prefix)
                intent.putExtra("name", name)
                intent.putExtra("position", position)
                context.startService(intent)
            }
        }

        ad.setPositiveButton("Нет") { dialog, arg1 -> notifyDataSetChanged() }

        ad.setCancelable(true)
        ad.setOnCancelListener { notifyDataSetChanged() }

        ad.show()

    }

}

data class DwnCurr(
    val isLoading: Boolean = false,
    val title: String,
    val href: String,
    val position: Int,
)

class SubjAdapter(private val context: Context, private val subjs: Array<String>, private val mListener: SubjectsFragment.OnFragmentInteractionListener) : BaseAdapter() {
    internal var subjects: Array<String>
    internal var subjects_prefix: Array<String>
    val ada: SubjAdapter

    init {
        subjects = context.resources.getStringArray(R.array.subjects)
        subjects_prefix = context.resources.getStringArray(R.array.subjects_prefix)
        ada = this
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var item: View? = convertView
        if (item == null)
            item = LayoutInflater.from(context).inflate(R.layout.subjects_list_item, parent, false)
        val subj = subjs[position]
        val qDBHelper = QuestionsDataBaseHelper(context, "start_table")

        val icon_subj = item!!.findViewById<ImageView>(R.id.subjectIcon)
        val name = item.findViewById<TextView>(R.id.name)
        name.text = subj
        val checker = item.findViewById<TextView>(R.id.checker)
        val prefix = item.findViewById<TextView>(R.id.prefix)
        val update_icon = item.findViewById<ImageView>(R.id.updateIcon)

        val card = item.findViewById<CardView>(R.id.subjs_card)
        card.setOnClickListener { v ->
            v.isEnabled = false

            if (checker.text.toString().contentEquals("false")) {
                showMessage("Отсутствуют задания", "Требуется загрузить задания. Начать загрузку?",
                        prefix.text.toString(), name.text.toString())
            } else {
                mListener.onFragmentInteraction(prefix.text.toString())
            }
            v.isEnabled = true
        }

        val verPref = context.getSharedPreferences("version_tests", AppCompatActivity.MODE_PRIVATE)
        val latestPref = context.getSharedPreferences("latest_version_tests", AppCompatActivity.MODE_PRIVATE)
        val criticalUpdate = context.getSharedPreferences("critical_update", AppCompatActivity.MODE_PRIVATE)

        if (subj.contentEquals(subjects[position])) {
            if (qDBHelper.checkTable(subjects_prefix[position])) {
                icon_subj.setImageResource(context.resources.getIdentifier(
                        "ico_" + subjects_prefix[position], "drawable", context.packageName))
                checker.text = "true"

                val latestVersion = latestPref.getString(subjects_prefix[position], "")
                val currentVersion = verPref.getString(subjects_prefix[position], "")
                Log.d("myLogs", subjects_prefix[position] + " " + currentVersion + " " + latestVersion)
                val sub_pref = subjects_prefix[position]
                update_icon.visibility = View.VISIBLE
                if (!currentVersion!!.contentEquals(latestVersion!!) || !criticalUpdate.getBoolean(subjects_prefix[position], false)) {
                    update_icon.setOnClickListener { v ->
                        showMessage("Доступны обновления", "Для этого предмета доступны обновления. Скачать?",
                                sub_pref, name.text.toString())
                        v.visibility = View.GONE
                    }
                    update_icon.setImageResource(R.drawable.ico_download)
                } else {
                    update_icon.setOnClickListener { v ->
                        showMessage("Что-то пошло не так?", "Загрузить задания заново?", sub_pref, name.text.toString())
                        v.visibility = View.GONE
                    }
                    update_icon.setImageResource(R.drawable.ico_reload)
                }
            } else {
                icon_subj.setImageResource(R.drawable.ico_download)
                checker.text = "false"
                update_icon.visibility = View.GONE
            }
            prefix.text = subjects_prefix[position]
        }
        Log.i("visibility", "" + update_icon.visibility + " " + View.GONE)

        return item
    }

    class BroadIntReceiver(val adapter: SubjRecAdapter): BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val res = intent!!.getBooleanExtra("broadcastMessage", false)
            Log.d("BROAD", res.toString())
            adapter.notifyDataSetChanged()
        }
    }

    fun showMessage(title: String, message: String, prefix: String, name: String) {
        val ad = AlertDialog.Builder(context)
        ad.setTitle(title)
        ad.setMessage(message)

        ad.setNegativeButton("Да") { dialog, arg1 ->
            if (Connection.hasConnection(context)) {
                val intent = Intent(context, DownloadForeground::class.java)
                intent.putExtra("prefix", prefix)
                intent.putExtra("name", name)
                context.startService(intent)
            }
        }

        ad.setPositiveButton("Нет") { dialog, arg1 -> notifyDataSetChanged() }

        ad.setCancelable(true)
        ad.setOnCancelListener { notifyDataSetChanged() }

        ad.show()

    }

    override fun getCount(): Int {
        return subjs.size
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}
