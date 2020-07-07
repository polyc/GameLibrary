package com.example.gamelibrary.search

import android.app.SearchManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.gamelibrary.R
import com.example.gamelibrary.data.Game
import com.google.gson.JsonArray
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import org.json.JSONArray
import org.json.JSONObject

const val TAG = "Search"

const val url = "https://api.rawg.io/api/"

class SearchGamesActivity : AppCompatActivity() {

    private lateinit var queue: RequestQueue
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<GameViewHolder>
    private lateinit var viewManager: RecyclerView.LayoutManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_games)

        queue = Volley.newRequestQueue(this)

        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                search(query,false)
            }
        }
        else {
            search(null,true)
        }

    }

    private fun search(query :String?, defaultQuery: Boolean = true){
        val q = if(defaultQuery){"games"} else {"games/?slug="+query}
        val queryRequest = StringRequest(Request.Method.GET, url+q, Response.Listener { response ->
            val obj:JSONObject = JSONObject(response)
            val array: JSONArray = obj.getJSONArray("results")
            val gameList :MutableList<Game?> = mutableListOf()

            for (game_idx in 0 until array.length()-1) {
                val game: JSONObject = array[game_idx] as JSONObject
                val name = game.get("name").toString()
                val backgroundImage: String? = game.get("background_image").toString()
                val metacriticRating: Int? = game.get("metacritic") as Int
                gameList.add(Game(name, backgroundImage, metacriticRating))
            }

            viewManager = LinearLayoutManager(this)
            viewAdapter = GameAdapter(gameList)

            recyclerView = findViewById<RecyclerView>(R.id.my_recycler_view).apply{
                setHasFixedSize(true)
                layoutManager = viewManager
                adapter = viewAdapter
            }


        }, Response.ErrorListener { Log.d(TAG, "didn't work") })

        queue.add(queryRequest)
    }
}