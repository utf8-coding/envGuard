package com.utf8coding.envGuard.data

import android.util.Log

data class BinData(
    /**
     * 垃圾桶编号
     */
    val id: Long,

    /**
     * 纬度位置
     */
    val latitude: String,

    /**
     * 垃圾量，已有垃圾占比，默认为0
     */
    val level: Double ,

    /**
     * 经度位置
     */
    val longitude: String,

    /**
     * 垃圾桶位置文字描述
     */
    val otherThing: String,

    /**
     * 垃圾桶状态(0为异常，1为正常，默认为1)
     */
    val state: Long
) {
    override fun toString(): String {
        return "id: $id, latitude纬度: $latitude, longitude经度: $longitude, level: $level, state: $state}"
    }
}