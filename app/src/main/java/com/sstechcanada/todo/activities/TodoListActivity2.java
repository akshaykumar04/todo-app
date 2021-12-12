package com.sstechcanada.todo.activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.sstechcanada.todo.R;
import com.sstechcanada.todo.activities.auth.LoginActivity;
import com.sstechcanada.todo.adapters.TodoListFirestoreAdapter;
import com.sstechcanada.todo.broadcast_receivers.DailyAlarmReceiver;
import com.sstechcanada.todo.databinding.ActivityTodoListBinding;
import com.sstechcanada.todo.models.TodoTaskFirestore;
import com.sstechcanada.todo.utils.SwipeController;
import com.sstechcanada.todo.utils.SwipeControllerActions;

import es.dmoral.toasty.Toasty;

import static com.sstechcanada.todo.activities.MasterTodoListActivity.listId;
import static com.sstechcanada.todo.activities.MasterTodoListActivity.listName;
import static com.sstechcanada.todo.activities.MasterTodoListActivity.purchaseCode;
import static com.sstechcanada.todo.activities.auth.LoginActivity.SHAREDPREF;
import static com.sstechcanada.todo.activities.auth.LoginActivity.userAccountDetails;

public class TodoListActivity2 extends AppCompatActivity {


    private static final int ADD_TASK_REQUEST = 1;
    private static final int EDIT_TASK_REQUEST = 2;
    private static final int ID_TODOLIST_LOADER = 2018;
    public static int db_cnt = 0;
    public static LottieAnimationView lottieAnimationView;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersColRef = db.collection("Users");
    String userID;
    ProgressBar loadingProgressBar;
    FloatingActionButton fab;
    private int list_limit = 15;
    private RecyclerView mRecyclerView;
    private ActivityTodoListBinding mBinding;
    private SharedPreferences mSharedPreferences, ll;
    private AppCompatImageView toolbar_profile;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference databaseReference;
    private TodoListFirestoreAdapter todoListFirestoreAdapter;
    SharedPreferences.Editor editor;

    public static void showPlaceHolder() {
        lottieAnimationView.setVisibility(View.VISIBLE);
    }

    public static void hidePlaceHolder() {
        lottieAnimationView.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_todo_list);
        mRecyclerView = mBinding.rvTodoList;
        loadingProgressBar = mBinding.loadingProgressBar;
        fab = mBinding.fab;

        if (Integer.valueOf(purchaseCode) != 0) {
            Log.i("purchase code", purchaseCode);
            Log.i("purchase code", "purchaseCode");

            mBinding.completedTab.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_check_circle_24, 0);
        }

        SharedPreferences prefs = getSharedPreferences(SHAREDPREF, MODE_PRIVATE);
        editor = getSharedPreferences(SHAREDPREF, MODE_PRIVATE).edit();
        showProgressBar();
//        setContentView(R.layout.activity_todo_list);
        lottieAnimationView = findViewById(R.id.placeholderImage);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
//        layoutManager.setReverseLayout(true);
//        layoutManager.setStackFromEnd(true);
//        mRecyclerView.setLayoutManager(layoutManager);
//        mTodoListAdapter = new TodoListAdapter(this, this);
//        mRecyclerView.setAdapter(mTodoListAdapter);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userID = user.getUid();

        setUpFirestoreRecyclerView();

        if (prefs.getBoolean("flagTodoListFirstRun",true)) {
            mBinding.buttonTapTargetView.setVisibility(View.INVISIBLE);
            callWalkThrough();
        }

        //Limit Set

        setValue();

        AdView adView = mBinding.adView;
        if (purchaseCode.equals("0")) {

            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        } else {
            adView.setVisibility(View.GONE);
        }


