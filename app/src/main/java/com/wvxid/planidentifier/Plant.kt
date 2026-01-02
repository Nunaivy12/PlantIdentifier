package com.wvxid.planidentifier

import android.os.Parcelable
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@Parcelize
@IgnoreExtraProperties
data class Plant(
    var id: String = "",
    val commonName: String = "",
    val family: String = "",
    val genus: String = "",
    val scientificName: String = "",
    val canFound: String = "",
    val description: String = "",
    val category: String = "",
    val imageName: String = "",
    val healthStatus: String? = null,
    val suggestion: String? = null
) : Parcelable