package com.example.gamelibrary.search

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gamelibrary.R
import com.example.gamelibrary.data.Game
import com.google.android.gms.tasks.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.game_element.view.*

class GameAdapter (private val gameList: MutableList<Game?>, private val db: FirebaseFirestore, private val userId: String)
    :RecyclerView.Adapter<GameViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val itemView: View = LayoutInflater.from(parent.context).inflate(R.layout.game_element, parent, false)
        return GameViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
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
                        setImageResource(R.drawable.ic_baseline_check_24)
                    else
                        setImageResource(R.drawable.ic_baseline_add_24)
                }
            }.addOnFailureListener{}


            // Add/Remove a Game from library
            holder.addRemove.setOnClickListener { view ->
                view.findViewById<FloatingActionButton>(R.id.addRemove).apply {
                    db.collection("userData").document(userId).get().addOnSuccessListener {userData ->

                        val library = userData.get("library") as Map<String, String>
                        if(library.containsKey(game.id.toString())){
                            db.collection("userData").document(userId).update(hashMapOf<String,Any>(
                                "library."+game.id.toString() to FieldValue.delete()
                            ))
                            setImageResource(R.drawable.ic_baseline_add_24)
                        }
                        else{
                            db.collection("userData").document(userId).update(mapOf(
                                "library."+game.id.toString() to game.name
                            ))
                            setImageResource(R.drawable.ic_baseline_check_24)
                        }

                    }.addOnFailureListener{

                    }
                }
            }
        }

    }

    override fun getItemCount() = gameList.size

}