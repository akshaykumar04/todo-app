package com.sstechcanada.todo.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.sstechcanada.todo.R
import com.sstechcanada.todo.databinding.ActivityRemoveAdsBinding
import com.sstechcanada.todo.utils.RemoveAdsUtils
import com.sstechcanada.todo.utils.SaveSharedPreference
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_profile.*
import java.util.concurrent.TimeUnit

class RemoveAdsActivity : AppCompatActivity() {

    private var mRewardedAd: RewardedAd? = null
    private var mAuth: FirebaseAuth? = null
    private val TAG = "RemoveAdsActivity"
    private lateinit var binding: ActivityRemoveAdsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRemoveAdsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        startTimer()
        setupAds()
    }

    private fun initViews() {
        binding.maxDays =  30
        binding.maxHours = 24
        binding.maxMinutes = 60
    }


    private fun setupAds() {
        if (SaveSharedPreference.getAdsEnabled(this)) {
            loadRewardedAd()
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

    private fun startTimer() {
        val timeLeft: Long = 100000//432000000 + 61200000 + 2100000
        try {
            val timer = object: CountDownTimer(
                (timeLeft ?: 0L),1_000L){
                override fun onTick(millisUntilFinished: Long) {
                    refreshTimeLeft(timeLeft = millisUntilFinished)
                }

                override fun onFinish() {
                    refreshTimeLeft(timeLeft = 0L)
                    Toasty.info(this@RemoveAdsActivity,"Time Up", Toast.LENGTH_LONG).show()
                    finish()
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

        binding.days = days.toInt()
        binding.hours = hours.toInt()
        binding.minutes = minutes.toInt() + 1
    }

    private fun showRewardedVideo() {
        if (mRewardedAd != null) {
            mRewardedAd?.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d(TAG, "Ad was dismissed.")
                        mRewardedAd = null
                        loadRewardedAd()
                        val moveToMasterTodoListActivity = Intent(this@RemoveAdsActivity, MasterTodoListActivity::class.java)
                        startActivity(moveToMasterTodoListActivity)
                        finish()
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
                val data: MutableMap<String, String?> = HashMap()
                data["adsPausedTimestamp"] = RemoveAdsUtils.getTimeStampOfNextWeek()
                data["purchase_code"] = "3"
                mAuth?.currentUser?.uid?.let {
                    FirebaseFirestore.getInstance().collection("Users").document(it).set(
                        data,
                        SetOptions.merge()
                    ).addOnSuccessListener {
                        Toasty.success(
                            this,
                            "Ads are paused for a week",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                Log.d("TAG", "User earned the reward.")
            }
        } else {
            Toasty.info(this, "No Ads available, try again later.", Toast.LENGTH_SHORT).show()
        }
    }
}