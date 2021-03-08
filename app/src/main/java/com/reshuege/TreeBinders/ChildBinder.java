package com.reshuege.TreeBinders;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.reshuege.R;
import com.reshuege.TreeView.Probcat;

import tellh.com.recyclertreeview_lib.TreeNode;
import tellh.com.recyclertreeview_lib.TreeViewBinder;

public class ChildBinder  extends TreeViewBinder<ChildBinder.ViewHolder> {

    /*Bundle bundle;

    public ChildBinder(Bundle bundle){
        this.bundle = bundle;
    }*/

    @Override
    public ChildBinder.ViewHolder provideViewHolder(View itemView) {
        return new ViewHolder(itemView);
    }

    @Override
    public void bindView(ChildBinder.ViewHolder holder, int i, TreeNode treeNode) {
        //Cat_butt cat = (Cat_butt) treeNode.getContent();
        Probcat content = (Probcat) treeNode.getContent();
        holder.tv.setText(content.getName().replace("просмотреть", ""));
        holder.check.setChecked(content.isLeaf);
        holder.check.setOnClickListener(v -> {
            if (holder.check.isChecked()) {

            }
        });
    }

    @Override
    public int getLayoutId() {
        return R.layout.expan_child_view;
    }

    public class ViewHolder extends TreeViewBinder.ViewHolder{
        CheckBox check;
        TextView tv;

        public ViewHolder(View rootview){
            super(rootview);
            check = rootview.findViewById(R.id.child_check);
            tv = rootview.findViewById(R.id.child_text);

            /*learn = rootview.findViewById(R.id.but_learn);
            watch = rootview.findViewById(R.id.but_watch);*/
        }
    }
}