package com.example.gamelibrary.data

data class UserData(
    var name: String?,
    var surname: String?,
    var email: String?,
    var library: Map<String, String>) {
}