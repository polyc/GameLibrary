package com.example.gamelibrary.data

class Game(val name :String, val backgroundImage :String?, val metacriticRating :Int?){
    override fun toString(): String {
        return name + " " + metacriticRating.toString()
    }
}