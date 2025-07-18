package com.ap.watchtogive.model

data class UserData(
    val uid: String,
    val displayName: String? = null,
    val email: String? = null
)