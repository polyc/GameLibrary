package com.example.gamelibrary.library

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

const val url = "https://api.rawg.io/api/"

const val TAGLIB = "GameLibrary"

class LibraryAdapter(private val gameList: MutableList<String>,
                     private val db: FirebaseFirestore,
                     private val userId: String,
                     private val queue: RequestQueue,
                     private val context: Activity):
    RecyclerView.Adapter<LibraryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibraryViewHolder {
        val itemView: View = LayoutInflater.from(parent.context).inflate(R.layout.library_element, parent, false)
        return LibraryViewHolder(itemView)
    }

    override fun getItemCount()= gameList.size

    override fun onBindViewHolder(holder: LibraryViewHolder, position: Int) {
        val pref = context.getSharedPreferences("Library", MODE_PRIVATE)

        if(!pref.contains(gameList[position])){
            val request = JsonObjectRequest(Request.Method.GET, getQuery(gameList[position]), null,
                Response.Listener { response ->
                    val gameJson: JSONObject = response
                    pref.edit().putString(gameList[position], gameJson.toString()).apply()
                    setHolder(holder, gameJson, position)

                },Response.ErrorListener { Log.d(TAGLIB, "Unable to get game with gameId:${gameList[position]}") })

            queue.add(request)
        }
        else{
            setHolder(holder, JSONObject(pref.getString(gameList[position], "Item not Found")!!), position)
        }

    }

    private fun setHolder(holder: LibraryViewHolder, game: JSONObject, position: Int){
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

        holder.remove.setOnClickListener {
            db.collection("userData").document(userId).update(hashMapOf<String,Any>(
                "library."+game.get("id").toString() to FieldValue.delete()
            ))
            gameList.remove(game.get("id").toString())
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, itemCount);
        }

        holder.details.setOnClickListener{
            val intent = Intent(context, GameActivity::class.java)
            intent.putExtra("gameID", gameList[position])
            context.startActivity(intent)
        }
    }

    private
    fun getQuery(gameId: String) = url + "games/${gameId}"
}