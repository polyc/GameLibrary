package com.example.gamelibrary.search

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.gamelibrary.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val card : CardView = itemView.findViewById(R.id.card_view)
    val title : TextView = itemView.findViewById(R.id.name)
    val metacritic : TextView = itemView.findViewById(R.id.metacritic)
    val backgroundImage :ImageView = itemView.findViewById(R.id.background_image)
    val addRemove :FloatingActionButton = itemView.findViewById(R.id.addRemove)
}