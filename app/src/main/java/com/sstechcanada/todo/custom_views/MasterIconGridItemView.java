package com.sstechcanada.todo.custom_views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.sstechcanada.todo.R;

public class MasterIconGridItemView extends FrameLayout {

    private TextView textView;
    private CardView cardView;

    public MasterIconGridItemView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.item_grid_master_list, this);
        textView = (TextView) getRootView().findViewById(R.id.text);
        cardView = (CardView) getRootView().findViewById(R.id.cardView);
    }

    public void display(int text,boolean isSelected) {
//        int imageResource = getContext().getResources().getIdentifier(text, null, getContext().getPackageName());
        Drawable drawable = getContext().getResources().getDrawable(text);
        textView.setBackground(drawable);
//        textView.setText(text);
        display(isSelected);
    }

    public void display(boolean isSelected) {
        cardView.setBackgroundResource(isSelected ? R.color.colorPrimary : R.color.colorAccent);
    }
}