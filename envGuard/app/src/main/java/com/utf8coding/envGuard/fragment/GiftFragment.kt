package com.utf8coding.envGuard.fragment

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.utf8coding.envGuard.R
import com.utf8coding.envGuard.adapters.CartListAdapter
import com.utf8coding.envGuard.adapters.GiftShopListAdapter
import com.utf8coding.envGuard.data.GiftData
import com.utf8coding.envGuard.viewModel.GiftFragmentViewModel

class GiftFragment : BaseFragment() {

    private lateinit var viewModel: GiftFragmentViewModel
    private lateinit var bottomSheet: FrameLayout
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>
    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var shopListRecyclerView: RecyclerView
    private lateinit var cartFab: ExtendedFloatingActionButton
    private lateinit var settlementButton: Button
    private val shopGiftList = ArrayList<GiftData>()
    private lateinit var shopAdapter: GiftShopListAdapter
    private lateinit var cartAdapter: CartListAdapter
    private var isBlockTransfer = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gift, container, false)
    }

    override fun refresh() {
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this)[GiftFragmentViewModel::class.java]

        initViews()

        getGiftData()

        initRecyclerViews()

    }

    private fun initViews(){
        view?.let {
            bottomSheet = it.findViewById(R.id.giftBottomSheet)
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
            shopListRecyclerView = it.findViewById(R.id.giftRecyclerView)
            cartRecyclerView = it.findViewById(R.id.cartRecyclerView)
            cartFab = it.findViewById(R.id.cartFab)
            settlementButton = it.findViewById(R.id.settlement)

            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            bottomSheetBehavior.addBottomSheetCallback(object:
                BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_HIDDEN -> cartFab.show()
                        BottomSheetBehavior.STATE_COLLAPSED -> bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                    }
                }
                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            })

            cartFab.hide()
            cartFab.setOnClickListener {
                cartFab.hide()
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }

            settlementButton.setOnClickListener {
                viewModel.doSettlement(cartAdapter.getCartList())
            }
        }
    }

    private fun initRecyclerViews(){
        shopAdapter = GiftShopListAdapter(shopGiftList) { giftData, value ->
            Log.i("GiftFragment:", "giftData: $giftData, value: $value")
            cartAdapter.changeCountByGiftData(giftData, value)
            isBlockTransfer = true

            //根据购物车信息处理FAB
            if (cartAdapter.getCartListSize() != 0){
                cartFab.show()
            } else {
                cartFab.hide()
            }
        }
        shopListRecyclerView.adapter = shopAdapter
        shopListRecyclerView.layoutManager = LinearLayoutManager(context)
        //todo 不起作用
        shopListRecyclerView.setOnDragListener { _, _ ->
            if (shopListRecyclerView.canScrollVertically(-1)) {
                Log.i("GiftFragment:", "hideFAB")
                cartFab.hide()
            } else {
                Log.i("GiftFragment:", "showFAB")
                cartFab.show()
            }
            false
        }

        cartAdapter = CartListAdapter {
            shopAdapter.changeCountById(it.id, it.currentValue)
            if (it.isShowFab){
                cartFab.show()
            } else {
                cartFab.hide()
            }
        }
        cartRecyclerView.adapter = cartAdapter
        cartRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun getGiftData(){
        viewModel.getShopGiftList().observe(
            viewLifecycleOwner
        ) {
            shopGiftList.clear()
            shopGiftList.addAll(it)
            shopAdapter.mNotifyDataSetChanged()
        }
    }

}