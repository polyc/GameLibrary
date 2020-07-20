package com.example.gamelibrary.search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.gamelibrary.R
import com.example.gamelibrary.data.Game
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.game_element.view.*

class SearchAdapter (val gameList: MutableList<Game?>,
                     private val db: FirebaseFirestore,
                     private val userId: String,
                     private val context: Context)
    :RecyclerView.Adapter<SearchViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val itemView: View = LayoutInflater.from(parent.context).inflate(R.layout.game_element, parent, false)
        return SearchViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val game: Game? = gameList[position]
        if(game != null){
            //set game title
            holder.itemView.name.text = game.name

            //set metacritic rating
            if(game.metacriticRating != null)
                holder.itemView.metacritic.text = game.metacriticRating.toString()
            else
                holder.itemView.metacritic.text = "-"

            //set background image
            Picasso.get().load(game.backgroundImage).fit().into(holder.backgroundImage)

            //get userData Firestore document
            db.collection("userData").document(userId).get().addOnSuccessListener {
                //set the fab according to presence into library
                val userData = it
                holder.addRemove.apply {
                    val library = userData.get("library") as Map<String, String>

                    if (library.containsKey(game.id.toString()))
                        setImageResource(R.drawable.ic_baseline_remove_24)
                    else
                        setImageResource(R.drawable.ic_baseline_add_24)
                }
            }.addOnFailureListener{}


            // Add/Remove a Game from library
            holder.addRemove.setOnClickListener { view ->
                view.findViewById<FloatingActionButton>(R.id.addRemove).apply {
                    db.collection("userData").document(userId).get().addOnSuccessListener {userData ->

                        val library = userData.get("library") as Map<String, String>

                        //remove game
                        if(library.containsKey(game.id.toString())){
                            db.collection("userData").document(userId).update(hashMapOf<String,Any>(
                                "library."+game.id.toString() to FieldValue.delete()
                            ))
                            setImageResource(R.drawable.ic_baseline_add_24)
                            Toast.makeText(context, "Game removed from library", Toast.LENGTH_SHORT).show()
                        }
                        else{//add game
                            db.collection("userData").document(userId).update(mapOf(
                                "library."+game.id.toString() to game.name
                            ))
                            setImageResource(R.drawable.ic_baseline_remove_24)
                            Toast.makeText(context, "Game added to library", Toast.LENGTH_SHORT).show()

                        }

                    }.addOnFailureListener{
                        Toast.makeText(context, "Task Failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }

    override fun getItemCount() = gameList.size

}