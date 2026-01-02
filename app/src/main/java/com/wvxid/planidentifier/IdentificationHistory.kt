package com.wvxid.planidentifier

data class IdentificationHistory(
    val id: Int? = null,
    val userId: Int,
    val plantId: Long, // Added to store the ID of the identified plant
    val imageUri: String,
    val timestamp: Long
)