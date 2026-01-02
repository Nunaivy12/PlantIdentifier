package com.wvxid.planidentifier

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Message(
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    @ServerTimestamp val timestamp: Date? = null,
    val participants: List<String> = listOf()
)