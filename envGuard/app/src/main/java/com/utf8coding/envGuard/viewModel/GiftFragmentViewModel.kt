package com.utf8coding.envGuard.viewModel

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.utf8coding.envGuard.MyApplication
import com.utf8coding.envGuard.data.GiftData
import com.utf8coding.envGuard.utils.GenerateTestContentUtils

class GiftFragmentViewModel : ViewModel() {
    private val giftShopList = MutableLiveData(ArrayList<GiftData>())

    fun getShopGiftList(): MutableLiveData<ArrayList<GiftData>>{
        giftShopList.value?.clear()
        giftShopList.value?.addAll(GenerateTestContentUtils.generateGiftList())
        return giftShopList
    }

    fun doSettlement(giftDataList: ArrayList<GiftData>){
        Toast.makeText(MyApplication.context, "结算成功，相关信息将会发到你的邮箱！", Toast.LENGTH_SHORT).show()
    }
}