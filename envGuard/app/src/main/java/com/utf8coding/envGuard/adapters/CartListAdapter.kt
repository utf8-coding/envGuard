package com.utf8coding.envGuard.adapters

import android.animation.Animator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.utf8coding.envGuard.MyApplication
import com.utf8coding.envGuard.R
import com.utf8coding.envGuard.data.GiftData

class CartListAdapter(private var onDataChangedListener: (response: ResponseCartGiftData) -> Unit): RecyclerView.Adapter<CartListAdapter.ViewHolder>() {

    private val cartDataList = ArrayList<CartDataSet>()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val giftImg: ImageView = view.findViewById(R.id.giftImg)
        val giftName: TextView = view.findViewById(R.id.giftName)
        val giftDescription: TextView = view.findViewById(R.id.giftDescription)
        val giftPrice: TextView = view.findViewById(R.id.giftPrice)
        val addButton: ImageView = view.findViewById(R.id.addButton)
        val minusButton: ImageView = view.findViewById(R.id.minusButton)
        val itemCountCard: CardView = view.findViewById(R.id.itemCountCard)
        val itemCount: TextView = view.findViewById(R.id.itemCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gift_shop_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val curData = cartDataList[position].giftData.copy()
        val curCount = cartDataList[position].count

        fun changeViewByAmount(value: Int){
            holder.itemCount.text = value.toString()

            //为零就移除
            if (cartDataList[position].count == 0){
                cartDataList.remove(cartDataList[position])
                notifyDataSetChanged()
                onDataChangedListener(ResponseCartGiftData(curData.id, 0, cartDataList.size != 0))
            } else {
                //外传
                onDataChangedListener(ResponseCartGiftData(curData.id, curCount, cartDataList.size != 0))
            }

        }

        fun initHolderViews(){
            Glide.with(MyApplication.context).load(cartDataList[position].giftData.giftImgUrl).into(holder.giftImg)
            holder.let { h ->
                h.itemCountCard.visibility = VISIBLE
                h.minusButton.visibility = VISIBLE
                cartDataList[position].cartAmountTextView = h.itemCount
                h.giftName.text = cartDataList[position].giftData.name
                h.giftDescription.text = cartDataList[position].giftData.description
                h.giftPrice.text = cartDataList[position].giftData.price.toString()
                h.itemCount.text = cartDataList[position].count.toString()
                h.addButton.setOnClickListener{
                    //加一
                    cartDataList[position].count = cartDataList[position].count.plus(1)
                    changeViewByAmount(cartDataList[position].count)
                }
                h.minusButton.setOnClickListener{
                    //减一
                    cartDataList[position].count = cartDataList[position].count.minus(1)
                    changeViewByAmount(cartDataList[position].count)
                }
            }
        }

        initHolderViews()
    }

    override fun getItemCount(): Int {
        return cartDataList.size
    }

    fun changeCountByGiftData(giftData: GiftData, setValue: Int){
        var matchingDataSet: CartDataSet? = null
        var matchingDataSetCount: Int? = null
        //找到对应的（如果有）
        if (cartDataList.size != 0) {
            for (i in 0 until cartDataList.size) {
                if (cartDataList[i].giftData.id == giftData.id ){
                    matchingDataSetCount = i
                    matchingDataSet = cartDataList[i]
                }
            }
        }

        //处理
        if (matchingDataSet != null && matchingDataSet.count != setValue && setValue != 0){
            //存在，正常
            matchingDataSet.count = setValue
            //设置textView显示：
            matchingDataSet.cartAmountTextView?.text = setValue.toString()
        } else if (matchingDataSet != null && matchingDataSet.count != setValue && setValue == 0) {
            //为零移除
            if (setValue == 0 && matchingDataSetCount != null){
                cartDataList.removeAt(matchingDataSetCount)
                notifyDataSetChanged()
            }
        } else if (matchingDataSet == null && setValue != 0) {
            //不存在不为零，添加
            cartDataList.add(CartDataSet(giftData, setValue, null))
            notifyDataSetChanged()
        }

    }

    fun getCartListSize(): Int{
        return cartDataList.size
    }

    fun getCartList(): ArrayList<GiftData>{
        val list = ArrayList<GiftData>()
        for (i in cartDataList){
            list.add(i.giftData)
        }
        return list
    }

    inner class ResponseCartGiftData(val id: Int, val currentValue: Int, val isShowFab: Boolean) // inner不知道对不对

    inner class CartDataSet(val giftData: GiftData, var count: Int, var cartAmountTextView: TextView?)
}