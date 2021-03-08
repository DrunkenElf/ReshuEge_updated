package com.reshuege.TreeView

import tellh.com.recyclertreeview_lib.LayoutItemType
import com.reshuege.R

class Dir(var dirName: String, private val id: String, var isBig: Boolean) : LayoutItemType {

    override fun getLayoutId(): Int {
        return R.layout.item_dir
    }
}