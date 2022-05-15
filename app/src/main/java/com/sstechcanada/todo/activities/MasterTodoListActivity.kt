package com.sstechcanada.todo.activities

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Canvas
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.BillingProcessor.IBillingHandler
import com.anjlab.android.iab.v3.TransactionDetails
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.sstechcanada.todo.R
import com.sstechcanada.todo.activities.auth.LoginActivity
import com.sstechcanada.todo.activities.auth.ProfileActivity
import com.sstechcanada.todo.adapters.MasterListFirestoreAdapter
import com.sstechcanada.todo.adapters.MasterListGridViewAdapter
import com.sstechcanada.todo.custom_views.MasterIconGridItemView
import com.sstechcanada.todo.utils.Constants
import com.sstechcanada.todo.utils.SaveSharedPreference
import com.sstechcanada.todo.utils.SwipeController
import com.sstechcanada.todo.utils.SwipeControllerActions
import es.dmoral.toasty.Toasty
import hotchemi.android.rate.AppRate
import kotlinx.android.synthetic.main.act_bar.*
import kotlinx.android.synthetic.main.activity_master_todo_list.*

class MasterTodoListActivity : AppCompatActivity(), IBillingHandler {
    var userID: String? = null
    private val db = FirebaseFirestore.getInstance()
    private val usersColRef = db.collection("Users")
    private val mHandler: Handler = Handler(Looper.getMainLooper())
    private var listLimit = 15

    private var mAuth: FirebaseAuth? = null
    private var user: FirebaseUser? = null
    var progressBar: ProgressBar? = null

