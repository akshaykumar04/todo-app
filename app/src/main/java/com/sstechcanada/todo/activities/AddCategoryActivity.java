package com.sstechcanada.todo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sstechcanada.todo.R;
import com.sstechcanada.todo.activities.auth.LoginActivity;
import com.sstechcanada.todo.adapters.CategoryAdapter;
import com.sstechcanada.todo.models.Category;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class AddCategoryActivity extends AppCompatActivity {

    //view objects
    EditText editTextName;
    Button buttonAddCategory;
    ListView listViewCategory;
    TextView toolBarTitle;
    private AppCompatImageView toolbar_profile;

    List<Category> categories;

    DatabaseReference databaseCategories;
    String userID;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();

//        databaseCategories = FirebaseDatabase.getInstance().getReference("categories");
        databaseCategories = FirebaseDatabase.getInstance().getReference(userID).child("benefits");

        editTextName = (EditText) findViewById(R.id.editTextName);
        listViewCategory = (ListView) findViewById(R.id.listViewCategory);

        buttonAddCategory = (Button) findViewById(R.id.buttonAddCategory);

        categories = new ArrayList<>();

        toolBarTitle = findViewById(R.id.toolbarTitle);
        toolBarTitle.setText("Add Benefits");

        toolbar_profile = findViewById(R.id.profile_toolbar);
        toolbar_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AddCategoryActivity.this, LoginActivity.class));
            }
        });

        buttonAddCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCategory();
            }
        });

        listViewCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Category category = categories.get(i);
                showUpdateDialog(category.getCategoryId(), category.getCategoryName());

//                //creating an intent
//                Intent intent = new Intent(getApplicationContext(), CategoryActivity.class);
//
//                //putting category name and id to intent
//                intent.putExtra(CATEGORY_ID, category.getCategoryId());
//                intent.putExtra(CATEGORY_NAME, category.getCategoryName());
//
//                //starting the activity with intent
//                startActivity(intent);
            }
        });

        listViewCategory.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Category category = categories.get(i);
                showUpdateDialog(category.getCategoryId(), category.getCategoryName());
                return true;
            }
        });
    }

    /*
     * This method is saving a new category to the
     * Firebase Realtime Database
     * */
    private void addCategory() {
        //getting the values to save
        String name = editTextName.getText().toString().trim();

        //checking if the value is provided
        if (!name.isEmpty()) {

            //getting a unique id using push().getKey() method
            //it will create a unique id and we will use it as the Primary Key for our Category
            String id = databaseCategories.push().getKey();

            //Making first word capital
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            //creating an Category Object
            Category category = new Category(id, name);

            //Saving the Category
            databaseCategories.child(id).setValue(category);

            //setting edittext to blank again
            editTextName.setText("");

            //displaying a success toast
            Toasty.success(this, "Benefit added", Toast.LENGTH_SHORT).show();
        } else {
            //if the value is not given displaying a toast
            Toasty.warning(this, "Please enter a name", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        //attaching value event listener
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

                //creating adapter
                CategoryAdapter categotyAdapter = new CategoryAdapter(AddCategoryActivity.this, categories);
                //attaching adapter to the listview
                listViewCategory.setAdapter(categotyAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showUpdateDialog(final String categoryId, String categoryName) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.update_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextName = (EditText) dialogView.findViewById(R.id.editTextName);
        final Button buttonUpdate = (Button) dialogView.findViewById(R.id.buttonUpdateCategory);
        final Button buttonDelete = dialogView.findViewById(R.id.buttonDeleteCategory);


        dialogBuilder.setTitle(categoryName);
        final AlertDialog b = dialogBuilder.create();
        b.show();


        buttonUpdate.setOnClickListener(view -> {
            String name = editTextName.getText().toString().trim();
            if (!TextUtils.isEmpty(name)) {
                updateCategory(categoryId, name);
                b.dismiss();
            }
        });

        buttonDelete.setOnClickListener(view -> {

            deleteCategory(categoryId);
            b.dismiss();
        });

    }


    private void updateCategory(String id, String name) {
        //getting the specified category reference
        DatabaseReference dR = FirebaseDatabase.getInstance().getReference(userID).child("benefits").child(id);
//        DatabaseReference dR = FirebaseDatabase.getInstance().getReference("categories").child(id);
        //updating category
        Category category = new Category(id, name);
        dR.setValue(category);
        Toasty.success(getApplicationContext(), "Benefits Updated", Toast.LENGTH_SHORT).show();
    }


    private void deleteCategory(String id) {
        //getting the specified category reference
        DatabaseReference dR = FirebaseDatabase.getInstance().getReference(userID).child("benefits").child(id);
//        DatabaseReference dR = FirebaseDatabase.getInstance().getReference("categories").child(id);
        //removing category
        dR.removeValue();

        //getting the tracks reference for the specified category
        Toasty.error(getApplicationContext(), "Benefits Deleted", Toast.LENGTH_SHORT).show();

    }


}