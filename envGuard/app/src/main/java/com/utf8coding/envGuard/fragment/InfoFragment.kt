package com.utf8coding.envGuard.fragment

import android.animation.Animator
import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.utf8coding.envGuard.R
import com.utf8coding.envGuard.activities.secondary.ArticleReadingActivity
import com.utf8coding.envGuard.utils.DensityUtils
import com.utf8coding.envGuard.viewModel.InfoFragmentViewModel
import com.utf8coding.envGuard.adapters.InfoRecyclerViewAdapter
import com.utf8coding.envGuard.adapters.InfoRecyclerViewHeaderAdapter
import com.utf8coding.envGuard.data.ArticleData

class InfoFragment : BaseFragment() {

    private lateinit var viewModel: InfoFragmentViewModel
    private val recyclerView: RecyclerView?
        get() {
            return view?.findViewById(R.id.recyclerView)
        }
//    private val refreshLayout: RefreshLayout?
//        get() {
//            return view?.findViewById(R.id.refreshLayout)
//        }
//    private val loadingHeader: MaterialHeader?
//        get() {
//            return view?.findViewById(R.id.loadingHeader)
//        }

    private var medSearchButton: ImageView? = null
    private var articleSearchButton: ImageView? = null
    private var recyclerViewAdapter = InfoRecyclerViewAdapter(ArrayList(), arrayListOf(0, 0))
    private var recyclerViewHeaderAdapter = InfoRecyclerViewHeaderAdapter()
    private val articleDataListForRecyclerView = ArrayList<ArticleData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[InfoFragmentViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
//        refreshLayout?.setOnRefreshListener {
//            refresh()
//        }
//        loadingHeader?.setOnHoverListener { _, _ ->
//            refresh()
//            false
//        }
    }

    override fun onResume() {
        super.onResume()
        medSearchButton?.visibility = VISIBLE
        articleSearchButton?.visibility = VISIBLE
        medSearchButton?.animate()?.y(10f)?.alpha(1f)?.interpolator = DecelerateInterpolator()
        articleSearchButton?.animate()?.y(10f)?.alpha(1f)?.interpolator = DecelerateInterpolator()
     }

    @SuppressLint("NotifyDataSetChanged")
    override fun refresh(){
        viewModel.getArticleList().observe(viewLifecycleOwner) { newArticleDataList ->
            articleDataListForRecyclerView.clear()
            articleDataListForRecyclerView.addAll(newArticleDataList)
            recyclerViewAdapter.notifyDataSetChanged()
//            refreshLayout?.finishRefresh()
        }
    }

    private fun initRecyclerView(){
        //拿List：
        viewModel.getArticleList().observe(viewLifecycleOwner) { newArticleDataList ->
            articleDataListForRecyclerView.clear()
            articleDataListForRecyclerView.addAll(newArticleDataList)
            val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            recyclerView?.layoutManager = layoutManager

            //header adapter
            recyclerViewHeaderAdapter = InfoRecyclerViewHeaderAdapter()

            //初始化adapter
            recyclerViewAdapter = InfoRecyclerViewAdapter(
                articleDataListForRecyclerView,
                arrayListOf(
                    DensityUtils.horizontalDp(activity as Context).toInt(),
                DensityUtils.verticalDp(activity as Context).toInt()),
                object: InfoRecyclerViewAdapter.OnItemClickListener{

                    override fun onInfoClick(imageView: View, item: ArticleData) {
                        val imagePair = Pair(imageView,"transitionImgView")
                        val bundle =
                            ActivityOptions.makeSceneTransitionAnimation(
                                activity,
                                imagePair
                            )
                                .toBundle()
                        val intent = Intent(activity, ArticleReadingActivity::class.java)
                        intent.putExtra("articleData", item)
                        startActivity(intent, bundle)
                    }

                    override fun onInfoClick(item: ArticleData) {
                        val intent = Intent(activity, ArticleReadingActivity::class.java)
                        intent.putExtra("articleData", item)
                        startActivity(intent)
                    }
                }
            )
            //用于 header：
            val concatAdapter = ConcatAdapter(recyclerViewHeaderAdapter, recyclerViewAdapter)
            recyclerView?.adapter = concatAdapter
        }
    }
    //tools:
    private fun makeToast(msg: String){
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
    }
    private fun makeILog(msg: String) {
        Log.i("InfoFragment:", msg)
    }

    inner class EmptyAnimationListener: Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator?) {}
        override fun onAnimationEnd(animation: Animator?) {}
        override fun onAnimationCancel(animation: Animator?) {}
        override fun onAnimationRepeat(animation: Animator?) {}
    }
}