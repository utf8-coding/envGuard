package com.utf8coding.envGuardAdmin.viewModels

import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.utf8coding.envGuardAdmin.MyApplication
import com.utf8coding.envGuardAdmin.data.BinData
import com.utf8coding.envGuardAdmin.utils.NetWorkUtils

class MainActivityViewModel: ViewModel() {
    val binList: MutableLiveData<ArrayList<BinData>> = MutableLiveData(ArrayList())

    companion object {
        const val DOUBLE_BACK_PRESS_DELAY = 2000
    }
    var firstBackPressedTime: Long = 0L

    fun getBinDataList(onChange: (list: ArrayList<BinData>) -> Unit, onFailure: () -> Unit): MutableLiveData<ArrayList<BinData>>{
        NetWorkUtils.getAllBinData(
            onResponse = { isSuccess, list ->
                if (isSuccess){
                    binList.value?.clear()
                    binList.value?.addAll(list)
                    binList.value?.let { onChange(it) }
                } else {
                    onFailure()
                }
            },
            onFailure = {
                onFailure()
            }
        )
        return binList
    }

    fun commitData(newData: BinData, onSuccess: () -> Unit, onFailure: () -> Unit){
        NetWorkUtils.commitBinData(
            newData.id, newData.longitude, newData.latitude, newData.state, newData.level , newData.description,
            onSuccess = {
                onSuccess()
            },
            onFailure = {
                onFailure()
            }
        )
    }

    fun sendMail(){
        NetWorkUtils.sendMail(
            "1825391677@qq.com",
            onSuccess = {
                Toast.makeText(MyApplication.context, "发送成功", Toast.LENGTH_SHORT).show()
            },
            onFailure = {
                Toast.makeText(MyApplication.context, "发送失败", Toast.LENGTH_SHORT).show()
            }
        )
    }

    fun addBin(lat: Double, long: Double, onSuccess: () -> Unit, onFailure: () -> Unit){
        NetWorkUtils.addBin(
            lat,
            long,
            onSuccess = {
                onSuccess()
            },
            {
                onFailure()
            }
        )
    }
}