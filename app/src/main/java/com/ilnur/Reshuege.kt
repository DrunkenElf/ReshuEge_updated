package com.ilnur

import android.app.Application
import android.database.sqlite.SQLiteException
import coil.Coil
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.util.CoilUtils
import com.androidnetworking.AndroidNetworking
import com.facebook.drawee.backends.pipeline.Fresco
import com.ilnur.DataBase.MyDB
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.DEBUG_PROPERTY_NAME
import kotlinx.coroutines.DEBUG_PROPERTY_VALUE_ON
import okhttp3.OkHttpClient

@HiltAndroidApp
class Reshuege : Application(){

    override fun onCreate() {
        super.onCreate()
        System.setProperty(DEBUG_PROPERTY_NAME, DEBUG_PROPERTY_VALUE_ON)
        AndroidNetworking.initialize(applicationContext)
        AndroidNetworking.enableLogging()

        /*val imageLoader = ImageLoader.Builder(this)
            .componentRegistry {
                add(SvgDecoder(this@Reshuege))
            }
            .okHttpClient {
                OkHttpClient.Builder()
                    .cache(CoilUtils.createDefaultCache(this))
                    .build()
            }
            .build()
        Coil.setImageLoader(imageLoader)*/
    }

}