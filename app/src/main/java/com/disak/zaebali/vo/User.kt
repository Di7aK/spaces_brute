package com.disak.zaebali.vo

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("owner_name") val ownerName: String?
)