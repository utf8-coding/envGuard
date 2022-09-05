package com.utf8coding.envGuardAdmin.data

import com.google.gson.annotations.SerializedName

data class UserData(val id: Int, val name: String, @SerializedName("photo") val headUri: String)
