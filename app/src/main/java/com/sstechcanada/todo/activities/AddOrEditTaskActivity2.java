package com.sstechcanada.todo.activities;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.cardview.widget.CardView;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.sstechcanada.todo.R;
import com.sstechcanada.todo.activities.auth.LoginActivity;
import com.sstechcanada.todo.adapters.GridViewAdapter;
import com.sstechcanada.todo.custom_views.GridItemView;
import com.sstechcanada.todo.data.TodoListContract;
import com.sstechcanada.todo.data.TodoListDbHelper;
import com.sstechcanada.todo.databinding.ActivityAddOrEditTaskBinding;
import com.sstechcanada.todo.models.Category;
import com.sstechcanada.todo.models.TodoTask;
import com.sstechcanada.todo.models.TodoTaskFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import es.dmoral.toasty.Toasty;

import static com.sstechcanada.todo.data.TodoListContract.TodoListEntry.COLUMN_CATEGORY;

public class AddOrEditTaskActivity2 extends AppCompatActivity {
    private static final String TAG = AddOrEditTaskActivity2.class.getSimpleName();
    private static final String[] numbers = new String[20];
    //To Convert String to Array or Array to String
    public static String strSeparator = ", ";
    List<Category> categories;
    DatabaseReference databaseCategories;
    CardView addCategories;
    ChipGroup chipGroup;
    ProgressBar progressBar;
    LinearLayout addMoreCategories;
    String userID;
    private ActivityAddOrEditTaskBinding mBinding;
    private String mTaskId = "-1";
    private String mAddOrEdit;
    private GridView gridView;
    private GridViewAdapter adapter;
    private ArrayList<String> selectedStrings;
    private int category_count = 0, chip_count;
    private String selectedResult = "";
    private TodoTaskFirestore todoTaskToAddOrEdit;
    private TextView tv, noOfCat, addMoreCat, toolBarTitle;
    private AppCompatImageView toolbar_profile, toolbarBackIcon;
    private FirebaseAuth mAuth;
    //For Delete Status
    private int status;;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference benefitCollectionRef;
    private String[] record;


    public static String convertArrayToString(ArrayList<String> array) {
        String str = "";
        for (int i = 0; i < array.size(); i++) {
            str = str + array.get(i);
            // Do not append comma at the end of last element
            if (i < array.size() - 1) {
                str = str + strSeparator;
            }
        }
        return str;
    }

    public static String[] convertStringToArray(String str) {
        String[] arr = {};
        if (str.length() != 0) {
            arr = str.split(strSeparator);
        }
        return arr;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_or_edit_task);

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar_profile = findViewById(R.id.profile_toolbar);
        toolbar_profile.setOnClickListener(view -> startActivity(new Intent(AddOrEditTaskActivity2.this, LoginActivity.class)));
        toolbarBackIcon = findViewById(R.id.arrow_back);
        toolbarBackIcon.setVisibility(View.VISIBLE);
        toolbarBackIcon.setOnClickListener(view -> {
            super.onBackPressed();
        });
        toolBarTitle = findViewById(R.id.toolbarTitle);
        toolBarTitle.setText("Add/Update Task");

        mAuth = FirebaseAuth.getInstance();
        userID=mAuth.getCurrentUser().getUid();
        benefitCollectionRef=db.collection("Users").document(userID).collection("Benefits");

        loadBannerAd();

        long dueDate;
        int taskCompleted;
        tv = findViewById(R.id.tv);
        noOfCat = findViewById(R.id.tv_category_number);
        addMoreCat = findViewById(R.id.tv_add_more);
        if (savedInstanceState == null) {
            Bundle bundle = getIntent().getExtras();
            mAddOrEdit = bundle.getString(getString(R.string.intent_adding_or_editing_key));

            if (mAddOrEdit.equals(getString(R.string.add_new_task))) {
                // when adding a task, default to high priority and no due date
//               ADDING NEW TASK
                mBinding.rbHighPriority.setChecked(true);
                mBinding.rbNoDueDate.setChecked(true);
            } else {
                todoTaskToAddOrEdit = bundle.getParcelable(getString(R.string.intent_todo_key));
                mTaskId = todoTaskToAddOrEdit.getDocumentID();
                mBinding.etTaskDescription.setText(todoTaskToAddOrEdit.getDescription());
                selectPriorityRadioButton(todoTaskToAddOrEdit.getPriority());

                dueDate = todoTaskToAddOrEdit.getDueDate();
                Log.d(TAG, "Due date in millis " + dueDate);
                if (dueDate == TodoTask.NO_DUE_DATE) {
                    mBinding.rbNoDueDate.setChecked(true);
                } else {
                    mBinding.rbSelectDueDate.setChecked(true);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(dueDate);
                    mBinding.dpDueDate.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
                }

                taskCompleted = todoTaskToAddOrEdit.getCompleted();
                mBinding.cbTaskCompleted.setChecked(taskCompleted == TodoTask.TASK_COMPLETED);
            }
        } else {
            mAddOrEdit = savedInstanceState.getString(getString(R.string.add_or_edit_key));
            mTaskId = savedInstanceState.getString(getString(R.string.id_key));
            mBinding.etTaskDescription.setText(savedInstanceState.getString(getString(R.string.task_description_key)));
            selectPriorityRadioButton(savedInstanceState.getInt(getString(R.string.priority_key)));
            boolean noDueDate = savedInstanceState.getBoolean(getString(R.string.no_due_date_key));

            if (noDueDate) {
                mBinding.rbNoDueDate.setChecked(true);
            } else {
                mBinding.rbSelectDueDate.setChecked(true);
            }
            mBinding.dpDueDate.updateDate(savedInstanceState.getInt(getString(R.string.year_key)),
                    savedInstanceState.getInt(getString(R.string.month_key)),
                    savedInstanceState.getInt(getString(R.string.day_key)));

            mBinding.cbTaskCompleted.setChecked(savedInstanceState.getBoolean(getString(R.string.completed_key)));
        }
        setTitle(mAddOrEdit);

