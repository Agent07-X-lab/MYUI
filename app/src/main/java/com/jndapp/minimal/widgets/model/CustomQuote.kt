package com.jndapp.minimal.widgets.model

import java.io.Serializable

data class CustomQuote(
    val quote: String,
    val author: String,
    val id: Long = System.currentTimeMillis()
) : Serializable
