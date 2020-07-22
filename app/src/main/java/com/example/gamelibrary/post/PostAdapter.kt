package com.example.gamelibrary.post

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.style.URLSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gamelibrary.R
import com.example.gamelibrary.data.Post
import com.example.gamelibrary.game.TAGFRAG
import com.squareup.picasso.Picasso

class PostAdapter(val postList: MutableList<Post?>) : RecyclerView.Adapter<PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val itemView: View = LayoutInflater.from(parent.context).inflate(R.layout.post_element, parent, false)
        return PostViewHolder(itemView)
    }

    override fun getItemCount() = postList.size

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        if(post != null){
            //set post title
            holder.name.text = post.name

            //set author nickname
            val author: SpannableString = SpannableString(post.username.subSequence(3, post.username.length))
            val authorUrlSpan = URLSpan(post.usernameUrl)
            author.setSpan(authorUrlSpan, 0, author.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            holder.author.apply {
                text = author
                setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.usernameUrl))
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    }
                }
            }

            //set post creation date
            val parsedDate = post.format.parse(post.created)
            parsedDate.toString().apply {
                holder.createdAt.text = "${subSequence(0, 16)}${subSequence(29, length)}"
            }

            //set post image
            val image = post.image
            Log.d(TAGFRAG, image)
            if(image != "null") {
                holder.postImage.apply {
                    visibility = View.VISIBLE
                }
                Picasso.get().load(post.image).fit().into(holder.postImage)
            }

            //set Reddit post link
            val goReddit: SpannableString = SpannableString("View on Reddit")
            val urlSpan = URLSpan(post.url)
            goReddit.setSpan(urlSpan, 8, 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            holder.goReddit.apply {
                text = goReddit
                setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.url))
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    }
                }
            }

            //set post content
            holder.content.apply {
                setOnTouchListener { _, motionEvent ->
                    (motionEvent.action == MotionEvent.ACTION_MOVE)
                }
                settings.defaultTextEncodingName = "utf-8";
                loadData(post.text, "text/html; charset=utf-8", "utf-8")
                //loadUrl(post.url)
            }
        }
    }
}