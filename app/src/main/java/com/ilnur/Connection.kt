package com.ilnur

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Handler
import androidx.appcompat.app.AlertDialog


/**
 * Created by Admin on 27.02.2016.
 */
object Connection {

    private val BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkTXGmt3Fue3m3FiWt7DlXYnEqP2e7GAIDo/fY+drOWwyc+uKuSL1L1gQGUwOLAaq4z4w5FVU3JgMfvq+aJvR4/TY78BK+MfUE0LRP4mT8emWAtoN/KrAD3NuU2I/5VBsJG1QQ08kJa68BVmcDI0lUV2/XdWzUHQoTUyP+OC9ic5ltb1YZcLyfCsgaA3f1EibwMVUSGLhEGj5rqkyxTGiCa27zywdDoE8XdgV6RiQ58i+TqxjJp2w9fCmCgHI3qmaYzxaqA9KPqTZ5dyagDpul/Pm9sIoAvcz1xdTW4tkm/uUGdZRpSS3DTpCAZQ48DhH8PD2US1+xFQiZf8s3AmlPQIDAQAB"
    private val SALT = byteArrayOf(-46, 65, 30, -128, -113, -103, -57, 74, -64, 51, 88, -95, -45, 77, -117, -36, -11, 32, -64, 89)

    fun hasConnection(context: Context): Boolean {

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var wifiInfo: NetworkInfo? = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        if (wifiInfo != null && wifiInfo.isConnected) {
            return true
        }
        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        if (wifiInfo != null && wifiInfo.isConnected) {
            return true
        }
        wifiInfo = cm.activeNetworkInfo
        if (wifiInfo != null && wifiInfo.isConnected) {
            return true
        }
        showErrorConnectionMessage(context)
        return false
    }

    fun hasConnectionMain(context: Context, show: Boolean): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var wifiInfo: NetworkInfo? = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        if (wifiInfo != null && wifiInfo.isConnected) {
            return true
        }
        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        if (wifiInfo != null && wifiInfo.isConnected) {
            return true
        }
        wifiInfo = cm.activeNetworkInfo
        if (wifiInfo != null && wifiInfo.isConnected) {
            return true
        }
        if (show)
            showErrorConnectionMessage(context)
        return false
    }


    private fun showErrorConnectionMessage(context: Context) {
        val mHandler = Handler()
        mHandler.post {
            val ad = AlertDialog.Builder(context)
            ad.setTitle(context.getString(R.string.connection_error_title))
            ad.setMessage(context.getString(R.string.connection_error))

            ad.setPositiveButton("Ок") { dialog, arg1 -> }

            ad.setCancelable(true)

            ad.show()
        }
    }

}