        if (mAddOrEdit.equals(getString(R.string.add_new_task))) {
//            ADDING NEW TASK
            mBinding.btnAddOrUpdateTask.setText(R.string.add_task);
            mBinding.tvCompletionLabel.setVisibility(View.INVISIBLE);
            mBinding.cbTaskCompleted.setVisibility(View.INVISIBLE);
        } else {
            mBinding.btnAddOrUpdateTask.setText(R.string.update_task);
        }
        chipGroup = findViewById(R.id.chipGroup);
        addCategories = findViewById(R.id.addCategories);
        addCategories.setOnClickListener(view -> {
//                Intent intent = new Intent(AddOrEditTaskActivity.this,
//                        SelectCategoriesDailog.class);
//                startActivity(intent);

            selectCategoriesAlert();
        });

        //Grid View End
        if (todoTaskToAddOrEdit != null) {
//            TodoListDbHelper todoListDbHelper = new TodoListDbHelper(AddOrEditTaskActivity2.this);
//            ArrayList<HashMap<String, String>> userlist = todoListDbHelper.getUser(mTaskId);
            String[] record;
//            for (HashMap<String, String> user : userlist) {
//                selectedResult = user.get(COLUMN_CATEGORY);
                record = convertStringToArray(todoTaskToAddOrEdit.getBenefitsString());
                category_count = record.length;
                display_categories(record);
//            }
        }

        MobileAds.initialize(this, initializationStatus -> {
        });

    }

    private void loadBannerAd() {
        AdView adView = mBinding.adView;
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

    }

    private void selectPriorityRadioButton(int priority) {
//        switch (priority) {
//            case TodoTask.HIGH_PRIORITY:
//                mBinding.rbHighPriority.setChecked(true);
//                break;
//            case TodoTask.MEDIUM_PRIORITY:
//                mBinding.rbMediumPriority.setChecked(true);
//                break;
//            case TodoTask.LOW_PRIORITY:
//                mBinding.rbLowPriority.setChecked(true);
//        }
    }

//    private void addChip(String pItem, ChipGroup pChipGroup) {
//        Chip lChip = new Chip(this);
//        lChip.setText(pItem);
//        lChip.setTextColor(getResources().getColor(R.color.colorAccent));
//        lChip.setChipBackgroundColor(getResources().getColorStateList(R.color.design_default_color_primary));
//
//        pChipGroup.addView(lChip, pChipGroup.getChildCount() - 1);
//    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // save values on device rotation
        outState.putString(getString(R.string.task_description_key), mBinding.etTaskDescription.getText().toString());
        int priority = TodoTask.HIGH_PRIORITY;
        if (mBinding.rbMediumPriority.isChecked()) {
            priority = TodoTask.MEDIUM_PRIORITY;
        } else if (mBinding.rbLowPriority.isChecked()) {
            priority = TodoTask.LOW_PRIORITY;
        }

        outState.putInt(getString(R.string.priority_key), priority);
        outState.putBoolean(getString(R.string.no_due_date), mBinding.rbNoDueDate.isChecked());
        outState.putInt(getString(R.string.year_key), mBinding.dpDueDate.getYear());
        outState.putInt(getString(R.string.month_key), mBinding.dpDueDate.getMonth());
        outState.putInt(getString(R.string.day_key), mBinding.dpDueDate.getDayOfMonth());
        outState.putBoolean(getString(R.string.completed_key), mBinding.cbTaskCompleted.isChecked());
        outState.putString(getString(R.string.add_or_edit_key), mAddOrEdit);
        outState.putString(getString(R.string.id_key), mTaskId);
