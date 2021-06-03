package com.sstechcanada.todo.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.sstechcanada.todo.R;
import com.sstechcanada.todo.activities.auth.LoginActivity;
import com.sstechcanada.todo.adapters.MasterListFirestoreAdapter;
import com.sstechcanada.todo.adapters.MasterListGridViewAdapter;
import com.sstechcanada.todo.adapters.TodoListAdapter;
import com.sstechcanada.todo.broadcast_receivers.DailyAlarmReceiver;
import com.sstechcanada.todo.custom_views.GridItemView;
import com.sstechcanada.todo.data.TodoListContract;
import com.sstechcanada.todo.data.TodoListDbHelper;
import com.sstechcanada.todo.models.List;
import com.sstechcanada.todo.models.TodoTask;
import com.sstechcanada.todo.utils.NotificationUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

import static com.sstechcanada.todo.activities.auth.LoginActivity.userAccountDetails;

public class MasterTodoListActivity extends AppCompatActivity {
    private static final String TAG = MasterTodoListActivity.class.getSimpleName();
    String userID;
    ImageView placeholderImage;
    private int list_limit = 15;
    public static int list_cnt=0;
    private RecyclerView mRecyclerView;
    private TodoListAdapter mTodoListAdapter;
//    private MasterTodoListActivity mBinding;
    private SharedPreferences mSharedPreferences, ll;
    private AppCompatImageView toolbar_profile;
    private TodoListDbHelper tld;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference databaseReference;
    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private CollectionReference usersColRef=db.collection("Users");
    private MasterListFirestoreAdapter masterListFirestoreAdapter;
    private GridView gridView;
    ProgressBar progressBar,loadingProgressBar;
    String selectedDrawable,sdrawable;
    ArrayList<String> listDrawable;
    private MasterListGridViewAdapter gridAdapter;
    public static String listId;
    public static String purchaseCode="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master_todo_list);

        mRecyclerView = findViewById(R.id.rv_todo_list);
        loadingProgressBar= findViewById(R.id.loadingProgressBar);
        showProgressBar();
//        placeholderImage=findViewById(R.id.placeholderImage);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userID = user.getUid();
        setPurchaseCode();
//        lottieAnimationView = findViewById(R.id.placeholderImage);

        setUpFirestoreRecyclerView();

        //Limit Set

