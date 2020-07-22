package com.example.gamelibrary.data

import java.text.SimpleDateFormat
import java.util.*

data class Post(val id: Int,
                val name :String,
                val text :String,
                val url :String,
                val username :String,
                val usernameUrl :String,
                val created: String?,
                val image: String) {

    val format: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ITALY)

    init {
        format.timeZone = TimeZone.getTimeZone("UTC")
    }
}