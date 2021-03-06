package com.sstechcanada.todo.activities;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.cardview.widget.CardView;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sstechcanada.todo.R;
import com.sstechcanada.todo.activities.auth.LoginActivity;
import com.sstechcanada.todo.adapters.GridViewAdapter;
import com.sstechcanada.todo.custom_views.GridItemView;
import com.sstechcanada.todo.data.TodoListContract;
import com.sstechcanada.todo.data.TodoListDbHelper;
import com.sstechcanada.todo.databinding.ActivityAddOrEditTaskBinding;
import com.sstechcanada.todo.models.Category;
import com.sstechcanada.todo.models.TodoTask;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import static com.sstechcanada.todo.data.TodoListContract.TodoListEntry.COLUMN_CATEGORY;

public class AddOrEditTaskActivity extends AppCompatActivity {
    private static final String TAG = AddOrEditTaskActivity.class.getSimpleName();
    //To Convert String to Array or Array to String
    public static String strSeparator = "__,__";
    private static String[] numbers = new String[20];
    List<Category> categories;
    DatabaseReference databaseCategories;
    CardView addCategories;
    ChipGroup chipGroup;
    ProgressBar progressBar;
    LinearLayout addMoreCategories;
    private ActivityAddOrEditTaskBinding mBinding;
    private int mTaskId = -1;
    private String mAddOrEdit;
    private GridView gridView;
    private GridViewAdapter adapter;
    private ArrayList<String> selectedStrings;
    private int category_count = 0, chip_count;
    private String selectedResult = "";
    private TodoTask todoTaskToAddOrEdit;
    private TextView tv, noOfCat, addMoreCat, toolBarTitle;
    private AppCompatImageView toolbar_profile;
    private FirebaseAuth mAuth;
    String userID;

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
        String[] arr={};
        if(str.length() != 0){arr = str.split(strSeparator);}
        return arr;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_or_edit_task);

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar_profile = findViewById(R.id.profile_toolbar);
        toolbar_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AddOrEditTaskActivity.this, LoginActivity.class));
            }
        });
        toolBarTitle = findViewById(R.id.toolbarTitle);
        toolBarTitle.setText("Add/Update Task");

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();


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
                mBinding.rbHighPriority.setChecked(true);
                mBinding.rbNoDueDate.setChecked(true);
            } else {
                todoTaskToAddOrEdit = bundle.getParcelable(getString(R.string.intent_todo_key));
                mTaskId = todoTaskToAddOrEdit.getId();
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
            mTaskId = savedInstanceState.getInt(getString(R.string.id_key));
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
            mBinding.btnAddOrUpdateTask.setText(R.string.add_task);
            mBinding.tvCompletionLabel.setVisibility(View.INVISIBLE);
            mBinding.cbTaskCompleted.setVisibility(View.INVISIBLE);
        } else {
            mBinding.btnAddOrUpdateTask.setText(R.string.update_task);
        }
        chipGroup = findViewById(R.id.chipGroup);
        addCategories = findViewById(R.id.addCategories);
        addCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(AddOrEditTaskActivity.this,
//                        SelectCategoriesDailog.class);
//                startActivity(intent);

                selectCategoriesAlert();
            }
        });

        //Grid View End
        if (todoTaskToAddOrEdit != null) {
            TodoListDbHelper todoListDbHelper = new TodoListDbHelper(AddOrEditTaskActivity.this);
            ArrayList<HashMap<String, String>> userlist = todoListDbHelper.getUser(mTaskId);
            String record[];
            for (HashMap<String, String> user : userlist) {
                selectedResult = user.get(COLUMN_CATEGORY);
                record = convertStringToArray(selectedResult);
                category_count = record.length;
                display_categories(record);
            }
        }

        MobileAds.initialize(this, initializationStatus -> {
        });

    }

    private void selectPriorityRadioButton(int priority) {
        switch (priority) {
            case TodoTask.HIGH_PRIORITY:
                mBinding.rbHighPriority.setChecked(true);
                break;
            case TodoTask.MEDIUM_PRIORITY:
                mBinding.rbMediumPriority.setChecked(true);
                break;
            case TodoTask.LOW_PRIORITY:
                mBinding.rbLowPriority.setChecked(true);
        }
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
        outState.putInt(getString(R.string.id_key), mTaskId);
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
            Toast.makeText(this, getString(R.string.description_cannot_be_empty), Toast.LENGTH_SHORT).show();
        }
        else if (chipGroup.getChildCount() == 0){
            Toast.makeText(this, getString(R.string.category_cannot_be_empty), Toast.LENGTH_SHORT).show();
        }else {
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

            TodoTask todoTask = new TodoTask(description, selectedResult, category_count, priority, dueDate, mTaskId, isCompleted);

            insertOrUpdate(todoTask);

            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
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

        Log.d(TAG, todoTask.getDueDate() + "");

        if (mAddOrEdit.equals(getString(R.string.add_new_task))) {
            getContentResolver().insert(TodoListContract.TodoListEntry.CONTENT_URI, contentValues);

        } else {
            Uri uri = TodoListContract.TodoListEntry.CONTENT_URI.buildUpon().appendPath(id).build();
            getContentResolver().update(uri, contentValues, "_id=?", new String[]{id});
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
        loadCategories();
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false);
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                Toast.makeText(getBaseContext(), "Cancel clicked", Toast.LENGTH_SHORT).show();
            }
        });
        alert.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedResult = convertArrayToString(selectedStrings);
                category_count = selectedStrings.size();
//                if (!selectedResult.equals("")) {
                String record[] = convertStringToArray(selectedResult);
                // Calling Display Category
                display_categories(record);
//                }
                if (todoTaskToAddOrEdit != null) {

                    todoTaskToAddOrEdit.setCategory(selectedResult);
                    todoTaskToAddOrEdit.setCategory_count(category_count);
                }
                loadAd();
            }
        });

        addMoreCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AddOrEditTaskActivity.this, AddCategoryActivity.class));
            }
        });


        AlertDialog dialog = alert.create();
        dialog.show();
    }

    public void loadCategories() {
        databaseCategories = FirebaseDatabase.getInstance().getReference(userID).child("benefits");
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

                adapter = new GridViewAdapter(categories, AddOrEditTaskActivity.this);
                gridView.setAdapter(adapter);
                gridView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                addMoreCategories.setVisibility(View.VISIBLE);
//                //creating adapter
//                CategoryAdapter categotyAdapter = new CategoryAdapter(AddCategoryActivity.this, categories);
//                //attaching adapter to the listview
//                listViewCategory.setAdapter(categotyAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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

//                Toast.makeText(AddOrEditTaskActivity.this, convertArrayToString(selectedStrings), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadAd() {
        InterstitialAd mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.interstitial_ad_unit_test_id));
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mInterstitialAd.show();
            }
        });
    }

    void display_categories(String record[]) {
        int[] colors = new int[] {
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
        if (chip_count == 0){
            noOfCat.setText(chip_count + " Benefits Selected");
            return;
        }

        for (int i = 0; i < chip_count; i++) {
            Chip chip = new Chip(this);
            ChipDrawable drawable = ChipDrawable.createFromAttributes(this, null, 0, R.style.Widget_MaterialComponents_Chip_Choice);
            chip.setChipDrawable(drawable);
            if(i>=0 && i<10){
                chip.setChipBackgroundColorResource(colors[i]);
            }else if(i>=10){
                chip.setChipBackgroundColorResource(colors[i%10]);
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

}
