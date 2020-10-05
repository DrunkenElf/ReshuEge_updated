package com.ilnur.Fragments

import android.os.Bundle


import androidx.preference.PreferenceFragmentCompat

import com.ilnur.R

/**
 * Created by Admin on 16.11.2015.
 */
class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref)
    }
}
