package com.reshuege.TreeBinders

import android.content.Context
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.EditText

import com.github.rubensousa.raiflatbutton.RaiflatButton
import com.google.android.material.textfield.TextInputLayout


import tellh.com.recyclertreeview_lib.TreeNode
import tellh.com.recyclertreeview_lib.TreeViewBinder
import com.reshuege.Connection
import com.reshuege.DownloadTasks.*
import com.reshuege.R
import com.reshuege.TreeBinders.Tail_check_binder.ViewHolder

class Tail_check_binder(private val cont: Context, private val subject_prefix: String, private val section: String) : TreeViewBinder<ViewHolder>() {

    override fun provideViewHolder(itemView: View): ViewHolder {
        return ViewHolder(itemView)
    }

    override fun bindView(holder: ViewHolder, position: Int, node: TreeNode<*>) {
        val tail = node.content as Tail_check
        holder.inputLayout.hint = tail.hint

        if (tail.type == 2)
            holder.edit.inputType = InputType.TYPE_CLASS_TEXT
        else
            holder.edit.inputType = InputType.TYPE_CLASS_NUMBER


        holder.btn.setOnClickListener { v -> goToSearch(tail.type, holder.check, holder.edit) }
    }

    private fun goToSearch(type: Int, box: CheckBox, searchEdit: EditText) {
        Log.i("CLICKbtnPREFIX", "" + subject_prefix)
        Log.i("CLICKbtnSECTION", "" + section)
        if (Connection.hasConnection(cont)) {
            when (type) {
                0 -> {
                    val downloadVariant = DownloadVariant(cont, subject_prefix,
                            searchEdit.text.toString(), section, box.isChecked)
                    downloadVariant.execute()
                }
                1 -> {
                    val openTask = OpenTask1(cont, subject_prefix, searchEdit.text.toString())
                    openTask.search()
                }
                2 -> {
                    /*val searchTask = SearchTask(cont, subject_prefix, searchEdit.text.toString())
                    searchTask.execute()*/
                    Searchtask1(cont, subject_prefix, searchEdit.text.toString()).search()
                }
            }
        }
    }


    override fun getLayoutId(): Int {
        return R.layout.tail_check
    }

    inner class ViewHolder(rootView: View) : TreeViewBinder.ViewHolder(rootView) {
        var inputLayout: TextInputLayout
        var edit: EditText
        var check: CheckBox
        var btn: RaiflatButton

        init {
            this.inputLayout = rootView.findViewById(R.id.input_lay)
            this.edit = rootView.findViewById(R.id.edit_text)
            this.check = rootView.findViewById(R.id.checkbox)
            this.btn = rootView.findViewById(R.id.find)
        }

    }
}