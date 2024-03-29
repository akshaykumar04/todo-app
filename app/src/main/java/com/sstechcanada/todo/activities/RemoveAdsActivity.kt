package com.sstechcanada.todo.activities

import android.content.res.ColorStateList
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.sstechcanada.todo.R
import com.sstechcanada.todo.activities.auth.LoginActivity
import com.sstechcanada.todo.databinding.ActivityRemoveAdsBinding
import com.sstechcanada.todo.utils.RemoveAdsUtils
import com.sstechcanada.todo.utils.SaveSharedPreference
import es.dmoral.toasty.Toasty
import java.util.concurrent.TimeUnit


class RemoveAdsActivity : AppCompatActivity() {

    private var mRewardedAd: RewardedAd? = null
    private var mAuth: FirebaseAuth? = null
    private val TAG = "RemoveAdsActivity"
    private lateinit var binding: ActivityRemoveAdsBinding
    private var storedTimeStamp: String = "0"
    lateinit var timer: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRemoveAdsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        fetchStoredTimeStamp()
        setupAds()
        initViews()
        setOnClicks()
    }

    private fun setOnClicks() {
        binding.fabBack2.setOnClickListener {
            onBackPressed()
        }

        binding.btnWatchAds.setOnClickListener {
            if (mRewardedAd != null) {
                showRewardedVideo()
            } else {
                Toasty.warning(this,"No ads available - please try again soon! An ad may become available in the next few seconds. The button turns dark blue when an ad is ready.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initViews() {
        binding.maxDays =  30
        binding.maxHours = 24
        binding.maxMinutes = 60
        binding.maxSeconds = 60
    }


    private fun setupAds() {
        loadRewardedAd()
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
                    binding.btnWatchAds.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.discp_border))
                }

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    Log.d(TAG, "Ad was loaded.")
                    mRewardedAd = rewardedAd
                    binding.btnWatchAds.backgroundTintList =
                        ColorStateList.valueOf(resources.getColor(R.color.todo))
                }
            })
    }

    private fun startTimer(storedTimeStamp: Long) {
        val timeLeft: Long =
            storedTimeStamp - RemoveAdsUtils.getServerTime()  //- 604740000//432000000 + 61200000 + 2100000
        try {
            timer = object : CountDownTimer(
                (timeLeft ?: 0L), 1_000L
            ) {
                override fun onTick(millisUntilFinished: Long) {
                    refreshTimeLeft(timeLeft = millisUntilFinished)
                }

                override fun onFinish() {
                    refreshTimeLeft(timeLeft = 0L)
                    Toasty.info(
                        this@RemoveAdsActivity,
                        "Ad Free Membership Expired",
                        Toast.LENGTH_LONG
                    ).show()
                    SaveSharedPreference.setAdsEnabled(this@RemoveAdsActivity, true)
                }
            }
            timer.start()
        }catch (e: Exception){
            refreshTimeLeft(timeLeft = timeLeft ?: 0L)
            Log.v("PreContestActivity","Message: ${e.message}")
        }
    }

    private fun refreshTimeLeft(timeLeft: Long) {
        val days = TimeUnit.MILLISECONDS.toDays(timeLeft)
        val hourDiff = timeLeft - (days * 1000 * 60 * 60 * 24)
        val hours = TimeUnit.MILLISECONDS.toHours(hourDiff)
        val minuteDiff = hourDiff - (hours * 1000 * 60 * 60)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(minuteDiff)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(minuteDiff - (minutes * 1000* 60))

        binding.days = days.toInt()
        binding.hours = hours.toInt()
        binding.minutes = minutes.toInt()
        binding.seconds = seconds.toInt()
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
                timer.cancel()
                fetchStoredTimeStamp()
            }
        } else {
            Toasty.info(this, "No ads available - please try again soon! An ad may become available in the next few seconds. The button turns dark blue when an ad is ready.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchStoredTimeStamp() {
        mAuth?.currentUser?.uid?.let {
            FirebaseFirestore.getInstance().collection("Users").document(it).get()
                .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                    documentSnapshot["adsPausedTimestamp"]?.toString()?.let { time ->
                        storedTimeStamp = time
                        startTimer(time.toLong())
                    }

                }.addOnFailureListener { }
        }
    }

    private fun grantReward() {
        val data: MutableMap<String, String> = HashMap()
        data["purchase_code"] = "3"
        data["adsPausedTimestamp"] = RemoveAdsUtils.getTimeStampOfNextWeek(storedTimeStamp)
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
                        }
                }.addOnFailureListener {
                    //no-op
                }
        }
    }
}