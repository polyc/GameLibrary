package com.example.gamelibrary.game

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.webkit.WebView
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.gamelibrary.R
import com.example.gamelibrary.data.Game
import com.example.gamelibrary.search.SearchActivity
import com.squareup.picasso.Picasso
import java.util.*

private const val TAG = "DetailsFragment"
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
        retainInstance = true

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
            }
        }

        //set genres
        view.findViewById<RelativeLayout>(R.id.genres_container).apply {
            val genres = game.genres
            var lastViewId:Int = 0
            if (genres != null && genres.isNotEmpty()) {
                for (genre in genres){
                    val textView = TextView(context)
                    textView.text = genre
                    textView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                    textView.background = resources.getDrawable(R.drawable.rounded_corner, null)
                    addView(textView)
                    Log.d(TAG, textView.id.toString())
                    val lp = textView.layoutParams as RelativeLayout.LayoutParams
                    lp.apply {
                        if (lastViewId != 0){
                            addRule(RelativeLayout.BELOW, lastViewId)
                        }
                        gravity = Gravity.CENTER_HORIZONTAL
                        bottomMargin = 30
                    }
                    lastViewId +=1
                    textView.id = lastViewId
                    textView.layoutParams = lp
                    textView.setOnClickListener {
                        val intent = Intent(context, SearchActivity::class.java)
                        intent.putExtra("queryFilter", "genres=${genre.toLowerCase(Locale.ROOT)}")
                        startActivity(intent)
                    }
                }
            }
        }

        //set tags
        view.findViewById<RelativeLayout>(R.id.tags_container).apply {
            val tags = game.tags
            var lastViewId:Int = 0
            if (tags != null && tags.isNotEmpty()) {
                for (tag in tags){
                    val textView = TextView(context)
                    textView.text = tag
                    textView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                    textView.background = resources.getDrawable(R.drawable.rounded_corner, null)
                    addView(textView)
                    val lp = textView.layoutParams as RelativeLayout.LayoutParams
                    lp.apply {
                        if (lastViewId != 0){
                            addRule(RelativeLayout.BELOW, lastViewId)
                        }
                        gravity = Gravity.CENTER_HORIZONTAL
                        bottomMargin = 30
                    }
                    lastViewId +=1
                    textView.id = lastViewId
                    textView.layoutParams = lp
                    textView.setOnClickListener {
                        val intent = Intent(context, SearchActivity::class.java)
                        intent.putExtra("queryFilter", "tags=${tag.toLowerCase(Locale.ROOT)}")
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