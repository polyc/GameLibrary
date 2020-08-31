package com.example.gamelibrary.search

import android.app.SearchManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.gamelibrary.EndlessRecyclerViewScrollListner
import com.example.gamelibrary.R
import com.example.gamelibrary.data.Game
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.json.JSONArray
import org.json.JSONObject

private const val TAG = "Search"

const val url = "https://api.rawg.io/api/"

class SearchActivity : AppCompatActivity() {

    private lateinit var queue: RequestQueue
    private lateinit var recyclerView: RecyclerView
    private var viewAdapter: RecyclerView.Adapter<SearchViewHolder>? = null
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var user : FirebaseUser? = FirebaseAuth.getInstance().currentUser
    private lateinit var db :FirebaseFirestore
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var scrollListener: EndlessRecyclerViewScrollListner? = null
    private var query: String? = null
    private var defaultQuery = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_games)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //get the db reference
        db = Firebase.firestore

        //init a request queue
        queue = Volley.newRequestQueue(this)

        swipeRefreshLayout = findViewById(R.id.search_refresh_layout)
        swipeRefreshLayout.isRefreshing = true

        //intercept the query, if any
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                this.query = query
                defaultQuery = false
                //save query to search suggestion provider
                SearchRecentSuggestions(this, SearchGamesRecentSuggestionsProvider.AUTHORITY,
                    SearchGamesRecentSuggestionsProvider.MODE).saveRecentQuery(query, null)

                //perform the custom search
                setRefreshListener(this.query, defaultQuery)
                search(this.query, defaultQuery)

            }
        }
        else {//perform the default search
            val queryFilter = intent.extras?.getString("queryFilter")
            if (queryFilter!= null)
                this.query = queryFilter
            else
                this.query = null
            defaultQuery = true
            setRefreshListener(this.query, defaultQuery)
            search(this.query, defaultQuery)
        }
    }

    override fun onRestart() {
        super.onRestart()
        //handle logout case
        if(Firebase.auth.uid == null)
            finish()
    }

    //state again the onCreate behavior since this is a single-top activity
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setContentView(R.layout.activity_search_games)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        swipeRefreshLayout = findViewById(R.id.search_refresh_layout)
        swipeRefreshLayout.isRefreshing = true

        if (Intent.ACTION_SEARCH == intent?.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                this.query = query
                defaultQuery = false
                //save query to search suggestion provider
                SearchRecentSuggestions(this, SearchGamesRecentSuggestionsProvider.AUTHORITY,
                    SearchGamesRecentSuggestionsProvider.MODE).saveRecentQuery(query, null)

                //perform the custom search
                setRefreshListener(this.query, defaultQuery)
                search(this.query, defaultQuery)
            }
        }
        else {
            val queryFilter = intent?.extras?.getString("queryFilter")
            if (queryFilter!= null)
                this.query = queryFilter
            else
                this.query = null
            defaultQuery = true
            setRefreshListener(this.query, defaultQuery)
            search(this.query, defaultQuery)
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        val inflater = menuInflater
        //inflate the search_menu from search_menu.xml
        inflater.inflate(R.menu.search_menu, menu)

        //Init the search widget
        val searchManager = getSystemService(android.content.Context.SEARCH_SERVICE) as SearchManager
        (menu?.findItem(R.id.search_widget)?.actionView as SearchView).apply {
            // Assumes current activity is the searchable activity
            setSearchableInfo(searchManager.getSearchableInfo(componentName))

            //Theme hint text text
            this.findViewById<EditText>(androidx.appcompat.R.id.search_src_text).apply {
                setTextColor(Color.WHITE)
                setHintTextColor(Color.WHITE)
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId){
        android.R.id.home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun search(query :String?, defaultQuery: Boolean = true){
        if(viewAdapter != null)
            (viewAdapter as SearchAdapter).apply {
                gameList.clear()
                notifyDataSetChanged()
            }

        if (scrollListener != null)
            scrollListener?.resetState()

        //prepare the query for API
        val q =
            if(defaultQuery){
                if (this.query != null){
                    "games?$query"
                }
                else
                    "games"
            }
            else {
                "games?search=$query"
            }

        Log.d(TAG, url+q)
        //Setup the request
        val queryRequest = StringRequest(Request.Method.GET, url+q, { response ->
            val gameList = parseResult(response)
            setupRecyclerView(gameList)
            swipeRefreshLayout.isRefreshing = false
            if(gameList.isEmpty()){
                findViewById<ImageView>(R.id.empty_results_image).apply {
                    visibility = ImageView.VISIBLE
                }
                findViewById<TextView>(R.id.empty_results_text).apply {
                    visibility = TextView.VISIBLE
                }
            }
        }, {
            swipeRefreshLayout.isRefreshing = false
            Log.d(TAG, "Not able to get search results")
            Toast.makeText(applicationContext, "Not able to get search results, check your network", Toast.LENGTH_SHORT).show()
        })

        //Add query to queue
        queue.add(queryRequest)
    }

    private fun loadMoreSearch(query :String?, page :Int, defaultQuery: Boolean = true, totalItemsCount: Int){
        val q =
            if(defaultQuery){
                if (this.query != null){
                    "games?page=$page&$query"
                }
                else{
                    "games?page=$page"
                }
            }
            else {"games?search="+query+"&page="+page}

        //Setup the request
        val queryRequest = StringRequest(Request.Method.GET, url+q, { response ->
            val gameList = parseResult(response)
            (viewAdapter as SearchAdapter).apply {
                swipeRefreshLayout.isRefreshing = false
                this.gameList.addAll(gameList)
                notifyItemRangeInserted(totalItemsCount, gameList.size)
            }

        }, {
            swipeRefreshLayout.isRefreshing = false
            Log.d(TAG, "Not able to get search results")
            Toast.makeText(applicationContext, "Not able to get search results, check your network", Toast.LENGTH_SHORT).show()
        })

        //Add query to queue
        swipeRefreshLayout.isRefreshing = true
        queue.add(queryRequest)
    }

    private fun setupRecyclerView(gameList: MutableList<Game?>){
        //Setup the RecyclerView
        viewManager = LinearLayoutManager(this)
        viewAdapter = SearchAdapter(gameList, db, user!!.uid)

        recyclerView = findViewById<RecyclerView>(R.id.my_recycler_view).apply{
            layoutManager = viewManager
            adapter = viewAdapter
        }

        scrollListener = object: EndlessRecyclerViewScrollListner(viewManager as LinearLayoutManager){
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                loadMoreSearch(query, page, defaultQuery, totalItemsCount)
            }
        }

        recyclerView.addOnScrollListener(scrollListener as EndlessRecyclerViewScrollListner)
    }

    private fun parseResult(response: String): MutableList<Game?>{
        val obj:JSONObject = JSONObject(response)
        //get the "results" from the response object
        val array: JSONArray = obj.getJSONArray("results")
        //init the adapter list
        val gameList :MutableList<Game?> = mutableListOf()

        //parse the JSONArray
        for (game_idx in 0 until array.length()) {
            val game: JSONObject = array[game_idx] as JSONObject
            val name = game.get("name").toString()
            val id = game.get("id") as Int
            val backgroundImage: String? = game.get("background_image").toString()

            var metacriticRating: Int? = null
            if (!game.isNull("metacritic"))
                metacriticRating = game.get("metacritic") as Int?
            //populate the adapter list
            gameList.add(Game(name, id, backgroundImage, metacriticRating))
        }
        return gameList
    }

    //set refresh layout listener and color for progressbar
    private fun setRefreshListener(query: String?, defaultQuery: Boolean){
        swipeRefreshLayout.apply {
            setOnRefreshListener {
                search(query, defaultQuery)
            }
            setColorSchemeResources(R.color.colorPrimary)
        }
    }
}