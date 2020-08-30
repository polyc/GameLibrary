package com.example.gamelibrary.settings

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.example.gamelibrary.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SettingsActivity : AppCompatActivity(), DeleteAccountDialogFragment.NoticeDialogListener{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(findViewById(R.id.settings_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.setting_card, SettingsFragment())
            .commit()

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId){
        android.R.id.home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        val db = Firebase.firestore
        val user = Firebase.auth.currentUser!!
        db.collection("userData").document(user.uid).delete().addOnSuccessListener{
            user.delete().addOnCompleteListener {
                finish()
            }
        }.addOnFailureListener{
            Toast.makeText(applicationContext, "Unable to delete this account now", Toast.LENGTH_SHORT)
        }
        Toast.makeText(applicationContext, "We are logging you out and we will delete your account", Toast.LENGTH_LONG)
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
    }
}