package com.reshuege.utils

import android.graphics.Bitmap
import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class TaskImage(
        var id_orig: String? = null,
        var id_masked: String? = null,
        var filename: String? = null,
        var path: String? = null,
        var bitmap: Bitmap? = null,
        var indice: Int? = null
): Parcelable
@Keep
@Parcelize
data class Results(
        var id_orig: String? = null,
        var id_masked: String? = null,
        var filename: String? = null,
        var path: String? = null,
        var indice: Int? = null
): Parcelable
