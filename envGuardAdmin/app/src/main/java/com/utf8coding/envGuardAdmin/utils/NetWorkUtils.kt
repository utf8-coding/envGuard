package com.utf8coding.envGuardAdmin.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import com.utf8coding.envGuardAdmin.MyApplication
import com.utf8coding.envGuardAdmin.data.BinData
import com.utf8coding.envGuardAdmin.data.UserData
import com.utf8coding.envGuardAdmin.network.NetWorkResponse
import com.utf8coding.envGuardAdmin.network.NetworkService
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import java.util.concurrent.TimeUnit

object NetWorkUtils {

    fun isNetWorkAvailable(): Boolean {
        val runtime = Runtime.getRuntime()
        try {
            val pingProcess = runtime.exec("/system/bin/ping -c 1 www.baidu.com")
            val exitCode = pingProcess.waitFor()
            return exitCode == 0
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun login(userName: String, password: String, onResponse: (loginSuccess: Boolean, code: Int) -> Unit, onFailure: (t: Throwable) -> Unit){
        enqueueAction(
            action = {
                getGeneralAppService().login(userName, password)
            },
            onFailure = { call, t ->
                onFailure(t)
            },
            onResponse = { call, rsp ->
                    if (rsp.body() != null){
                        when (rsp.body()!!.code) {
                            200 -> {
                                onResponse(true, NetWorkResponse.SUCCESS)
                            }
                            2003 -> {
                                onResponse(false, NetWorkResponse.WRONG_PASS_OR_NO_USER)
                            }
                            //理论不会
                            2007 -> {
                                Log.e("NetWorkUtils:", "empty user name! not possible?")
                            }
                        }
                    } else {
                        //空body(理论不会发生)
                        onResponse(false, NetWorkResponse.EMPTY_BODY)
                        Log.e("NetWorkUtils: ", "EMPTY RESPONSE BODY when logging in, check server, rsp code: ${rsp.code()}")
                    }
            }
        )
    }

    fun getAllBinData(onResponse: (isSuccess: Boolean, dataList: ArrayList<BinData>) -> Unit, onFailure: (t: Throwable) -> Unit){
        enqueueAction(
            action = {
                getGeneralAppService().getAllBin()
            },
            onResponse = { _, rsp ->
                if (rsp.body() != null){
                    onResponse(true, rsp.body()!!.data.list)
                } else {
                    //空body(理论不会发生)
                    onResponse(false, arrayListOf())
                    Log.e("NetWorkUtils: ", "EMPTY RESPONSE BODY when getting bin data, check server, rsp code: ${rsp.code()}")
                }
            },
            onFailure = { _, t ->
                onFailure(t)
                Log.e("NetWorkUtils: ", "get bin data onFailure: $t")
            }
        )
    }

    fun sendMail(mailAddress: String, onSuccess: () -> Unit, onFailure: (t: Throwable) -> Unit){
        enqueueAction(
            action = {
                getGeneralAppService(15000).sendMail(mailAddress)
            },
            onResponse = { _, rsp ->
                if (rsp.body() != null){
                    if (rsp.body()!!.success){
                        onSuccess()
                    }
                } else {
                    Log.e("NetWorkUtils: ", "EMPTY RESPONSE BODY when sending mail, check server, rsp code: ${rsp.code()}")
                }
            },
            onFailure = { _, t ->
                onFailure(t)
            }
        )
    }

    fun commitBinData(
        id: Int,
        longitude: Double,
        latitude: Double,
        status: Int,
        level: Float,
        description: String,
        onSuccess: () -> Unit,
        onFailure: (t: Throwable) -> Unit
    ){
        enqueueAction(
            action = {
                getGeneralAppService().commitBinData(id, longitude, latitude, status, level, description)
            },
            onResponse = { _, rsp ->
                if (rsp.body() != null){
                    if (rsp.body()!!.success){
                        onSuccess()
                    }
                } else {
                    Log.e("NetWorkUtils: ", "EMPTY RESPONSE BODY when committing bin data, check server, rsp code: ${rsp.code()}")
                }
            },
            onFailure = { _, t ->
                onFailure(t)
                Log.e("NetWorkUtils: ", "ON_FAILURE when committing bin data, check server, error stack: $t")
            }
        )
    }

    fun addBin(long: Double, lati: Double,
               onSuccess: () -> Unit,
               onFailure: (t: Throwable) -> Unit){
        enqueueAction(
            action = {
                getGeneralAppService().addBin(long, lati)
            },
            onResponse = { _, rsp ->
                if (rsp.body() != null){
                    if (rsp.body()!!.success){
                        onSuccess()
                    }
                } else {
                    Log.e("NetWorkUtils: ", "EMPTY RESPONSE BODY when adding bin data, check server, rsp code: ${rsp.code()}")
                }
            },
            onFailure = { _, t ->
                onFailure(t)
                Log.e("NetWorkUtils: ", "ON_FAILURE when adding bin data, check server, error stack: $t")
            }
        )
    }

    //new okhttp client that able to hold a token:
    private fun generateClient(timeOut: Long): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(timeOut, TimeUnit.MILLISECONDS)
            .readTimeout(timeOut, TimeUnit.MILLISECONDS)
            .addInterceptor { chain ->
                var cookie = MyApplication.context.getSharedPreferences("network", MODE_PRIVATE).getString("cookie", "")
                if (cookie == null){
                    cookie = ""
                    Log.e("NetworkUtils:", "null cookie content!!")
                }
                val request: Request = chain.request()
                    .newBuilder()
                    .addHeader("Cookie", cookie)
                    .build()
                chain.proceed(request)
            }.build()
    }

    private fun getGeneralAppService(timeOut: Long = 1000): NetworkService {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://47.107.225.197:8098/")
            .client(generateClient(timeOut))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(NetworkService::class.java)
    }

    private fun <T> enqueueAction(action: () -> Call<T>, onResponse: (call: Call<T>, response: Response<T>) -> Unit, onFailure: (call: Call<T>, t: Throwable) -> Unit){
        action().enqueue(object: Callback<T> {
            override fun onResponse(
                call: Call<T>,
                response: Response<T>
            ) {
                //所有的请求都读一个cookie存起来，保活
                val rsp = response
                if (rsp.headers().get("Set-Cookie") != null){
                    val cookie = rsp.headers().get("Set-Cookie")
                    MyApplication.context.getSharedPreferences("network", MODE_PRIVATE).edit()
                        .putString("cookie", cookie)
                        .apply()
                    Log.i("NetworkUtils:", "cookie saved: $cookie")
                }
                //执行内容：
                onResponse(call, rsp)
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                onFailure(call, t)
            }

        })
    }

}