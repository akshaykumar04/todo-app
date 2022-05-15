package com.sstechcanada.todo.activities

import android.content.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.CollectionReference
import com.sstechcanada.todo.adapters.BenefitsAdapter
import android.os.Bundle
import com.sstechcanada.todo.R
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.ads.AdView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.SetOptions
import es.dmoral.toasty.Toasty
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import com.sstechcanada.todo.models.Category
import kotlinx.android.synthetic.main.act_bar.*
import kotlinx.android.synthetic.main.activity_category.*
import java.util.HashMap

import com.bumptech.glide.Glide
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.sstechcanada.todo.activities.auth.LoginActivity
import com.sstechcanada.todo.activities.auth.ProfileActivity
import com.sstechcanada.todo.utils.SaveSharedPreference
import kotlinx.android.synthetic.main.item_grid.*


class AddBenefitsActivity : AppCompatActivity(),
    BenefitsAdapter.Callbacks {
    var userID: String? = null
    private var mAuth: FirebaseAuth? = null
    var db = FirebaseFirestore.getInstance()
    private var benefitCollectionRef: CollectionReference? = null
    private var userColRef: CollectionReference? = null
    private var benefitsAdapter: BenefitsAdapter? = null
    var editor: SharedPreferences.Editor? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)
        mAuth = FirebaseAuth.getInstance()
        userID = mAuth!!.currentUser!!.uid
        benefitCollectionRef = db.collection("Users").document(userID!!).collection("Benefits")
        userColRef = db.collection("Users").document(userID!!).collection("Lists")
        val prefs = getSharedPreferences(LoginActivity.SHAREDPREF, MODE_PRIVATE)
        editor = getSharedPreferences(LoginActivity.SHAREDPREF, MODE_PRIVATE).edit()

        arrow_back.visibility = View.VISIBLE
        arrow_back.setOnClickListener { super.onBackPressed() }
        setUpRecyclerView()
        benefitsAdapter?.setCallbacks(this@AddBenefitsActivity)
        toolbarTitle.text = getString(R.string.add_benefit)
        profile_toolbar.setOnClickListener {
            startActivity(
                Intent(this@AddBenefitsActivity, ProfileActivity::class.java)
            )
        }
        Glide.with(this).load(mAuth?.currentUser?.photoUrl).into(profile_toolbar)
        buttonAddCategory.setOnClickListener { addCategory() }
        if (SaveSharedPreference.getAdsEnabled(this)) {
            val adView = findViewById<AdView>(R.id.adView)
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
        }

        toolbarTitle.setOnLongClickListener {
            getFcmToken()
            return@setOnLongClickListener true
        }

        if (prefs.getBoolean("flagTodoBenefitsFirstRun", true)) {
            buttonTapTargetView?.visibility = View.INVISIBLE
            showBenefitsTutorial()
        }

    }

    private fun setUpRecyclerView() {
        val query: Query? = benefitCollectionRef
        val options = query?.let {
            FirestoreRecyclerOptions.Builder<Category>().setQuery(
                it, Category::class.java
            ).build()
        }
        benefitsAdapter = options?.let { BenefitsAdapter(it, this) }
        listViewCategory.setHasFixedSize(true)
        listViewCategory.layoutManager = LinearLayoutManager(this)
        listViewCategory.adapter = benefitsAdapter
    }

    override fun onStart() {
        super.onStart()
        benefitsAdapter!!.startListening()
    }

    override fun onStop() {
        super.onStop()
        benefitsAdapter!!.stopListening()
    }

    private fun addCategory() {
        //getting the values to save
        val name = editTextName!!.text.toString().trim { it <= ' ' }

        //checking if the value is provided
        if (name.isNotEmpty()) {
            val documentReferenceCurrentReference = benefitCollectionRef!!.document()
            val category: MutableMap<String, String> = HashMap()
            category["category_name"] = name
            documentReferenceCurrentReference.set(category, SetOptions.merge())
                .addOnSuccessListener {
                    Toasty.success(
                        applicationContext, "Benefit added", Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener {
                    Toasty.error(
                        applicationContext,
                        "Benefit addition Failed: ",
                        Toast.LENGTH_LONG
                    ).show()
                }

//            //Saving the Category
//            databaseCategories.child(id).setValue(category);

            //setting edittext to blank again
            editTextName!!.setText("")

            //displaying a success toast
        } else {
            //if the value is not given displaying a toast
            Toasty.warning(this, "Please enter a name", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            val msg = getString(R.string.msg_token_fmt, token)
            Log.d("FCM", msg)
            Toast.makeText(baseContext, "FCM Token Copied to Clipboard", Toast.LENGTH_SHORT).show()
            val clipboard: ClipboardManager =
                getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label", msg)
            clipboard.setPrimaryClip(clip)
        })
    }

    private fun showBenefitsTutorial() {
        TapTargetSequence(this)
            .targets(
                TapTarget.forView(buttonAddCategory, "Add Button", "Click here to add a new benefit")
                    .outerCircleColor(R.color.chip_5)
                    .outerCircleAlpha(0.98f)
                    .targetCircleColor(R.color.colorUncompletedBackground)
                    .titleTextSize(22)
                    .titleTextColor(R.color.colorUncompletedBackground)
                    .descriptionTextSize(16)
                    .titleTypeface(ResourcesCompat.getFont(this, R.font.poppins_semibold))
                    .textTypeface(ResourcesCompat.getFont(this, R.font.raleway_medium))
                    .titleTypeface(ResourcesCompat.getFont(this, R.font.poppins_semibold))
                    .textTypeface(ResourcesCompat.getFont(this, R.font.raleway_medium))
                    .descriptionTextColor(R.color.black)
                    .textColor(R.color.black)
                    .dimColor(R.color.black)
                    .drawShadow(true)
                    .cancelable(false)
                    .tintTarget(true)
                    .transparentTarget(true)
                    .targetRadius(50),
                TapTarget.forView(
                    buttonTapTargetView,
                    "Benefits",
                    "Add benefits that are important to you. You can then assign any benefit to any task."
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

                    buttonTapTargetView.visibility = View.GONE
                    editor?.putBoolean("flagTodoBenefitsFirstRun", false)
                    editor?.apply()
                }

                override fun onSequenceStep(lastTarget: TapTarget, targetClicked: Boolean) {}
                override fun onSequenceCanceled(lastTarget: TapTarget) {
                    editor?.putBoolean("flagTodoBenefitsFirstRun", false)
                    buttonTapTargetView.visibility = View.GONE
                    editor?.apply()
                }
            }).start()
    }


    override fun showProgressBar() {
        progressCat.visibility = View.VISIBLE
    }

    override fun hideProgressBar() {
        progressCat.visibility = View.INVISIBLE
    }

}