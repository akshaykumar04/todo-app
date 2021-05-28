package com.sstechcanada.todo.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.sstechcanada.todo.custom_views.MasterIconGridItemView;

import java.util.ArrayList;

public class MasterListGridViewAdapter extends BaseAdapter {
    private Activity activity;
    private String selectedDrawable;
    ArrayList<String> listDrawable;
    int selectedPosition;


    public MasterListGridViewAdapter(String selectedDrawable, Activity activity) {
        this.selectedDrawable = selectedDrawable;
        this.activity = activity;
    }

    public MasterListGridViewAdapter(ArrayList<String> listDrawable, Activity activity) {
        this.listDrawable = listDrawable;
        this.activity = activity;
    }


    @Override
    public int getCount() {
//        return strings.length;
        return listDrawable.size();
    }

    @Override
    public Object getItem(int position) {
//        String s = listDrawable.get(position);
//        return strings[position];
        return listDrawable.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MasterIconGridItemView customView = (convertView == null) ? new MasterIconGridItemView(activity) : (MasterIconGridItemView) convertView;
        String s = listDrawable.get(position);
        customView.display(s, listDrawable.contains(position));

        return customView;
    }

}