package com.reshuege.TreeView

import tellh.com.recyclertreeview_lib.LayoutItemType
import com.reshuege.R

class File(var fileName: String, var fileId: String) : LayoutItemType {

    override fun getLayoutId(): Int {
        return R.layout.item_file
    }
}