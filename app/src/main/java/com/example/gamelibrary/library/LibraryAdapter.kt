package com.example.gamelibrary.library

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.example.gamelibrary.R
import com.example.gamelibrary.data.Game
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.game_element.view.*
import org.json.JSONObject

const val url = "https://api.rawg.io/api/"

const val TAGLIB = "GameLibrary"

class LibraryAdapter(private val gameList: MutableList<String>,
                     private val db: FirebaseFirestore,
                     private val userId: String,
                     private val queue: RequestQueue):
    RecyclerView.Adapter<LibraryViewHolder>() {

    private var gameObjMap: MutableMap<String, Game> = mutableMapOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibraryViewHolder {
        val itemView: View = LayoutInflater.from(parent.context).inflate(R.layout.library_element, parent, false)
        return LibraryViewHolder(itemView)
    }

    override fun getItemCount()= gameList.size

    override fun onBindViewHolder(holder: LibraryViewHolder, position: Int) {
        if(!gameObjMap.containsKey(gameList[position])){
            val request = StringRequest(Request.Method.GET, getQuery(gameList[position]), Response.Listener { response ->
                val gameJson: JSONObject = JSONObject(response)
                val id = gameJson.get("id") as Int
                val name = gameJson.get("name") as String
                val backgroundImage = gameJson.get("background_image") as String

                var metacriticRating: Int? = null
                if(!gameJson.isNull("metacritic"))
                    metacriticRating = gameJson.get("metacritic") as Int

                //ADD OTHER FIELDS TO OBJECT
                val game = Game(name, id, backgroundImage, metacriticRating)
                gameObjMap[id.toString()] = game

                setHolder(holder, game, position)

            },Response.ErrorListener { Log.d(TAGLIB, "Unable to get game with gameId:${gameList[position]}") })

            queue.add(request)
        }
        else{
            setHolder(holder, gameObjMap[gameList[position]] ?: error("Item not found"), position)
        }

    }

    private fun setHolder(holder: LibraryViewHolder, game: Game, position: Int){

        //set game title
        holder.itemView.name.text = game.name

        //set metacritic rating
        if(game.metacriticRating != null)
            holder.itemView.metacritic.text = game.metacriticRating.toString()
        else
            holder.itemView.metacritic.text = "-"

        //set background image
        Picasso.get().load(game.backgroundImage).fit().into(holder.backgroundImage)

        holder.remove.setOnClickListener {
            db.collection("userData").document(userId).update(hashMapOf<String,Any>(
                "library."+game.id.toString() to FieldValue.delete()
            ))
            gameList.remove(game.id.toString())
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, itemCount);
        }
    }

    private
    fun getQuery(gameId: String) = url + "games/${gameId}"
}