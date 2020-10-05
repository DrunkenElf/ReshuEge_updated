package com.ilnur.utils

import android.content.Context
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.preferencesKey
import kotlinx.coroutines.flow.map


class SettingsImp(val context: Context){

    private val dataStore = context.createDataStore(name = "settings_pref")
    private val versionTests = context.createDataStore(name = "version_tests")
    private val latestVersionTests = context.createDataStore(name = "latest_version_tests")
    private val versionTheory = context.createDataStore(name = "version_theory")
    private val subjectsQuesCount = context.createDataStore(name = "subjects_questions_count")
    private val theoryTests = context.createDataStore(name = "theory_tests")
    private val critUpdate = context.createDataStore(name = "critical_update")

    companion object {

        private val is_logged = preferencesKey<Boolean>("is_logged")

    }

    suspend fun setLogged(isLogged: Boolean){
        dataStore.edit {
            it[is_logged] = isLogged
        }
    }

    val isLogged = dataStore.data.map {
        it -> it[is_logged] ?: false
    }
}