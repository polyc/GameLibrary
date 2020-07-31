package com.example.gamelibrary.data

data class Game(val name :String,
                val id :Int,
                val backgroundImage :String? = null,
                val metacriticRating :Int? = null,
                val description: String? = null,
                val website: String? = null,
                val releaseDate: String? = null,
                val averagePlaytime: Int? = null,
                val developer: String? = null,
                val publisher: String? = null,
                val genres: MutableList<String>? = null,
                val tags: MutableList<String>? = null
)