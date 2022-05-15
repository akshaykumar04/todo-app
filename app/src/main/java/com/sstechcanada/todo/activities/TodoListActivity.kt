package com.sstechcanada.todo.activities

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.sstechcanada.todo.R
import com.sstechcanada.todo.activities.auth.LoginActivity
import com.sstechcanada.todo.activities.auth.ProfileActivity
import com.sstechcanada.todo.adapters.TodoListFirestoreAdapter
import com.sstechcanada.todo.databinding.ActivityTodoListBinding
import com.sstechcanada.todo.models.TodoTaskFirestore
import com.sstechcanada.todo.utils.SaveSharedPreference
import com.sstechcanada.todo.utils.SwipeController
import com.sstechcanada.todo.utils.SwipeControllerActions
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.act_bar.*
import kotlinx.android.synthetic.main.activity_todo_list.*

class TodoListActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val usersColRef = db.collection("Users")
    var userID: String? = null
    var loadingProgressBar: ProgressBar? = null
    var fab: FloatingActionButton? = null
    private var list_limit = 15
    private var mBinding: ActivityTodoListBinding? = null
    private var toolbar_profile: AppCompatImageView? = null
    private var mAuth: FirebaseAuth? = null
    private var user: FirebaseUser? = null
    private var todoListFirestoreAdapter: TodoListFirestoreAdapter? = null
    var editor: SharedPreferences.Editor? = null
    private var mInterstitialAd: InterstitialAd? = null
    private var listId: String? = null
    private var listName: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_todo_list)
        loadingProgressBar = mBinding?.loadingProgressBar
        fab = mBinding?.fab
        if (Integer.valueOf(MasterTodoListActivity.purchaseCode) == 1 || Integer.valueOf(MasterTodoListActivity.purchaseCode) == 2) {
            Log.i("purchase code", MasterTodoListActivity.purchaseCode)
            Log.i("purchase code", "purchaseCode")
            mBinding?.completedTab?.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_baseline_check_circle_24,
                0
            )
        }
        val prefs = getSharedPreferences(LoginActivity.SHAREDPREF, MODE_PRIVATE)
        editor = getSharedPreferences(LoginActivity.SHAREDPREF, MODE_PRIVATE).edit()
        showProgressBar()
        lottieAnimationView = findViewById(R.id.placeholderImage)
        mAuth = FirebaseAuth.getInstance()
        user = mAuth!!.currentUser
        if (user != null) {
            userID = user!!.uid
        }
        if (prefs.getBoolean("flagTodoListFirstRun", true)) {
            mBinding?.buttonTapTargetView?.visibility = View.INVISIBLE
            callWalkThrough()
        }
        setupObservers()
        val adView = mBinding?.adView
        if (SaveSharedPreference.getAdsEnabled(this)) {
            loadFullScreenAds()
            val adRequest = AdRequest.Builder().build()
            adView?.loadAd(adRequest)
        } else {
            adView?.visibility = View.GONE
        }
        toolbar_profile = findViewById(R.id.profile_toolbar)
        Glide.with(this).load(mAuth?.currentUser?.photoUrl).into(profile_toolbar)
        toolbar_profile?.setOnClickListener {
            startActivity(
                Intent(
                    this@TodoListActivity,
                    ProfileActivity::class.java
                )
            )
        }
        fab?.setOnClickListener {
            db_cnt = todoListFirestoreAdapter?.itemCount ?: 0
            Log.i("ItemCount", "FAB Clicked")
            if (LoginActivity.userAccountDetails[1].toInt() > (todoListFirestoreAdapter?.itemCount ?: 0)) {
                setValue()
                if (isLogin) {
                    val intent = Intent(this@TodoListActivity, AddOrEditTaskActivity::class.java)
                    intent.putExtra(
                        getString(R.string.intent_adding_or_editing_key),
                        getString(R.string.add_new_task)
                    )
                    startActivityForResult(intent, ADD_TASK_REQUEST)
                }
            } else {
                if (isLogin) {
                    if (MasterTodoListActivity.purchaseCode != "2") {
                        val intent = Intent(this@TodoListActivity, AppUpgradeActivity::class.java)
                        //                        intent.putExtra(getString(R.string.intent_adding_or_editing_key), getString(R.string.add_new_task));
                        Toasty.info(
                            applicationContext,
                            getString(R.string.upgrade_master_list),
                            Toast.LENGTH_SHORT
                        ).show()
                        startActivity(intent)
                    } else if (MasterTodoListActivity.purchaseCode == "2") {
                        Toasty.warning(
                            applicationContext,
                            "Sorry, You cannot add more to-do items. You have reached the max-limit!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
        mBinding?.completedTab?.setOnClickListener {
            if (Integer.valueOf(MasterTodoListActivity.purchaseCode) == 1 || Integer.valueOf(MasterTodoListActivity.purchaseCode) == 2) {
                startActivity(Intent(this@TodoListActivity, CompletedTodoListActivity::class.java))
            } else {
                startActivity(Intent(this@TodoListActivity, AppUpgradeActivity::class.java))
            }
        }
        rv_todo_list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 12 || dy < 0 && fab!!.isShown) {
                    fab?.hide()
                }
                if (dy < -12 && !fab!!.isShown) {
                    fab?.show()
                }
                if (!recyclerView.canScrollVertically(-1)) {
                    fab?.show()
                }
            }
        })
    }

    private fun setupObservers() {
        fetchIntent()
        //Limit Set
        setValue()
    }

    private fun fetchIntent() {
        if (intent.extras != null) {
            listName = intent.getStringExtra("ListName")
            listId = intent.getStringExtra("ListId")
            if (listId != null && listName != null) {
                setUpFirestoreRecyclerView()
            }
        }
    }

    private fun setUpFirestoreRecyclerView() {
//        listId=getIntent().getStringExtra("ListId");
        Log.i("ListId", "Setupdrecyasdf")
        Log.i("ListId", "n$listId")
        FirebaseCrashlytics.getInstance().log(this.javaClass.simpleName + "listId = " + listId)
        FirebaseCrashlytics.getInstance().log(this.javaClass.simpleName + "UserId = " + userID)
        val query = listId?.let {
            userID?.let { it1 ->
                usersColRef.document(it1).collection("Lists").document(
                    it
                ).collection("Todo").whereEqualTo("Status", "Pending")
                    .orderBy("priority", Query.Direction.DESCENDING)
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
                Log.i("click", "right")
                if (SaveSharedPreference.getAdsEnabled(this@TodoListActivity)) {
                    loadFullScreenAds()
                }
                AlertDialog.Builder(this@TodoListActivity)
                    .setIcon(android.R.drawable.ic_menu_delete)
                    .setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to delete this task?")
                    .setPositiveButton("Yes") { dialog: DialogInterface?, which: Int ->
                        if (SaveSharedPreference.getAdsEnabled(this@TodoListActivity)) {
                            if (mInterstitialAd != null) {
                                mInterstitialAd?.show(this@TodoListActivity)
                                val documentSnapshot =
                                    todoListFirestoreAdapter?.snapshots?.getSnapshot(position)
                                val id = documentSnapshot?.id
                                userID?.let {
                                    listId?.let { it1 ->
                                        id?.let { it2 ->
                                            usersColRef.document(it).collection("Lists")
                                                .document(it1).collection("Todo").document(it2)
                                                .delete()
                                        }
                                    }
                                }
                                Toasty.error(
                                    this@TodoListActivity,
                                    "Task Deleted",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                val documentSnapshot =
                                    todoListFirestoreAdapter!!.snapshots.getSnapshot(position)
                                val id = documentSnapshot.id
                                usersColRef.document(userID!!).collection("Lists")
                                    .document(listId!!).collection("Todo").document(id).delete()
                                Toasty.error(
                                    this@TodoListActivity,
                                    "Task Deleted",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            val documentSnapshot =
                                todoListFirestoreAdapter?.snapshots?.getSnapshot(position)
                            val id = documentSnapshot?.id
                            if (id != null) {
                                userID?.let {
                                    usersColRef.document(it).collection("Lists").document(listId!!)
                                        .collection("Todo").document(id).delete()
                                }
                            }
                            Toasty.error(this@TodoListActivity, "Task Deleted", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                    .setNegativeButton("No", null)
                    .show()
            }

            override fun onLeftClicked(position: Int) {
                val documentSnapshot = todoListFirestoreAdapter!!.snapshots.getSnapshot(position)
                val id = documentSnapshot.id
                val task = documentSnapshot.toObject(
                    TodoTaskFirestore::class.java
                )
                var todoTask: TodoTaskFirestore? = null
                if (task != null) {
                    todoTask = TodoTaskFirestore(
                        task.description,
                        task.priority,
                        task.dueDate,
                        id,
                        task.status,
                        task.category,
                        2,
                        task.benefits,
                        task.benefitsString,
                        task.timestampCompleted
                    )
                }
                val intent = Intent(this@TodoListActivity, AddOrEditTaskActivity::class.java)
                intent.putExtra("Adding or editing", "Edit Task")
                intent.putExtra("Todo", todoTask)
                startActivity(intent)
            }
        })
        val itemTouchhelper = ItemTouchHelper(swipeController)
        itemTouchhelper.attachToRecyclerView(rv_todo_list)
        rv_todo_list.adapter = todoListFirestoreAdapter
        hideProgressBar()
        rv_todo_list.addItemDecoration(object : ItemDecoration() {
            override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                swipeController.onDraw(c)
            }
        })
        db_cnt = todoListFirestoreAdapter!!.itemCount
    }

    override fun onStart() {
        super.onStart()
        if (todoListFirestoreAdapter != null) todoListFirestoreAdapter!!.startListening()
        mBinding!!.listNameTextView.text = listName
    }

    override fun onStop() {
        super.onStop()
        if (todoListFirestoreAdapter != null) todoListFirestoreAdapter!!.stopListening()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.todo_list_menu, menu)
        //For 3 Dot menu
        return false
    }

    //        else if (list_limit <= db_cnt) {
//            //Limit Check
//            Toasty.info(this, getString(R.string.upgrade_todo_list), Toast.LENGTH_LONG, true).show();
//            startActivity(new Intent(TodoListActivity2.this, AppUpgradeActivity2.class));
//            return false;
//        }
    private val isLogin: Boolean
        get() {
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                Toasty.warning(this, getString(R.string.login_first), Toast.LENGTH_LONG).show()
                startActivity(Intent(this@TodoListActivity, LoginActivity::class.java))
                return false
            }
            //        else if (list_limit <= db_cnt) {
//            //Limit Check
//            Toasty.info(this, getString(R.string.upgrade_todo_list), Toast.LENGTH_LONG, true).show();
//            startActivity(new Intent(TodoListActivity2.this, AppUpgradeActivity2.class));
//            return false;
//        }
            return true
        }

    fun setValue() {
        if (user != null) {
            list_limit = 15
        }
    }

    private fun showProgressBar() {
        loadingProgressBar!!.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    private fun hideProgressBar() {
        if (loadingProgressBar!!.visibility == View.VISIBLE) {
            loadingProgressBar!!.visibility = View.INVISIBLE
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this@TodoListActivity, MasterTodoListActivity::class.java))
    }

    private fun loadFullScreenAds() {
        val adRequest = AdRequest.Builder().build()
        //ca-app-pub-3111421321050812/5967628112 our
        //test ca-app-pub-3940256099942544/1033173712
        InterstitialAd.load(this, "ca-app-pub-3111421321050812/5967628112", adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    // The mInterstitialAd reference will be null until
                    // an ad is loaded.
                    mInterstitialAd = interstitialAd
                    mInterstitialAd!!.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                // Called when fullscreen content is dismissed.
                                Log.d("TAG", "The ad was dismissed.")
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                // Called when fullscreen content failed to show.
                                Log.d("TAG", "The ad failed to show.")
                            }

                            override fun onAdShowedFullScreenContent() {
                                // Called when fullscreen content is shown.
                                // Make sure to set your reference to null so you don't
                                // show it a second time.
                                mInterstitialAd = null
                                Log.d("TAG", "The ad was shown.")
                            }
                        }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    // Handle the error
                    mInterstitialAd = null
                }
            })
    }

    private fun callWalkThrough() {
        TapTargetSequence(this)
            .targets(
                TapTarget.forView(fab, "Add Button", "Click here to add a new list item")
                    .outerCircleColor(R.color.chip_5)
                    .outerCircleAlpha(0.98f)
                    .targetCircleColor(R.color.colorUncompletedBackground)
                    .titleTextSize(22)
                    .titleTextColor(R.color.colorUncompletedBackground)
                    .descriptionTextSize(16)
                    .titleTypeface(ResourcesCompat.getFont(this, R.font.poppins_semibold))
                    .textTypeface(ResourcesCompat.getFont(this, R.font.raleway_medium))
                    .descriptionTextColor(R.color.black)
                    .textColor(R.color.black)
                    .dimColor(R.color.black)
                    .drawShadow(true)
                    .cancelable(false)
                    .tintTarget(true)
                    .transparentTarget(true)
                    .targetRadius(80),
                TapTarget.forView(
                    mBinding?.buttonTapTargetView,
                    "List Items",
                    "Swipe right and click on the pencil icon to edit a list item. " +
                            "\n\nSwipe left and click on the garbage can icon to delete a list item. " +
                            "\n\nThis app works better for one-time tasks (e.g. \"buy glasses\") than recurring ones (e.g. \"go to gym every day\")."
                )
                    .outerCircleColor(R.color.chip_5)
                    .outerCircleAlpha(0.98f)
                    .targetCircleColor(R.color.colorUncompletedBackground)
                    .titleTextSize(22)
                    .titleTextColor(R.color.colorUncompletedBackground)
                    .descriptionTextSize(16)
                    .titleTypeface(ResourcesCompat.getFont(this, R.font.poppins_semibold))
                    .textTypeface(ResourcesCompat.getFont(this, R.font.raleway_medium))
                    .descriptionTextColor(R.color.black)
                    .textColor(R.color.black)
                    .dimColor(R.color.black)
                    .drawShadow(true)
                    .cancelable(false)
                    .tintTarget(true)
                    .transparentTarget(true)
                    .targetRadius(60)
            ).listener(object : TapTargetSequence.Listener {
                override fun onSequenceFinish() {

//                Toast.makeText(TodoListActivity2.this,"Sequence Finished",Toast.LENGTH_SHORT).show();
                    Toasty.success(
                        this@TodoListActivity,
                        "You are all set now!",
                        Toast.LENGTH_SHORT
                    ).show()
                    //                flagTodoListFirstRun = false;
                    mBinding?.buttonTapTargetView?.visibility = View.GONE
                    editor?.putBoolean("flagTodoListFirstRun", false)
                    editor?.apply()
                }

                override fun onSequenceStep(lastTarget: TapTarget, targetClicked: Boolean) {}
                override fun onSequenceCanceled(lastTarget: TapTarget) {
                    editor?.putBoolean("flagTodoListFirstRun", false)
                    mBinding?.buttonTapTargetView?.visibility = View.GONE
                    editor?.apply()
                }
            }).start()
    }

    companion object {
        private const val ADD_TASK_REQUEST = 1
        var db_cnt = 0

        @JvmField
        var lottieAnimationView: LottieAnimationView? = null

        @JvmStatic
        fun showPlaceHolder() {
            lottieAnimationView?.visibility = View.VISIBLE
        }

        @JvmStatic
        fun hidePlaceHolder() {
            lottieAnimationView?.visibility = View.INVISIBLE
        }
    }
}