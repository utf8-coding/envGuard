package com.utf8coding.envGuardAdmin.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.utf8coding.envGuardAdmin.MyApplication
import com.utf8coding.envGuardAdmin.R
import com.utf8coding.envGuardAdmin.data.BinData
import kotlin.math.*

class MainRecyclerViewAdapter(private val binDataList: ArrayList<BinData>, private val onBinDataClick: (binData: BinData) -> Unit): RecyclerView.Adapter<MainRecyclerViewAdapter.ViewHolder>() {
    private val dataList = ArrayList<BinDataParcel>()
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val totalView: ConstraintLayout = view.findViewById(R.id.totalView)
        val binName: TextView = view.findViewById(R.id.binName)
        val normalSign: ImageView = view.findViewById(R.id.normalImage)
        val abnormalSign: ImageView = view.findViewById(R.id.abnormalImage)
        val binIndicator: LinearProgressIndicator = view.findViewById(R.id.binIndicator)
        val binLevelText: TextView = view.findViewById(R.id.binLevelText)
        val positionText: TextView = view.findViewById(R.id.positionText)
        val descriptionText: TextView = view.findViewById(R.id.descriptionText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_main_bin, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
        fun initHolderViews(){
            holder.let { h ->
                h.binName.text = "${data.binData.id}号垃圾桶"
                if (data.binData.state == 1){
                    h.normalSign.visibility = View.VISIBLE
                    h.abnormalSign.visibility = View.INVISIBLE
                    h.binIndicator.setIndicatorColor(MyApplication.context.resources.getColor(R.color.blue_primary))
                    h.binIndicator.trackColor = MyApplication.context.resources.getColor(R.color.blue_light_shadow)
                } else {
                    h.normalSign.visibility = View.INVISIBLE
                    h.abnormalSign.visibility = View.VISIBLE
                    h.binIndicator.setIndicatorColor(MyApplication.context.resources.getColor(R.color.orange))
                    h.binIndicator.trackColor = MyApplication.context.resources.getColor(R.color.orange_light)
                }
                h.binIndicator.progress = (data.binData.level * 100).toInt()
                h.binLevelText.text = "${(data.binData.level * 100)}%"
                h.positionText.text = "${data.binData.latitude}*${data.binData.longitude}"
                if (data.binData.description != null){
                    h.descriptionText.text = data.binData.description
                } else {
                    h.descriptionText.text = "暂无"
                }
                h.totalView.setOnClickListener {
                    onBinDataClick(data.binData)
                }
            }
        }

        initHolderViews()
    }

    override fun getItemCount(): Int {
        return binDataList.size
    }

    fun refreshLocation(mLatitude: Double, mLongitude: Double){
        latitude = mLatitude
        longitude = mLongitude

    }

    fun mNotifyDataSetChanged(){
        calcDistanceAndChangeData()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun calcDistanceAndChangeData(){
        fun calcDistance(la1: Double, la2: Double, lo1: Double, lo2: Double): Double{
            val earthRadius = 6378.137

            val lat1 = Math.toRadians(la1)
            val lat2 = Math.toRadians(la2)
            val lng1 = Math.toRadians(lo1)
            val lng2 = Math.toRadians(lo2)

            val a = lat1 - lat2
            val b = lng1 - lng2

            val s = 2 * asin(
                sqrt(
                    sin(a / 2).pow(2.0) + cos(lat1) * cos(lat2) * sin(b / 2).pow(2.0)
                )
            )
            return s * earthRadius
        }

        dataList.clear()
        for (i in binDataList){
            dataList.add(BinDataParcel(
                i,
                calcDistance(latitude, i.latitude, longitude, i.longitude))
            )
        }
        notifyDataSetChanged()
        Log.i("MainRecyclerViewAdapter:", "on mNotifyDataSetChanged, dataList now: $dataList")
    }

    inner class BinDataParcel(val binData: BinData, var distance: Double)
}