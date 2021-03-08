package com.reshuege.TreeBinders


import android.view.View
import android.widget.TextView


import tellh.com.recyclertreeview_lib.TreeNode
import tellh.com.recyclertreeview_lib.TreeViewBinder

import com.reshuege.R

import com.reshuege.TreeView.File

class FileNodeBinder : TreeViewBinder<FileNodeBinder.ViewHolder>() {
    override fun provideViewHolder(itemView: View): ViewHolder{
        return  ViewHolder(itemView)
    }

    override fun bindView(holder: ViewHolder, position: Int, node: TreeNode<*>) {
        val fileNode = node.content as File
        holder.tvName.text = fileNode.fileName
    }

    override fun getLayoutId(): Int {
        return R.layout.item_file
    }

    inner class ViewHolder(rootView: View): TreeViewBinder.ViewHolder(rootView) {
        internal val tvName: TextView

        init {
            this.tvName = rootView.findViewById<View>(R.id.tv_name) as TextView
        }
    }

}