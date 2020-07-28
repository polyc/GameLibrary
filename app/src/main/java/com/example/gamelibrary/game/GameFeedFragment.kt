package com.example.gamelibrary.game

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.gamelibrary.EndlessRecyclerViewScrollListner
import com.example.gamelibrary.R
import com.example.gamelibrary.data.Game
import com.example.gamelibrary.data.Post
import com.example.gamelibrary.post.PostAdapter
import com.example.gamelibrary.post.PostViewHolder
import org.json.JSONArray
import org.json.JSONObject


const val url = "https://api.rawg.io/api/games/"

private const val TAG = "Feed"

class GameFeedFragment(val game: Game): Fragment() {
    private lateinit var queue: RequestQueue
    private lateinit var recyclerView: RecyclerView
    private var viewAdapter: RecyclerView.Adapter<PostViewHolder>? = null
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var scrollListener: EndlessRecyclerViewScrollListner? = null
    private var query: String? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.game_feed_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        swipeRefreshLayout = view.findViewById(R.id.feed_refresh_layout)
        setRefreshListener()
        swipeRefreshLayout?.isRefreshing = true


        queue = Volley.newRequestQueue(activity)

        query = url + game.id + "/reddit"

        getPosts()
    }

    private fun getPosts(){
        if(viewAdapter != null)
            (viewAdapter as PostAdapter).apply {
                postList.clear()
                notifyDataSetChanged()
            }

        if (scrollListener != null)
            scrollListener?.resetState()

        //get data from API
        val request = JsonObjectRequest(Request.Method.GET, query, null,
            Response.Listener { response ->
                val postList = parseResult(response)
                setupRecyclerView(postList)
                swipeRefreshLayout?.isRefreshing = false
            },Response.ErrorListener {
                Log.d(TAG, "Unable to get game posts")
                swipeRefreshLayout?.isRefreshing = false})

        //Add query to queue
        queue.add(request)
    }

    private fun parseResult(response: JSONObject): MutableList<Post?>{
        //get the "results" from the response object
        val array: JSONArray = response.getJSONArray("results")
        //init the adapter list
        val postList :MutableList<Post?> = mutableListOf()

        //parse the JSONArray
        for (post_idx in 0 until array.length()) {
            val post: JSONObject = array[post_idx] as JSONObject
            val name = post.get("name").toString()
            val id = post.getInt("id")
            val image = post.getString("image")
            val url = post.getString("url")
            val username = post.getString("username")
            val usernameUrl = post.getString("username_url")
            val text = stripHtml(post.getString("text"))
            val createdAt = post.getString("created")
            //populate the adapter list
            postList.add(Post(id, name, text!!, url, username, usernameUrl, createdAt, image))
        }
        return postList
    }

    private fun setupRecyclerView(postList: MutableList<Post?>) {
        //Setup the RecyclerView
        viewManager = LinearLayoutManager(activity?.applicationContext)
        viewAdapter = PostAdapter(postList)

        recyclerView = requireView().findViewById(R.id.feed_recycler_view)
        recyclerView.apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        scrollListener = object: EndlessRecyclerViewScrollListner(viewManager as LinearLayoutManager){
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                loadMoreSearch(page, totalItemsCount)
            }
        }

        recyclerView.addOnScrollListener(scrollListener as EndlessRecyclerViewScrollListner)
    }

    private fun loadMoreSearch(page :Int, totalItemsCount: Int){
        val q = "$query?page=$page"

        //Setup the request
        val queryRequest = JsonObjectRequest(Request.Method.GET, q, null, Response.Listener { response ->
            val postList = parseResult(response)
            (viewAdapter as PostAdapter).apply {
                this.postList.addAll(postList)
                notifyItemRangeInserted(totalItemsCount, postList.size)
            }

        }, Response.ErrorListener { Log.d(com.example.gamelibrary.search.TAG, "didn't work") })

        //Add query to queue
        queue.add(queryRequest)
    }

    //set refresh layout listener and color for progressbar
    private fun setRefreshListener(){
        swipeRefreshLayout?.apply {
            setOnRefreshListener {
                getPosts()
            }
            setColorSchemeResources(R.color.colorPrimary)
        }
    }

    fun stripHtml(html: String?): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            Html.escapeHtml(html).toString()
        }
    }
}