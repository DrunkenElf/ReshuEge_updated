package com.ilnur

import com.google.gson.annotations.SerializedName

class User {
    @SerializedName("login")
    var login: String? = null
    @SerializedName("password")
    var password: String? = null
    @SerializedName("session_id")
    var session_id: String? = null

    constructor()

    constructor(login: String?, password: String?, session_id: String?) {
        this.login = login
        this.password = password
        this.session_id = session_id
    }
}
