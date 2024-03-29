package com.reshuege.TreeBinders

import tellh.com.recyclertreeview_lib.LayoutItemType
import com.reshuege.R

class Head : LayoutItemType {
    var imgRes: Int = 0
    lateinit var title: String

    constructor() {}

    constructor(imgRes: Int, title: String) {
        this.imgRes = imgRes
        this.title = title
    }

    override fun getLayoutId(): Int {
        return R.layout.head
    }
}
