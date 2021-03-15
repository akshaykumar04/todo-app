package com.sstechcanada.todo.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sstechcanada.todo.R;
import com.sstechcanada.todo.models.Category;

import java.util.List;

public class CategoryAdapter extends ArrayAdapter<Category> {
    private final Activity context;
    List<Category> categories;

    public CategoryAdapter(Activity context, List<Category> categories) {
        super(context, R.layout.layout_artist_list, categories);
        this.context = context;
        this.categories = categories;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        @SuppressLint({"ViewHolder", "InflateParams"}) View listViewItem = inflater.inflate(R.layout.layout_artist_list, null, true);

        TextView textViewName = (TextView) listViewItem.findViewById(R.id.textViewName);

        Category category = categories.get(position);
        textViewName.setText(category.getCategoryName());

        return listViewItem;
    }
}