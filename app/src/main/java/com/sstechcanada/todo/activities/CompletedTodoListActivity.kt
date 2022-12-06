package com.sstechcanada.todo.activities

import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.sstechcanada.todo.R
import com.sstechcanada.todo.activities.auth.LoginActivity
import com.sstechcanada.todo.activities.auth.ProfileActivity
import com.sstechcanada.todo.adapters.TodoListFirestoreAdapter
import com.sstechcanada.todo.models.TodoTaskFirestore
import com.sstechcanada.todo.utils.SaveSharedPreference
import com.sstechcanada.todo.utils.SwipeController
import com.sstechcanada.todo.utils.SwipeControllerActions
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.act_bar.*
import kotlinx.android.synthetic.main.activity_completed_todo_list.*

class CompletedTodoListActivity : AppCompatActivity() {
    private var usersColRef: CollectionReference? = null
    private var listLimit = 15
    private var user: FirebaseUser? = null
    private var todoListFirestoreAdapter: TodoListFirestoreAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_completed_todo_list)

        user = FirebaseAuth.getInstance().currentUser
        usersColRef = FirebaseFirestore.getInstance().collection("Users")

        TodoListActivity.lottieAnimationView = findViewById(R.id.placeholderImage)
        setUpFirestoreRecyclerView()

        //Limit Set
        setValue()
        val adView = findViewById<AdView>(R.id.adView)
        if (SaveSharedPreference.getAdsEnabled(this)) {
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
        } else {
            adView.visibility = View.GONE
        }
        user?.photoUrl?.let {
            Glide.with(this).load(it).into(profile_toolbar)
        }

        profile_toolbar.setOnClickListener {
            startActivity(
                Intent(this@CompletedTodoListActivity, ProfileActivity::class.java)
            )
        }
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            db_cnt = todoListFirestoreAdapter!!.itemCount
            setValue()
            if (isLogin) {
                val intent =
                    Intent(this@CompletedTodoListActivity, AddOrEditTaskActivity::class.java)
                intent.putExtra(
                    getString(R.string.intent_adding_or_editing_key),
                    getString(R.string.add_new_task)
                )
                startActivityForResult(intent, ADD_TASK_REQUEST)
            }
        }

        ongoingTab.setOnClickListener { onBackPressed() }
    }

    private fun setUpFirestoreRecyclerView() {
        FirebaseCrashlytics.getInstance()
            .log(this.javaClass.simpleName + "listId = " + MasterTodoListActivity.listId)
        FirebaseCrashlytics.getInstance().log(this.javaClass.simpleName + "UserId = " + user?.uid)
        val query = user?.uid?.let {
            MasterTodoListActivity.listId?.let { it1 ->
                usersColRef?.document(it)?.collection("Lists")?.document(
                    it1
                )?.collection("Todo")?.whereEqualTo("Status", "Completed")
                    ?.orderBy("priority", Query.Direction.DESCENDING)
            }
        }
        val options = query?.let {
            FirestoreRecyclerOptions.Builder<TodoTaskFirestore>()
                .setQuery(it, TodoTaskFirestore::class.java).build()
        }
        todoListFirestoreAdapter = options?.let { TodoListFirestoreAdapter(it, this) }
        rv_todo_list.setHasFixedSize(true)
        rv_todo_list.layoutManager = LinearLayoutManager(this)
        val swipeController = SwipeController(this, object : SwipeControllerActions() {
            override fun onRightClicked(position: Int) {
                Log.i("cluck", "right")
                AlertDialog.Builder(this@CompletedTodoListActivity)
                    .setIcon(android.R.drawable.ic_menu_delete)
                    .setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to delete this task?")
                    .setPositiveButton("Yes") { dialog, which ->
                        val documentSnapshot =
                            todoListFirestoreAdapter!!.snapshots.getSnapshot(position)
                        val id = documentSnapshot.id
                        user?.uid?.let {
                            MasterTodoListActivity.listId?.let { it1 ->
                                usersColRef?.document(it)?.collection("Lists")
                                    ?.document(it1)?.collection("Todo")
                                    ?.document(id)
                                    ?.delete()
                            }
                        }
                    }
                    .setNegativeButton("No", null)
                    .show()
            }

            override fun onLeftClicked(position: Int) {
                val documentSnapshot = todoListFirestoreAdapter?.snapshots?.getSnapshot(position)
                val todoTask = documentSnapshot?.toObject(
                    TodoTaskFirestore::class.java
                )
                val intent =
                    Intent(this@CompletedTodoListActivity, AddOrEditTaskActivity::class.java)
                intent.putExtra("Adding or editing", "Edit Task")
                intent.putExtra("Todo", todoTask)
                startActivity(intent)
            }
        })
        val itemTouchhelper = ItemTouchHelper(swipeController)
        itemTouchhelper.attachToRecyclerView(rv_todo_list)
        rv_todo_list.adapter = todoListFirestoreAdapter
        rv_todo_list.addItemDecoration(object : ItemDecoration() {
            override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                swipeController.onDraw(c)
            }
        })
        db_cnt = todoListFirestoreAdapter!!.itemCount
    }

    override fun onStart() {
        super.onStart()
        todoListFirestoreAdapter?.startListening()
        listNameTextView.text = MasterTodoListActivity.listName
    }

    override fun onStop() {
        super.onStop()
        todoListFirestoreAdapter?.stopListening()
    }

    //Limit Check
    private val isLogin: Boolean
        get() {
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                Toasty.warning(this, getString(R.string.login_first), Toast.LENGTH_LONG).show()
                startActivity(Intent(this@CompletedTodoListActivity, LoginActivity::class.java))
                return false
            } else if (listLimit <= db_cnt) {
                //Limit Check
                Toasty.info(this, getString(R.string.upgrade_master_list), Toast.LENGTH_LONG, true)
                    .show()
                startActivity(
                    Intent(
                        this@CompletedTodoListActivity,
                        AppUpgradeActivity::class.java
                    )
                )
                return false
            }
            return true
        }

    fun setValue() {
        if (user != null) {
            listLimit = 15
        }
    }

    companion object {
        private const val ADD_TASK_REQUEST = 1
        var db_cnt = 0
    }
}