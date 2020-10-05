package com.ilnur.Json

import com.google.gson.annotations.SerializedName

//private Tema[] temas;

class Tema {
    @SerializedName("name")
    var name: String? = null
    @SerializedName("data")
    var data: String? = null
    @SerializedName("id")
    var id: Int = 0
    @SerializedName("temas")
    var temas: Array<Tema>? = null
    @SerializedName("open")
    var open: Int = 0

    val temasNames: String
        get() {
            val sb = StringBuilder()
            for (i in temas!!.indices) {
                if (i != temas!!.size - 1) {
                    sb.append(temas!![i].name + " / " + temas!![i].id + "\n")
                } else
                    sb.append(temas!![i].name + " / " + temas!![i].id)
            }
            return sb.toString()
        }
}
