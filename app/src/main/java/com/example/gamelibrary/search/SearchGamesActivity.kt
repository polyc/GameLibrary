package com.example.gamelibrary.search

import android.app.SearchManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.gamelibrary.R
import com.example.gamelibrary.data.Game
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.json.JSONArray
import org.json.JSONObject

const val TAG = "Search"

const val url = "https://api.rawg.io/api/"

class SearchGamesActivity : AppCompatActivity() {

    private var page =  1
    private lateinit var queue: RequestQueue
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<GameViewHolder>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var user : FirebaseUser? = FirebaseAuth.getInstance().currentUser
    private lateinit var db :FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_games)
        setSupportActionBar(findViewById(R.id.toolbar))

        //get the db reference
        db = Firebase.firestore

        //init a request queue
        queue = Volley.newRequestQueue(this)

        //intercept the query, if any
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                //perform the custom search
                search(query, page,false)
            }
        }
        else {//perform the default search
            search(null, page,true)
        }

    }

    //state again the onCreate behavior since this is a single-top activity
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setContentView(R.layout.activity_search_games)
        setSupportActionBar(findViewById(R.id.toolbar))

        if (Intent.ACTION_SEARCH == intent?.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                search(query, page,false)
            }
        }
        else {
            search(null, page,true)
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        val inflater = menuInflater
        //inflate the menu from menu.xml
        inflater.inflate(R.menu.menu, menu)

        //Init the search widget
        val searchManager = getSystemService(android.content.Context.SEARCH_SERVICE) as SearchManager
        (menu?.findItem(R.id.search_widget)?.actionView as SearchView).apply {
            // Assumes current activity is the searchable activity
            setSearchableInfo(searchManager.getSearchableInfo(componentName))

            //Theme text
            this.findViewById<EditText>(androidx.appcompat.R.id.search_src_text).apply {
                setTextColor(Color.WHITE)
                setHintTextColor(Color.WHITE)
            }
        }
        return true
    }

    private fun search(query :String?, page :Int, defaultQuery: Boolean = true){
        //prepare the query for API
        val q =
            if(defaultQuery){
                "games?page=$page"
            }
            else {"games?search="+query+"&page="+page}

        //Setup the request
        val queryRequest = StringRequest(Request.Method.GET, url+q, Response.Listener { response ->
            val obj:JSONObject = JSONObject(response)
            //get the "results" from the response object
            val array: JSONArray = obj.getJSONArray("results")
            //init the adapter list
            val gameList :MutableList<Game?> = mutableListOf()

            //parse the JSONArray
            for (game_idx in 0 until array.length()-1) {
                val game: JSONObject = array[game_idx] as JSONObject
                val name = game.get("name").toString()
                val id = game.get("id") as Int
                val backgroundImage: String? = game.get("background_image").toString()

                var metacriticRating :Int? = null
                if(!game.isNull("metacritic"))
                    metacriticRating = game.get("metacritic") as Int?
                //populate the adapter list
                gameList.add(Game(name, id, backgroundImage, metacriticRating))
            }

            //Setup the RecyclerView
            viewManager = LinearLayoutManager(this)
            viewAdapter = GameSearchAdapter(gameList, db, user!!.uid, applicationContext)

            recyclerView = findViewById<RecyclerView>(R.id.my_recycler_view).apply{
                setHasFixedSize(true)
                layoutManager = viewManager
                adapter = viewAdapter
            }


        }, Response.ErrorListener { Log.d(TAG, "didn't work") })

        //Add query to queue
        queue.add(queryRequest)
    }
}