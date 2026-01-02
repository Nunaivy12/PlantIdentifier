package com.wvxid.planidentifier

import java.util.Date

data class Chat(
    val otherUserId: String = "",
    var otherUserName: String = "", // Will be populated later
    val lastMessage: String = "",
    val timestamp: Date? = null
)