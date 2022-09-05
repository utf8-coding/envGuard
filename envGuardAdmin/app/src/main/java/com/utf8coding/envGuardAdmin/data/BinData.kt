package com.utf8coding.envGuardAdmin.data

import com.google.gson.annotations.SerializedName

data class BinData(val id: Int, val latitude: Double, val longitude: Double, var level: Float, val state: Int, @SerializedName("otherThing") val description: String)
