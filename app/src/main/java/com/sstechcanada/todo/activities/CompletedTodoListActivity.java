package com.sstechcanada.todo.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
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
import com.sstechcanada.todo.adapters.TodoListAdapter;
import com.sstechcanada.todo.adapters.TodoListFirestoreAdapter;
import com.sstechcanada.todo.broadcast_receivers.DailyAlarmReceiver;
import com.sstechcanada.todo.data.TodoListContract;
import com.sstechcanada.todo.data.TodoListDbHelper;
import com.sstechcanada.todo.databinding.ActivityCompletedTodoListBinding;
import com.sstechcanada.todo.models.List;
import com.sstechcanada.todo.models.TodoTask;
import com.sstechcanada.todo.models.TodoTaskFirestore;
import com.sstechcanada.todo.utils.NotificationUtils;
import com.sstechcanada.todo.utils.SwipeController;
import com.sstechcanada.todo.utils.SwipeControllerActions;

import es.dmoral.toasty.Toasty;

import static com.sstechcanada.todo.activities.MasterTodoListActivity.listId;
import static com.sstechcanada.todo.activities.MasterTodoListActivity.listName;
import static com.sstechcanada.todo.activities.MasterTodoListActivity.purchaseCode;
import static com.sstechcanada.todo.activities.TodoListActivity2.lottieAnimationView;

public class CompletedTodoListActivity extends AppCompatActivity {
    private static final String TAG = TodoListActivity.class.getSimpleName();
    private static final int ADD_TASK_REQUEST = 1;
    private static final int EDIT_TASK_REQUEST = 2;
    private static final int ID_TODOLIST_LOADER = 2018;
    String userID;
    ImageView placeholderImage;
    private int list_limit = 15;
    public static int db_cnt=0;
    private RecyclerView mRecyclerView;
    private TodoListAdapter mTodoListAdapter;
//    private ActivityCompletedTodoListBinding mBinding;
    private SharedPreferences mSharedPreferences, ll;
    private AppCompatImageView toolbar_profile;
    private TodoListDbHelper tld;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference databaseReference;
    TextView ongoingTab;
    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private CollectionReference usersColRef=db.collection("Users");
    private TodoListFirestoreAdapter todoListFirestoreAdapter;
    TextView textViewListName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed_todo_list);
//        mBinding = ActivityCompletedTodoListBinding.inflate(getLayoutInflater());
//        View view = mBinding.getRoot();
//        setContentView(view);



        mRecyclerView = findViewById(R.id.rv_todo_list);
//        placeholderImage=findViewById(R.id.placeholderImage);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userID=user.getUid();

        textViewListName=findViewById(R.id.listNameTextView);
        lottieAnimationView=findViewById(R.id.placeholderImage);



        setUpFirestoreRecyclerView();

        //Limit Set

        setValue();

        AdView adView = findViewById(R.id.adView);

        if(purchaseCode.equals("0")){
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }else{
            adView.setVisibility(View.GONE);
        }



        toolbar_profile = findViewById(R.id.profile_toolbar);
        toolbar_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CompletedTodoListActivity.this, LoginActivity.class));
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db_cnt = todoListFirestoreAdapter.getItemCount();
//                Log.i("ItemCount", String.valueOf(db_cnt));
                setValue();
                if (isLogin()) {
                    Intent intent = new Intent(CompletedTodoListActivity.this, AddOrEditTaskActivity2.class);
                    intent.putExtra(getString(R.string.intent_adding_or_editing_key), getString(R.string.add_new_task));
                    startActivityForResult(intent, ADD_TASK_REQUEST);
                }
            }
        });

        ongoingTab = findViewById(R.id.ongoingTab);
        ongoingTab.setOnClickListener(view -> {
            startActivity(new Intent(CompletedTodoListActivity.this, TodoListActivity2.class));
        });

    }

    private void setUpFirestoreRecyclerView() {
        Query query =usersColRef.document(userID).collection("Lists").document(
                listId).collection("Todo").whereEqualTo("Status","Completed").orderBy("priority", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<TodoTaskFirestore> options =new FirestoreRecyclerOptions.Builder<TodoTaskFirestore>().setQuery(query,TodoTaskFirestore.class).build();
        todoListFirestoreAdapter=new TodoListFirestoreAdapter(options,this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        SwipeController swipeController = new SwipeController(this, new SwipeControllerActions() {
            @Override
            public void onRightClicked(int position) {
                Log.i("cluck", "right");

                new AlertDialog.Builder(CompletedTodoListActivity.this)
                        .setIcon(android.R.drawable.ic_menu_delete)
                        .setTitle("Confirm Delete")
                        .setMessage("Are you sure you want to delete this task?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DocumentSnapshot documentSnapshot = todoListFirestoreAdapter.getSnapshots().getSnapshot(position);
                                String id = documentSnapshot.getId();
                                usersColRef.document(userID).collection("Lists").document(listId).collection("Todo").document(id).delete();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();

//                mAdapter.players.remove(position);
//                mAdapter.notifyItemRemoved(position);
//                mAdapter.notifyItemRangeChanged(position, mAdapter.getItemCount());
            }
        });
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(mRecyclerView);
        mRecyclerView.setAdapter(todoListFirestoreAdapter);

        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });

        db_cnt = todoListFirestoreAdapter.getItemCount();

    }

    @Override
    protected void onStart() {
        super.onStart();
        todoListFirestoreAdapter.startListening();
        textViewListName.setText(listName);
    }

    @Override
    protected void onStop() {
        super.onStop();
        todoListFirestoreAdapter.stopListening();
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
            startActivity(new Intent(CompletedTodoListActivity.this, LoginActivity.class));
            return false;
        } else if (list_limit <= db_cnt) {
            //Limit Check
            Toasty.info(this, getString(R.string.upgrade_master_list), Toast.LENGTH_LONG, true).show();
            startActivity(new Intent(CompletedTodoListActivity.this, AppUpgradeActivity.class));
            return false;
        }
        return true;
    }

    public void setValue() {
        if (user != null) {
            list_limit = 15;
        }
    }



}