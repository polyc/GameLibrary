package com.example.gamelibrary.game

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.example.gamelibrary.R
import com.example.gamelibrary.data.Game
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import org.json.JSONObject


const val TAG = "Library"

class GameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        setSupportActionBar(findViewById(R.id.library_toolbar))

        //retrieve Json String From SharedPreferences
        val gameID: String = intent.extras?.getString("gameID")!!
        val sharedPref = getSharedPreferences("Library", Context.MODE_PRIVATE)
        val gameJson = JSONObject(sharedPref.getString(gameID, "Error")!!)

        //Setup appbar title with game name
        val name = gameJson.getString("name")
        supportActionBar?.title = name
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //Parse all other relevant fields
        val id = gameJson.getInt("id")
        val backgroundImage = gameJson.getString("background_image")

        var metacriticRating: Int? = null
        if(!gameJson.isNull("metacritic"))
            metacriticRating = gameJson.getInt("metacritic")

        val description = gameJson.getString("description")
        val website = gameJson.getString("website")
        val releaseDate = gameJson.getString("released")
        var averagePlaytime: Int? = null

        if(!gameJson.isNull("playtime"))
            averagePlaytime = gameJson.getInt("playtime")

        val developers = gameJson.getJSONArray("developers")
        var devName: String? = null
        if (developers.length() != 0) {
            val developer = developers[0] as JSONObject
            devName = developer.getString("name")
        }


        val publishers = gameJson.getJSONArray("publishers")
        var pubName: String? = null
        if (publishers.length() != 0){
            val publisher = publishers[publishers.length()-1] as JSONObject
            pubName = publisher.getString("name")
        }
        
        //instantiate the game data object
        val game = Game(name, id, backgroundImage, metacriticRating, description,
            website, releaseDate, averagePlaytime,devName, pubName)

        //Setup the TabLayout
        val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val feedFirst = pref.getBoolean("feed_first", false)
        val first = if (feedFirst) { "Reddit Feed"} else{"Details"}
        val second = if (feedFirst){ "Details"} else {"Reddit Feed"}

        val tabLayout :TabLayout = findViewById(R.id.tab_layout)
        tabLayout.addTab(tabLayout.newTab().setText(first))
        tabLayout.addTab(tabLayout.newTab().setText(second))
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL

        //Log.d(TAG, "${game.toString()}")

        //Setup the ViewPager
        val numOfTabs = 2
        val viewPager = findViewById<ViewPager2>(R.id.pager)
        val pageAdapter: GameTabAdapter = GameTabAdapter(supportFragmentManager, lifecycle, numOfTabs, game, feedFirst)
        viewPager.adapter = pageAdapter


        //Add the possibility to directly select the tab
        tabLayout.addOnTabSelectedListener(object :
            OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }

        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            finish()
            return true
        }
        else
            return super.onOptionsItemSelected(item)
    }
}