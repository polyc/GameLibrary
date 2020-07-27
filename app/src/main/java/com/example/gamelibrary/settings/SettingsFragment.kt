package com.example.gamelibrary.settings

import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.gamelibrary.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

private const val TAG = "Settings"

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val namePreference = findPreference<EditTextPreference>("name")
        val surnamePreference = findPreference<EditTextPreference>("surname")
        val emailPreference = findPreference<EditTextPreference>("email")
        val logoutPreference = findPreference<Preference>("logout_button")

        setOnEditTextPreferenceChangeListener(namePreference!!)
        setOnEditTextPreferenceChangeListener(surnamePreference!!)
        setOnEditTextPreferenceChangeListener(emailPreference!!)

        logoutPreference?.setOnPreferenceClickListener {
            Firebase.auth.signOut()
            activity?.finish()
            true
        }

    }

    private fun  setOnEditTextPreferenceChangeListener(preference: EditTextPreference){
        preference.onPreferenceChangeListener =  Preference.OnPreferenceChangeListener { preference, newValue ->
            val user = Firebase.auth.currentUser!!
            val db: FirebaseFirestore = Firebase.firestore
            val value = newValue.toString()
            db.collection("userData").document(user.uid).update(preference.key, value)
            true
        }
    }
}
