package com.sstechcanada.todo.activities

import com.anjlab.android.iab.v3.BillingProcessor.IBillingHandler
import com.savvyapps.togglebuttonlayout.ToggleButtonLayout
import com.anjlab.android.iab.v3.BillingProcessor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.ads.interstitial.InterstitialAd
import android.os.Bundle
import com.sstechcanada.todo.R
import es.dmoral.toasty.Toasty
import android.widget.Toast
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.sstechcanada.todo.activities.auth.LoginActivity
import android.view.WindowManager
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.anjlab.android.iab.v3.SkuDetails
import com.anjlab.android.iab.v3.TransactionDetails
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.sstechcanada.todo.utils.SaveSharedPreference
import kotlinx.android.synthetic.main.activity_app_upgrade.*
import java.util.ArrayList
import java.util.HashMap

class AppUpgradeActivity : AppCompatActivity(), IBillingHandler {
    private val activityTag = "AppUpgradeActivity"

    var bp: BillingProcessor? = null
    private val mAuth = FirebaseAuth.getInstance()
    var userID = mAuth.currentUser!!.uid
    var db = FirebaseFirestore.getInstance()
    var purchaseProductId = "1"
    private var purchaseTransactionDetails: List<SkuDetails>? = null
    var pur_code: String = MasterTodoListActivity.purchaseCode
    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_upgrade)
        setupPriceToggle()
        setUpOnClicks()
        setupBilling()
    }

    private fun setupBilling() {
        bp = BillingProcessor(
            this,
            getString(R.string.billing_license_key),
            this@AppUpgradeActivity
        )
        bp?.initialize()
    }

    private fun setUpOnClicks() {
        fabBack.setOnClickListener { onBackPressed() }
        if (SaveSharedPreference.getAdsEnabled(this)) {
            loadFullScreenAd()
        }
    }

    override fun onStart() {
        super.onStart()
        when (MasterTodoListActivity.purchaseCode) {
            "0" -> {
                toggle_button_layout.setToggled(R.id.toggle_right, true)
                tvListsCount.text = getString(R.string.create_up_to_20_to_do_lists)
                list8.visibility = View.GONE
                purchaseProductId = "tier2"
                pur_code = "2"
            }
            "1", "3" -> {
                toggle_button_layout.setToggled(R.id.toggle_right, true)
                tvListsCount.text = getString(R.string.create_up_to_20_to_do_lists)
                list8.visibility = View.GONE
                purchaseProductId = "tier2"
                pur_code = "2"
            }
            "2" -> {
                toggle_button_layout.isEnabled = false
            }
        }
    }

    private fun setupPriceToggle() {
        toggle_button_layout.onToggledListener =
            { _: ToggleButtonLayout?, (id), _: Boolean? ->
                when (MasterTodoListActivity.purchaseCode) {
                    "0" -> {
                        when (id) {
                            R.id.toggle_left -> {
                                tvListsCount.text = getString(R.string.create_up_to_3_to_do_lists)
                                list8.visibility = View.VISIBLE
                                purchaseProductId = "tier1"
                                pur_code = "1"
                                toggleListPointsVisibility(true)
                            }
                            R.id.toggle_right -> {
                                tvListsCount.text = getString(R.string.create_up_to_20_to_do_lists)
                                list8.visibility = View.GONE
                                purchaseProductId = "tier2"
                                pur_code = "2"
                                toggleListPointsVisibility(true)
                            }
                            R.id.toggle_remove_ads -> {
                                tvListsCount.setText(R.string.removes_ads_completely)
                                purchaseProductId = "adfree"
                                pur_code = "3"
                                toggleListPointsVisibility(false)
                            }
                        }
                    }
                    "1" -> {
                        when (id) {
                            R.id.toggle_left -> {
                                toggle_button_layout.setToggled(R.id.toggle_right, true)
                                Toasty.success(
                                    applicationContext,
                                    "You are already subscribed to Tier 1",
                                    Toast.LENGTH_SHORT
                                ).show()
                                tvListsCount.text = getString(R.string.create_up_to_20_to_do_lists)
                                list8.visibility = View.GONE
                                purchaseProductId = "tier2"
                                pur_code = "2"
                                toggleListPointsVisibility(true)
                            }
                            R.id.toggle_right -> {
                                tvListsCount.text = getString(R.string.create_up_to_20_to_do_lists)
                                list8.visibility = View.GONE
                                purchaseProductId = "tier2"
                                pur_code = "2"
                                toggleListPointsVisibility(true)
                            }
                            R.id.toggle_remove_ads -> {
                                tvListsCount.setText(R.string.removes_ads_completely)
                                purchaseProductId = "adfree"
                                pur_code = "3"
                                toggleListPointsVisibility(false)
                            }
                        }
                    }
                    "3" -> {
                        when (id) {
                            R.id.toggle_left -> {
                                tvListsCount.text = getString(R.string.create_up_to_3_to_do_lists)
                                list8.visibility = View.VISIBLE
                                purchaseProductId = "tier1"
                                pur_code = "1"
                                toggleListPointsVisibility(true)
                            }
                            R.id.toggle_right -> {
                                tvListsCount.text = getString(R.string.create_up_to_20_to_do_lists)
                                list8.visibility = View.GONE
                                purchaseProductId = "tier2"
                                pur_code = "2"
                                toggleListPointsVisibility(true)
                            }
                            R.id.toggle_remove_ads -> {
                                toggle_button_layout.setToggled(R.id.toggle_right, true)
                                Toasty.success(
                                    applicationContext,
                                    "You are already subscribed to Ad free membership",
                                    Toast.LENGTH_SHORT
                                ).show()
                                tvListsCount.setText(R.string.removes_ads_completely)
                                purchaseProductId = "tier2"
                                pur_code = "2"
                                tvListsCount.text = getString(R.string.create_up_to_20_to_do_lists)
                                list8.visibility = View.GONE
                                toggleListPointsVisibility(true)
                            }
                        }
                    }
                }
            }
    }

    private fun toggleListPointsVisibility(visibility: Boolean) {
        if (visibility) {
            list2.visibility = View.VISIBLE
            list3.visibility = View.VISIBLE
            list4.visibility = View.VISIBLE
            list5.visibility = View.VISIBLE
            list7.visibility = View.VISIBLE
            list8.visibility = View.VISIBLE
        } else {
            list2.visibility = View.INVISIBLE
            list3.visibility = View.INVISIBLE
            list4.visibility = View.INVISIBLE
            list5.visibility = View.INVISIBLE
            list7.visibility = View.INVISIBLE
            list8.visibility = View.INVISIBLE
        }
    }

    private fun setPurchaseCodeInDatabase(product_Id: String) {

        val purchaseCodeMap: MutableMap<String, String> = HashMap()
        when (product_Id) {
            "tier1" -> {
                pur_code = "1"
            }
            "tier2" -> {
                pur_code = "2"
            }
            "adfree" -> {
                pur_code = "3"
            }
        }
        SaveSharedPreference.setAdsEnabled(this, false)
        purchaseCodeMap["purchase_code"] = pur_code
        db.collection("Users").document(userID).set(purchaseCodeMap, SetOptions.merge())
            .addOnSuccessListener {
                MasterTodoListActivity.purchaseCode = pur_code
                setPurchaseCode()
            }
    }

    private fun setPurchaseCode() {

        db.collection("Users").document(userID).get()
            .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                MasterTodoListActivity.purchaseCode = documentSnapshot["purchase_code"].toString()
                db.collection("UserTiers").document(MasterTodoListActivity.purchaseCode).get()
                    .addOnSuccessListener { documentSnapshot1: DocumentSnapshot ->
                        Log.i(
                            "purchasecode",
                            "purchase code :" + MasterTodoListActivity.purchaseCode
                        )
                        Log.i(
                            "purchasecode",
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
                        Toasty.success(applicationContext, "Package Upgraded", Toast.LENGTH_SHORT)
                            .show()
                        loadingProgressBarUpgrade?.visibility = View.GONE
                        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                        val intent =
                            Intent(this@AppUpgradeActivity, MasterTodoListActivity::class.java)
                        startActivity(intent)
                    }
            }.addOnFailureListener {

            }
    }

    override fun onBackPressed() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(this@AppUpgradeActivity)
        }
        super.onBackPressed()
    }

    public override fun onDestroy() {
        if (bp != null) {
            bp?.release()
        }
        super.onDestroy()
    }

    override fun onBillingInitialized() {
        Log.d(activityTag, "onBillingInitialized: ")
        val productIdList = ArrayList<String>()
        productIdList.add("tier1")
        productIdList.add("tier2")
        productIdList.add("adfree")
        purchaseTransactionDetails = bp?.getSubscriptionListingDetails(productIdList)
        buttonUpgrade.setOnClickListener {
            if (bp!!.isSubscriptionUpdateSupported) {
                bp?.subscribe(this@AppUpgradeActivity, purchaseProductId)
            } else {
                Log.d("MainActivity", "onBillingInitialized: Subscription updated is not supported")
            }
        }
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        Log.d(activityTag, "onProductPurchased: $productId")
        loadingProgressBarUpgrade.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
        setPurchaseCodeInDatabase(productId)
    }

    override fun onPurchaseHistoryRestored() {
        Log.d(activityTag, "onPurchaseHistoryRestored: ")
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
        Log.d(activityTag, "onBillingError: $error")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!bp!!.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun loadFullScreenAd() {
        val adRequest = AdRequest.Builder().build()
        //ca-app-pub-3111421321050812/5967628112 our
        //test ca-app-pub-3940256099942544/1033173712
        InterstitialAd.load(this, "ca-app-pub-3111421321050812/5967628112", adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                    mInterstitialAd?.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                Log.d("TAG", "The ad was dismissed.")
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                Log.d("TAG", "The ad failed to show.")
                            }

                            override fun onAdShowedFullScreenContent() {
                                mInterstitialAd = null
                                Log.d("TAG", "The ad was shown.")
                            }
                        }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    mInterstitialAd = null
                }
            })
    }
}