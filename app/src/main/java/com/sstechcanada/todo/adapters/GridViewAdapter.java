package com.sstechcanada.todo.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.sstechcanada.todo.custom_views.GridItemView;
import com.sstechcanada.todo.models.Category;

import java.util.ArrayList;
import java.util.List;

public class GridViewAdapter extends BaseAdapter {
    private Activity activity;
    private String[] strings;
    List<Category> categories;
    public List selectedPositions;

    public GridViewAdapter(String[] strings, Activity activity) {
        this.strings = strings;
        this.activity = activity;
        selectedPositions = new ArrayList<>();
    }

    public GridViewAdapter(List<Category> categories, Activity activity) {
        this.categories = categories;
        this.activity = activity;
        selectedPositions = new ArrayList<>();
    }

    @Override
    public int getCount() {
//        return strings.length;
        return categories.size();
    }

    @Override
    public Object getItem(int position) {
        Category category = categories.get(position);
//        return strings[position];
        return category.getCategoryName();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GridItemView customView = (convertView == null) ? new GridItemView(activity) : (GridItemView) convertView;
        Category category = categories.get(position);
        customView.display(category.getCategoryName(), selectedPositions.contains(position));
//        customView.display(strings[position], selectedPositions.contains(position));
        return customView;
    }

}