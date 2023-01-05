package com.sstechcanada.todo.activities.auth

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.sstechcanada.todo.BuildConfig
import com.sstechcanada.todo.R
import com.sstechcanada.todo.activities.AboutActivity
import com.sstechcanada.todo.activities.MasterTodoListActivity
import com.sstechcanada.todo.activities.RemoveAdsActivity
import com.sstechcanada.todo.activities.SplashActivity
import com.sstechcanada.todo.utils.RemoveAdsUtils
import com.sstechcanada.todo.utils.SaveSharedPreference
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var mRewardedAd: RewardedAd? = null
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private val TAG = "ProfileActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        setupFirebaseLogin()
        initViews()
        initOnClicks()
        setupAds()

    }


    private fun initViews() {
        userEmail.text = mAuth?.currentUser?.email

        if (mAuth?.currentUser?.isAnonymous == true) {
            userName.text = "Guest"
        } else {
            userName.text = mAuth?.currentUser?.displayName
        }

        mAuth?.currentUser?.photoUrl?.let {
            Glide.with(this).load(it).into(roundedImage)
        }
        if (MasterTodoListActivity.purchaseCode == "0") {
            userType?.setText(R.string.free_user)
        } else {
            userType?.setText(R.string.premium_user)
        }
    }

    private fun initOnClicks() {
        fabBack.setOnClickListener { onBackPressed() }
        cardAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
        cardShare.setOnClickListener { shareApp() }
        cardRate.setOnClickListener { openGooglePlayForRating() }
        cardDelete.setOnClickListener { deleteWarningDialog() }
        cardSignOut.setOnClickListener { showSignOutDialog() }
        cardRemoveAds.setOnClickListener {
            if (SaveSharedPreference.getIsRemoveAdsTimestampNull(this)) {
                showRewardedVideo()
            } else {
                startActivity(Intent(this@ProfileActivity, RemoveAdsActivity::class.java))
            }
        }
    }

    private fun setupFirebaseLogin() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        mAuth = FirebaseAuth.getInstance()
    }

    private fun setupAds() {
        if (SaveSharedPreference.getAdsEnabled(this)) {
            adView.loadAd(AdRequest.Builder().build())
            adView.visibility = View.VISIBLE
            loadRewardedAd()
        } else {
            adView.visibility = View.GONE
        }

    }

    private fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            this,
            getString(R.string.rewarded_ad_unit_id),
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, adError.toString())
                    mRewardedAd = null
                }

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    Log.d(TAG, "Ad was loaded.")
                    mRewardedAd = rewardedAd
                }
            })
    }

    private fun showRewardedVideo() {
        if (mRewardedAd != null) {
            mRewardedAd?.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d(TAG, "Ad was dismissed.")
                        mRewardedAd = null
                        loadRewardedAd()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Log.d(TAG, "Ad failed to show.")
                        mRewardedAd = null
                    }

                    override fun onAdShowedFullScreenContent() {
                        Log.d(TAG, "Ad showed fullscreen content.")
                    }
                }

            mRewardedAd?.show(this) {
                grantReward()
            }
        } else {
            Toasty.info(this, "No ads available, try again later", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSignOutDialog() {
        val alert = AlertDialog.Builder(this)
        alert.setTitle(R.string.sign_out)
        alert.setMessage(R.string.are_you_sure)
        alert.setPositiveButton(
            R.string.yes
        ) { _: DialogInterface?, _: Int -> signOut() }
        alert.setNegativeButton(
            R.string.no
        ) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
        alert.show()
    }

    private fun signOut() {
        mAuth?.signOut()
        mGoogleSignInClient?.signOut()?.addOnCompleteListener(
            this
        ) {
            clearPrefs()
        }
        finishAffinity()
        startActivity(Intent(this, SplashActivity::class.java))
    }

    private fun deleteUserAccount() {
        mAuth = FirebaseAuth.getInstance()
        mAuth?.currentUser?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("TAG", "User account deleted.")
                Toasty.success(this, "Account Deleted", Toast.LENGTH_SHORT).show()
                signOut()
                finishAffinity()
                startActivity(Intent(this, SplashActivity::class.java))
            } else {
                Toasty.error(this, "Server Error, Please try login in again", Toast.LENGTH_SHORT).show()
            }

        }

    }

    private fun openGooglePlayForRating() {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=com.sstechcanada.todo")
                )
            )
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=com.sstechcanada.todo")
                )
            )
        }
    }

    private fun shareApp() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Organize: Tasks & Priorities")
            var shareMessage =
                "Check out this list maker. It automatically sorts your list based on the number of benefits you assign to each item:\n\n"
            shareMessage =
                """${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}""".trimIndent()
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "choose one"))
        } catch (e: Exception) {

        }
    }

    private fun deleteWarningDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottomsheet)
        val btnDelete: MaterialButton = dialog.findViewById(R.id.btnDelete)
        val userText: AppCompatTextView = dialog.findViewById(R.id.tvUser)
        val tvUserDes: AppCompatTextView = dialog.findViewById(R.id.tvUserDes)
        val checkBox: CheckBox = dialog.findViewById(R.id.checkDelete)

        if (mAuth?.currentUser?.isAnonymous == true) {
            userText.text = "Sorry to see you go, guest"
        } else {
            userText.text = "Sorry to see you go, " + mAuth?.currentUser?.displayName
        }
        tvUserDes.text = "If you're experiencing any issues, please give us the opportunity to help you by sending us your Feedback.\n\nIf you continue, your account will be deleted immediately and all the associated data with your account will be deleted forever.\n\nPost account deletion, you will be able to create a new account. However, your previous data will be inaccessible."

        checkBox.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                btnDelete.isEnabled = true
                btnDelete.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.btnRed))
            } else {
                btnDelete.isEnabled = false
                btnDelete.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.discp_border))
            }
        }

        btnDelete.setOnClickListener {
            if (checkBox.isChecked) {
                deleteUserAccount()
            } else {
                Toasty.info(this, "Please check the checkbox to continue", Toast.LENGTH_SHORT).show()
            }
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

    private fun getTimeStampOfNextWeek(): String {
        val now = System.currentTimeMillis() / 1000
        val afterAWeek = now + 604800
        return afterAWeek.toString()
    }

    private fun clearPrefs() {
        val editor = PreferenceManager.getDefaultSharedPreferences(this)
        editor.edit().clear().apply()
    }

    private fun grantReward() {
        val data: MutableMap<String, String> = HashMap()
        data["purchase_code"] = "3"
        data["adsPausedTimestamp"] = RemoveAdsUtils.getTimeStampOfNextWeek()
        SaveSharedPreference.setAdsEnabled(this, false)
        SaveSharedPreference.setIsRemoveAdsTimestampNull(this, false)
        mAuth?.currentUser?.uid?.let {
            FirebaseFirestore.getInstance().collection("Users").document(it).set(
                data,
                SetOptions.merge()
            ).addOnSuccessListener {
                MasterTodoListActivity.purchaseCode = "3"
                Toasty.success(
                    this,
                    "Ads are paused for a week",
                    Toast.LENGTH_LONG
                ).show()
                setPurchaseCode()

            }
        }
        Log.d("TAG", "User earned the reward.")
    }

    private fun setPurchaseCode() {
        mAuth?.currentUser?.uid?.let {
            FirebaseFirestore.getInstance().collection("Users").document(it).get()
                .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                    MasterTodoListActivity.purchaseCode =
                        documentSnapshot["purchase_code"].toString()
                    FirebaseFirestore.getInstance().collection("UserTiers")
                        .document(MasterTodoListActivity.purchaseCode).get()
                        .addOnSuccessListener { documentSnapshot1: DocumentSnapshot ->
                            LoginActivity.userAccountDetails.add(
                                0,
                                documentSnapshot1["masterListLimit"].toString()
                            )
                            LoginActivity.userAccountDetails.add(
                                1,
                                documentSnapshot1["todoItemLimit"].toString()
                            )
                            val moveToRemoveAdsActivity = Intent(this@ProfileActivity, RemoveAdsActivity::class.java)
                            startActivity(moveToRemoveAdsActivity)
                        }
                }.addOnFailureListener {
                    //no-op
                }
        }
    }

}