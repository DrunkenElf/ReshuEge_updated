package com.reshuege.Session

import android.content.Context
import androidx.appcompat.app.AppCompatActivity


class Settings {

    fun setSession(session: Session, context: Context) {
        val settings = context.getSharedPreferences("settings", AppCompatActivity.MODE_PRIVATE)
        settings.edit().putString("session", session.session).putString("state", session.sessionState.toString()).apply()
    }

    fun getSession(context: Context): Session {
        val settings = context.getSharedPreferences("settings", AppCompatActivity.MODE_PRIVATE)
        return Session(
                session = settings.getString("session", null),
                sessionState = SessionState.valueOf(settings.getString("state", SessionState.anonymus.toString())!!)
        )
    }

    fun setLoginAndPassword(login: String, password: String, context: Context) {
        val settings = context.getSharedPreferences("settings", AppCompatActivity.MODE_PRIVATE)
        settings.edit().putString("login", login).putString("password", password).apply()
    }

    fun getLogin(context: Context): String? {
        val settings = context.getSharedPreferences("settings", AppCompatActivity.MODE_PRIVATE)
        return settings.getString("login", "")
    }

    fun getPassword(context: Context): String? {
        val settings = context.getSharedPreferences("settings", AppCompatActivity.MODE_PRIVATE)
        return settings.getString("password", "")
    }

    fun getFirstStartFlag(context: Context): Boolean {
        val settings = context.getSharedPreferences("settings", AppCompatActivity.MODE_PRIVATE)
        return settings.getBoolean("first_start", false)
    }

    fun setFirstStartFlag(flag: Boolean, context: Context) {
        val settings = context.getSharedPreferences("settings", AppCompatActivity.MODE_PRIVATE)
        settings.edit().putBoolean("first_start", flag).apply()
    }
}
