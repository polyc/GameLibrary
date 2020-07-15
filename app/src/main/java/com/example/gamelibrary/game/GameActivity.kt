package com.example.gamelibrary.game

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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

        val gameID: String = intent.extras?.getString("gameID")!!
        val sharedPref = getSharedPreferences("Library", Context.MODE_PRIVATE)
        val gameJson = JSONObject(sharedPref.getString(gameID, "Error")!!)

        val id = gameJson.getInt("id")
        val name = gameJson.getString("name")
        supportActionBar?.title = name

        val backgroundImage = gameJson.getString("background_image")

        var metacriticRating: Int? = null
        if(!gameJson.isNull("metacritic"))
            metacriticRating = gameJson.getInt("metacritic")

        val description = gameJson.getString("description_raw")
        val website = gameJson.getString("website")
        val releaseDate = gameJson.getString("released")
        var averagePlaytime: Int? = null

        if(!gameJson.isNull("playtime"))
            averagePlaytime = gameJson.getInt("playtime")

        val developers = gameJson.getJSONArray("developers")
        val developer = developers[0] as JSONObject
        val devName = developer.getString("name")

        val publishers = gameJson.getJSONArray("publishers")
        val publisher = publishers[publishers.length()-1] as JSONObject
        val pubName = publisher.getString("name")

        //ADD OTHER FIELDS TO OBJECT
        val game = Game(name, id, backgroundImage, metacriticRating, description,
            website, releaseDate, averagePlaytime,devName, pubName)


        val tabLayout :TabLayout = findViewById(R.id.tab_layout)
        tabLayout.addTab(tabLayout.newTab().setText("Details"))
        tabLayout.addTab(tabLayout.newTab().setText("Reddit Feed"))
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL

        //Log.d(TAG, "${game.toString()}")

        val numOfTabs = 2
        val viewPager = findViewById<ViewPager2>(R.id.pager)
        val pageAdapter: GameTabAdapter = GameTabAdapter(supportFragmentManager, lifecycle, numOfTabs, game)
        viewPager.adapter = pageAdapter



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
}