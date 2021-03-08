package com.reshuege.DataBase

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.createDataStore
import dagger.hilt.android.qualifiers.ApplicationContext

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DataPreferences @Inject constructor(@ApplicationContext context: Context)  {

    private val subj_questions_count: DataStore<Preferences> =
        context.createDataStore(name = "subjects_questions_count")
    private val results_prefs: DataStore<Preferences> =
        context.createDataStore(name = "subjects_results")

    suspend fun questionsThemeCount(href: String, theme: String): Int {
        return subj_questions_count.data.map { preferences ->
            preferences[getQuestionsThemeCount(href, theme)] ?: 0
        }.first()
    }
    suspend fun questionsCount(href: String): Int {
        return subj_questions_count.data.map { preferences ->
            preferences[getQuestionsCount(href)] ?: -1
        }.first()
    }

    suspend fun resultsCount(href: String, variant: Int): Int {
        return results_prefs.data.map { preferences ->
            preferences[getQuestionsResultCount(href, variant)] ?: -1
        }.first()
    }


    suspend fun saveQuestionsThemeCount(href: String, theme: String, res: Int) {
            subj_questions_count.edit { preferences ->
                preferences[getQuestionsThemeCount(href, theme)] = res
            }
    }
    suspend fun saveQuestionsCount(href: String, res: Int?) {
        if (res != null && res != -1 && res != 0)
            subj_questions_count.edit { preferences ->
                preferences[getQuestionsCount(href)] = res
            }
    }

    suspend fun saveResultsCount(href: String, variant: Int, res: Int) {
        results_prefs.edit { preferences ->
            preferences[getQuestionsResultCount(href, variant)] = res
        }
    }


    private companion object {
        //total tasks in variant
        fun getQuestionsCount(href: String) =
            intPreferencesKey("${href}_questions_count")

        fun getQuestionsThemeCount(href: String, theme: String) =
            intPreferencesKey("${href}_${theme}_questions_count")

        //solved tasks
        fun getQuestionsResultCount(href: String, variant: Int) =
            intPreferencesKey("${href}_${variant}_result")
    }

}