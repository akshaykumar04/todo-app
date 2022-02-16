package com.sstechcanada.todo.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
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

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.sstechcanada.todo.R;
import com.sstechcanada.todo.activities.auth.LoginActivity;
import com.sstechcanada.todo.adapters.GridViewAdapter;
import com.sstechcanada.todo.custom_views.GridItemView;
import com.sstechcanada.todo.databinding.ActivityAddOrEditTaskBinding;
import com.sstechcanada.todo.models.Category;
import com.sstechcanada.todo.models.TodoTask;
import com.sstechcanada.todo.models.TodoTaskFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;

import static com.sstechcanada.todo.activities.MasterTodoListActivity.listId;
import static com.sstechcanada.todo.activities.MasterTodoListActivity.purchaseCode;

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
    String taskCompleted = "Pending";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference benefitCollectionRef;
    CollectionReference UserColRef;
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
    private int status;
    private String[] record;
    private String description;
    AlertDialog alertDialog;
    ProgressBar loadingProgressBarUpdate;
    private TextView deleteItem;
    private InterstitialAd mInterstitialAd;
    private RewardedAd mRewardedAd;


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
        loadingProgressBarUpdate = findViewById(R.id.loadingProgressBarUpdate);
        toolbarBackIcon.setVisibility(View.VISIBLE);
        toolbarBackIcon.setOnClickListener(view -> {
            super.onBackPressed();
        });
        toolBarTitle = findViewById(R.id.toolbarTitle);
        toolBarTitle.setText("Add/Update Task");

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        FirebaseCrashlytics.getInstance().log(this.getClass().getSimpleName()+"listId = "+listId);
        FirebaseCrashlytics.getInstance().log(this.getClass().getSimpleName()+"UserId = "+userID);
        benefitCollectionRef = db.collection("Users").document(userID).collection("Benefits");
        UserColRef = db.collection("Users").document(userID).collection("Lists").document(listId).collection("Todo");
        Log.i("ListId", "Add or edit: " + listId);

        if (purchaseCode.equals("0")) {
            loadBannerAds();
            loadFullScreenAds();
//            loadRewardedAds();
        }

        long dueDate;

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
                selectedResult = todoTaskToAddOrEdit.getBenefitsString();
                mTaskId = todoTaskToAddOrEdit.getDocumentID();
                mBinding.etTaskDescription.setText(todoTaskToAddOrEdit.getDescription());

                taskCompleted = todoTaskToAddOrEdit.getStatus();

                mBinding.cbTaskCompleted.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (!mBinding.cbTaskCompleted.isChecked()) {
                            new AlertDialog.Builder(AddOrEditTaskActivity2.this)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setTitle("Confirm Incomplete")
                                    .setMessage("Are you sure you want to mark this task as incomplete?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
//
                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            mBinding.cbTaskCompleted.setChecked(true);
                                        }
                                    })
                                    .show();

                        } else {

                            new AlertDialog.Builder(AddOrEditTaskActivity2.this)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setTitle("Confirm Complete")
                                    .setMessage("Are you sure you want to mark this task as completed?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (purchaseCode.equals("0")) {
                                                if (mInterstitialAd != null) {
                                                    mInterstitialAd.show(AddOrEditTaskActivity2.this);
                                                }
                                            }
                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            mBinding.cbTaskCompleted.setChecked(false);
                                        }
                                    })
                                    .show();
                        }
                    }
                });
                mBinding.cbTaskCompleted.setChecked(taskCompleted.equals("Completed"));

                if (taskCompleted.equals("Completed")) {
                    mBinding.timestampCompletedtextView.setText(todoTaskToAddOrEdit.getTimestampCompleted());
                    mBinding.timestampCompletedtextView.setVisibility(View.VISIBLE);
                }

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

            if (taskCompleted.equals("Completed")) {
                mBinding.cbTaskCompleted.setChecked(true);
                mBinding.timestampCompletedtextView.setText(todoTaskToAddOrEdit.getTimestampCompleted());
            } else {
                mBinding.cbTaskCompleted.setChecked(false);
            }


        }
        setTitle(mAddOrEdit);

        if (mAddOrEdit.equals(getString(R.string.add_new_task))) {
//            ADDING NEW TASK
            mBinding.btnAddOrUpdateTask.setText(R.string.add_task);
            mBinding.tvCompletionLabel.setVisibility(View.INVISIBLE);
            mBinding.cbTaskCompleted.setVisibility(View.INVISIBLE);
            mBinding.deleteTodoItem.setVisibility(View.INVISIBLE);
        } else {
            mBinding.btnAddOrUpdateTask.setText(R.string.update_task);
            mBinding.deleteTodoItem.setVisibility(View.VISIBLE);
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
//            String[] record;
//            for (HashMap<String, String> user : userlist) {
//                selectedResult = user.get(COLUMN_CATEGORY);
            record = convertStringToArray(todoTaskToAddOrEdit.getBenefitsString());
            category_count = record.length;
            display_categories(record);
//            }
        }

        MobileAds.initialize(this, initializationStatus -> {
        });

        deleteItem = findViewById(R.id.deleteTodoItem);
        deleteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteTodoItem();
            }
        });

    }

    private void deleteTodoItem() {
        loadFullScreenAds();
//       if (purchaseCode.equals("0")) {
//           new AlertDialog.Builder(this)
//                   .setIcon(android.R.drawable.ic_menu_delete)
//                   .setTitle("Confirm Delete")
//                   .setMessage("In this free version of the app, an ad appears when you delete a list item. Continue?")
//                   .setPositiveButton("Yes", (dialog, which) -> {
//                       if (purchaseCode.equals("0")) {
//                           if (mRewardedAd != null) {
//                               Activity activityContext = AddOrEditTaskActivity2.this;
//                               mRewardedAd.show(activityContext, rewardItem -> {
//                                   // Handle the reward.
//                                   Log.d(TAG, "The user earned the reward.");
//                                   UserColRef.document(todoTaskToAddOrEdit.getDocumentID()).delete();
//                                   Toasty.error(AddOrEditTaskActivity2.this, "Task Deleted",Toast.LENGTH_SHORT).show();
//                               });
//                           } else {
//                               Log.d(TAG, "The rewarded ad wasn't ready yet.");
//                               Toasty.info(AddOrEditTaskActivity2.this, "Please wait while the Ad is loading",Toast.LENGTH_SHORT).show();
//                           }
//                       } else {
//                           UserColRef.document(todoTaskToAddOrEdit.getDocumentID()).delete();
//                           finish();
//                       }
//                   })
//                   .setNegativeButton("No", null)
//                   .show();
//       } else {
           new AlertDialog.Builder(this)
                   .setIcon(android.R.drawable.ic_menu_delete)
                   .setTitle("Confirm Delete")
                   .setMessage("Are you sure you want to delete this task?")
                   .setPositiveButton("Yes", (dialog, which) -> {
                       if (purchaseCode.equals("0")) {
                           if (mInterstitialAd != null) {
                               mInterstitialAd.show(AddOrEditTaskActivity2.this);
                               UserColRef.document(todoTaskToAddOrEdit.getDocumentID()).delete();
                               Toasty.error(AddOrEditTaskActivity2.this, "Task Deleted",Toast.LENGTH_SHORT).show();
                               finish();
                           } else {
                               UserColRef.document(todoTaskToAddOrEdit.getDocumentID()).delete();
                               Toasty.error(AddOrEditTaskActivity2.this, "Task Deleted",Toast.LENGTH_SHORT).show();
                               finish();
                           }

                       } else {
                           UserColRef.document(todoTaskToAddOrEdit.getDocumentID()).delete();
                           Toasty.error(AddOrEditTaskActivity2.this, "Task Deleted",Toast.LENGTH_SHORT).show();
                           finish();
                       }

                   })
                   .setNegativeButton("No", null)
                   .show();
//       }
    }

    private void loadBannerAds() {

        AdView adView = findViewById(R.id.adView);
        if (purchaseCode.equals("0")) {
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        } else {
            adView.setVisibility(View.GONE);
        }


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
        loadingProgressBarUpdate.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        description = mBinding.etTaskDescription.getText().toString().trim();
        int priority = category_count;
        int isCompleted = TodoTask.TASK_NOT_COMPLETED;
        long dueDate = TodoTask.NO_DUE_DATE;

        Log.d(TAG, "Here");

        if (description.equals("")) {
            Toasty.warning(this, getString(R.string.description_cannot_be_empty), Toast.LENGTH_SHORT, true).show();
            loadingProgressBarUpdate.setVisibility(View.GONE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
//        } else if (chipGroup.getChildCount() == 0) {
//            Toasty.warning(this, getString(R.string.category_cannot_be_empty), Toast.LENGTH_SHORT, true).show();
        } else {
            //Making First Char Capital
            description = description.substring(0, 1).toUpperCase() + description.substring(1);
            uploadDataToFirestore();

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
//            uploadDataToFirestore();


            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_OK, returnIntent);

//            if (mAddOrEdit.equals(getString(R.string.add_new_task)) || isCompleted == 0) {
//                finish();
//            }
        }

    }

    private void uploadDataToFirestore() {
        List<String> benefitsArrayFirestore;
        if (record != null) {
            benefitsArrayFirestore = Arrays.asList(record);
        } else {
            benefitsArrayFirestore = Collections.emptyList();
        }

        if (mAddOrEdit.equals(getString(R.string.add_new_task))) {

            Map<String, Object> newTaskMap = new HashMap<>();
            newTaskMap.put("description", description);
            newTaskMap.put("priority", benefitsArrayFirestore.size());
            newTaskMap.put("Benefits", benefitsArrayFirestore);
            String task_status = "Pending";
            newTaskMap.put("Status", task_status);
            newTaskMap.put("TimestampCompleted", " ");

            UserColRef.document().set(newTaskMap).addOnSuccessListener(aVoid -> {
                loadingProgressBarUpdate.setVisibility(View.GONE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Toasty.success(AddOrEditTaskActivity2.this, "New list item added successfully", Toasty.LENGTH_SHORT).show();
//                Intent intent = new Intent(AddOrEditTaskActivity2.this, TodoListActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
                finish();
                onBackPressed();
            }).addOnFailureListener(e -> {
                loadingProgressBarUpdate.setVisibility(View.GONE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Toasty.error(AddOrEditTaskActivity2.this, "Something went wrong", Toasty.LENGTH_SHORT).show();
            });

        } else {
            Map<String, Object> updateTaskMap = new HashMap<>();
            updateTaskMap.put("description", description);
            updateTaskMap.put("priority", benefitsArrayFirestore.size());
            updateTaskMap.put("Benefits", benefitsArrayFirestore);
            String task_status;
            if (mBinding.cbTaskCompleted.isChecked()) {
                task_status = "Completed";
                Calendar calendar = Calendar.getInstance();
                String dateStr = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
                SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
                Log.i("dateTime", "TimestampCompleted" + dateStr);
                String timeStr = sdf.format(calendar.getTime());
                updateTaskMap.put("TimestampCompleted", dateStr + " " + timeStr);
            } else {
                task_status = "Pending";
            }
            updateTaskMap.put("Status", task_status);
            Log.i("task456", todoTaskToAddOrEdit.getDocumentID() + "jjj");
            UserColRef.document(todoTaskToAddOrEdit.getDocumentID()).set(updateTaskMap, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    loadingProgressBarUpdate.setVisibility(View.GONE);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    Toasty.success(AddOrEditTaskActivity2.this, "List item updated successfully", Toasty.LENGTH_SHORT).show();
//                    Intent intent = new Intent(AddOrEditTaskActivity2.this, TodoListActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    startActivity(intent);
                    finish();
                    onBackPressed();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    loadingProgressBarUpdate.setVisibility(View.GONE);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    Toasty.error(AddOrEditTaskActivity2.this, "Something went wrong", Toasty.LENGTH_SHORT).show();
                }
            });
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

        addMoreCategories.setOnClickListener(view -> startActivity(new Intent(AddOrEditTaskActivity2.this, AddBenefitsActivity.class)));

        if (purchaseCode.equals("0")) {
            bannerAd.loadAd(new AdRequest.Builder().build());
        } else {
            bannerAd.setVisibility(View.GONE);
        }


        alertDialog = alert.create();
        alertDialog.show();
    }

    public void loadCategories() {

//        databaseCategories = FirebaseDatabase.getInstance().getReference("categories");
//        databaseCategories = FirebaseDatabase.getInstance().getReference(userID).child("benefits");

        benefitCollectionRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                categories = new ArrayList<>();
                selectedStrings = new ArrayList<>();
                categories.clear();
                for (DocumentSnapshot dataSnapshot : value) {
                    Category category = new Category(dataSnapshot.getId(), (String) dataSnapshot.get("category_name"));
                    categories.add(category);
                }

                //iterating through all the nodes

                adapter = new GridViewAdapter(categories, AddOrEditTaskActivity2.this);
                gridView.setAdapter(adapter);
                gridView.setVisibility(View.VISIBLE);

                record = convertStringToArray(selectedResult);
                for (int i = 0; i < categories.size(); i++) {
                    for (int j = 0; j < record.length; j++) {
                        if (record[j].equals(categories.get(i).getCategory_name())) {
                            adapter.selectedPositions.add(i);
                            selectedStrings.add(record[j]);
                        }
                    }
                }
//                        for(int j=0;j<record.length;j++){
//                                int k;
//                                k=categories.indexOf(record[j]);
//                                adapter.selectedPositions.add(k);
//                                selectedStrings.add(record[j]);
//                            }
////
                selectedResult = "";
                selectedResult = convertArrayToString(selectedStrings);

                progressBar.setVisibility(View.INVISIBLE);
                addMoreCategories.setVisibility(View.VISIBLE);


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

    private void loadFullScreenAds() {
        AdRequest adRequest = new AdRequest.Builder().build();
//ca-app-pub-3111421321050812/5967628112 our
        //test ca-app-pub-3940256099942544/1033173712
        InterstitialAd.load(this, "ca-app-pub-3111421321050812/5967628112", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;

                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when fullscreen content is dismissed.
                                Log.d("TAG", "The ad was dismissed.");
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                // Called when fullscreen content failed to show.
                                Log.d("TAG", "The ad failed to show.");
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when fullscreen content is shown.
                                // Make sure to set your reference to null so you don't
                                // show it a second time.
                                mInterstitialAd = null;
                                Log.d("TAG", "The ad was shown.");
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        mInterstitialAd = null;
                    }
                });
    }

    private void loadRewardedAds() {
        AdRequest adRequest = new AdRequest.Builder().build();
//test ca-app-pub-3940256099942544/5224354917
        // our ca-app-pub-3111421321050812/7739858878
        RewardedAd.load(this, "ca-app-pub-3111421321050812/7739858878",
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        Log.d(TAG, loadAdError.getMessage());
                        mBinding.deleteTodoItem.setVisibility(View.INVISIBLE);
                        mRewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;
                        Log.d(TAG, "Ad was loaded.");
                        mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when ad is shown.
                                Log.d(TAG, "Ad was shown.");
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when ad fails to show.
                                Log.d(TAG, "Ad failed to show.");
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Set the ad reference to null so you don't show the ad a second time.
                                finish();
                                Log.d(TAG, "Ad was dismissed.");
                                mRewardedAd = null;
                            }
                        });
                    }
                });

    }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }
}