//        outState.putString("category", selectedResult);
        super.onSaveInstanceState(outState);
    }

    public void addOrUpdateTask(View view) {
        String description = mBinding.etTaskDescription.getText().toString().trim();
        int priority = category_count;
        int isCompleted = TodoTask.TASK_NOT_COMPLETED;
        long dueDate = TodoTask.NO_DUE_DATE;
        Log.d(TAG, "Here");

        if (description.equals("")) {
            Toasty.warning(this, getString(R.string.description_cannot_be_empty), Toast.LENGTH_SHORT, true).show();
        } else if (chipGroup.getChildCount() == 0) {
            Toasty.warning(this, getString(R.string.category_cannot_be_empty), Toast.LENGTH_SHORT, true).show();
        } else {
            //Making First Char Capital
            description = description.substring(0, 1).toUpperCase() + description.substring(1);

            // get the priority setting
            if (mBinding.rbMediumPriority.isChecked()) {
                priority = TodoTask.MEDIUM_PRIORITY;
            } else if (mBinding.rbLowPriority.isChecked()) {
                priority = TodoTask.LOW_PRIORITY;
            }

            // get the due date, if one has been selected
            if (mBinding.rbSelectDueDate.isChecked()) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(mBinding.dpDueDate.getYear(), mBinding.dpDueDate.getMonth(), mBinding.dpDueDate.getDayOfMonth(), 0, 0, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                dueDate = calendar.getTimeInMillis();
                Log.d(TAG, "millis = " + dueDate);
            }

            if (mBinding.cbTaskCompleted.isChecked()) {
                isCompleted = TodoTask.TASK_COMPLETED;
            } else {
                isCompleted = TodoTask.TASK_NOT_COMPLETED;
            }
//          aDDING TO SQLITE
//            TodoTask todoTask = new TodoTask(description, selectedResult, category_count, priority, dueDate, mTaskId, isCompleted);
//            insertOrUpdate(todoTask);
//
            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_OK, returnIntent);

            if (mAddOrEdit.equals(getString(R.string.add_new_task)) || isCompleted == 0) {
                finish();
            }
        }

    }

    private void insertOrUpdate(TodoTask todoTask) {
        // I used to have this functionality in TodoListActivity's onActivityResult method, but
        // then I couldn't reach it when editing a task directly from the App Widget
        String id = String.valueOf(todoTask.getId());
        ContentValues contentValues = new ContentValues();
        contentValues.put(TodoListContract.TodoListEntry.COLUMN_DESCRIPTION, todoTask.getDescription());
        contentValues.put(TodoListContract.TodoListEntry.COLUMN_PRIORITY, todoTask.getPriority());
        contentValues.put(TodoListContract.TodoListEntry.COLUMN_DUE_DATE, todoTask.getDueDate());
        contentValues.put(TodoListContract.TodoListEntry.COLUMN_COMPLETED, todoTask.getCompleted());
        contentValues.put(TodoListContract.TodoListEntry.COLUMN_CATEGORY, todoTask.getCategory());
        contentValues.put(TodoListContract.TodoListEntry.COLUMN_CATEGORY_COUNT, todoTask.getCategory_count());
        status = todoTask.getCompleted();

        Log.d(TAG, todoTask.getDueDate() + "");

        if (mAddOrEdit.equals(getString(R.string.add_new_task))) {
            getContentResolver().insert(TodoListContract.TodoListEntry.CONTENT_URI, contentValues);

        } else {
            Uri uri = TodoListContract.TodoListEntry.CONTENT_URI.buildUpon().appendPath(id).build();
            getContentResolver().update(uri, contentValues, "_id=?", new String[]{id});
            if (todoTask.getCompleted() == 1) {
                todoDeleteDialog(todoTask, id);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //attaching value event listener

    }

    public void selectCategoriesAlert() {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.activity_select_categories_dailog, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Select Benefits");
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        gridView = alertLayout.findViewById(R.id.grid_view_alert);
        addMoreCategories = alertLayout.findViewById(R.id.addMoreCategoriesLayout);
        progressBar = alertLayout.findViewById(R.id.progress_circular);
        AdView bannerAd = alertLayout.findViewById(R.id.adView);
        loadCategories();
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false);
        alert.setNegativeButton("Cancel", (dialog, which) -> {
//                Toast.makeText(getBaseContext(), "Cancel clicked", Toast.LENGTH_SHORT).show();
        });
        alert.setPositiveButton("Done", (dialog, which) -> {
            selectedResult = convertArrayToString(selectedStrings);
            category_count = selectedStrings.size();
//                if (!selectedResult.equals("")) {
            record = convertStringToArray(selectedResult);
            // Calling Display Category
            display_categories(record);
//                }
            if (todoTaskToAddOrEdit != null) {

                todoTaskToAddOrEdit.setBenefitsString(selectedResult);
//                todoTaskToAddOrEdit.setCategory_count();
            }
//            loadAd(); //disabled full screen ads as requested
        });

        addMoreCategories.setOnClickListener(view -> startActivity(new Intent(AddOrEditTaskActivity2.this, AddCategoryActivity2.class)));

        bannerAd.loadAd(new AdRequest.Builder().build());

        AlertDialog dialog = alert.create();
        dialog.show();
    }

    public void loadCategories() {
//        databaseCategories = FirebaseDatabase.getInstance().getReference("categories");
        databaseCategories = FirebaseDatabase.getInstance().getReference(userID).child("benefits");
        categories = new ArrayList<>();
        selectedStrings = new ArrayList<>();

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

                        adapter = new GridViewAdapter(categories, AddOrEditTaskActivity2.this);
                        gridView.setAdapter(adapter);
                        gridView.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                        addMoreCategories.setVisibility(View.VISIBLE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toasty.error(getApplicationContext(), "Benefit fetch failed ", Toast.LENGTH_LONG).show();
                    }
                });

            }
        });


        gridView.setOnItemClickListener((parent, v, position, id) -> {
            int selectedIndex = adapter.selectedPositions.indexOf(position);
            if (selectedIndex > -1) {
                adapter.selectedPositions.remove(selectedIndex);
                ((GridItemView) v).display(false);
                selectedStrings.remove(parent.getItemAtPosition(position));
            } else {
                adapter.selectedPositions.add(position);
                ((GridItemView) v).display(true);
                selectedStrings.add((String) parent.getItemAtPosition(position));
            }

//                Toast.makeText(AddOrEditTaskActivity.this, convertArrayToString(selectedStrings), Toast.LENGTH_SHORT).show();
        });

    }

