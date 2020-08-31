package com.example.gamelibrary.library

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.example.gamelibrary.R
import com.example.gamelibrary.about.AboutActivity
import com.example.gamelibrary.data.UserData
import com.example.gamelibrary.search.SearchActivity
import com.example.gamelibrary.settings.SettingsActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
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
private const val TAG = "LibraryActivity"

class LibraryActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: LibraryAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var queue: RequestQueue? = null
    private lateinit var keys: MutableList<String>
    private lateinit var gameNames: MutableList<String>
    private lateinit var  googleSignInClient: GoogleSignInClient
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var userObj: UserData

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)
        setSupportActionBar(findViewById(R.id.library_toolbar))

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        init()
    }

    override fun onStart() {
        super.onStart()
        //not logged in
        if(auth.uid == null){
            setTheme(R.style.AppTheme)
            setContentView(R.layout.activity_library)
            setSupportActionBar(findViewById(R.id.library_toolbar))
            init()
        }
    }

    private fun init(){
        if(auth.uid != null){
            db = Firebase.firestore
            db.collection("userData").document(auth.currentUser!!.uid).get().addOnSuccessListener { doc ->
                populateLibrary(doc)
            }
        }
        else {//user is not logged in or has signed out
            googleSignInClient.signOut()
            signIn()
        }

        //setup a listener for addGames button
        findViewById<FloatingActionButton>(R.id.addGames).setOnClickListener{
            val intent = Intent(this, SearchActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent)
        }

        //set refresh layout listener and color for progressbar
        swipeRefreshLayout = findViewById(R.id.library_refresh_layout)
        swipeRefreshLayout.isRefreshing = true
        swipeRefreshLayout.apply {
            setOnRefreshListener {
                refreshLibrary(true)
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
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    Log.d(TAG, "${user.toString()}")

                    //Update UserData to Firebase, if not present any. Else populate the UI with library
                    db = Firebase.firestore
                    userObj = UserData(account.givenName, account.familyName, account.email, mapOf(Pair("init", "init")))
                    db.collection("userData").document(user!!.uid).get().addOnSuccessListener{ doc ->
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
                    }.addOnFailureListener{
                        Toast.makeText(applicationContext, "Not able to get your library, check your network connection", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(applicationContext, "Sign In failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun getLibrary(snapshot: DocumentSnapshot): Map<String, String> {
        //get Library Map from Firebase
        return snapshot.get("library") as Map<String, String>
    }

    //call it after authentication only
    private fun populateLibrary(doc: DocumentSnapshot){
        //init the request queue
        if(queue == null)
            queue = Volley.newRequestQueue(applicationContext)

        //get a mutable list of game ID's
        val library = getLibrary(doc)

        keys = library.filterKeys { key-> key != "init" }.keys.toMutableList()

        //get a mutable list of values (Game name)
        gameNames = library.values.filter { value-> value != "init" }.toMutableList()

        //prepare the recycler view
        viewManager = LinearLayoutManager(this)
        viewAdapter = LibraryAdapter(keys, gameNames, db, auth.uid!!, queue!!, this)

        recyclerView = findViewById<RecyclerView>(R.id.library_recycler_view).apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }
        swipeRefreshLayout.isRefreshing = false
    }

    private fun refreshLibrary(force: Boolean = false){
        db.collection("userData").document(auth.currentUser!!.uid).
        get().addOnSuccessListener{
            val library = getLibrary(it)
            keys.clear()
            keys.addAll(library.filterKeys { key-> key != "init" }.keys.toMutableList())
            viewAdapter.gameList.clear()
            viewAdapter.gameList.addAll(keys)

            gameNames.clear()
            gameNames.addAll(library.values.filter { value-> value != "init" }.toMutableList())
            viewAdapter.gameNames.clear()
            viewAdapter.gameNames.addAll(gameNames)
            //notify the adapter
            viewAdapter.forceRefresh = force
            viewAdapter.notifyDataSetChanged()
        }
        swipeRefreshLayout.isRefreshing = false
    }

    override fun onRestart() {
        //Refresh Library
        super.onRestart()
        //if user didn't logout
        if (auth.uid != null){
            swipeRefreshLayout.isRefreshing = true
            refreshLibrary()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        val inflater = menuInflater
        //inflate the search_menu library_menu.xml
        inflater.inflate(R.menu.library_menu, menu)

        //Setup library filtering
        val searchView = menu?.findItem(R.id.actionSearch)?.actionView as SearchView
        searchView.apply {
            imeOptions = EditorInfo.IME_ACTION_DONE
            setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(p0: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(p0: String?): Boolean {
                    viewAdapter.filter.filter(p0)
                    return false
                }
            })

            //set search hint text color
            val id = context.resources.getIdentifier("android:id/search_src_text", null, null)
            findViewById<TextView>(id).apply {
                setTextColor(Color.WHITE)
            }
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.library_action_settings -> {
            startActivity(Intent(applicationContext, SettingsActivity::class.java))
            true
        }
        R.id.library_action_about -> {
            startActivity(Intent(applicationContext, AboutActivity::class.java))
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

}

