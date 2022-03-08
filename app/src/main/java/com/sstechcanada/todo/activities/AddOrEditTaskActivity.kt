package com.sstechcanada.todo.activities

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
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
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class AddOrEditTaskActivity : AppCompatActivity() {
    var categories: MutableList<Category>? = null
    var userID: String? = null
    var taskCompleted = "Pending"
    var db = FirebaseFirestore.getInstance()
    var benefitCollectionRef: CollectionReference? = null
    var UserColRef: CollectionReference? = null
    private var mBinding: ActivityAddOrEditTaskBinding? = null
    private var mTaskId: String? = "-1"
    private var mAddOrEdit: String? = null
    private var adapter: GridViewAdapter? = null
    private var selectedStrings: ArrayList<String>? = null
    private var category_count = 0
    private var chip_count = 0
    private var selectedResult = ""
    private var todoTaskToAddOrEdit: TodoTaskFirestore? = null
    private var mAuth: FirebaseAuth? = null
    private var gridView: GridView? = null
    private var progressBar: ProgressBar? = null

    private var record: Array<String>? = null
    private var description: String? = null
    var alertDialog: AlertDialog? = null
    private var mInterstitialAd: InterstitialAd? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_or_edit_task)
        profile_toolbar.setOnClickListener {
            startActivity(
                Intent(this@AddOrEditTaskActivity, ProfileActivity::class.java)
            )
        }
        arrow_back.visibility = View.VISIBLE
        arrow_back.setOnClickListener { super.onBackPressed() }
        toolbarTitle.text = "Add/Update Task"
        mAuth = FirebaseAuth.getInstance()
        userID = mAuth!!.currentUser!!.uid
        FirebaseCrashlytics.getInstance()
            .log(this.javaClass.simpleName + "listId = " + MasterTodoListActivity.listId)
        FirebaseCrashlytics.getInstance().log(this.javaClass.simpleName + "UserId = " + userID)
        benefitCollectionRef = db.collection("Users").document(userID!!).collection("Benefits")
        UserColRef = db.collection("Users").document(userID!!).collection("Lists")
            .document(MasterTodoListActivity.listId).collection("Todo")
        Log.i("ListId", "Add or edit: " + MasterTodoListActivity.listId)
        Glide.with(this).load(mAuth!!.currentUser!!.photoUrl).into(profile_toolbar)
        if (SaveSharedPreference.getAdsEnabled(this)) {
            loadBannerAds()
            loadFullScreenAds()
        }

        val dueDate: Long
        if (savedInstanceState == null) {
            val bundle = intent.extras
            mAddOrEdit = bundle?.getString(getString(R.string.intent_adding_or_editing_key))
            if (mAddOrEdit == getString(R.string.add_new_task)) {

            } else {
                todoTaskToAddOrEdit = bundle?.getParcelable(getString(R.string.intent_todo_key))
                selectedResult = todoTaskToAddOrEdit!!.benefitsString
                mTaskId = todoTaskToAddOrEdit!!.documentID
                mBinding?.etTaskDescription?.setText(todoTaskToAddOrEdit!!.description)
                taskCompleted = todoTaskToAddOrEdit!!.status
                mBinding?.cbTaskCompleted?.setOnClickListener {
                    if (!mBinding?.cbTaskCompleted!!.isChecked) {
                        AlertDialog.Builder(this@AddOrEditTaskActivity)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Confirm Incomplete")
                            .setMessage("Are you sure you want to mark this task as incomplete?")
                            .setPositiveButton("Yes") { dialog: DialogInterface?, which: Int -> }
                            .setNegativeButton("No") { dialog: DialogInterface?, which: Int ->
                                mBinding?.cbTaskCompleted?.isChecked = true
                            }
                            .show()
                    } else {
                        AlertDialog.Builder(this@AddOrEditTaskActivity)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Confirm Complete")
                            .setMessage("Are you sure you want to mark this task as completed?")
                            .setPositiveButton("Yes") { dialog: DialogInterface?, which: Int ->
                                if (SaveSharedPreference.getAdsEnabled(this)) {
                                    if (mInterstitialAd != null) {
                                        mInterstitialAd?.show(this@AddOrEditTaskActivity)
                                    }
                                }
                            }
                            .setNegativeButton("No") { dialog: DialogInterface?, which: Int ->
                                mBinding?.cbTaskCompleted?.isChecked = false
                            }
                            .show()
                    }
                }
                mBinding?.cbTaskCompleted?.isChecked = taskCompleted == "Completed"
                if (taskCompleted == "Completed") {
                    mBinding?.timestampCompletedtextView!!.text =
                        todoTaskToAddOrEdit!!.timestampCompleted
                    mBinding?.timestampCompletedtextView!!.visibility = View.VISIBLE
                }
                dueDate = todoTaskToAddOrEdit?.dueDate!!
                Log.d(TAG, "Due date in millis $dueDate")
            }
        } else {
            mAddOrEdit = savedInstanceState.getString(getString(R.string.add_or_edit_key))
            mTaskId = savedInstanceState.getString(getString(R.string.id_key))
            mBinding?.etTaskDescription?.setText(savedInstanceState.getString(getString(R.string.task_description_key)))
            if (taskCompleted == "Completed") {
                mBinding?.cbTaskCompleted?.isChecked = true
                mBinding?.timestampCompletedtextView!!.text =
                    todoTaskToAddOrEdit!!.timestampCompleted
            } else {
                mBinding?.cbTaskCompleted?.isChecked = false
            }
        }
        title = mAddOrEdit
        if (mAddOrEdit == getString(R.string.add_new_task)) {
//            ADDING NEW TASK
            mBinding?.btnAddOrUpdateTask?.setText(R.string.add_task)
            mBinding?.tvCompletionLabel?.visibility = View.INVISIBLE
            mBinding?.cbTaskCompleted?.visibility = View.INVISIBLE
            mBinding?.deleteTodoItem!!.visibility = View.INVISIBLE
        } else {
            mBinding?.btnAddOrUpdateTask?.setText(R.string.update_task)
            mBinding?.deleteTodoItem!!.visibility = View.VISIBLE
        }

        addCategories.setOnClickListener { showBenefitsBottomSheet() }

        //Grid View End
        if (todoTaskToAddOrEdit != null) {
            record = convertStringToArray(todoTaskToAddOrEdit!!.benefitsString)
            category_count = record!!.size
            displayBenefits(record)
        }
        MobileAds.initialize(this) { }
        mBinding?.deleteTodoItem?.setOnClickListener { deleteTodoItem() }
        mBinding?.btnAddOrUpdateTask?.setOnClickListener { addOrUpdateTask() }
    }

    private fun deleteTodoItem() {
        loadFullScreenAds()
        AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_menu_delete)
            .setTitle("Confirm Delete")
            .setMessage("Are you sure you want to delete this task?")
            .setPositiveButton("Yes") { dialog: DialogInterface?, which: Int ->
                if (SaveSharedPreference.getAdsEnabled(this)) {
                    if (mInterstitialAd != null) {
                        mInterstitialAd!!.show(this@AddOrEditTaskActivity)
                        UserColRef!!.document(todoTaskToAddOrEdit!!.documentID).delete()
                        Toasty.error(
                            this@AddOrEditTaskActivity,
                            "Task Deleted",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        UserColRef!!.document(todoTaskToAddOrEdit!!.documentID).delete()
                        Toasty.error(
                            this@AddOrEditTaskActivity,
                            "Task Deleted",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                } else {
                    UserColRef!!.document(todoTaskToAddOrEdit!!.documentID).delete()
                    Toasty.error(this@AddOrEditTaskActivity, "Task Deleted", Toast.LENGTH_SHORT)
                        .show()
                    finish()
                }
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

        outState.putBoolean(getString(R.string.completed_key), mBinding?.cbTaskCompleted?.isChecked!!)
        outState.putString(getString(R.string.add_or_edit_key), mAddOrEdit)
        outState.putString(getString(R.string.id_key), mTaskId)
        //        outState.putString("category", selectedResult);
        super.onSaveInstanceState(outState)
    }

    private fun addOrUpdateTask() {
        loadingProgressBarUpdate?.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
        description = mBinding?.etTaskDescription?.text.toString().trim { it <= ' ' }
        Log.d(TAG, "Here")
        if (description == "") {
            Toasty.warning(
                this,
                getString(R.string.description_cannot_be_empty),
                Toast.LENGTH_SHORT,
                true
            ).show()
            loadingProgressBarUpdate!!.visibility = View.GONE
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        } else {
            //Making First Char Capital
            description = description?.substring(0, 1)?.toUpperCase() + description?.substring(1)
            uploadDataToFirestore()

            val returnIntent = Intent()
            setResult(RESULT_OK, returnIntent)
        }
    }

    private fun uploadDataToFirestore() {
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
            UserColRef!!.document().set(newTaskMap).addOnSuccessListener {
                loadingProgressBarUpdate!!.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                Toasty.success(
                    this@AddOrEditTaskActivity,
                    "New list item added successfully",
                    Toasty.LENGTH_SHORT
                ).show()
                finish()
                onBackPressed()
            }.addOnFailureListener { e: Exception? ->
                loadingProgressBarUpdate!!.visibility = View.GONE
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
            if (mBinding?.cbTaskCompleted?.isChecked == true) {
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
                UserColRef?.document(it)
                    ?.set(updateTaskMap, SetOptions.merge())?.addOnSuccessListener {
                        loadingProgressBarUpdate!!.visibility = View.GONE
                        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                        Toasty.success(
                            this@AddOrEditTaskActivity,
                            "List item updated successfully",
                            Toasty.LENGTH_SHORT
                        ).show()
                        finish()
                        onBackPressed()
                    }?.addOnFailureListener {
                        loadingProgressBarUpdate!!.visibility = View.GONE
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

    override fun onStart() {
        super.onStart()
        //attaching value event listener
    }

    private fun loadCategories() {
        benefitCollectionRef!!.addSnapshotListener { value: QuerySnapshot?, error: FirebaseFirestoreException? ->
            categories = ArrayList()
            selectedStrings = ArrayList()
            categories?.clear()
            for (dataSnapshot in value!!) {
                val category = Category(dataSnapshot.id, dataSnapshot["category_name"] as String?)
                categories?.add(category)
            }

            //iterating through all the nodes
            adapter = GridViewAdapter(categories, this@AddOrEditTaskActivity)
            gridView!!.adapter = adapter
            gridView!!.visibility = View.VISIBLE
            record = convertStringToArray(selectedResult)
            for (i in categories?.indices!!) {
                for (j in record!!.indices) {
                    if (record!![j] == categories?.get(i)?.category_name) {
                        adapter!!.selectedPositions.add(i)
                        selectedStrings!!.add(record!![j])
                    }
                }
            }
            selectedResult = ""
            selectedResult = convertArrayToString(selectedStrings)
            progressBar!!.visibility = View.INVISIBLE
        }
        gridView!!.onItemClickListener =
            OnItemClickListener { parent: AdapterView<*>, v: View, position: Int, id: Long ->
                val selectedIndex = adapter!!.selectedPositions.indexOf(position)
                if (selectedIndex > -1) {
                    adapter!!.selectedPositions.removeAt(selectedIndex)
                    (v as GridItemView).display(false)
                    selectedStrings!!.remove(parent.getItemAtPosition(position))
                } else {
                    adapter!!.selectedPositions.add(position)
                    (v as GridItemView).display(true)
                    selectedStrings!!.add(parent.getItemAtPosition(position) as String)
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

    fun displayBenefits(record: Array<String>?) {
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
        chip_count = record?.size!!
        tv_add_more?.text = "Click here to add more Benefits"
        if (chip_count == 0) {
            tv_category_number!!.text = "$chip_count Benefits Selected"
            return
        }
        for (i in 0 until chip_count) {
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
            tv_category_number?.text = "$chip_count Benefits Selected"
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
        fabDone.setOnClickListener { view: View? ->
            updateSelectedBenefits()
            dialog.dismiss()
        }
        fabAdd.setOnClickListener { view: View? ->
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
        category_count = selectedStrings!!.size
        record = convertStringToArray(selectedResult)
        displayBenefits(record)
        todoTaskToAddOrEdit?.let {
            todoTaskToAddOrEdit?.benefitsString = selectedResult
        }

    }

    companion object {
        private val TAG = AddOrEditTaskActivity::class.java.simpleName

        //To Convert String to Array or Array to String
        var strSeparator = ", "
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
}