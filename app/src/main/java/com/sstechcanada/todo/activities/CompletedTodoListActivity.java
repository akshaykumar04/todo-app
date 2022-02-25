package com.sstechcanada.todo.activities;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
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

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
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
import com.sstechcanada.todo.activities.auth.ProfileActivity;
import com.sstechcanada.todo.adapters.TodoListFirestoreAdapter;
import com.sstechcanada.todo.models.TodoTaskFirestore;
import com.sstechcanada.todo.utils.SwipeController;
import com.sstechcanada.todo.utils.SwipeControllerActions;

import es.dmoral.toasty.Toasty;

import static com.sstechcanada.todo.activities.MasterTodoListActivity.listId;
import static com.sstechcanada.todo.activities.MasterTodoListActivity.listName;
import static com.sstechcanada.todo.activities.MasterTodoListActivity.purchaseCode;
import static com.sstechcanada.todo.activities.TodoListActivity.lottieAnimationView;

public class CompletedTodoListActivity extends AppCompatActivity {

    private static final int ADD_TASK_REQUEST = 1;
    private static final int EDIT_TASK_REQUEST = 2;
    private static final int ID_TODOLIST_LOADER = 2018;
    public static int db_cnt = 0;
    String userID;
    ImageView placeholderImage;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersColRef = db.collection("Users");
    private int list_limit = 15;
    private RecyclerView mRecyclerView;

    //    private ActivityCompletedTodoListBinding mBinding;
    private SharedPreferences mSharedPreferences, ll;
    private AppCompatImageView toolbar_profile;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference databaseReference;
    TextView ongoingTab;
    TextView textViewListName;
    private TodoListFirestoreAdapter todoListFirestoreAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed_todo_list);

        mRecyclerView = findViewById(R.id.rv_todo_list);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userID = user.getUid();

        textViewListName = findViewById(R.id.listNameTextView);
        lottieAnimationView = findViewById(R.id.placeholderImage);

        setUpFirestoreRecyclerView();

        //Limit Set

        setValue();

        AdView adView = findViewById(R.id.adView);

        if (purchaseCode.equals("0")) {
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        } else {
            adView.setVisibility(View.GONE);
        }


        toolbar_profile = findViewById(R.id.profile_toolbar);
        Glide.with(this).load(mAuth.getCurrentUser().getPhotoUrl()).into(toolbar_profile);
        toolbar_profile.setOnClickListener(view -> startActivity(new Intent(CompletedTodoListActivity.this, ProfileActivity.class)));

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            db_cnt = todoListFirestoreAdapter.getItemCount();
//                Log.i("ItemCount", String.valueOf(db_cnt));
            setValue();
            if (isLogin()) {
                Intent intent = new Intent(CompletedTodoListActivity.this, AddOrEditTaskActivity2.class);
                intent.putExtra(getString(R.string.intent_adding_or_editing_key), getString(R.string.add_new_task));
                startActivityForResult(intent, ADD_TASK_REQUEST);
            }
        });

        ongoingTab = findViewById(R.id.ongoingTab);
        ongoingTab.setOnClickListener(view -> onBackPressed());

    }

    private void setUpFirestoreRecyclerView() {
        FirebaseCrashlytics.getInstance().log(this.getClass().getSimpleName()+"listId = "+listId);
        FirebaseCrashlytics.getInstance().log(this.getClass().getSimpleName()+"UserId = "+userID);
        Query query = usersColRef.document(userID).collection("Lists").document(
                listId).collection("Todo").whereEqualTo("Status", "Completed").orderBy("priority", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<TodoTaskFirestore> options = new FirestoreRecyclerOptions.Builder<TodoTaskFirestore>().setQuery(query, TodoTaskFirestore.class).build();
        todoListFirestoreAdapter = new TodoListFirestoreAdapter(options, this);
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
            }

            @Override
            public void onLeftClicked(int position) {
                DocumentSnapshot documentSnapshot = todoListFirestoreAdapter.getSnapshots().getSnapshot(position);
                TodoTaskFirestore todoTask = documentSnapshot.toObject(TodoTaskFirestore.class);

                Intent intent = new Intent(CompletedTodoListActivity.this, AddOrEditTaskActivity2.class);
                intent.putExtra("Adding or editing", "Edit Task");
                intent.putExtra("Todo", todoTask);
                startActivity(intent);


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