package com.ilnur.TreeBinders

import android.view.View
import android.widget.ImageView
import android.widget.TextView


import tellh.com.recyclertreeview_lib.TreeNode
import tellh.com.recyclertreeview_lib.TreeViewBinder
import com.ilnur.R

class HeadBinder : TreeViewBinder<HeadBinder.ViewHolder>() {
    override fun provideViewHolder(itemView: View): ViewHolder {
        return ViewHolder(itemView)
    }

    override fun bindView(holder: ViewHolder, position: Int, node: TreeNode<*>) {
        val head = node.content as Head
        holder.title.text = head.title
        holder.img.setImageResource(head.imgRes)
    }

    override fun getLayoutId(): Int {
        return R.layout.head
    }

    inner class ViewHolder(rootView: View) : TreeViewBinder.ViewHolder(rootView) {
        var title: TextView
        var img: ImageView

        init {
            this.title = rootView.findViewById(R.id.head_title)
            this.img = rootView.findViewById(R.id.head_img)
        }

    }
}
