package com.sstechcanada.todo.custom_views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.cardview.widget.CardView;

import com.sstechcanada.todo.R;

public class MasterIconGridItemView extends FrameLayout {

    private ImageView iconImageView;
    private CardView cardView;

    public MasterIconGridItemView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.item_grid_master_list, this);
        iconImageView = (ImageView) getRootView().findViewById(R.id.iconImageView);
        cardView = (CardView) getRootView().findViewById(R.id.cardView);
    }

    public void display(int text,boolean isSelected) {
//        int imageResource = getContext().getResources().getIdentifier(text, null, getContext().getPackageName());
        Drawable drawable = getContext().getResources().getDrawable(text);
        iconImageView.setBackground(drawable);
//        textView.setText(text);
        display(isSelected);
    }

    public void display(boolean isSelected) {
        cardView.setBackgroundResource(isSelected ? R.drawable.selected_icon_bg : R.color.colorUncompletedBackground);
    }
}