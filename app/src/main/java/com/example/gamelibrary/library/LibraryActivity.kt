package com.example.gamelibrary.library

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

const val RC_SIGN_IN = 1
const val TAG = "GoogleSignIn"

class LibraryActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<LibraryViewHolder>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var queue: RequestQueue? = null
    private lateinit var library :MutableList<String>
    private lateinit var  googleSignInClient: GoogleSignInClient
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

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

        // Initialize Firebase Auth
        auth = Firebase.auth

        //check if the user has already logged in
        val signInAccount = GoogleSignIn.getLastSignedInAccount(this)
        if(signInAccount == null || signInAccount.isExpired)
            signIn()
        else
            firebaseAuthWithGoogle(signInAccount.idToken!!)

        //setup a listener for addGames button
        findViewById<FloatingActionButton>(R.id.addGames).setOnClickListener{
            val intent = Intent(this, SearchGamesActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent)
        }

        //set refresh layout listener and color for progressbar
        swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.library_refresh_layout)
        swipeRefreshLayout.isRefreshing = true
        swipeRefreshLayout.apply {
            setOnRefreshListener {
                refreshLibrary()
                isRefreshing = false
            }
            setColorSchemeResources(R.color.colorPrimary)
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
                //Perform authentication
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
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

                    //Update UserData to Firebase, if not present any. Else populate the UI with library
                    db = Firebase.firestore
                    val userObj = UserData(user!!.displayName, mapOf(Pair("init", "init")))
                    db.collection("userData").document(user.uid).get().addOnSuccessListener{ doc ->
                        Log.d(TAG, "${doc.toString()}")
                        if (doc.data == null){
                            //write UserData to Firebase
                            db.collection("userData").document(user.uid).set(userObj)
                            db.collection("userData").document(user.uid).get().addOnSuccessListener{ doc ->
                                populateLibrary(doc)
                            }
                        }
                        else{
                            //display library
                            populateLibrary(doc)
                        }
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    // ...
                    Toast.makeText(applicationContext, "Sign In failed", Toast.LENGTH_SHORT).show()
                }

                // ...
            }
    }

    private fun getLibrary(snapshot: DocumentSnapshot): Map<String, String> {
        //get Library Map from Firebase
        var libData = snapshot.get("library") as Map<String, String>
        //don't consider init key
        return libData.filterKeys { key-> key != "init" }
    }

    //call it after authentication only
    private fun populateLibrary(doc: DocumentSnapshot){
        //init the request queue
        if(queue == null)
            queue = Volley.newRequestQueue(applicationContext)

        //get a mutable list of game ID's
        library = getLibrary(doc).keys.toMutableList()

        //prepare the recycler view
        viewManager = LinearLayoutManager(this)
        viewAdapter = LibraryAdapter(library, db, auth.uid!!, queue!!, this)

        recyclerView = findViewById<RecyclerView>(R.id.library_recycler_view).apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }
        swipeRefreshLayout.isRefreshing = false
    }

    private fun refreshLibrary(){
        db.collection("userData").document(auth.currentUser!!.uid).
        get().addOnSuccessListener{
            library.clear()
            library.addAll(getLibrary(it).keys.toMutableList())
            //notify the adapter
            viewAdapter.notifyDataSetChanged()
        }
        swipeRefreshLayout.isRefreshing = false
    }

    override fun onRestart() {
        //Refresh Library
        super.onRestart()
        swipeRefreshLayout.isRefreshing = true
        refreshLibrary()
    }
}

