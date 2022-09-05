package com.utf8coding.envGuard.network

import android.content.Context
import android.util.Log
import com.utf8coding.envGuard.MyApplication
import com.utf8coding.envGuard.data.*
import com.utf8coding.envGuard.utils.GenerateTestContentUtils
import com.utf8coding.healthcare.data.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object NetworkUtils {

    fun getNearestBinList(
        latitude: String, longitude: String,
        onSuccess: (binList: ArrayList<BinData>) -> Unit,
        onFailure: (error: String) -> Unit
    ) {
        enqueueAction(
            action = { getGeneralAppService().getNearestBin(latitude, longitude)},
            onResponse = { _, response ->
                val body = response.body()
                if (body == null){
                    onFailure("null body!")
                    //todo: 测试用
                    onSuccess(GenerateTestContentUtils.generateBinList())
                    Log.i("NetworkUtils:", "binList null body!")
                } else {
                    onSuccess(body.data.binList)
                }
            },
            onFailure = { _, t ->

                Log.i("NetworkUtils:", "binList unFailure!")
                //todo: 测试用
                onSuccess(GenerateTestContentUtils.generateBinList())

//                onFailure(t.toString())
                t.printStackTrace()
            }
        )
    }

    //获取文章的一系列
    interface GetArticleByIdListener{
        fun onSuccess(articleData: ArticleData)
        fun onFail()
    }
    fun getArticleById(essayId: Int, listener: GetArticleByIdListener){
        if (essayId.toString() != ""){
            val retrofit = Retrofit.Builder()
                .baseUrl("http://47.107.225.197:8098/")
                .client(generateClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val appService = retrofit.create(NetworkService::class.java)
            appService.getArticleById(essayId).enqueue(object : Callback<NetWorkResponse<NetWorkResponse.Essay>> {
                override fun onResponse(call: Call<NetWorkResponse<NetWorkResponse.Essay>>,
                                        response: Response<NetWorkResponse<NetWorkResponse.Essay>>
                ) {
                    val articleData = response.body()?.data
                    if (articleData == null){
                        makeWLog("null article body?")
                    } else {
                        listener.onSuccess(articleData.essay)
                        makeILog("get articleData success")
                    }
                }

                override fun onFailure(
                    call: Call<NetWorkResponse<NetWorkResponse.Essay>>,
                    t: Throwable
                ) {
                    listener.onFail()
                    t.printStackTrace()
                    makeWLog("article list getting failed!!")
                }
            })
        } else {
            makeWLog("id is empty or null when get articles")
        }
    }

    fun getSuggestedArticle(userId: Int,
                            onSuccess: (mArticleDataList: ArrayList<ArticleData>) -> Unit,
                            onFailure: () -> Unit
                            ){
        getGeneralAppService().getSuggestedArticle(userId).enqueue(object : Callback<NetWorkResponse<NetWorkResponse.EssayList>> {
            override fun onResponse(call: Call<NetWorkResponse<NetWorkResponse.EssayList>>,
                                    response: Response<NetWorkResponse<NetWorkResponse.EssayList>>
            ) {
                val articleDataList = response.body()?.data
                if (articleDataList == null) {
                    makeWLog("null article list body?")
                } else {
                    onSuccess(articleDataList.essayList)
                    makeILog("get articleData list success ${articleDataList.essayList.size}")
                }
            }
            override fun onFailure(
                call: Call<NetWorkResponse<NetWorkResponse.EssayList>>,
                t: Throwable
            ) {
//                onFailure()
                onSuccess(GenerateTestContentUtils.generateArticleList())
                t.printStackTrace()
                makeWLog("suggestion article list getting failed!!")
            }
        })
    }

    //new okhttp client that able to hold a token:
    private fun generateBlankClient(): OkHttpClient{
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private fun generateClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                var cookie = MyApplication.context.getSharedPreferences("cookie", Context.MODE_PRIVATE).getString("cookie", "")
                if (cookie == null){
                    cookie = ""
                    Log.e("NetworkUtils:", "null token!!")
                }
                val request: Request = chain.request()
                    .newBuilder()
                    .addHeader("Cookie", cookie)
                    .build()
                chain.proceed(request)
            }.build()
    }
    private fun generateClient(timeOut: Long): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(timeOut, TimeUnit.MILLISECONDS)
            .readTimeout(timeOut, TimeUnit.MILLISECONDS)
            .addInterceptor { chain ->
                var cookie = MyApplication.context.getSharedPreferences("cookie", Context.MODE_PRIVATE).getString("cookie", "")
                if (cookie == null){
                    cookie = ""
                    Log.e("NetworkUtils:", "null token!!")
                }
                val request: Request = chain.request()
                    .newBuilder()
                    .addHeader("Cookie", cookie)
                    .build()
                chain.proceed(request)
            }.build()
    }

    private fun getGeneralAppService(): NetworkService {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://47.107.225.197:8098/")
            .client(generateBlankClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(NetworkService::class.java)
    }
    private fun getGeneralAppService(timeOut: Long): NetworkService {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://47.107.225.197:8098/")
            .client(generateClient(timeOut))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(NetworkService::class.java)
    }

    private fun <T> enqueueAction(action: () -> Call<T>, onResponse: (call: Call<T>, response: Response<T>) -> Unit, onFailure: (call: Call<T>, t: Throwable) -> Unit){
        action().enqueue(object: Callback<T>{
            override fun onResponse(
                call: Call<T>,
                response: Response<T>
            ) {
                onResponse(call, response)
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                onFailure(call, t)
            }

        })
    }

    //tools:
    private fun makeILog(msg: String){
        Log.i("networkUtil:", msg)
    }
    private fun makeWLog(msg: String){
        Log.w("networkUtil:", msg)
    }



}