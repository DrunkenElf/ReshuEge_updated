package com.reshuege.Json

import com.google.gson.annotations.SerializedName

class Subject {
    @SerializedName("name")
    var name: String? = null
    @SerializedName("href")
    var href: String? = null
    @SerializedName("date")
    var date: String? = null

    constructor() {}

    constructor(name: String, date: String, href: String) {
        this.name = name
        this.date = date
        this.href = href
    }
}