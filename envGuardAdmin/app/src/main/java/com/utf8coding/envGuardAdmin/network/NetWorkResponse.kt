package com.utf8coding.envGuardAdmin.network

import com.google.gson.annotations.SerializedName
import com.utf8coding.envGuardAdmin.data.BinData

class NetWorkResponse<T>(val success: Boolean, val code: Int, val message: String, val data: T){
    companion object {
        const val SUCCESS = 1
        const val EMPTY_BODY = 2
        const val WRONG_PASS_OR_NO_USER = 3
        const val NO_CONNECTION = 4
    }
    class BinDataList(@SerializedName("Bin") val list: ArrayList<BinData>)
}