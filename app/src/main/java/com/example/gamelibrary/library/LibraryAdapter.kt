package com.example.gamelibrary.library

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.example.gamelibrary.R
import com.example.gamelibrary.game.GameActivity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.game_element.view.*
import org.json.JSONObject
import java.util.*

const val url = "https://api.rawg.io/api/"

private const val TAG = "GameLibrary"

class LibraryAdapter(private val gameListFull: MutableList<String>,
                     private val gameNamesFull: MutableList<String>,
                     private val db: FirebaseFirestore,
                     private val userId: String,
                     private val queue: RequestQueue,
                     private val context: Activity):
    RecyclerView.Adapter<LibraryViewHolder>(), Filterable {

    var gameList:MutableList<String> = mutableListOf()
    var gameNames:MutableList<String> = mutableListOf()
    var forceRefresh: Boolean = false

    init {
        gameList.addAll(gameListFull)
        gameNames.addAll(gameNamesFull)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibraryViewHolder {
        val itemView: View = LayoutInflater.from(parent.context).inflate(R.layout.library_element, parent, false)
        return LibraryViewHolder(itemView)
    }

    override fun getItemCount()= gameList.size

    override fun onBindViewHolder(holder: LibraryViewHolder, position: Int) {
        val pref = context.getSharedPreferences("Library", MODE_PRIVATE)

        val gameId = gameList[position]

        if(forceRefresh || !pref.contains(gameId)){
            //get data from API
            val request = JsonObjectRequest(Request.Method.GET, getQuery(gameId), null,
                Response.Listener { response ->
                    val gameJson: JSONObject = response
                    val gameString = gameJson.toString()

                    //Put the Json String in SharedPreferences (cache API result)
                    pref.edit().putString(gameId, gameString).apply()

                    //setup the ViewHolder
                    setHolder(holder, gameJson, position, pref)

                },Response.ErrorListener {
                    Log.d(TAG, "Unable to get game with gameId:${gameId}, trying with cache")
                    //setup ViewHolder if there's data in cache
                    if(pref.contains(gameId))
                        setHolder(holder, JSONObject(pref.getString(gameId, "")!!), position, pref)
                })
            queue.add(request)
            forceRefresh = false
        }
        else{
            //setup ViewHolder if there's data in cache
            if(pref.contains(gameId))
                setHolder(holder, JSONObject(pref.getString(gameId, "")!!), position, pref)
        }
    }

    private fun setHolder(holder: LibraryViewHolder, game: JSONObject, position: Int, pref: SharedPreferences){
        //set game title
        holder.itemView.name.text = game.getString("name")

        //set metacritic rating
        if(!game.isNull("metacritic"))
            holder.itemView.metacritic.text = game.getInt("metacritic").toString()
        else
            holder.itemView.metacritic.text = "-"

        //set background image
        if(!game.isNull("background_image"))
            Picasso.get().load(game.getString("background_image")).fit().into(holder.backgroundImage)

        //setup the remove FAB behavior
        holder.remove.setOnClickListener {
            //delete entry in Firebase
            db.collection("userData").document(userId).update(hashMapOf<String,Any>(
                "library."+game.get("id").toString() to FieldValue.delete()
            ))
            //delete entry in adapter data list and name list
            val id = game.getString("id")
            pref.edit().remove(gameList[position]).apply()
            gameList.remove(id)
            gameListFull.remove(id)
            val name = game.getString("name")
            gameNames.remove(name)
            gameNamesFull.remove(name)

            //notify the adapter
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, itemCount);

        }

        //setup the details FAB behavior
        holder.details.setOnClickListener{
            //start GameActivity
            val intent = Intent(context, GameActivity::class.java)
            intent.putExtra("gameID", gameList[position])
            context.startActivity(intent)
        }
    }

    private fun getQuery(gameId: String) = url + "games/${gameId}"

    override fun getFilter(): Filter {
        //return filter by name for now
        return filter
    }

    private val filter:Filter = object :Filter(){
        private var lastConstraintLen = 0

        override fun performFiltering(p0: CharSequence?): FilterResults {
            val results = FilterResults()

            if (p0 == null || p0.isEmpty()){//filtering bar is empty
                val filteredIdList = mutableListOf<String>()
                val gameNameFiltered = mutableListOf<String>()
                filteredIdList.addAll(gameListFull)
                gameNameFiltered.addAll(gameNamesFull)
                val result = Pair(filteredIdList, gameNameFiltered)
                lastConstraintLen = 0
                results.values = result
                return results
            }
            else if (lastConstraintLen > p0.length){//typing backward
                val filterPattern = p0.toString().toLowerCase(Locale.ROOT).trim()
                val result = filterListsByName(Pair(gameListFull, gameNamesFull), filterPattern)
                lastConstraintLen = p0.length
                results.values = result
            }
            else{//typing forward
                val filterPattern = p0.toString().toLowerCase(Locale.ROOT).trim()
                val result = filterListsByName(Pair(gameList, gameNames), filterPattern)
                lastConstraintLen = p0.length
                results.values = result
            }
            return results
        }

        override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
            val result = p1?.values as Pair<*, *>
            gameList.clear()
            gameList.addAll(result.first as MutableList<String>)
            gameNames.clear()
            gameNames.addAll(result.second as MutableList<String>)
            notifyDataSetChanged()
        }

        private fun filterListsByName(lists: Pair<MutableList<String>, MutableList<String>>, filterPattern: String)
                :Pair<MutableList<String>, MutableList<String>>{

            val filteredIdList = mutableListOf<String>()
            val gameNameFiltered = mutableListOf<String>()
            for (game_idx in 0 until  lists.first.size){
                val gameId = lists.first.elementAt(game_idx)
                val gameName = lists.second.elementAt(game_idx)
                if (gameName.toLowerCase(Locale.ROOT).contains(filterPattern)) {
                    filteredIdList.add(gameId)
                    gameNameFiltered.add(gameName)
                }
            }
            return Pair(filteredIdList, gameNameFiltered)
        }

    }
}