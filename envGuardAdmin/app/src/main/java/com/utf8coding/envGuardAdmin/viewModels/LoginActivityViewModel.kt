package com.utf8coding.envGuardAdmin.viewModels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.utf8coding.envGuardAdmin.MyApplication
import com.utf8coding.envGuardAdmin.network.NetWorkResponse
import com.utf8coding.envGuardAdmin.utils.NetWorkUtils
import kotlin.concurrent.thread

class LoginActivityViewModel: ViewModel() {
    fun login(userName: String, password: String, onSuccess: () -> Unit, onFailure: (rsp: Int) -> Unit){
        NetWorkUtils.login(
            userName, password,
            onResponse = { isSuccess, rspCode ->
                if (isSuccess){
                    Log.i("LoginActivityViewModel:", "login success")
                    onSuccess()
                } else {
                    Log.i("LoginActivityViewModel:", "login fail code: $rspCode")
                    onFailure(rspCode)
                }
            },
            onFailure = {
                Log.i("LoginActivityViewModel:", "login fail code: ${NetWorkResponse.NO_CONNECTION}, $it")
                onFailure(NetWorkResponse.NO_CONNECTION)
            }
        )
    }

    fun testConnection(onFailure: () -> Unit){
        thread {
            if (!NetWorkUtils.isNetWorkAvailable()){
                Log.i("LoginActivityViewModel:", "testConnection success")
                onFailure()
            } else {
                Log.i("LoginActivityViewModel:", "testConnection fail")
            }
        }
    }

    fun keepPassword(userName: String, password: String, isBioLogin: Boolean){
        MyApplication.context.getSharedPreferences("loginInfo", Context.MODE_PRIVATE).edit()
            .putString("userName", userName)
            .putString("password", password)
            .putBoolean("isBioLogin", isBioLogin)
            .apply()
    }

    fun clearPassWord(){
        MyApplication.context.getSharedPreferences("loginInfo", Context.MODE_PRIVATE).edit()
            .putString("userName", "")
            .putString("password", "")
            .apply()
    }

    fun getLoginInfo(): ArrayList<String>{
        val pref = MyApplication.context.getSharedPreferences("loginInfo", Context.MODE_PRIVATE)
        val userName = pref.getString("userName", "").toString()
        val password = pref.getString("password", "").toString()
        val isBioLogin = pref.getBoolean("isBioLogin", false).toString()
        return arrayListOf(userName, password, isBioLogin)
    }
}