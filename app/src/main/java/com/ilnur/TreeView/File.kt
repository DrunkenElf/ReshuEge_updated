package com.ilnur.TreeView

import tellh.com.recyclertreeview_lib.LayoutItemType
import com.ilnur.R

class File(var fileName: String, var fileId: String) : LayoutItemType {

    override fun getLayoutId(): Int {
        return R.layout.item_file
    }
}