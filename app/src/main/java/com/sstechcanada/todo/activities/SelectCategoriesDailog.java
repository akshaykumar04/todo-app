package com.sstechcanada.todo.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sstechcanada.todo.R;
import com.sstechcanada.todo.adapters.GridViewAdapter;
import com.sstechcanada.todo.custom_views.GridItemView;
import com.sstechcanada.todo.models.Category;

import java.util.ArrayList;
import java.util.List;

public class SelectCategoriesDailog extends AppCompatActivity {

    private GridView gridView;
    private static final int EDIT_TASK_REQUEST = 2;
    private GridViewAdapter adapter;
    private ArrayList<String> selectedStrings;
    List<Category> categories;
    DatabaseReference databaseCategories;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_categories_dailog);
        gridView = findViewById(R.id.grid_view);


        databaseCategories = FirebaseDatabase.getInstance().getReference("categories");
        categories = new ArrayList<>();
        selectedStrings = new ArrayList<>();
        databaseCategories.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //clearing the previous category list
                categories.clear();

                //iterating through all the nodes
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Category category = postSnapshot.getValue(Category.class);
                    categories.add(category);
                }

                adapter = new GridViewAdapter(categories, SelectCategoriesDailog.this);
                gridView.setAdapter(adapter);
//                //creating adapter
//                CategoryAdapter categotyAdapter = new CategoryAdapter(AddCategoryActivity.this, categories);
//                //attaching adapter to the listview
//                listViewCategory.setAdapter(categotyAdapter);
            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                int selectedIndex = adapter.selectedPositions.indexOf(position);
                if (selectedIndex > -1) {
                    adapter.selectedPositions.remove(selectedIndex);
                    ((GridItemView) v).display(false);
                    selectedStrings.remove((String) parent.getItemAtPosition(position));
                } else {
                    adapter.selectedPositions.add(position);
                    ((GridItemView) v).display(true);
                    selectedStrings.add((String) parent.getItemAtPosition(position));
                }
            }
        });


    }



}