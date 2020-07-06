package com.example.gamelibrary.search

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gamelibrary.R

class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val title : TextView = itemView.findViewById(R.id.name)
    val metacritic : TextView = itemView.findViewById(R.id.metacritic)
}