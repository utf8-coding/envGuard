package com.utf8coding.envGuard.fragment

import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.amap.api.location.AMapLocationClient
import com.amap.api.maps2d.AMap
import com.amap.api.maps2d.MapView
import com.amap.api.maps2d.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.utf8coding.envGuard.R
import com.utf8coding.envGuard.data.BinData
import com.utf8coding.envGuard.viewModel.MapFragmentViewModel
import java.lang.Thread.sleep
import kotlin.concurrent.thread
import kotlin.properties.Delegates


class MapFragment : BaseFragment() {

    private lateinit var mapView: MapView
    private lateinit var binName: TextView
    private lateinit var binIndicator: LinearProgressIndicator
    private lateinit var binIndicatorText: TextView
    private lateinit var bottomSheet: FrameLayout
    private var currentLat = 0.00
    private var currentLong = 0.00
    private var aMap: AMap? = null
    private val binListForShow: ArrayList<BinData> = ArrayList()
    private val markerList: ArrayList<Marker> = ArrayList()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>

    private lateinit var viewModel: MapFragmentViewModel
    override fun refresh() {
        viewModel.getNearestBinList(currentLat.toString(), currentLong.toString()).observe(viewLifecycleOwner
        ) { t ->
            t?.let {
                binListForShow.clear()
                binListForShow.addAll(it)
                reDrawBins()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[MapFragmentViewModel::class.java]

        initPermission()

        initFindViews(view)

        initMapView(savedInstanceState)

        initBottomSheet()

    }

    private fun initPermission(){
        if (Build.VERSION.SDK_INT >= 23) {
            if(context != null && activity != null){
                if (
                    ActivityCompat.checkSelfPermission(context!!, ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(context!!, ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    //未赋予权限，申请权限
                    if (
                        ActivityCompat
                            .shouldShowRequestPermissionRationale(
                                activity!!,
                                ACCESS_COARSE_LOCATION
                            )
                    ) {
                        //选择不开启权限
                        Toast.makeText(context, "允许本权限则方可使用地图功能", Toast.LENGTH_SHORT).show()
                    }
                    //申请权限
                    ActivityCompat.requestPermissions(activity!!, arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION), 1)

                }
            }

        }
    }

    private fun initFindViews(view: View){
        mapView = view.findViewById(R.id.map)
        binName = view.findViewById(R.id.binName)
        binIndicator = view.findViewById(R.id.binIndicator)
        binIndicatorText = view.findViewById(R.id.progressText)
        bottomSheet = view.findViewById(R.id.mapBottomSheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
    }

    private fun initMapView(savedInstanceState: Bundle?){
        AMapLocationClient.updatePrivacyShow(context, true, true)
        AMapLocationClient.updatePrivacyAgree(context, true)

        mapView.onCreate(savedInstanceState)
        if (aMap == null) {
            aMap = mapView.map
        }
        val myLocationStyle = MyLocationStyle()
        myLocationStyle.showMyLocation(true)
        myLocationStyle.interval(2000)
        aMap!!.setMyLocationStyle(myLocationStyle)
        aMap!!.isMyLocationEnabled = true
        aMap!!.uiSettings.isMyLocationButtonEnabled = true
        aMap!!.setOnMyLocationChangeListener { location ->
            currentLat = location.latitude; currentLong = location.longitude
            viewModel.getNearestBinList(location.latitude.toString(), location.longitude.toString()).observe(viewLifecycleOwner
            ) { t ->
                t?.let {
                    binListForShow.clear()
                    binListForShow.addAll(it)
                    reDrawBins()
                }
            }
            Log.e("MapFragment:", "lat: ${location.latitude}, lon: ${location.longitude}")
        }
    }

    private fun initBottomSheet(){
        bottomSheetBehavior.state = STATE_HIDDEN
        bottomSheetBehavior.shouldSkipHalfExpandedStateWhenDragging() //加入更多展示后消除
    }

    private fun reDrawBins(){
        Log.i("MapFragment:", "drawing bins: $binListForShow")
        markerList.clear()
        for (i in binListForShow){
            val latLng = LatLng(i.latitude.toDouble(), i.longitude.toDouble())
            val marker = aMap!!.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("${i.id} 号垃圾桶")
                    .icon(
                        BitmapDescriptorFactory.fromBitmap(
                            BitmapFactory.decodeResource(resources, R.drawable.ic_bin_map)
                        )
                    )
            )
            markerList.add(marker)
        }

        aMap?.setOnMarkerClickListener {
            var onTapBinData: BinData?
            var isPoped = false
            for (i in 0..markerList.size){
                if (markerList[i] == it){
                    onTapBinData = binListForShow[i]
                    isPoped = true
                } else {
                    if (isPoped) break
                    else continue
                }
                Log.i("MapFragment:", "弹窗，垃圾桶信息：$onTapBinData")
                it.showInfoWindow()
                binIndicator.setProgressCompat((onTapBinData.level * 100).toInt(), true)
                if (bottomSheetBehavior.state == STATE_HIDDEN){
                    binIndicatorText.text = "${(onTapBinData.level * 100).toInt()}%"
                    binName.text = "${onTapBinData.id} 号垃圾桶"
                    bottomSheetBehavior.state = STATE_EXPANDED
                } else {
                    thread {
                        bottomSheetBehavior.state = STATE_HIDDEN
                        sleep(50)
                        activity?.runOnUiThread{
                            binIndicatorText.text = "${(onTapBinData.level * 100).toInt()}%"
                            binName.text = "${onTapBinData.id} 号垃圾桶"
                        }
                        sleep(150)
                        bottomSheetBehavior.state = STATE_EXPANDED
                    }
                }
                bottomSheetBehavior.saveFlags = BottomSheetBehavior.SAVE_ALL
            }
            true
        }
    }

}