        setValue();

        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);



        toolbar_profile = findViewById(R.id.profile_toolbar);
        toolbar_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MasterTodoListActivity.this, LoginActivity.class));
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Integer.parseInt(userAccountDetails.get(0))>masterListFirestoreAdapter.getItemCount()){
                    Log.i("purchasecode","masterlist limit :"+(userAccountDetails.get(0)));
                    Log.i("purchasecode","masterlist items :"+(masterListFirestoreAdapter.getItemCount()));
                    setValue();
                    if (isLogin()) {
                        addNewlistAlert();
                    }
                }else {
                    if (isLogin()) {
                        Intent intent = new Intent(MasterTodoListActivity.this, AppUpgradeActivity.class);
//                        intent.putExtra(getString(R.string.intent_adding_or_editing_key), getString(R.string.add_new_task));
                        startActivity(intent);
                    }

                }
            }
        });
    }

    private void setPurchaseCode() {
        usersColRef.document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                purchaseCode=documentSnapshot.get("purchase_code").toString();
                db.collection("UserTiers").document(purchaseCode).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Log.i("purchasecode","purchase code :"+purchaseCode);
                       Log.i("purchasecode","new :"+documentSnapshot.get("masterListLimit").toString());
                        userAccountDetails.add(0, documentSnapshot.get("masterListLimit").toString());
                        userAccountDetails.add(1, documentSnapshot.get("todoItemLimit").toString());
                        hideProgressBar();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    public void addNewlistAlert() {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.add_list_dialog, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Add List");
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
//        gridView = alertLayout.findViewById(R.id.grid_view_alert);
//        addMoreCategories = alertLayout.findViewById(R.id.addMoreCategoriesLayout);
        progressBar = alertLayout.findViewById(R.id.progress_circular);
        progressBar.setVisibility(View.GONE);
        AdView bannerAd = alertLayout.findViewById(R.id.adView);
//        loadImages();
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false);
        alert.setNegativeButton("Cancel", (dialog, which) -> {
//                Toast.makeText(getBaseContext(), "Cancel clicked", Toast.LENGTH_SHORT).show();
        });
        alert.setPositiveButton("Done", (dialog, which) -> {
            sdrawable=selectedDrawable;
//            int imageResource = getResources().getIdentifier(sdrawable, null, getPackageName());
            String name = ((EditText) alertLayout.findViewById(R.id.editTextListName)).getText().toString();
            String description = ((EditText) alertLayout.findViewById(R.id.editTextListDescription)).getText().toString();

            usersColRef.document(userID).collection("Lists");

            Map<String, Object> newList = new HashMap<>();
            newList.put("ListName", name);
            newList.put("Image", sdrawable);
            newList.put("ListDescription", description);

            usersColRef.document(userID).collection("Lists").document().set(newList).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toasty.success(MasterTodoListActivity.this,"New Todo-Item Successfully Added");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toasty.error(MasterTodoListActivity.this,"Something went wrong");
                }
            });
        });


        bannerAd.loadAd(new AdRequest.Builder().build());

        AlertDialog dialog = alert.create();
        dialog.show();


    }


    public void loadImages() {

        listDrawable=new ArrayList<>();
        listDrawable.add("@drawable/master_list_default_icon");
        listDrawable.add("@drawable/ic_to_do_list");
        listDrawable.add("@drawable/ic_lock");
        listDrawable.add("@drawable/circle_per_item");

        gridAdapter = new MasterListGridViewAdapter(listDrawable, MasterTodoListActivity.this);
        gridView.setAdapter(gridAdapter);
        gridView.setVisibility(View.VISIBLE);

        gridView.setOnItemClickListener((parent, v, position, id) -> {
//            gridView.setAdapter(null);
//            gridView.setAdapter(gridAdapter);

            int selectedIndex = position;
            if (selectedIndex > -1) {
                ((GridItemView) v).display(false);
                selectedDrawable="";
            } else {
                ((GridItemView) v).display(true);
                selectedDrawable= (String) gridAdapter.getItem(position);
            }

        });
        progressBar.setVisibility(View.INVISIBLE);


    }


    private void setUpFirestoreRecyclerView() {
        Query query =usersColRef.document(userID).collection("Lists");
        FirestoreRecyclerOptions<List> options =new FirestoreRecyclerOptions.Builder<List>().setQuery(query,List.class).build();
        masterListFirestoreAdapter=new MasterListFirestoreAdapter(options,this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(masterListFirestoreAdapter);
        list_cnt = masterListFirestoreAdapter.getItemCount();

    }

    @Override
    protected void onStart() {
        super.onStart();
        masterListFirestoreAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        masterListFirestoreAdapter.stopListening();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.todo_list_menu, menu);
        //For 3 Dot menu
        return false;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.action_delete_test:
                // later a service will take care of this
                deleteAllCheckedTasks();
                break;
            case R.id.action_test_something:
                // just a place for me to test whatever I'm testing at the moment
                NotificationUtils.notifyUserOfDueAndOverdueTasks(this);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            updateWidget();
        }
    }

    private void updateWidget() {
        // let the widget know there's been a database or sort order change
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        sendBroadcast(intent);
    }

    @Override
    protected void onResume() {

        super.onResume();
        // This is so that if we've edited a task directly from the widget, the widget will still
        // get updated when we come to this activity after clicking UPDATE TASK in AddOrEditTaskActivity
        updateWidget();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    private void deleteAllCheckedTasks() {
        // this is just for testing, later a service will do it periodically
        Uri uri = TodoListContract.TodoListEntry.CONTENT_URI;
        int deletedTasksCount = getContentResolver().delete(uri, "completed=?", new String[]{String.valueOf(TodoTask.TASK_COMPLETED)});
        if (deletedTasksCount > 0) {
            updateWidget();
        }
    }

    public void scheduleDailyDueCheckerAlarm() {
        Intent intent = new Intent(getApplicationContext(), DailyAlarmReceiver.class);

        final PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                DailyAlarmReceiver.REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        long firstMillis = System.currentTimeMillis(); // alarm is set right away

        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
    }

    public void cancelAlarm() {
        Intent intent = new Intent(getApplicationContext(), DailyAlarmReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, DailyAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }

    public boolean isLogin() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toasty.warning(this, getString(R.string.login_first), Toast.LENGTH_LONG).show();
            startActivity(new Intent(MasterTodoListActivity.this, LoginActivity.class));
            return false;
        } else if (list_limit <= list_cnt) {
            //Limit Check
            Toasty.info(this, getString(R.string.cannot_create) + list_limit + " task in free tier, Please upgrade app to premium version", Toast.LENGTH_LONG, true).show();
            startActivity(new Intent(MasterTodoListActivity.this, AppUpgradeActivity.class));
            return false;
        }
        return true;
    }

    public void setValue() {
        if (user != null) {
            list_limit = 15;
        }
    }

    private void showProgressBar() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }


    private void hideProgressBar() {
        if (loadingProgressBar.getVisibility() == View.VISIBLE) {
            loadingProgressBar.setVisibility(View.INVISIBLE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }

    }

}