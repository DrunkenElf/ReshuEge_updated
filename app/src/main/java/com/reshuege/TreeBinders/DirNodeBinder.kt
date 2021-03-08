package com.reshuege.TreeBinders


import android.graphics.Typeface
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView


import tellh.com.recyclertreeview_lib.TreeNode
import tellh.com.recyclertreeview_lib.TreeViewBinder
import com.reshuege.R
import com.reshuege.TreeView.Dir

class DirNodeBinder : TreeViewBinder<DirNodeBinder.ViewHolder>() {

    override fun provideViewHolder(itemView: View): ViewHolder {
        return ViewHolder(itemView)
    }

    override fun bindView(holder: ViewHolder, position: Int, node: TreeNode<*>) {
        holder.ivArrow.rotation = 0f
        holder.ivArrow.setImageResource(R.drawable.ic_keyboard_arrow_right_black_24dp)
        val rotateDegree = if (node.isExpand) 90 else 0
        holder.ivArrow.rotation = rotateDegree.toFloat()
        val dirNode = node.content as Dir
        if (!dirNode.isBig) {
            holder.tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 21f)
            holder.tvName.setTypeface(holder.tvName.typeface, Typeface.NORMAL)
        } else {
            holder.tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 23f)
            holder.tvName.setTypeface(holder.tvName.typeface, Typeface.BOLD)
        }
        holder.tvName.text = dirNode.dirName
        if (node.isLeaf)
            holder.ivArrow.visibility = View.INVISIBLE
        else
            holder.ivArrow.visibility = View.VISIBLE
    }

    override fun getLayoutId(): Int {
        return R.layout.item_dir
    }

    class ViewHolder(rootView: View) : TreeViewBinder.ViewHolder(rootView) {
        val ivArrow: ImageView
        val tvName: TextView

        init {
            this.ivArrow = rootView.findViewById<View>(R.id.iv_arrow) as ImageView
            this.tvName = rootView.findViewById<View>(R.id.tv_name) as TextView
        }
    }
}
