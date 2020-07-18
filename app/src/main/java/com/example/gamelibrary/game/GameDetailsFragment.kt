package com.example.gamelibrary.game

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.gamelibrary.R
import com.example.gamelibrary.data.Game
import com.squareup.picasso.Picasso


class GameDetailsFragment(private val game: Game): Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.game_details_fragment, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //Set game image
        view.findViewById<ImageView>(R.id.background_image_details).apply {
            val backgroundImage = game.backgroundImage
            if (backgroundImage != null)
                Picasso.get().load(backgroundImage).fit().noFade().into(this)
        }

        //Set release date
        view.findViewById<TextView>(R.id.release_date).apply {
            val release = game.releaseDate
            if (release != "null")
                text = release
        }

        //set playtime
        view.findViewById<TextView>(R.id.avg_playtime).apply {
            text = game.averagePlaytime.toString()+ " Hours"
        }

        //set developer
        view.findViewById<TextView>(R.id.developer).apply {
            val developer = game.developer
            if (developer != null)
                text = game.developer
        }

        //set publisher
        view.findViewById<TextView>(R.id.publisher).apply {
            val publisher = game.publisher
            if (publisher != null)
                text = game.publisher
        }

        //set website
        view.findViewById<TextView>(R.id.website).apply {
            val website = game.website
            if (website!!.isNotEmpty()) {
                text = website
                setTextColor(resources.getColor(android.R.color.holo_blue_bright))
                paintFlags = Paint.UNDERLINE_TEXT_FLAG
                isClickable = true
                setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(text.toString()))
                    if (intent.resolveActivity(context.packageManager) != null) {
                        startActivity(intent)
                    }
                }
            }
        }

        //set description
        view.findViewById<WebView>(R.id.game_description).apply {
            setOnTouchListener { _, motionEvent ->
                (motionEvent.action == MotionEvent.ACTION_MOVE)
            }
            settings.defaultTextEncodingName = "utf-8";
            loadData(game.description!!, "text/html; charset=utf-8", "utf-8")
        }
    }
}