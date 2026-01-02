package com.wvxid.planidentifier

import com.google.firebase.firestore.PropertyName

data class Post(
    var id: String = "",
    @get:PropertyName("userId") @set:PropertyName("userId") var userId: String = "",
    var content: String = "",
    @get:PropertyName("imageUrl") @set:PropertyName("imageUrl") var imageUri: String? = null,
    var timestamp: Long = 0
)
