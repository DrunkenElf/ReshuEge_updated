package com.reshuege.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.createDataStore
import kotlinx.coroutines.flow.first

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

        private val is_logged = booleanPreferencesKey("is_logged")

        private fun tests_id(href: String) =
            intPreferencesKey("tests_$href")

        private fun theory_id(href: String) =
            intPreferencesKey("theory_$href")

        private fun crit_upd(href: String) =
            booleanPreferencesKey("crit_$href")
    }

    suspend fun setLogged(isLogged: Boolean){
        dataStore.edit {
            it[is_logged] = isLogged
        }
    }

    suspend fun saveSubjTestCrit(href: String, value: Boolean) =
        critUpdate.edit {
            it[crit_upd(href)] = value
        }

    suspend fun getSubjTestCrit(href: String) =
        critUpdate.data.map {
            it -> it[crit_upd(href)] ?: true
        }.first()


    suspend fun saveSubjTheorVersion(href: String, version: Int) =
        versionTheory.edit {
            it[theory_id(href)] = version
        }

    suspend fun getSubjTheorVersion(href: String) =
        versionTheory.data.map {
            it -> it[theory_id(href)] ?: -1
        }.first()


    suspend fun saveSubjVersion(href: String, version: Int) =
        versionTests.edit {
            it[tests_id(href)] = version
        }

    suspend fun getSubjVersion(href: String) =
        versionTests.data.map {
            it -> it[tests_id(href)] ?: -1
        }.first()


    val isLogged = dataStore.data.map {
        it -> it[is_logged] ?: false
    }
}