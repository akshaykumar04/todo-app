package com.sstechcanada.todo.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
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

//    @Override
//    public boolean hasStableIds() {
//        return super.hasStableIds();
//    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Nullable
    @Override
    public Category getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public int getCount() {
        return categories.size();
    }

    static class ViewHolder {
        private TextView textViewName;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(R.layout.layout_artist_list, null);
            holder = new ViewHolder();
            holder. textViewName = (TextView) convertView.findViewById(R.id.textViewName);
            convertView.setTag(holder);

        }else{
            holder = (ViewHolder) convertView.getTag();
        }

//        LayoutInflater inflater = context.getLayoutInflater();
//        @SuppressLint({"ViewHolder", "InflateParams"}) View listViewItem = inflater.inflate(R.layout.layout_artist_list, null, true);
//
//        textViewName = (TextView) listViewItem.findViewById(R.id.textViewName);

        Category category = categories.get(position);
        holder.textViewName.setText(category.getCategory_name());

        return convertView;
    }
}