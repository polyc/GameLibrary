package com.example.gamelibrary.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gamelibrary.R
import com.example.gamelibrary.data.Game
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.game_element.view.*

class GameAdapter (private val gameList: MutableList<Game?>)
    :RecyclerView.Adapter<GameViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val itemView: View = LayoutInflater.from(parent.context).inflate(R.layout.game_element, parent, false)
        return GameViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game: Game? = gameList[position]
        if(game != null){
            holder.itemView.name.text = game.name

            if(game.metacriticRating != null)
                holder.itemView.metacritic.text = game.metacriticRating.toString()
            else
                holder.itemView.metacritic.text = "-"

            Picasso.get().load(game.backgroundImage).fit().into(holder.backgroundImage)
        }

    }

    override fun getItemCount() = gameList.size

}