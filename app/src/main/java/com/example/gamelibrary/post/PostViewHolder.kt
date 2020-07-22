package com.example.gamelibrary.post

import android.view.View
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.alespero.expandablecardview.ExpandableCardView
import com.example.gamelibrary.R

class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)  {
    var card :CardView = itemView.findViewById(R.id.post_card_view)
    var name :TextView = itemView.findViewById(R.id.post_name)
    var author :TextView = itemView.findViewById(R.id.post_author)
    var createdAt :TextView = itemView.findViewById(R.id.post_created_at)
    var postImage : ImageView = itemView.findViewById(R.id.post_image)
    var readMore :ExpandableCardView = itemView.findViewById(R.id.read_more_card)
    var content :WebView = itemView.findViewById(R.id.post_text)
    var goReddit :TextView = itemView.findViewById(R.id.post_go_reddit)

}