    //    String
    private var selectedDrawable = 0
    private var sdrawable = 0
    private var masterListFirestoreAdapter: MasterListFirestoreAdapter? = null
    private var gridView: GridView? = null
    private var mInterstitialAd: InterstitialAd? = null
    private var gridAdapter: MasterListGridViewAdapter? = null
    private var doubleBackToExitPressedOnce = false
    private var bp: BillingProcessor? = null
    var editor: SharedPreferences.Editor? = null
    private val mRunnable = Runnable { doubleBackToExitPressedOnce = false }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_master_todo_list)

        val prefs = getSharedPreferences(LoginActivity.SHAREDPREF, MODE_PRIVATE)
        editor = getSharedPreferences(LoginActivity.SHAREDPREF, MODE_PRIVATE).edit()
        listDrawable = arrayOf(
            R.drawable.master_list_default_icon,
            R.drawable.idea,
            R.drawable.ic_lock,
            R.drawable.ic_to_do_list,
            R.drawable.circle_per_item,
            R.drawable.sport,
            R.drawable.movie,
            R.drawable.globe,
            R.drawable.music,
            R.drawable.heart,
            R.drawable.diet,
            R.drawable.book,
            R.drawable.shopping_cart
        )
        openRatingPopup()
        fetchIntent()

        mAuth = FirebaseAuth.getInstance()
        user = mAuth?.currentUser
        user?.let {
            userID = it.uid
        }
        if (user != null) {
            userID = user?.uid
        }
        getPurchaseCode()

        setUpFirestoreRecyclerView()
        if (prefs.getBoolean("flagMasterListFirstRun", true)) {
            buttonTapTargetView.visibility = View.INVISIBLE
            callWalkThrough()
        }


        //Limit Set
        setValue()
        Glide.with(this).load(mAuth?.currentUser?.photoUrl).into(profile_toolbar)
        profile_toolbar.setOnClickListener {
            startActivity(
                Intent(this@MasterTodoListActivity, ProfileActivity::class.java)
            )
        }
        fab.setOnClickListener {
            if (LoginActivity.userAccountDetails[0].toInt() > (masterListFirestoreAdapter?.itemCount ?: 0)
            ) {
                Log.i("purchasecode", "masterlist limit :" + LoginActivity.userAccountDetails[0])
                Log.i("purchasecode", "masterlist items :" + (masterListFirestoreAdapter?.itemCount
                    ?: 0))
                setValue()
                if (isLogin) {
                    addNewListAlert()
                }
            } else {
                if (isLogin) {
                    if (purchaseCode != "2") {
                        Toasty.info(
                            applicationContext,
                            getString(R.string.upgrade_master_list),
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent =
                            Intent(this@MasterTodoListActivity, AppUpgradeActivity::class.java)
                        //                        intent.putExtra(getString(R.string.intent_adding_or_editing_key), getString(R.string.add_new_task));
                        startActivity(intent)
                    } else if (purchaseCode == "2") {
                        Toasty.warning(
                            applicationContext,
                            "Sorry, You cannot add more to-do list. You have reached the max-limit!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun getPurchaseCode() {
        showProgressBar()
        userID?.let {
            usersColRef.document(it).get()
                .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                    purchaseCode = documentSnapshot["purchase_code"].toString()
                    db.collection("UserTiers").document(purchaseCode).get()
                        .addOnSuccessListener { documentSnapshot1: DocumentSnapshot ->
                            Log.i("purchaseCode", "purchase code :$purchaseCode")
                            Log.i(
                                "purchaseCode",
                                "new :" + documentSnapshot1["masterListLimit"].toString()
                            )
                            LoginActivity.userAccountDetails.add(
                                0,
                                documentSnapshot1["masterListLimit"].toString()
                            )
                            LoginActivity.userAccountDetails.add(
                                1,
                                documentSnapshot1["todoItemLimit"].toString()
                            )
                            if (purchaseCode != "0") {
                                bp = BillingProcessor(
                                    this@MasterTodoListActivity,
                                    getString(R.string.license_key),
                                    this@MasterTodoListActivity
                                )
                                bp?.initialize()
                                SaveSharedPreference.setAdsEnabled(this, false)
                            } else {
                                hideProgressBar()
                                adView?.visibility = View.VISIBLE
                                SaveSharedPreference.setAdsEnabled(this, true)
                                if (SaveSharedPreference.getAdsEnabled(this)) {
                                    loadFullScreenAds()
                                    val adRequest = AdRequest.Builder().build()
                                    adView?.loadAd(adRequest)
                                } else {
                                    adView?.visibility = View.GONE
                                }
                            }
                        }
                }.addOnFailureListener { }
        }
    }

    private fun addNewListAlert() {
        val inflater = layoutInflater
        val alertLayout = inflater.inflate(R.layout.add_list_dialog, null)
        val alert = AlertDialog.Builder(this)
        alert.setTitle("Add List")

        alert.setView(alertLayout)
        gridView = alertLayout.findViewById(R.id.grid_view_alert)

        progressBar = alertLayout.findViewById(R.id.progress_circular)
        val bannerAd: AdView = alertLayout.findViewById(R.id.adView)
        loadImages(0)

        alert.setCancelable(false)
        alert.setNegativeButton("Cancel") { _: DialogInterface?, _: Int -> }
        alert.setPositiveButton("Done") { _: DialogInterface?, _: Int ->
            sdrawable = selectedDrawable
            val name =
                (alertLayout.findViewById<View>(R.id.editTextListName) as EditText).text.toString()

            userID?.let { usersColRef.document(it).collection("Lists") }
            val newList: MutableMap<String, Any> = HashMap()
            newList["ListName"] = name
            newList["positionImage"] = sdrawable
            userID?.let {
                usersColRef.document(it).collection("Lists").document().set(newList)
                    .addOnSuccessListener {
                        Toasty.success(
                            this@MasterTodoListActivity,
                            "New List Successfully Added"
                        ).show()
                    }
                    .addOnFailureListener {
                        Toasty.error(
                            this@MasterTodoListActivity,
                            "Something went wrong"
                        ).show()
                    }
            }
        }
        if (SaveSharedPreference.getAdsEnabled(this)) {
            bannerAd.loadAd(AdRequest.Builder().build())
        } else {
            bannerAd.visibility = View.GONE
        }
        val dialog = alert.create()
        dialog.show()
    }

    fun editListAlert(oldListName: String?, oldListIconPosition: Int, documentSnapshotId: String?) {
        val inflater = layoutInflater
        val alertLayout = inflater.inflate(R.layout.add_list_dialog, null)
        val alert = AlertDialog.Builder(this)
        alert.setTitle("Edit List")
        alert.setView(alertLayout)
        gridView = alertLayout.findViewById(R.id.grid_view_alert)
        progressBar = alertLayout.findViewById(R.id.progress_circular)
        val bannerAd: AdView = alertLayout.findViewById(R.id.adView)

        loadImages(oldListIconPosition)
        alert.setCancelable(false)
        alert.setNegativeButton("Cancel") { _: DialogInterface?, _: Int -> }
        alert.setPositiveButton("Done") { _: DialogInterface?, _: Int ->
            sdrawable = selectedDrawable
            val name =
                (alertLayout.findViewById<View>(R.id.editTextListName) as EditText).text.toString()
            //            String description = ((EditText) alertLayout.findViewById(R.id.editTextListDescription)).getText().toString();
            userID?.let { usersColRef.document(it).collection("Lists") }
            val list: MutableMap<String, Any> = HashMap()
            list["ListName"] = name
            list["positionImage"] = sdrawable
            //            list.put("ListDescription", description);
            userID?.let {
                documentSnapshotId?.let { it1 ->
                    usersColRef.document(it).collection("Lists").document(it1)
                        .update(list).addOnSuccessListener {
                            Toasty.success(this@MasterTodoListActivity, "List Successfully Edited").show()
                        }
                        .addOnFailureListener {
                            Toasty.error(this@MasterTodoListActivity, "Something went wrong").show()
                        }
                }
            }
        }
        if (SaveSharedPreference.getAdsEnabled(this)) {
            bannerAd.loadAd(AdRequest.Builder().build())
        } else {
            bannerAd.visibility = View.GONE
        }
        val dialog = alert.create()
        dialog.show()
        val listNameEditText = dialog.findViewById<EditText>(R.id.editTextListName)
        listNameEditText?.setText(oldListName)
    }

    private fun loadImages(iconPosition: Int) {
        gridAdapter = MasterListGridViewAdapter(listDrawable, this@MasterTodoListActivity)
        gridView?.adapter = gridAdapter
        gridView?.visibility = View.VISIBLE
        gridAdapter?.selectedPosition = iconPosition
        gridView?.onItemClickListener =
            OnItemClickListener { _: AdapterView<*>?, v: View, position: Int, _: Long ->
                Log.i("gridView", "on click")
                val selectedIndex = gridAdapter?.selectedPosition
                if (selectedIndex == position) {
                    (v as MasterIconGridItemView).display(false)
                    gridAdapter?.selectedPosition = -1
                    selectedDrawable = iconPosition
                } else {
                    Log.i("gridView", position.toString())
                    if (gridAdapter?.selectedPosition != -1) {
                        (gridView?.getChildAt(gridAdapter?.selectedPosition ?: 0) as? MasterIconGridItemView)?.display(
                            false
                        )
                    }
                    selectedDrawable = position
                    (v as MasterIconGridItemView).display(true)
                    gridAdapter?.selectedPosition = position
                }
            }
        progressBar?.visibility = View.INVISIBLE
    }

    private fun setUpFirestoreRecyclerView() {
        val query: CollectionReference? = userID?.let { usersColRef.document(it).collection("Lists") }
        val options = query?.let {
            FirestoreRecyclerOptions.Builder<com.sstechcanada.todo.models.List>()
                .setQuery(it, com.sstechcanada.todo.models.List::class.java).build()
        }
        masterListFirestoreAdapter = options?.let { MasterListFirestoreAdapter(it, this) }
        rv_todo_list.setHasFixedSize(true)
        rv_todo_list.layoutManager = LinearLayoutManager(this)
        val swipeController = SwipeController(this, object : SwipeControllerActions() {
            override fun onRightClicked(position: Int) {
                Log.i("cluck", "right")
                AlertDialog.Builder(this@MasterTodoListActivity)
                    .setIcon(android.R.drawable.ic_menu_delete)
                    .setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to delete this list?")
                    .setPositiveButton("Yes") { _: DialogInterface?, _: Int ->
                        if (SaveSharedPreference.getAdsEnabled(
                                applicationContext
                            )
                        ) {
                            if (mInterstitialAd != null) {
                                mInterstitialAd?.show(this@MasterTodoListActivity)
                                val documentSnapshot =
                                    masterListFirestoreAdapter?.snapshots?.getSnapshot(position)
                                val id = documentSnapshot?.id
                                userID?.let {
                                    if (id != null) {
                                        usersColRef.document(it).collection("Lists").document(id)
                                            .delete()
                                    }
                                }
                            } else {
                                val documentSnapshot =
                                    masterListFirestoreAdapter?.snapshots?.getSnapshot(position)
                                val id = documentSnapshot?.id
                                userID?.let {
                                    if (id != null) {
                                        usersColRef.document(it).collection("Lists").document(id)
                                            .delete()
                                    }
                                }
                            }
                        } else {
                            val documentSnapshot =
                                masterListFirestoreAdapter?.snapshots?.getSnapshot(position)
                            val id = documentSnapshot?.id
                            userID?.let {
                                if (id != null) {
                                    usersColRef.document(it).collection("Lists").document(id).delete()
                                }
                            }
                            Toasty.error(
                                this@MasterTodoListActivity,
                                "List Deleted",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    .setNegativeButton("No", null)
                    .show()
            }

            override fun onLeftClicked(position: Int) {
                Log.i("cluck", "left")
                val documentSnapshot = masterListFirestoreAdapter?.snapshots?.getSnapshot(position)
                val list = documentSnapshot?.toObject(
                    com.sstechcanada.todo.models.List::class.java
                )
                //                List list=masterListFirestoreAdapter.getItem(position);
                val oldListName = list?.listName
                //                String oldListDescription = list.getListDescription();
                val oldListIconPosition = list?.positionImage
                if (oldListIconPosition != null) {
                    editListAlert(oldListName, oldListIconPosition, documentSnapshot.id)
                }
            }
        })
        val itemTouchhelper = ItemTouchHelper(swipeController)
        itemTouchhelper.attachToRecyclerView(rv_todo_list)
        rv_todo_list.adapter = masterListFirestoreAdapter
        rv_todo_list.addItemDecoration(object : ItemDecoration() {
            override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                swipeController.onDraw(c)
            }
        })
        list_cnt = masterListFirestoreAdapter?.itemCount ?: 0
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
                    mInterstitialAd?.fullScreenContentCallback =
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
                                Toasty.error(
                                    this@MasterTodoListActivity,
                                    "List Deleted",
                                    Toast.LENGTH_SHORT
                                ).show()
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

    override fun onStart() {
        super.onStart()
        masterListFirestoreAdapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        masterListFirestoreAdapter?.stopListening()
    }

    //        return true;
    private val isLogin: Boolean
        get() {
            val user = FirebaseAuth.getInstance().currentUser
            return if (user == null) {
                Toasty.warning(this, getString(R.string.login_first), Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@MasterTodoListActivity, LoginActivity::class.java))
                false
            } else {
                true
            }
            //        return true;
        }

    fun setValue() {
        if (user != null) {
            listLimit = 15
        }
    }

    private fun showProgressBar() {
        loadingProgressBar.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    private fun hideProgressBar() {
        if (loadingProgressBar.visibility == View.VISIBLE) {
            loadingProgressBar.visibility = View.INVISIBLE
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacks(mRunnable)
    }

    override fun onBackPressed() {

//            if(getSupportFragmentManager().findFragmentById(R.id.fragment_container).getParentFragment()==R.layout.fragment_profile){
        if (doubleBackToExitPressedOnce) {
            finishAffinity()
            return
        }
        doubleBackToExitPressedOnce = true
        Toasty.info(this, "Please press back again to exit", Toast.LENGTH_SHORT).show()
        mHandler.postDelayed(mRunnable, 2000)
    }

    private fun callWalkThrough() {
        TapTargetSequence(this)
            .targets(
                TapTarget.forView(fab, "Add Button", "Click here to add a new list")
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
                    buttonTapTargetView,
                    "List",
                    "1: Swipe right and click on the pencil icon to update a list. \n2: Swipe left and click on the garbage can icon to delete a list."
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

//                Toast.makeText(MasterTodoListActivity.this,"Sequence Finished",Toast.LENGTH_SHORT).show();
                    Toasty.success(this@MasterTodoListActivity, "Awesome!", Toast.LENGTH_SHORT)
                        .show()

//                flagMasterListFirstRun = false;
                    buttonTapTargetView.visibility = View.GONE
                    editor?.putBoolean("flagMasterListFirstRun", false)
                    editor?.apply()
                }

                override fun onSequenceStep(lastTarget: TapTarget, targetClicked: Boolean) {}
                override fun onSequenceCanceled(lastTarget: TapTarget) {
                    buttonTapTargetView.visibility = View.GONE
                    editor?.putBoolean("flagMasterListFirstRun", false)
                    editor?.apply()
                }
            }).start()
    }

    private fun isUserSubscribed(purchaseCode: String) {
        try {
            val purchaseResult = bp?.loadOwnedPurchasesFromGoogle()


//            Toast.makeText(MasterTodoListActivity.this, "Inside billing: "+purchaseResult+ " "+purchaseCode, Toast.LENGTH_SHORT).show();
            var purchaseID = ""
            if (user != null) {
                when (purchaseCode) {
                    "1" -> {
                        purchaseID = "tier1"
                    }
                    "2" -> {
                        purchaseID = "tier2"
                    }
                    "3" -> {
                        purchaseID = "adfree"
                    }
                }
                if (purchaseResult == true) {
                    val subscriptionTransactionDetails =
                        bp?.getSubscriptionTransactionDetails(purchaseID)
                    if (subscriptionTransactionDetails != null) {
                        //User is still subscribed
//                        Toast.makeText(MasterTodoListActivity.this, "Inside billing+ user is still subscribed in", Toast.LENGTH_SHORT).show();
                    } else {
                        //Not subscribed
                        refreshPurchaseCodeInDatabase()
                        //                        Toast.makeText(MasterTodoListActivity.this, "Inside billing+ user is not subscribed", Toast.LENGTH_SHORT).show();
                    }
                }

//                Toast.makeText(MasterTodoListActivity.this, "Inside billing +user iobject not null" + purchaseID, Toast.LENGTH_SHORT).show();
            }
            hideProgressBar()
        } catch (e: Exception) {
//            Toast.makeText(MasterTodoListActivity.this, "Inside billing +Exception in "+e, Toast.LENGTH_SHORT).show();
            hideProgressBar()
        }
    }

    private fun refreshPurchaseCodeInDatabase() {

//        Toast.makeText(this, "set purchase code in db", Toast.LENGTH_SHORT).show();
        val purchaseCode: MutableMap<String, String> = HashMap()
        purchaseCode["purchase_code"] = "0"
        userID?.let {
            db.collection("Users").document(it).set(
                purchaseCode,
                SetOptions.merge()
            ).addOnSuccessListener { getPurchaseCode() }
        }
    }

    private fun openRatingPopup() {
        // callback listener.
        AppRate.with(this)
            .setInstallDays(1) // default 10, 0 means install day.
            .setLaunchTimes(25) // default 10
            .setRemindInterval(2) // default 1
            .setShowLaterButton(true)
            .setTitle(getString(R.string.will_you_rate_us_5_stars)) // default true
            .setDebug(false) // default false
            .setOnClickButtonListener { which: Int ->
                Log.d(
                    MasterTodoListActivity::class.java.name, which.toString()
                )
            }
            .monitor()
        AppRate.showRateDialogIfMeetsConditions(this)

        //Show Dialog Instantly
        //AppRate.with(this).showRateDialog(this);
    }

    private fun fetchIntent() {
        if (intent.hasExtra(Constants.TODO_RATE_APP)) {
            AppRate.with(this).showRateDialog(this)
        }
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {}
    override fun onPurchaseHistoryRestored() {}
    override fun onBillingError(errorCode: Int, error: Throwable?) {}
    override fun onBillingInitialized() {
        isUserSubscribed(purchaseCode)
    }

    companion object {
        var list_cnt = 0

        //    ArrayList<String>
        lateinit var listDrawable: Array<Int>
        var listId: String? = null
        var listName: String? = null
        var purchaseCode = "0"
    }
}