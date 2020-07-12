package com.example.gamelibrary.data

import android.os.Parcel
import android.os.Parcelable

data class Game(val name :String,
                val id :Int,
                val backgroundImage :String? = null,
                val metacriticRating :Int? = null,
                val description: String? = null,
                val website: String? = null,
                val releaseDate: String? = null,
                val averagePlaytime: Int? = null,
                val dominantColor: String? = null
):Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString(),
        parcel?.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel?.readInt(),
        parcel.readString()
    )

    override fun writeToParcel(p0: Parcel?, p1: Int) {
        p0?.writeString(name)
        p0?.writeInt(id)
        p0?.writeString(backgroundImage)
        if (metacriticRating != null) {
            p0?.writeInt(metacriticRating)
        }
        p0?.writeString(description)
        p0?.writeString(website)
        p0?.writeString(releaseDate)
        if (averagePlaytime != null) {
            p0?.writeInt(averagePlaytime)
        }
        p0?.writeString(dominantColor)

    }

    override fun describeContents(): Int {
        return hashCode()
    }

    companion object CREATOR : Parcelable.Creator<Game> {
        override fun createFromParcel(parcel: Parcel): Game {
            return Game(parcel)
        }

        override fun newArray(size: Int): Array<Game?> {
            return arrayOfNulls(size)
        }
    }
}