//        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//        getSupportActionBar().setCustomView(R.layout.abs_layout);
//        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
//        mRecyclerView.addItemDecoration(mDividerItemDecoration);

        toolbar_profile = findViewById(R.id.profile_toolbar);
        toolbar_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TodoListActivity2.this, LoginActivity.class));
            }
        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db_cnt = todoListFirestoreAdapter.getItemCount();
                Log.i("ItemCount", "FAB Clicked");
                if (Integer.parseInt(userAccountDetails.get(1)) > todoListFirestoreAdapter.getItemCount()) {
                    setValue();
                    if (isLogin()) {
                        Intent intent = new Intent(TodoListActivity2.this, AddOrEditTaskActivity2.class);
                        intent.putExtra(getString(R.string.intent_adding_or_editing_key), getString(R.string.add_new_task));
                        startActivityForResult(intent, ADD_TASK_REQUEST);
                    }
                } else {
                    if (isLogin()) {

                        if(!purchaseCode.equals("2")){
                            Intent intent = new Intent(TodoListActivity2.this, AppUpgradeActivity2.class);
//                        intent.putExtra(getString(R.string.intent_adding_or_editing_key), getString(R.string.add_new_task));
                            Toasty.info(getApplicationContext(), getString(R.string.upgrade_master_list), Toast.LENGTH_SHORT).show();
                            startActivity(intent);
                        }else if (purchaseCode.equals("2")){
                            Toasty.warning(getApplicationContext(), "Sorry, You cannot add more to-do items. You have reached the max-limit!", Toast.LENGTH_LONG).show();
                        }
                    }

                }

            }
        });

//        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

//        getSupportLoaderManager().initLoader(ID_TODOLIST_LOADER, null, this);

        //scheduleDailyDueCheckerAlarm();
        //cancelAlarm();

        mBinding.completedTab.setOnClickListener(view -> {
            if (Integer.valueOf(purchaseCode) != 0) {
                startActivity(new Intent(TodoListActivity2.this, CompletedTodoListActivity.class));
            } else {
                startActivity(new Intent(TodoListActivity2.this, AppUpgradeActivity2.class));
            }
//            startActivity(new Intent(TodoListActivity2.this, AppUpgradeActivity.class));
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if (dy > 12 ||dy<0 && fab.isShown())
                {
                    fab.hide();
                }
                if (dy < -12 && !fab.isShown()) {
                    fab.show();
                }
                if (!recyclerView.canScrollVertically(-1)) {
                    fab.show();
                }
            }

        });

    }

    private void setUpFirestoreRecyclerView() {
//        listId=getIntent().getStringExtra("ListId");
        Log.i("ListId", "Setupdrecyasdf");
        Log.i("ListId", "n" + listId);
        FirebaseCrashlytics.getInstance().log(this.getClass().getSimpleName()+"listId = "+listId);
        FirebaseCrashlytics.getInstance().log(this.getClass().getSimpleName()+"UserId = "+userID);
        Query query = usersColRef.document(userID).collection("Lists").document(
                listId).collection("Todo").whereEqualTo("Status", "Pending").orderBy("priority", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<TodoTaskFirestore> options = new FirestoreRecyclerOptions.Builder<TodoTaskFirestore>().setQuery(query, TodoTaskFirestore.class).build();
        todoListFirestoreAdapter = new TodoListFirestoreAdapter(options, this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        SwipeController swipeController = new SwipeController(this, new SwipeControllerActions() {
            @Override
            public void onRightClicked(int position) {
                Log.i("cluck", "right");

                new AlertDialog.Builder(TodoListActivity2.this)
                        .setIcon(android.R.drawable.ic_menu_delete)
                        .setTitle("Confirm Delete")
                        .setMessage("Are you sure you want to delete this task?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            DocumentSnapshot documentSnapshot = todoListFirestoreAdapter.getSnapshots().getSnapshot(position);
                            String id = documentSnapshot.getId();
                            usersColRef.document(userID).collection("Lists").document(listId).collection("Todo").document(id).delete();
                        })
                        .setNegativeButton("No", null)
                        .show();
            }

            //                mAdapter.players.remove(position);
//                mAdapter.notifyItemRemoved(position);
//                mAdapter.notifyItemRangeChanged(position, mAdapter.getItemCount());
            @Override
            public void onLeftClicked(int position) {
                DocumentSnapshot documentSnapshot = todoListFirestoreAdapter.getSnapshots().getSnapshot(position);
                String id = documentSnapshot.getId();
                TodoTaskFirestore task = documentSnapshot.toObject(TodoTaskFirestore.class);


                TodoTaskFirestore todoTask = new TodoTaskFirestore(task.getDescription(),
                        task.getPriority(),
                        task.getDueDate(),
                        id,
                        task.getStatus(),
                        task.getCategory(),
                        2,
                        task.getBenefits(),
                        task.getBenefitsString(),
                        task.getTimestampCompleted());

                Intent intent = new Intent(TodoListActivity2.this, AddOrEditTaskActivity2.class);
                intent.putExtra("Adding or editing", "Edit Task");
                intent.putExtra("Todo", todoTask);
                startActivity(intent);

            }
        });
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(mRecyclerView);
        mRecyclerView.setAdapter(todoListFirestoreAdapter);
        hideProgressBar();

        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });

        db_cnt = todoListFirestoreAdapter.getItemCount();


    }


