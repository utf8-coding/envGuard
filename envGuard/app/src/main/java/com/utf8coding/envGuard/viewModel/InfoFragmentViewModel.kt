package com.utf8coding.envGuard.viewModel

import android.content.Context
import androidx.lifecycle.*
import com.utf8coding.envGuard.MyApplication
import com.utf8coding.envGuard.data.ArticleData
import com.utf8coding.envGuard.network.NetworkUtils

class InfoFragmentViewModel: ViewModel() {
    var articleDataList: MutableLiveData<ArrayList<ArticleData>> = MutableLiveData(ArrayList())
    fun getArticleList(): MutableLiveData<ArrayList<ArticleData>>{
        NetworkUtils.getSuggestedArticle(getUserId(), { articleDataList.value = it }, {})
        return articleDataList
    }

    fun getUserId(): Int{
        return MyApplication.context.getSharedPreferences("userData", Context.MODE_PRIVATE).getInt("userId", -1)
    }
}