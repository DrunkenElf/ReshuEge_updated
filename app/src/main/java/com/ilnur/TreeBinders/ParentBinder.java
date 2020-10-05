package com.ilnur.TreeBinders;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.github.rubensousa.raiflatbutton.RaiflatButton;
import com.google.gson.Gson;
import com.ilnur.R;
import com.ilnur.TreeView.Probcat;

import tellh.com.recyclertreeview_lib.TreeNode;
import tellh.com.recyclertreeview_lib.TreeViewBinder;

public class ParentBinder extends TreeViewBinder<ParentBinder.ViewHolder> {
    Bundle bundle;

    /*public ParentBinder(Bundle bundle){
        this.bundle = bundle;
    }*/

    @Override
    public ParentBinder.ViewHolder provideViewHolder(View itemView) {
        return new ViewHolder(itemView);
    }

    @Override
    public void bindView(ParentBinder.ViewHolder holder, int i, TreeNode treeNode) {
        //Cat_butt cat = (Cat_butt) treeNode.getContent();
        Probcat content = (Probcat) treeNode.getContent();
        holder.tv.setText(content.getName());
        if (content.num.equals(""))
            content.num = "0";

        holder.input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().equals(""))
                    content.num = "0";
                content.num = s.toString().trim();
            }
        });
        holder.input.setText(content.num);
    }

    @Override
    public int getLayoutId() {
        return R.layout.expan_group_view;
    }

    public class ViewHolder extends TreeViewBinder.ViewHolder{
        TextView tv;
        EditText input;

        public ViewHolder(View rootview){
            super(rootview);
            tv = rootview.findViewById(R.id.group_text);
            input = rootview.findViewById(R.id.group_input);
            /*learn = rootview.findViewById(R.id.but_learn);
            watch = rootview.findViewById(R.id.but_watch);*/
        }
    }
}