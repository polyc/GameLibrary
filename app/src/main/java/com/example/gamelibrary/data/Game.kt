package com.example.gamelibrary.data

data class Game(val name :String,
                val backgroundImage :String?,
                val metacriticRating :Int?,
                val id :Int){
    override fun toString(): String {
        return name + " " + metacriticRating.toString()
    }
}