package com.sstechcanada.todo.custom_views

import android.content.Context
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import com.sstechcanada.todo.R
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView

class MasterIconGridItemView(context: Context?) : FrameLayout(
    context!!
) {
    private val iconImageView: ImageView
    private val cardView: CardView
    fun display(text: Int, isSelected: Boolean) {
//        int imageResource = getContext().getResources().getIdentifier(text, null, getContext().getPackageName());
        val drawable = context.resources.getDrawable(text)
        iconImageView.background = drawable
        //        textView.setText(text);
        display(isSelected)
    }

    fun display(isSelected: Boolean) {
        cardView.setBackgroundResource(if (isSelected) R.drawable.selected_icon_bg else R.color.colorUncompletedBackground)
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.item_grid_master_list, this)
        iconImageView = rootView.findViewById<View>(R.id.iconImageView) as ImageView
        cardView = rootView.findViewById<View>(R.id.cardView) as CardView
    }
}