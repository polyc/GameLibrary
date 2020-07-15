package com.example.gamelibrary.game

import android.annotation.SuppressLint
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

        view.findViewById<ImageView>(R.id.background_image_details).apply {
            Picasso.get().load(game.backgroundImage).fit().noFade().into(this)
        }

        view.findViewById<TextView>(R.id.release_date).apply {
            text = game.releaseDate
        }

        view.findViewById<TextView>(R.id.avg_playtime).apply {
            text = game.averagePlaytime.toString()+ " Hours"
        }

        view.findViewById<TextView>(R.id.developer).apply {
            text = game.developer
        }

        view.findViewById<TextView>(R.id.publisher).apply {
            text = game.publisher
        }


        view.findViewById<WebView>(R.id.game_description).apply {
            setOnTouchListener { _, motionEvent ->
                (motionEvent.action == MotionEvent.ACTION_MOVE)
            }
            settings.defaultTextEncodingName = "utf-8";
            loadData(game.description!!, "text/html; charset=utf-8", "utf-8")
        }
    }
}