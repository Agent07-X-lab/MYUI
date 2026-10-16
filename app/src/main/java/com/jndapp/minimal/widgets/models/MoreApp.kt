package com.jndapp.minimal.widgets.models

import java.io.Serializable

data class MoreApp(
    val name: String,
    val description: String,
    val play_store_url: String,
    val images: List<String>
) : Serializable
