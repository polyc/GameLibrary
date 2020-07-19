package com.example.gamelibrary.game

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.gamelibrary.data.Game

class GameTabAdapter(fragmentManager: FragmentManager,
                     lifecycle: Lifecycle,
                     private val numOfTabs: Int,
                     private val game: Game,
                     private val feedFirst: Boolean)
    :FragmentStateAdapter(fragmentManager, lifecycle){

    override fun getItemCount() = numOfTabs

    override fun createFragment(position: Int): Fragment {

        var first :Fragment = GameDetailsFragment(game)
        var second :Fragment = GameFeedFragment(game)

        if (feedFirst){
            first = GameFeedFragment(game)
            second = GameDetailsFragment(game)
        }

        return when (position) {
            0 -> first
            else -> second
        }
    }
}