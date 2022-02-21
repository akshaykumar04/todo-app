package com.sstechcanada.todo.custom_views

import android.content.Context
import android.widget.FrameLayout
import android.widget.TextView
import com.sstechcanada.todo.R
import android.view.LayoutInflater
import android.view.View

class GridItemView(context: Context?) : FrameLayout(context!!) {
    private val textView: TextView
    fun display(text: String?, isSelected: Boolean) {
        textView.text = text
        display(isSelected)
    }

    fun display(isSelected: Boolean) {
        textView.setBackgroundResource(if (isSelected) R.drawable.green_square else R.drawable.gray_square)
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.item_grid, this)
        textView = rootView.findViewById<View>(R.id.text) as TextView
    }
}