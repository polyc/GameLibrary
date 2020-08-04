package com.example.gamelibrary.search

import android.content.SearchRecentSuggestionsProvider

class SearchGamesRecentSuggestionsProvider : SearchRecentSuggestionsProvider() {
    init {
        setupSuggestions(AUTHORITY, MODE)
    }

    companion object {
        const val AUTHORITY = "com.example.gamelibrary.search.SearchGamesRecentSuggestionsProvider"
        const val MODE: Int = SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES
    }
}
