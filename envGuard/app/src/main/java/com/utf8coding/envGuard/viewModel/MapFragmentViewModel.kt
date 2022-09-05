package com.utf8coding.envGuard.viewModel

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.utf8coding.envGuard.MyApplication
import com.utf8coding.envGuard.data.BinData
import com.utf8coding.envGuard.network.NetworkUtils

class MapFragmentViewModel : ViewModel() {
    var binList: MutableLiveData<ArrayList<BinData>> = MutableLiveData(ArrayList())

    fun getNearestBinList(latitude: String, longitude: String): MutableLiveData<ArrayList<BinData>>{
        NetworkUtils.getNearestBinList(
            latitude, longitude,
            onSuccess = {
                Log.i("MapFragmentViewModel:", "${binList.value}")
                binList.value = it
            },
            onFailure = {
                //todo: 不友善
                Log.i("MapFragmentViewModel:", "${binList.value}")
                Toast.makeText(MyApplication.context, "访问出错：$it", Toast.LENGTH_SHORT).show()
            }
        )
        return binList
    }
}