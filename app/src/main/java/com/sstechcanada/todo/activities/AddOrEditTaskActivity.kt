package com.sstechcanada.todo.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.*
import com.sstechcanada.todo.R
import com.sstechcanada.todo.activities.auth.LoginActivity
import com.sstechcanada.todo.activities.auth.ProfileActivity
import com.sstechcanada.todo.adapters.GridViewAdapter
import com.sstechcanada.todo.custom_views.GridItemView
import com.sstechcanada.todo.databinding.ActivityAddOrEditTaskBinding
import com.sstechcanada.todo.models.Category
import com.sstechcanada.todo.models.TodoTaskFirestore
import com.sstechcanada.todo.utils.SaveSharedPreference
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.act_bar.*
import kotlinx.android.synthetic.main.activity_add_or_edit_task.*
import kotlinx.android.synthetic.main.activity_category.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class AddOrEditTaskActivity : AppCompatActivity() {
    private var categories: MutableList<Category>? = null
    private var userID: String? = null
    private var taskCompleted = "Pending"
    var db = FirebaseFirestore.getInstance()
    private var benefitCollectionRef: CollectionReference? = null
    private var userColRef: CollectionReference? = null
    private var mBinding: ActivityAddOrEditTaskBinding? = null
    private var mTaskId: String? = "-1"
    private var mAddOrEdit: String? = null
    private var adapter: GridViewAdapter? = null
    private var selectedStrings: ArrayList<String>? = null
    private var categoryCount = 0
    private var chipCount = 0
    private var selectedResult = ""
    private var todoTaskToAddOrEdit: TodoTaskFirestore? = null
    private var mAuth: FirebaseAuth? = null
    private var gridView: GridView? = null
    private var progressBar: ProgressBar? = null
    var editor: SharedPreferences.Editor? = null
    private var record: Array<String>? = null
    private var description: String? = null
    private var alertDialog: AlertDialog? = null
    private var mInterstitialAd: InterstitialAd? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_or_edit_task)
        mBinding?.includeToolBar?.profileToolbar?.setOnClickListener {
            startActivity(
                Intent(this@AddOrEditTaskActivity, ProfileActivity::class.java)
            )
        }
        mBinding?.includeToolBar?.fabPauseAds?.setOnClickListener {
            startActivity(Intent(this, RemoveAdsActivity::class.java))
        }
        arrow_back.visibility = View.VISIBLE
        arrow_back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        editor = getSharedPreferences(LoginActivity.SHAREDPREF, MODE_PRIVATE).edit()
        mAuth = FirebaseAuth.getInstance()
        userID = mAuth?.currentUser?.uid
        FirebaseCrashlytics.getInstance()
            .log(this.javaClass.simpleName + "listId = " + MasterTodoListActivity.listId)
        FirebaseCrashlytics.getInstance().log(this.javaClass.simpleName + "UserId = " + userID)
        benefitCollectionRef = userID?.let { db.collection("Users").document(it).collection("Benefits") }
        userColRef = MasterTodoListActivity.listId?.let {
            userID?.let { it1 ->
                db.collection("Users").document(it1).collection("Lists")
                    .document(it).collection("Todo")
            }
        }
        Log.i("ListId", "Add or edit: " + MasterTodoListActivity.listId)
        mAuth?.currentUser?.photoUrl?.let {
            Glide.with(this).load(it).into(profile_toolbar)
        }
        if (SaveSharedPreference.getAdsEnabled(this)) {
            loadBannerAds()
            loadFullScreenAds()
        }
        loadCategories()

        val dueDate: Long
        if (savedInstanceState == null) {
            val bundle = intent.extras
            mAddOrEdit = bundle?.getString(getString(R.string.intent_adding_or_editing_key))
            if (mAddOrEdit == getString(R.string.add_new_task)) {
                Log.d("AddOrEdit", "Adding New Task")
            } else {
                todoTaskToAddOrEdit = bundle?.getParcelable(getString(R.string.intent_todo_key))
                selectedResult = todoTaskToAddOrEdit?.benefitsString.toString()
                mTaskId = todoTaskToAddOrEdit?.documentID
                mBinding?.etTaskDescription?.setText(todoTaskToAddOrEdit!!.description)
                taskCompleted = todoTaskToAddOrEdit?.status.toString()
                mBinding?.fabCompleted?.setOnClickListener {
                    AlertDialog.Builder(this@AddOrEditTaskActivity)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Confirm Complete")
                        .setMessage("Are you sure you want to mark this task as completed?")
                        .setPositiveButton("Yes") { _: DialogInterface?, _: Int ->
//                            if (SaveSharedPreference.getAdsEnabled(this)) {
////                                    if (mInterstitialAd != null) {
////                                        mInterstitialAd?.show(this@AddOrEditTaskActivity)
////                                    }
//                            }
                            addOrUpdateTask(isCompleted = true)
                        }
                        .setNegativeButton("No") { _: DialogInterface?, _: Int ->
                        }
                        .show()
                }
//                mBinding?.cbTaskCompleted?.isChecked = taskCompleted == "Completed"
                if (taskCompleted == "Completed") {
                    mBinding?.timestampCompletedtextView?.text =
                        todoTaskToAddOrEdit?.timestampCompleted
                    mBinding?.timestampCompletedtextView?.visibility = View.VISIBLE
                }
                dueDate = todoTaskToAddOrEdit?.dueDate!!
                Log.d(TAG, "Due date in millis $dueDate")
            }
        } else {
            mAddOrEdit = savedInstanceState.getString(getString(R.string.add_or_edit_key))
            mTaskId = savedInstanceState.getString(getString(R.string.id_key))
            mBinding?.etTaskDescription?.setText(savedInstanceState.getString(getString(R.string.task_description_key)))
            if (taskCompleted == "Completed") {
//                mBinding?.cbTaskCompleted?.isChecked = true
                mBinding?.timestampCompletedtextView?.text =
                    todoTaskToAddOrEdit?.timestampCompleted
            } else {
//                mBinding?.cbTaskCompleted?.isChecked = false
            }
        }
        title = mAddOrEdit
        if (mAddOrEdit == getString(R.string.add_new_task)) {
//            ADDING NEW TASK
            mBinding?.includeToolBar?.toolbarTitle?.text = getString(R.string.add_task)
            mBinding?.btnAddOrUpdateTask?.setText(R.string.add_task)
            mBinding?.fabCompleted?.visibility = View.GONE
            mBinding?.fabDeleteItem?.visibility = View.GONE
            mBinding?.timestampCompletedtextView?.visibility = View.GONE
        } else {
            mBinding?.includeToolBar?.toolbarTitle?.text = getString(R.string.update_task)
            mBinding?.btnAddOrUpdateTask?.setText(R.string.update_task)
            mBinding?.fabDeleteItem?.visibility = View.VISIBLE
            mBinding?.fabCompleted?.visibility = View.VISIBLE
        }

        addCategories.setOnClickListener { showBenefitsBottomSheet() }

        //Grid View End
        if (todoTaskToAddOrEdit != null) {
            record = todoTaskToAddOrEdit?.benefitsString?.let { convertStringToArray(it) }
            categoryCount = record?.size!!
            displayBenefits(record)
        }
        MobileAds.initialize(this) { }
        mBinding?.fabDeleteItem?.setOnClickListener { deleteTodoItem() }
        mBinding?.btnAddOrUpdateTask?.setOnClickListener { addOrUpdateTask() }

        val prefs = getSharedPreferences(LoginActivity.SHAREDPREF, MODE_PRIVATE)
        if (prefs.getBoolean("flagTodoBenefitsAddEditTaskFirstRun", true)) {
            buttonTapTargetView?.visibility = View.INVISIBLE
            showBenefitsTutorial()
        }
    }

    private fun deleteTodoItem() {
        AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_menu_delete)
            .setTitle("Confirm Delete")
            .setMessage("Are you sure you want to delete this task?")
            .setPositiveButton("Yes") { _: DialogInterface?, _: Int ->
                todoTaskToAddOrEdit?.documentID?.let {
                    userColRef?.document(it)?.delete()
                }
                Toasty.error(
                    this@AddOrEditTaskActivity,
                    "Task Deleted",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun loadBannerAds() {
        val adView = findViewById<AdView>(R.id.adView)
        if (SaveSharedPreference.getAdsEnabled(this)) {
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
        } else {
            adView.visibility = View.GONE
        }
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        // save values on device rotation
        outState.putString(
            getString(R.string.task_description_key),
            mBinding?.etTaskDescription?.text.toString()
        )
        outState.putString(getString(R.string.add_or_edit_key), mAddOrEdit)
        outState.putString(getString(R.string.id_key), mTaskId)
        //        outState.putString("category", selectedResult);
        super.onSaveInstanceState(outState)
    }

    private fun addOrUpdateTask(isCompleted: Boolean?= false) {
        loadingProgressBarUpdate?.visibility = View.VISIBLE
        description = mBinding?.etTaskDescription?.text.toString().trim { it <= ' ' }
        if (description == "") {
            Toasty.warning(
                this,
                getString(R.string.description_cannot_be_empty),
                Toast.LENGTH_SHORT,
                true
            ).show()
            loadingProgressBarUpdate?.visibility = View.GONE
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        } else {
            //Making First Char Capital
            description = description?.substring(0, 1)?.uppercase(Locale.getDefault()) + description?.substring(1)
            uploadDataToFirestore(isCompleted = isCompleted)

            val returnIntent = Intent()
            setResult(RESULT_OK, returnIntent)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun uploadDataToFirestore(isCompleted: Boolean?= false) {
        val benefitsArrayFirestore: List<String> = if (record != null) {
            listOf(*record!!)
        } else {
            emptyList()
        }
        if (mAddOrEdit == getString(R.string.add_new_task)) {
            val newTaskMap: MutableMap<String, Any?> = HashMap()
            newTaskMap["description"] = description
            newTaskMap["priority"] = benefitsArrayFirestore.size
            newTaskMap["Benefits"] = benefitsArrayFirestore
            val taskStatus = "Pending"
            newTaskMap["Status"] = taskStatus
            newTaskMap["TimestampCompleted"] = " "
            userColRef?.document()?.set(newTaskMap)?.addOnSuccessListener {
                loadingProgressBarUpdate.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                Toasty.success(
                    this@AddOrEditTaskActivity,
                    "New list item added successfully",
                    Toasty.LENGTH_SHORT
                ).show()
                finish()
                onBackPressedDispatcher.onBackPressed()
            }?.addOnFailureListener {
                loadingProgressBarUpdate.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                Toasty.error(
                    this@AddOrEditTaskActivity,
                    "Something went wrong",
                    Toasty.LENGTH_SHORT
                ).show()
            }
        } else {
            val updateTaskMap: MutableMap<String, Any?> = HashMap()
            updateTaskMap["description"] = description
            updateTaskMap["priority"] = benefitsArrayFirestore.size
            updateTaskMap["Benefits"] = benefitsArrayFirestore
            val taskStatus: String
            if (isCompleted == true) {
                taskStatus = "Completed"
                val calendar = Calendar.getInstance()
                val dateStr = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.time)
                val sdf = SimpleDateFormat("h:mm a")
                Log.i("dateTime", "TimestampCompleted$dateStr")
                val timeStr = sdf.format(calendar.time)
                updateTaskMap["TimestampCompleted"] = "$dateStr $timeStr"
            } else {
                taskStatus = "Pending"
            }
            updateTaskMap["Status"] = taskStatus
            Log.i("task456", todoTaskToAddOrEdit?.documentID + "jjj")
            todoTaskToAddOrEdit?.documentID?.let {
                userColRef?.document(it)
                    ?.set(updateTaskMap, SetOptions.merge())?.addOnSuccessListener {
                        loadingProgressBarUpdate?.visibility = View.GONE
                        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                        Toasty.success(
                            this@AddOrEditTaskActivity,
                            "List item updated successfully",
                            Toasty.LENGTH_SHORT
                        ).show()
                        finish()
                       onBackPressedDispatcher.onBackPressed()
                    }?.addOnFailureListener {
                        loadingProgressBarUpdate?.visibility = View.GONE
                        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                        Toasty.error(
                            this@AddOrEditTaskActivity,
                            "Something went wrong",
                            Toasty.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }

    private fun loadCategories() {
        benefitCollectionRef?.addSnapshotListener { value: QuerySnapshot?, _: FirebaseFirestoreException? ->
            categories = ArrayList()
            selectedStrings = ArrayList()
            categories?.clear()
            if (value != null) {
                for (dataSnapshot in value) {
                    val category = Category(dataSnapshot.id, dataSnapshot["category_name"] as String?)
                    categories?.add(category)
                }
            }

            //iterating through all the nodes
            adapter = GridViewAdapter(categories, this@AddOrEditTaskActivity)
            gridView?.adapter = adapter
            gridView?.visibility = View.VISIBLE
            record = convertStringToArray(selectedResult)
            for (i in categories!!.indices) {
                for (j in record!!.indices) {
                    if (record!![j] == categories?.get(i)?.category_name) {
                        adapter!!.selectedPositions?.add(i)
                        selectedStrings!!.add(record!![j])
                    }
                }
            }
            selectedResult = ""
            selectedResult = convertArrayToString(selectedStrings)
            progressBar?.visibility = View.INVISIBLE
        }
        gridView?.onItemClickListener =
            OnItemClickListener { parent: AdapterView<*>, v: View, position: Int, _: Long ->
                val selectedIndex = adapter?.selectedPositions?.indexOf(position)
                if (selectedIndex != null) {
                    if (selectedIndex > -1) {
                        adapter?.selectedPositions?.removeAt(selectedIndex)
                        (v as GridItemView).display(false)
                        selectedStrings?.remove(parent.getItemAtPosition(position))
                    } else {
                        adapter?.selectedPositions?.add(position)
                        (v as GridItemView).display(true)
                        selectedStrings?.add(parent.getItemAtPosition(position) as String)
                    }
                }
            }
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

    @SuppressLint("SetTextI18n")
    private fun displayBenefits(record: Array<String>?) {
        val colors = intArrayOf(
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
        )
        chipGroup?.removeAllViews()
        chipGroup?.visibility = View.VISIBLE
        chipCount = record?.size!!
        tv_add_more?.text = "Click here to add more Benefits"
        if (chipCount == 0) {
            tv_category_number?.text = "$chipCount Benefits Selected"
            return
        }
        for (i in 0 until chipCount) {
            val chip = Chip(this)
            val drawable = ChipDrawable.createFromAttributes(
                this,
                null,
                0,
                R.style.Widget_MaterialComponents_Chip_Choice
            )
            chip.setChipDrawable(drawable)
            if (i in 0..9) {
                chip.setChipBackgroundColorResource(colors[i])
            } else if (i >= 10) {
                chip.setChipBackgroundColorResource(colors[i % 10])
            }
            chip.text = record[i] + ""
            chipGroup?.childCount
            chip.chipStartPadding
            chip.chipEndPadding
            chip.setTextAppearanceResource(R.style.SmallerText)
            chipGroup?.addView(chip)
            tv_category_number?.text = "$chipCount Benefits Selected"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (alertDialog != null && alertDialog?.isShowing == true) {
            alertDialog?.dismiss()
        }
    }

    private fun showBenefitsBottomSheet() {
        val fabDone: FloatingActionButton
        val fabAdd: FloatingActionButton
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.activity_select_categories_dailog)
        gridView = dialog.findViewById(R.id.grid_view_alert)
        progressBar = dialog.findViewById(R.id.progressBar)
        val bannerAd: AdView = dialog.findViewById(R.id.adView)
        fabDone = dialog.findViewById(R.id.fabDone)
        fabAdd = dialog.findViewById(R.id.fabMore)
        if (SaveSharedPreference.getAdsEnabled(this)) {
            bannerAd.loadAd(AdRequest.Builder().build())
        } else {
            bannerAd.visibility = View.GONE
        }
        loadCategories()
        fabDone.setOnClickListener {
            updateSelectedBenefits()
            dialog.dismiss()
        }
        fabAdd.setOnClickListener {
            startActivity(
                Intent(
                    this@AddOrEditTaskActivity,
                    AddBenefitsActivity::class.java
                )
            )
        }
        dialog.show()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)
    }

    private fun updateSelectedBenefits() {
        selectedResult = convertArrayToString(selectedStrings)
        categoryCount = selectedStrings?.size ?: 0
        record = convertStringToArray(selectedResult)
        displayBenefits(record)
        todoTaskToAddOrEdit?.let {
            todoTaskToAddOrEdit?.benefitsString = selectedResult
        }

    }

    companion object {
        private val TAG = AddOrEditTaskActivity::class.java.simpleName

        //To Convert String to Array or Array to String
        private var strSeparator = ", "
        fun convertArrayToString(array: ArrayList<String>?): String {
            var str = ""
            for (i in array!!.indices) {
                str += array[i]
                // Do not append comma at the end of last element
                if (i < array.size - 1) {
                    str += strSeparator
                }
            }
            return str
        }

        fun convertStringToArray(str: String): Array<String> {
            var arr = arrayOf<String>()
            if (str.isNotEmpty()) {
                arr = str.split(strSeparator.toRegex()).toTypedArray()
            }
            return arr
        }
    }

    private fun showBenefitsTutorial() {
        TapTargetSequence(this)
            .targets(
                TapTarget.forView(
                    addCategories,
                    "Assign Benefits",
                    "Assign benefits to this list item here. The more benefits assigned, the higher this item will appear on the main list."
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
                    editor?.putBoolean("flagTodoBenefitsAddEditTaskFirstRun", false)
                    editor?.apply()
                }

                override fun onSequenceStep(lastTarget: TapTarget, targetClicked: Boolean) {

                }
                override fun onSequenceCanceled(lastTarget: TapTarget) {
                    editor?.putBoolean("flagTodoBenefitsAddEditTaskFirstRun", false)
                    editor?.apply()
                }
            }).start()
    }
}