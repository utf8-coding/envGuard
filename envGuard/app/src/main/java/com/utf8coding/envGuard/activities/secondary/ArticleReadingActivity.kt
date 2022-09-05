package com.utf8coding.envGuard.activities.secondary

import android.animation.Animator
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.slider.Slider
import com.utf8coding.envGuard.R
import com.utf8coding.envGuard.utils.DensityUtils
import com.utf8coding.envGuard.viewModel.ArticleReadingActivityViewModel
import com.utf8coding.envGuard.adapters.article_reading.ArticleReadingTextAdapter
import com.utf8coding.envGuard.data.ArticleData

@Suppress("TypeParameterFindViewById")
class ArticleReadingActivity : AppCompatActivity() {
    lateinit var viewModel: ArticleReadingActivityViewModel
    private val headerImg: ImageView
        get() {
            return findViewById(R.id.articleReadingHeaderImg) as ImageView
        }
    private val recyclerView: RecyclerView
        get() {
            return findViewById(R.id.readingRecyclerView)
        }
    private val collapsingToolbarLayout: CollapsingToolbarLayout
        get() {
            return findViewById(R.id.collapsingToolbar)
        }
    private val appBarLayout: AppBarLayout
        get() {
            return findViewById(R.id.appBarLayout)
        }
    private val toolBar: androidx.appcompat.widget.Toolbar
        get() {
            return findViewById(R.id.toolbar)
        }
    private val textSizeSlider: Slider
        get() {
            return findViewById(R.id.textSizeSlider)
        }
    private val closeTextSizeButton: ImageButton
        get() {
            return findViewById(R.id.closeTextSizeButton)
        }
    private val textSizeCard: CardView
        get() {
            return findViewById(R.id.textSizeCard)
        }
    private var articleData: ArticleData = ArticleData(0, "出错了", "出错了", null)
    private var readingTextAdapter = ArticleReadingTextAdapter(articleData)
    private var isBackAnimate = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article_reading)
        viewModel = ViewModelProvider(this)[ArticleReadingActivityViewModel::class.java]
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        articleData = intent.getSerializableExtra("articleData") as ArticleData

        closeTextSizeButton.setOnClickListener {
            textSizeCard.animate().alpha(0f).setListener(OnAnimationEndListener{textSizeCard.visibility = View.INVISIBLE})
        }

        initCollapsingBarLayout()

        initCollectButton()

        intiCollectButtonLogic()

        initRecyclerView()

        initTextSizeSlider()
    }

//todo: 收藏要做加在这里
//    override fun onDestroy() {
//        if (viewModel.isCollected.value!!){
//            viewModel.collectArticle(articleData)
//        } else {
//            viewModel.unCollectArticle(articleData)
//        }
//        super.onDestroy()
//    }

    override fun onBackPressed() {

        if (!isBackAnimate) {
            headerImg.transitionName = ""
        }
        super.onBackPressed()
    }

    private fun initCollapsingBarLayout(){
        collapsingToolbarLayout.title = articleData.title
        if (articleData.headPicUrl != null) {
            Glide.with(this).load(articleData.headPicUrl).into(headerImg)
        } else {
            collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandingCollapsingViewTitleTextBlack)
            collapsingToolbarLayout.expandedTitleMarginStart -= DensityUtils.dp2px(this, 20f).toInt()
            appBarLayout.layoutParams.height = DensityUtils.dp2px(this, 120f).toInt()
        }

        //单独拎出来防止一个 Overload resolution ambiguity 的问题，这里是根据是页面否在顶部判断按返回上一个 activity 时是否有图片飞回的动画
        val listener =  AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            isBackAnimate = verticalOffset == 0
        }
        appBarLayout.addOnOffsetChangedListener(listener)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initRecyclerView() {
        Log.i("ArticleReadingActivity:", "init recyclerView")
        readingTextAdapter = ArticleReadingTextAdapter(articleData)
        recyclerView.outlineSpotShadowColor = resources.getColor(R.color.blue_shadow)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = readingTextAdapter
    }

    private fun initTextSizeSlider(){
        textSizeSlider.value = 20f
        textSizeSlider.addOnChangeListener{ _, value, _ ->
            readingTextAdapter.setTextSize(value)
        }
    }

    private fun intiCollectButtonLogic(){
        toolBar.setOnMenuItemClickListener{
            when (it.itemId) {
//                R.id.collect -> {
//                    viewModel.isCollected.value = !viewModel.isCollected.value!!
//                    viewModel.isCollected.value?.let { isCollected ->
//                        if (isCollected) {
//                            toolBar.menu.getItem(1).icon =
//                                ResourcesCompat.getDrawable(
//                                    resources,
//                                    R.drawable.ic_round_favorite_24, null
//                                )
//                        } else {
//                            toolBar.menu.getItem(1).icon =
//                                ResourcesCompat.getDrawable(
//                                    resources,
//                                    R.drawable.ic_baseline_favorite_border_24, null
//                                )
//                        }
//                    }
//
//                    true
//                }
                R.id.textSize -> {
                    textSizeCard.alpha = 0f
                    textSizeCard.visibility = View.VISIBLE
                    textSizeCard.animate()
                        .setListener(OnAnimationEndListener{})
                        .alpha(1f)
                    true
                }
                else -> false
            }
        }
    }

    private fun initCollectButton(){
//        viewModel.getIsCollected(articleData).observe(this) { isCollected ->
//            if (isCollected) {
//                toolBar.menu.getItem(1).icon =
//                    ResourcesCompat.getDrawable(
//                        resources,
//                        R.drawable.ic_round_favorite_24, null
//                    )
//            } else {
//                toolBar.menu.getItem(1).icon =
//                    ResourcesCompat.getDrawable(
//                        resources,
//                        R.drawable.ic_baseline_favorite_border_24, null
//                    )
//            }
//        }
    }

    inner class OnAnimationEndListener(private val onAnimationEndAction: (animation: Animator?) -> Unit): Animator.AnimatorListener{
        override fun onAnimationStart(animation: Animator?) {
        }
        override fun onAnimationEnd(animation: Animator?) {
            onAnimationEndAction(animation)
        }
        override fun onAnimationCancel(animation: Animator?) {
        }
        override fun onAnimationRepeat(animation: Animator?) {
        }
    }
}