package com.utf8coding.envGuard.adapters.article_reading

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.utf8coding.envGuard.R
import com.utf8coding.envGuard.data.ArticleData

class ArticleReadingTextAdapter(private val data: ArticleData): RecyclerView.Adapter<ArticleReadingTextAdapter.ViewHolder>() {

    lateinit var readingText: TextView

    inner class ViewHolder(val view: View):RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_article_reading_text, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.i("ArticleReadingTextAdapter", "binding data: $data")
        holder.view.findViewById<TextView>(R.id.articleReadingContentTextView).text = data.content
        readingText = holder.view.findViewById(R.id.articleReadingContentTextView)
    }

    override fun getItemCount(): Int {
        return 1
    }

    fun setTextSize(size: Float){
        readingText.textSize = size
    }

}