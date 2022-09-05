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
import java.lang.Exception

class GiftShopListAdapter(private val dataList: ArrayList<GiftData>, private var onDataChangedListener: (giftData: GiftData, value: Int) -> Unit): RecyclerView.Adapter<GiftShopListAdapter.ViewHolder>() {

    private val giftDataList = ArrayList<GiftDataSet>()

    init {
        initGiftDataList()
    }

    private fun initGiftDataList() {
        for (i in dataList){
            giftDataList.add(GiftDataSet(i, 0, null, null, null))
        }
    }

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
        Log.e("GiftShopAdapter:", "list: $giftDataList")
        val curData = giftDataList[position].giftData.copy()

        fun notifyCountValueChanged(){
            holder.itemCount.text = giftDataList[position].count.toString()
            // 动画
            if (giftDataList[position].count != 0){
                holder.itemCountCard.visibility = VISIBLE
                holder.minusButton.visibility = VISIBLE
                holder.itemCountCard.animate().alpha(1f).setListener(OnAnimationEndListener{})
                holder.minusButton.animate().alpha(1f).setListener(OnAnimationEndListener{})
            } else {
                holder.itemCountCard.animate().alpha(0f).setListener(
                    OnAnimationEndListener {
                        holder.itemCountCard.visibility = GONE
                    }
                )
                holder.minusButton.animate().alpha(0f).setListener(
                    OnAnimationEndListener {
                        holder.minusButton.visibility = GONE
                    }
                )
            }
            //传到外部
            onDataChangedListener(curData, giftDataList[position].count)
        }

        fun initHolderContent(){
            Glide.with(MyApplication.context).load(curData.giftImgUrl).into(holder.giftImg)
            holder.let { h ->
                giftDataList[position].countTextView = h.itemCount
                giftDataList[position].contCardView = h.itemCountCard
                giftDataList[position].minusButton = h.minusButton
                h.giftName.text = curData.name
                h.giftDescription.text = curData.description
                h.giftPrice.text = curData.price.toString()
                h.addButton.setOnClickListener {
                    //加一
                    giftDataList[position].count += 1
                    notifyCountValueChanged()
                }
                h.minusButton.setOnClickListener {
                    //减一
                    giftDataList[position].count -= 1
                    notifyCountValueChanged()
                }
            }
        }

        initHolderContent()

    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun changeCountById(id: Int, setValue: Int){
        var matchingDataSet: GiftDataSet? = null
        //找到对应的（如果有）
        if (giftDataList.size != 0) {
            for (i in giftDataList) {
                if (i.giftData.id == id ){
                    matchingDataSet = i
                }
            }
        }

        Log.e("GiftShopAdapter:", "${matchingDataSet != null} && ${matchingDataSet?.count != setValue} || ${setValue == 0}")
        //处理
        if (matchingDataSet != null && ( matchingDataSet.count != setValue || setValue == 0)){
            //存在，正常
            matchingDataSet.count = setValue
            //设置textView显示：
            matchingDataSet.countTextView?.text = setValue.toString()
            // 动画
            if (setValue != 0){
                matchingDataSet.contCardView?.visibility = VISIBLE
                matchingDataSet.minusButton?.visibility = VISIBLE
                matchingDataSet.contCardView?.animate()?.alpha(1f)?.setListener(OnAnimationEndListener{})
                matchingDataSet.minusButton?.animate()?.alpha(1f)?.setListener(OnAnimationEndListener{})
            } else {
                matchingDataSet.contCardView?.animate()?.alpha(0f)?.setListener(
                    OnAnimationEndListener {
                        matchingDataSet.contCardView?.visibility = GONE
                    }
                )
                matchingDataSet.minusButton?.animate()?.alpha(0f)?.setListener(
                    OnAnimationEndListener {
                        matchingDataSet.minusButton?.visibility = GONE
                    }
                )
            }
        }
    }

    fun mNotifyDataSetChanged(){
        initGiftDataList()
        notifyDataSetChanged()
    }

    open inner class OnAnimationEndListener(private val mOnAnimationEnd: (animation: Animator?) -> Unit): Animator.AnimatorListener{
        override fun onAnimationStart(animation: Animator?) {}
        override fun onAnimationEnd(animation: Animator?) {
            mOnAnimationEnd(animation)
        }
        override fun onAnimationCancel(animation: Animator?) {}
        override fun onAnimationRepeat(animation: Animator?) {}
    }

    inner class GiftDataSet(val giftData: GiftData, var count: Int, var countTextView: TextView?, var contCardView: CardView?, var minusButton: ImageView?)
}