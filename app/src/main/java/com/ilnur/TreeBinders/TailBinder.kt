package com.ilnur.TreeBinders

import android.content.Context
import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText


import com.github.rubensousa.raiflatbutton.RaiflatButton
import com.google.android.material.textfield.TextInputLayout

import tellh.com.recyclertreeview_lib.TreeNode
import tellh.com.recyclertreeview_lib.TreeViewBinder
import com.ilnur.Connection
import com.ilnur.DownloadTasks.OpenTask1
import com.ilnur.DownloadTasks.Searchtask1
import com.ilnur.R
import com.ilnur.TreeBinders.TailBinder.ViewHolder

class TailBinder(private val cont: Context, private val subject_prefix: String, private val section: String) : TreeViewBinder<ViewHolder>() {

    override fun provideViewHolder(itemView: View): ViewHolder {
        return ViewHolder(itemView)
    }

    override fun bindView(holder: ViewHolder, position: Int, node: TreeNode<*>) {
        val tail = node.content as Tail
        holder.inputLayout.hint = tail.hint

        if (tail.type == 2)
            holder.edit.inputType = InputType.TYPE_CLASS_TEXT
        else
            holder.edit.inputType = InputType.TYPE_CLASS_NUMBER

        holder.edit.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                true
            }
            false
        }

        holder.edit.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                holder.btn.performClick()
                true
            }
            false
        }

        holder.btn.setOnClickListener { v -> goToSearch(tail.type, holder.edit) }
    }

    private fun goToSearch(type: Int, searchEdit: EditText) {
        Log.i("CLICKbtnTYPE", "" + type)
        Log.i("CLICKbtnEDIT", "" + searchEdit.text.toString())
        if (Connection.hasConnection(cont)) {
            when (type) {
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
        return R.layout.tail
    }

    inner class ViewHolder(rootView: View) : TreeViewBinder.ViewHolder(rootView) {
        var inputLayout: TextInputLayout
        var edit: EditText
        var btn: RaiflatButton

        init {
            this.inputLayout = rootView.findViewById(R.id.input_lay)
            this.edit = rootView.findViewById(R.id.edit_text)
            this.btn = rootView.findViewById(R.id.find)
        }

    }
}
