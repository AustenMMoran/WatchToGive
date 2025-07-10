package com.ap.watchtogive.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Charity(
    val id : String = "0",
    val income : Int = 0,
    val name : String = ""
)
