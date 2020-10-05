package com.ilnur.TreeBinders


import android.view.View
import android.widget.TextView


import tellh.com.recyclertreeview_lib.TreeNode
import tellh.com.recyclertreeview_lib.TreeViewBinder

import com.ilnur.R

import com.ilnur.TreeView.File

class FileNodeBinder : TreeViewBinder<FileNodeBinder.ViewHolder>() {
    override fun provideViewHolder(itemView: View): ViewHolder{
        return  ViewHolder(itemView)
    }

    override fun bindView(holder: ViewHolder, position: Int, node: TreeNode<*>) {
        val fileNode = node.content as File
        holder.tvName.text = fileNode.fileName
        //holder.view.setImageResource(R.drawable.ic_fili_19dp);

    }

    override fun getLayoutId(): Int {
        return R.layout.item_file
    }

    inner class ViewHolder(rootView: View): TreeViewBinder.ViewHolder(rootView) {
        //public ImageView view;
        internal val tvName: TextView

        init {
            this.tvName = rootView.findViewById<View>(R.id.tv_name) as TextView
            //this.view = rootView.findViewById(R.id.view);
        }

        /*private constructor(rootView: View) : TreeViewBinder.ViewHolder(rootView)
        {
            internal val tvName: TextView

            init {
                this.tvName = rootView.findViewById<View>(R.id.tv_name) as TextView
                //this.view = rootView.findViewById(R.id.view);
            }

        }*/
    }

}