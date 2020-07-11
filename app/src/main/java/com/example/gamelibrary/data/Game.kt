package com.example.gamelibrary.data

data class Game(val name :String,
                val id :Int,
                val backgroundImage :String? = null,
                val metacriticRating :Int? = null,
                val description: String? = null,
                val website: String? = null,
                val releaseDate: String? = null,
                val averagePlaytime: Int? = null,
                val dominantColor: String? = null
){
    override fun toString(): String {
        return name + " " + metacriticRating.toString()
    }
}