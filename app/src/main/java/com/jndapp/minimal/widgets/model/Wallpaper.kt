package com.jndapp.minimal.widgets.model

import java.io.Serializable

data class Wallpaper(
    val name: String,
    val author: String,
    val url: String,
    val thumbnail: String,
    val collections: String,
    val downloadable: Boolean = true,
    val size: Long = 0L,
    val dimensions: String = "",
    val copyright: String = ""
) : Serializable {

    fun getCollectionsList(): List<String> {
        return collections.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
}
