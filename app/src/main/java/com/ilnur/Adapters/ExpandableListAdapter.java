package com.ilnur.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.ilnur.R;
import com.ilnur.utils.Categories;

import java.util.ArrayList;

public class ExpandableListAdapter extends BaseExpandableListAdapter {
    Categories categories;
    Context context;

    public ExpandableListAdapter(){}

    public void setCategories(Categories categories) {
        this.categories = categories;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public ExpandableListAdapter(Context context, Categories categories){
        this.context = context;
        this.categories = categories;
    }


    @Override
    public int getGroupCount() {
        return categories.names.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return categories.children.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return categories.names.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return categories.children.get(groupPosition).keySet().toArray()[childPosition].toString();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.expan_group_view, null);
        }

        if (isExpanded){

        } else {

        }

        TextView tv = convertView.findViewById(R.id.group_text);
        EditText input = convertView.findViewById(R.id.group_input);
        input.setFocusable(false);

        tv.setText(categories.names.get(groupPosition));

        // set tv and check input

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.expan_child_view, null);
        }

        CheckBox check = convertView.findViewById(R.id.child_check);
        TextView tv = convertView.findViewById(R.id.child_text);
        //Log.d("EXPAN", categories.children.get(groupPosition).keySet().toArray()[childPosition].toString());
        tv.setText(categories.children.get(groupPosition).keySet().toArray()[childPosition].toString());

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}
