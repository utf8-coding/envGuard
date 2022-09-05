package com.utf8coding.envGuardAdmin.network

import com.utf8coding.envGuardAdmin.data.BinData
import com.utf8coding.envGuardAdmin.data.UserData
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import java.util.function.DoubleToLongFunction

interface NetworkService {

    @POST("login")
    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun login(@Field("username") userName: String, @Field("password") passWord: String): Call<NetWorkResponse<UserData>>

    @POST("/Bin/getAllBin")
    fun getAllBin(): Call<NetWorkResponse<NetWorkResponse.BinDataList>>

    @POST("/Bin/changeBinInf")
    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun commitBinData(
        @Field("id") id: Int,
        @Field("longitude") longitude: Double,
        @Field("latitude") latitude: Double,
        @Field("state") status: Int,
        @Field("level") level: Float,
        @Field("description") description: String
    ): Call<NetWorkResponse<Any>>

    @POST("mail")
    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun sendMail(@Field("email") mailAddress: String): Call<NetWorkResponse<Any>>

    @POST("/Bin/addNewBin")
    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun addBin(@Field("longitude") longitude: Double, @Field("latitude") latitude: Double): Call<NetWorkResponse<Any>>

//    @POST("login")
//    @FormUrlEncoded
//    @Headers("Content-Type: application/x-www-form-urlencoded")
//    fun login(@Field("username") userName: String, @Field("password") passWord: String): Call<NetWorkResponse<UserData>>

}