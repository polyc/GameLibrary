package com.example.gamelibrary.settings

import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.widget.Toast
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.gamelibrary.R
import com.example.gamelibrary.search.SearchGamesRecentSuggestionsProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

private const val TAG = "SettingsFragment"

class SettingsFragment : PreferenceFragmentCompat(){
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        //User data
        val namePreference = findPreference<EditTextPreference>("name")
        val surnamePreference = findPreference<EditTextPreference>("surname")
        val emailPreference = findPreference<EditTextPreference>("email")
        setOnEditTextPreferenceChangeListener(namePreference!!)
        setOnEditTextPreferenceChangeListener(surnamePreference!!)
        setOnEditTextPreferenceChangeListener(emailPreference!!)

        //Search
        val clearSuggestions = findPreference<Preference>("clear_recent_search")
        clearSuggestions?.setOnPreferenceClickListener {
            SearchRecentSuggestions(context, SearchGamesRecentSuggestionsProvider.AUTHORITY,
                SearchGamesRecentSuggestionsProvider.MODE)
                .clearHistory()

            Toast.makeText(context, "Recent searches cleared", Toast.LENGTH_SHORT).show()
            true
        }

        //Logout
        val logoutPreference = findPreference<Preference>("logout_button")
        logoutPreference?.setOnPreferenceClickListener {
            Firebase.auth.signOut()
            activity?.finish()
            true
        }

        //Delete account
        val deletePreference = findPreference<Preference>("delete_account_button")
        deletePreference?.setOnPreferenceClickListener {
            val deleteAccountFragment = DeleteAccountDialogFragment()
            deleteAccountFragment.show(parentFragmentManager, null)
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
