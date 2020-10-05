package com.ilnur.TreeView;

import com.ilnur.R;

import tellh.com.recyclertreeview_lib.LayoutItemType;

public class Probcat implements LayoutItemType {
    String name;
    String href;
    public String num = "0";
    public boolean isLeaf = true;
    public boolean isChild = true;

    public Probcat(String name, String href, boolean isChild){
        this.name = name;
        this.href = href;
        this.isChild = isChild;
    }

    public String getHref() {
        return href;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public void setLeaf(boolean leaf) {
        isLeaf = leaf;
    }

    public String getName() {
        return name;
    }

    @Override
    public int getLayoutId() {
        if (isChild)
            return R.layout.expan_child_view;
        return R.layout.expan_group_view;
    }
}
