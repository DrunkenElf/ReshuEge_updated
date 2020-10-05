package com.ilnur.utils

import android.content.Context
import androidx.navigation.NavController

class MyNavController(context: Context) : NavController(context){

    override fun popBackStack(): Boolean {
        return super.popBackStack()
    }
}