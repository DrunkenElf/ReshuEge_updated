package com.ilnur.TreeBinders


import tellh.com.recyclertreeview_lib.LayoutItemType
import com.ilnur.R

class Tail : LayoutItemType {
    var type: Int = 0
    lateinit var hint: String

    constructor() {}

    constructor(type: Int, hint: String) {
        this.type = type
        this.hint = hint
    }

    override fun getLayoutId(): Int {
        return R.layout.tail
    }
}