package com.reshuege.Fragments

import android.os.Bundle


import androidx.preference.PreferenceFragmentCompat

import com.reshuege.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref)
    }
}