//    private void loadAd() {
//
//        AdRequest adRequest = new AdRequest.Builder().build();
//        InterstitialAd.load(this, getResources().getString(R.string.interstitial_ad_unit_test_id), adRequest, new InterstitialAdLoadCallback() {
//            @Override
//            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
//                super.onAdLoaded(interstitialAd);
//                mInterstitialAd = interstitialAd;
//                mInterstitialAd.show(AddOrEditTaskActivity.this);
//            }
//        });
//
//    }

    void display_categories(String[] record) {
        int[] colors = new int[]{
                R.color.chip_1,
                R.color.chip_2,
                R.color.chip_3,
                R.color.chip_4,
                R.color.chip_5,
                R.color.chip_6,
                R.color.chip_7,
                R.color.chip_8,
                R.color.chip_9,
                R.color.chip_10
        };
        chipGroup.removeAllViews();
        chipGroup.setVisibility(View.VISIBLE);
        chip_count = record.length;
        addMoreCat.setText("Click here to add more Benefits");
        if (chip_count == 0) {
            noOfCat.setText(chip_count + " Benefits Selected");
            return;
        }

        for (int i = 0; i < chip_count; i++) {
            Chip chip = new Chip(this);
            ChipDrawable drawable = ChipDrawable.createFromAttributes(this, null, 0, R.style.Widget_MaterialComponents_Chip_Choice);
            chip.setChipDrawable(drawable);
            if (i >= 0 && i < 10) {
                chip.setChipBackgroundColorResource(colors[i]);
            } else if (i >= 10) {
                chip.setChipBackgroundColorResource(colors[i % 10]);
            }
            chip.setText(record[i] + "");
            chipGroup.getChildCount();
            chip.getChipStartPadding();
            chip.getChipEndPadding();
            chip.setTextAppearanceResource(R.style.SmallerText);
            chipGroup.addView(chip);
            noOfCat.setText(chip_count + " Benefits Selected");
        }

    }

    private void todoDeleteDialog(TodoTask todoTask, String tid) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Task will be deleted");
        alert.setMessage("Marking this task as complete will permanently delete this task. \nAre you sure?");
        alert.setCancelable(false);
        alert.setPositiveButton(
                "Yes",
                (dialog, id) -> {
                    deleteTodo(tid);
                }
        );
        alert.setNegativeButton(
                "No",
                (dialog, id) -> {
                    todoTask.setCompleted(0);
                    insertOrUpdate(todoTask);
                    dialog.dismiss();
                }
        );
        alert.show();
    }

    private void deleteTodo(String tid) {
        TodoListDbHelper todoListDbHelper = new TodoListDbHelper(AddOrEditTaskActivity2.this);
        todoListDbHelper.deleteTodo(tid);
        Toasty.error(this, "Task Deleted", Toast.LENGTH_SHORT, true).show();
        finish();
    }

}
