package com.ilnur.Json

import com.google.gson.annotations.SerializedName

class Root {
    @SerializedName("name")
    var name: String? = null
    @SerializedName("temas")
    var temas: Array<Tema>? = null
    @SerializedName("date")
    var date: String? = null

    private var mas: Array<String?> = emptyArray()

    val temasNames: String
        get() {
            val sb = StringBuilder()
            for (i in temas!!.indices) {
                if (i == temas!!.size - 1) {
                    sb.append(temas!![i].name + " / " + temas!![i].id)
                } else {
                    sb.append(temas!![i].name + " / " + temas!![i].id + "\n")
                }
            }
            return sb.toString()
        }

    fun getTema(name: String): Tema? {
        var temp: Tema? = null
        for (i in temas!!.indices) {
            if (temas!![i].name == name)
                temp = temas!![i]
        }
        return temp
    }

    fun setTemasName() {
        mas = arrayOfNulls(temas!!.size)
        for (i in temas!!.indices) {
            if (temas!![i] == null)
                mas[i] = "Null"
            else {
                mas[i] = temas!![i].name
            }
        }
    }

    fun temasName(): Array<String?> {
        setTemasName()
        return mas
    }
}
