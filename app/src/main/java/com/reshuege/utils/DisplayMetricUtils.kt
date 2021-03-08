package com.reshuege.utils

import android.content.Context
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.WindowManager

object DisplayMetricUtils {

    /**
     * @return высота полоски с кнопками внизу экрана
     */
    fun getNavigationBarHeight(context: Context): Int {
        val resources = context.resources
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else 0
    }

    fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    fun getActionBarHeight(context: Context): Int {
        val tv = TypedValue()
        context.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)
        return context.resources.getDimensionPixelSize(tv.resourceId)
    }

    fun getContentHeight(context: Context): Int {
        var height = 0
        try {
            height = (getScreenSize(context).y
                    - getActionBarHeight(context).toFloat()
                    - getNavigationBarHeight(context).toFloat()
                    - getStatusBarHeight(context).toFloat()).toInt()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return height
    }

    fun convertDpToPixel(dp: Float, context: Context): Float {
        val resources = context.resources
        val metrics = resources.displayMetrics
        return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun convertPixelsToDp(px: Float, context: Context): Float {
        val resources = context.resources
        val metrics = resources.displayMetrics
        return px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun getDensity(context: Context): Float {
        val resources = context.resources
        val metrics = resources.displayMetrics
        return metrics.density
    }

    fun getDisplayWidthWithMargin(leftMargin: Float, rightMargin: Float, context: Context): Float {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = android.graphics.Point()
        display.getSize(size)
        return size.x.toFloat() - leftMargin - rightMargin
    }


    fun getScreenSize(context: Context): Point {
        val resources = context.resources
        val metrics = resources.displayMetrics
        return Point(metrics.widthPixels.toFloat(), metrics.heightPixels.toFloat())
    }

    fun scaleViewSize(context: Context, source: Point): Point {
        return scaleViewSize(source, getScreenSize(context))
    }

    fun scaleViewSize(source: Point, parent: Point): Point {
        val scale = Math.min(parent.x / source.x, parent.y / source.y)
        return Point(source.x * scale, source.y * scale)
    }

    class Point(val x: Float, val y: Float)

    fun setViewSize(view: View, size: Point) {
        if (size.x >= -2 && size.y >= -2) {
            val layoutParams = view.layoutParams
            layoutParams.width = size.x.toInt()
            layoutParams.height = size.y.toInt()
            view.layoutParams = layoutParams
        } else {
            throw IllegalArgumentException("incorrect view size: " + size.x + "x" + size.y)
        }
    }
}
