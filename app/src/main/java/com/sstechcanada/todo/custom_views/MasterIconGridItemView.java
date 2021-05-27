package com.sstechcanada.todo.custom_views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.sstechcanada.todo.R;

public class MasterIconGridItemView extends FrameLayout {

    private TextView textView;

    public MasterIconGridItemView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.item_grid, this);
        textView = (TextView) getRootView().findViewById(R.id.text);
    }

    public void display(String text, boolean isSelected) {
        int imageResource = getContext().getResources().getIdentifier(text, null, getContext().getPackageName());
        Drawable drawable = getContext().getResources().getDrawable(imageResource);
        textView.setBackground(drawable);
//        textView.setText(text);
        display(isSelected);
    }

    public void display(boolean isSelected) {
        textView.setBackgroundResource(isSelected ? R.drawable.green_square : R.drawable.gray_square);
    }
}