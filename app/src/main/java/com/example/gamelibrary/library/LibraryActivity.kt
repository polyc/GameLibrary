package com.example.gamelibrary.library

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.example.gamelibrary.R
import com.example.gamelibrary.data.UserData
import com.example.gamelibrary.search.SearchGamesActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

const val RC_SIGN_IN = 1
const val TAG = "GoogleSingIn"

class LibraryActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<LibraryViewHolder>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var queue: RequestQueue? = null

    private lateinit var  googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)
        setSupportActionBar(findViewById(R.id.toolbar))

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        signIn()

        // ...
        // Initialize Firebase Auth
        auth = Firebase.auth

        //setup a listener for addGames button
        findViewById<FloatingActionButton>(R.id.addGames).setOnClickListener{
            val intent = Intent(this, SearchGamesActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent)
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent,
            RC_SIGN_IN
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                // ...
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    Log.d(TAG, "${user.toString()}")

                    db = Firebase.firestore
                    val userObj = UserData(user!!.displayName, mapOf(Pair("init", "init")))
                    db.collection("userData").document(user.uid).get().addOnSuccessListener{ doc ->
                        Log.d(TAG, "${doc.toString()}")
                        if (doc.data == null){
                            db.collection("userData").document(user.uid).set(userObj)
                        }
                        else{
                            populateLibrary()
                        }
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    // ...
                    //Snackbar.make(view, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                    //updateUI(null)
                }

                // ...
            }
    }

    //call it after authentication only
    private fun populateLibrary(){

        if(queue == null)
            queue = Volley.newRequestQueue(applicationContext)

        db.collection("userData").document(auth.currentUser!!.uid).
            get().addOnSuccessListener{
            var library = it.get("library") as Map<String, String>
            library = library.filterKeys { key-> key != "init" }
            Log.d(TAG, library.toString())

            val keys = library.keys.toMutableList()

            viewManager = LinearLayoutManager(this)
            viewAdapter = LibraryAdapter(keys, db, auth.uid!!, queue!!)

            recyclerView = findViewById<RecyclerView>(R.id.library_recycler_view).apply{
                layoutManager = viewManager
                adapter = viewAdapter
            }


        }.addOnFailureListener(){}
    }

    /*override fun onResume() {

        super.onResume()
    }*/

    override fun onRestart() {
        //REFRESH DATA
        super.onRestart()
        populateLibrary()
    }
}