//    private void showHidePlaceholder() {
//        if (db_cnt <= 2) {
//            mBinding.placeholderImage.setVisibility(View.VISIBLE);
//        } else {
//            mBinding.placeholderImage.setVisibility(View.GONE);
//        }
//    }

    @Override
    protected void onStart() {
        super.onStart();
        todoListFirestoreAdapter.startListening();
        mBinding.listNameTextView.setText(listName);
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


//    @Override
//    public void onClick(TodoTask todoTask, View view) {
//        // are they checking or unchecking the task checkbox, or tapping the task to edit it?
//        if (view instanceof CheckBox) {
//            // checking off task gets it flagged for deletion soon, and unchecking it reprieves it
//            final String id = String.valueOf(todoTask.getId());
//            final Uri uri = TodoListContract.TodoListEntry.CONTENT_URI.buildUpon().appendPath(id).build();
//            int isCompleted;
//
//            final ContentValues contentValues = new ContentValues();
//            contentValues.put(TodoListContract.TodoListEntry.COLUMN_DESCRIPTION, todoTask.getDescription());
//            contentValues.put(TodoListContract.TodoListEntry.COLUMN_PRIORITY, todoTask.getPriority());
//            contentValues.put(TodoListContract.TodoListEntry.COLUMN_DUE_DATE, todoTask.getDueDate());
//
//            if (((CheckBox) view).isChecked()) {
//                isCompleted = TodoTask.TASK_COMPLETED;
//            } else {
//                isCompleted = TodoTask.TASK_NOT_COMPLETED;
//            }
//
//            contentValues.put(TodoListContract.TodoListEntry.COLUMN_COMPLETED, isCompleted);
//
//            // Wait half a second so they can briefly see the check appear or disappear before
//            // the task is moved to or from the bottom
//            Handler handler = new Handler();
//            handler.postDelayed(new Runnable() {
//                public void run() {
//                    getContentResolver().update(uri, contentValues, "_id=?", new String[]{id});
//                    updateWidget();
//                }
//            }, 500);
//        } else {
//            // edit the task
//            Intent intent = new Intent(this, AddOrEditTaskActivity2.class);
//            intent.putExtra(getString(R.string.intent_adding_or_editing_key), getString(R.string.edit_task));
//            intent.putExtra(getString(R.string.intent_todo_key), todoTask);
//            startActivityForResult(intent, EDIT_TASK_REQUEST);
//        }
//    }

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

//    @Override
//    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
//        if (loaderId == ID_TODOLIST_LOADER) {
//            String sortOrderPreference = getSortOrderPreference();
//            String sortOrder;
//            Uri todoListQueryUri = TodoListContract.TodoListEntry.CONTENT_URI;
//            // sort order preference is the primary sort, with the other sort order as secondary
//            if (sortOrderPreference.equals(getString(R.string.priority))) {
//                sortOrder = TodoListProvider.SORT_ORDER_PRIORITY;
//            } else {
//                sortOrder = TodoListProvider.SORT_ORDER_DUEDATE;
//            }
//
//            return new CursorLoader(this,
//                    todoListQueryUri,
//                    null,
//                    null,
//                    null,
//                    sortOrder);
//        } else {
//            throw new RuntimeException("Loader Not Implemented: " + loaderId);
//        }
//    }

//    @Override
//    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
////        mTodoListAdapter.swapCursor(data);
//    }
//
//    @Override
//    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
//    }
//
//    private String getSortOrderPreference() {
//        return mSharedPreferences.getString(getString(R.string.pref_sort_by_key), getString(R.string.priority));
//    }
//
//    @Override
//    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
//        mTodoListAdapter.swapCursor(null);
//        getSupportLoaderManager().restartLoader(ID_TODOLIST_LOADER, null, this);
//        updateWidget();
//    }

    @Override
    protected void onResume() {

        super.onResume();
        // This is so that if we've edited a task directly from the widget, the widget will still
        // get updated when we come to this activity after clicking UPDATE TASK in AddOrEditTaskActivity
        updateWidget();
//        showHidePlaceholder();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
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
            startActivity(new Intent(TodoListActivity2.this, LoginActivity.class));
            return false;
        }
//        else if (list_limit <= db_cnt) {
//            //Limit Check
//            Toasty.info(this, getString(R.string.upgrade_todo_list), Toast.LENGTH_LONG, true).show();
//            startActivity(new Intent(TodoListActivity2.this, AppUpgradeActivity2.class));
//            return false;
//        }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(TodoListActivity2.this, MasterTodoListActivity.class));
    }

    public void callWalkThrough() {

        new TapTargetSequence(this)
                .targets(
                        TapTarget.forView(fab, "Add Button", "Click here to add a new list item")
                                .outerCircleColor(R.color.chip_5)
                                .outerCircleAlpha(0.96f)
                                .targetCircleColor(R.color.colorUncompletedBackground)
                                .titleTextSize(22)
                                .titleTextColor(R.color.colorUncompletedBackground)
                                .descriptionTextSize(12)
                                .descriptionTextColor(R.color.black)
                                .textColor(R.color.black)
                                .dimColor(R.color.black)
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(true)
                                .transparentTarget(true)
                                .targetRadius(80),
                        TapTarget.forView(mBinding.buttonTapTargetView, "List Items", "1: Swipe right and click on the pencil icon to edit a list item. \n2: Swipe left and click on the garbage can icon to delete a list item.")
                                .outerCircleColor(R.color.chip_5)
                                .outerCircleAlpha(0.96f)
                                .targetCircleColor(R.color.colorUncompletedBackground)
                                .titleTextSize(22)
                                .titleTextColor(R.color.colorUncompletedBackground)
                                .descriptionTextSize(12)
                                .descriptionTextColor(R.color.black)
                                .textColor(R.color.black)
                                .dimColor(R.color.black)
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(true)
                                .transparentTarget(true)
                                .targetRadius(60)).listener(new TapTargetSequence.Listener() {
            @Override
            public void onSequenceFinish() {

//                Toast.makeText(TodoListActivity2.this,"Sequence Finished",Toast.LENGTH_SHORT).show();
                Toasty.success(TodoListActivity2.this, "You are all set now!", Toast.LENGTH_SHORT).show();
//                flagTodoListFirstRun = false;
                mBinding.buttonTapTargetView.setVisibility(View.GONE);
                editor.putBoolean("flagTodoListFirstRun", false);
                editor.apply();
            }

            @Override
            public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {


            }

            @Override
            public void onSequenceCanceled(TapTarget lastTarget) {
                editor.putBoolean("flagTodoListFirstRun", false);
                mBinding.buttonTapTargetView.setVisibility(View.GONE);
                editor.apply();
            }
        }).start();
    }

}
