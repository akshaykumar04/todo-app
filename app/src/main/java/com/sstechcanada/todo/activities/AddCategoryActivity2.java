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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.sstechcanada.todo.R;
import com.sstechcanada.todo.activities.auth.LoginActivity;
import com.sstechcanada.todo.adapters.CategoryAdapter;
import com.sstechcanada.todo.models.Category;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class AddCategoryActivity2 extends AppCompatActivity {

    //view objects
    EditText editTextName;
    Button buttonAddCategory;
    ListView listViewCategory;
    TextView toolBarTitle;
    private AppCompatImageView toolbar_profile, toolbarBackIcon;

    List<Category> categories;
//    DatabaseReference databaseCategories;
    String userID;
    private FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference benefitCollectionRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        mAuth = FirebaseAuth.getInstance();
        userID=mAuth.getCurrentUser().getUid();
        benefitCollectionRef=db.collection("Users").document(userID).collection("Benefits");


//        databaseCategories = FirebaseDatabase.getInstance().getReference("categories");
//        databaseCategories = FirebaseDatabase.getInstance().getReference(userID).child("benefits");

        editTextName = findViewById(R.id.editTextName);
        listViewCategory = findViewById(R.id.listViewCategory);
        toolbarBackIcon = findViewById(R.id.arrow_back);
        toolbarBackIcon.setVisibility(View.VISIBLE);
        toolbarBackIcon.setOnClickListener(view -> {
            super.onBackPressed();
        });


        buttonAddCategory = findViewById(R.id.buttonAddCategory);

        categories = new ArrayList<>();

        toolBarTitle = findViewById(R.id.toolbarTitle);
        toolBarTitle.setText("Add Benefits");

        toolbar_profile = findViewById(R.id.profile_toolbar);
        toolbar_profile.setOnClickListener(view -> startActivity(new Intent(AddCategoryActivity2.this, LoginActivity.class)));

        buttonAddCategory.setOnClickListener(view -> addCategory());

        listViewCategory.setOnItemClickListener((adapterView, view, i, l) -> {

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
        });

        listViewCategory.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Category category = categories.get(i);
                showUpdateDialog(category.getCategoryId(), category.getCategoryName());
                return true;
            }
        });

        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
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

            DocumentReference documentReferenceCurrentReference=benefitCollectionRef.document();
            Map<String,String> category =new HashMap<>();
            category.put("category_name",name);


            documentReferenceCurrentReference.set(category, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toasty.success(getApplicationContext(), "Benefit added", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toasty.error(getApplicationContext(), "Benefit addition Failed: ", Toast.LENGTH_LONG).show();
                }
            });

            //getting a unique id using push().getKey() method
            //it will create a unique id and we will use it as the Primary Key for our Category
//            String id = databaseCategories.push().getKey();
//
//            //Making first word capital
//            name = name.substring(0, 1).toUpperCase() + name.substring(1);
//            //creating an Category Object
//            Category category = new Category(id, name);
//
//            //Saving the Category
//            databaseCategories.child(id).setValue(category);

            //setting edittext to blank again
            editTextName.setText("");

            //displaying a success toast

        } else {
            //if the value is not given displaying a toast
            Toasty.warning(this, "Please enter a name", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        //attaching value event listener
       benefitCollectionRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                categories.clear();

                //iterating through all the nodes
                benefitCollectionRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(DocumentSnapshot dataSnapshot:queryDocumentSnapshots){
                            Category category = new Category(dataSnapshot.getId(),(String) dataSnapshot.get("category_name"));
                            categories.add(category);
                        }
                        CategoryAdapter categotyAdapter = new CategoryAdapter(AddCategoryActivity2.this, categories);
                        //attaching adapter to the listview
                        listViewCategory.setAdapter(categotyAdapter);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toasty.error(getApplicationContext(), "Benefit fetch failed ", Toast.LENGTH_LONG).show();
                    }
                });

            }
        });

       //clearing the previous category list

//                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
//                    Category category = postSnapshot.getValue(Category.class);
//                    categories.add(category);
//                }

                //creating adapter

    }

    private void showUpdateDialog(final String categoryId, String categoryName) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.update_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextName = dialogView.findViewById(R.id.editTextName);
        final Button buttonUpdate = dialogView.findViewById(R.id.buttonUpdateCategory);
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

        DocumentReference documentReferenceBenefitReference=benefitCollectionRef.document(id);

//        DatabaseReference dR = FirebaseDatabase.getInstance().getReference("categories").child(id);
        //updating category
        Category category = new Category(id, name);
        documentReferenceBenefitReference.update("category_name",name);
        Toasty.success(getApplicationContext(), "Benefits Updated", Toast.LENGTH_SHORT).show();
    }


    private void deleteCategory(String id) {
        //getting the specified category reference
        DocumentReference documentReferenceBenefitReference=benefitCollectionRef.document(id);
        //removing category
        documentReferenceBenefitReference.delete();

        //getting the tracks reference for the specified category
        Toasty.error(getApplicationContext(), "Benefits Deleted", Toast.LENGTH_SHORT).show();

    }


}