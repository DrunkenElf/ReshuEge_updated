package com.ilnur.TreeView

import tellh.com.recyclertreeview_lib.LayoutItemType
import com.ilnur.R

class Dir(var dirName: String, private val id: String, var isBig: Boolean) : LayoutItemType {

    override fun getLayoutId(): Int {
        return R.layout.item_dir
    }
}