package com.sstechcanada.todo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.savvyapps.togglebuttonlayout.ToggleButtonLayout;
import com.sstechcanada.todo.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;

import static com.sstechcanada.todo.activities.MasterTodoListActivity.purchaseCode;
import static com.sstechcanada.todo.activities.auth.LoginActivity.userAccountDetails;

public class AppUpgradeActivity2 extends AppCompatActivity implements BillingProcessor.IBillingHandler {

    private String TAG = "AppUpgradeActivity2";
    LottieAnimationView buttonUpgrade;
    FloatingActionButton fabBack;
    ToggleButtonLayout toggle_button_layout;
    TextView tvListsCount;
    BillingProcessor bp;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    String userID = mAuth.getCurrentUser().getUid();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String purchaseProductId = "1";
    private List<SkuDetails> purchaseTransactionDetails = null;
    String pur_code=purchaseCode;
    ProgressBar loadingProgressBarUpgrade;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_upgrade);

        buttonUpgrade = findViewById(R.id.buttonUpgrade);
        fabBack = findViewById(R.id.fabBack);
        toggle_button_layout = findViewById(R.id.toggle_button_layout);
        tvListsCount = findViewById(R.id.tvListsCount);
        loadingProgressBarUpgrade=findViewById(R.id.loadingProgressBarUpgrade);
        setupPriceToggle();

        bp = new BillingProcessor(this, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnDatsVXJEFzzwnOEBiE5wSffxr+dEazc3zbf5t5jK1NKYPlfBbeN2M8ZEA38YRt0pQ0WfnXGcJ0mauXH/0xtXdo9Hv6uyzn3W73W6RxTbc5fk2950Tn0fqHkTh6wZoEJBaLn5OnhUy6GE0Yf5VM4oj3HeY5li6ESi8PggUMeYmMcvLzcOsQ8rh4G2KBWqXcYOTMREyfFXp6jJLXHDrJqeeSAEnP/aGLPPyi2NRy5S7dp8qPIkjDYt6yU+FICSBcDAPPWO1jNZrWH43ObcDF4KNdp5CAf/HT5GLcwZv+CUvQGgtuOyiN193NE9wpV5jpA2BgV7FxENqe9T1NIPk8AMwIDAQAB", AppUpgradeActivity2.this);
        bp.initialize();

        fabBack.setOnClickListener(v -> onBackPressed());
        if (purchaseCode.equals("0")){
            loadFullScreenAd();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(purchaseCode.equals("0")){
            toggle_button_layout.setToggled(R.id.toggle_right, true);
            tvListsCount.setText(getString(R.string.create_up_to_20_to_do_lists));
            purchaseProductId = "tier2";
            pur_code="2";
        } else if(purchaseCode.equals("1")){

            toggle_button_layout.setToggled(R.id.toggle_right, true);
            tvListsCount.setText(getString(R.string.create_up_to_20_to_do_lists));
            purchaseProductId = "tier2";
            pur_code="2";

        }else if(purchaseCode.equals("2")){
            toggle_button_layout.setEnabled(false);
        }

    }

    private void setupPriceToggle() {

        toggle_button_layout.setOnToggledListener((toggleButtonLayout, toggle, aBoolean) -> {
            if(purchaseCode.equals("0")) {
                if (toggle.getId() == R.id.toggle_left) {
                    tvListsCount.setText(getString(R.string.create_up_to_3_to_do_lists));
                    purchaseProductId = "tier1";
                    pur_code = "1";

                } else if (toggle.getId() == R.id.toggle_right) {
                    tvListsCount.setText(getString(R.string.create_up_to_20_to_do_lists));
                    purchaseProductId = "tier2";
                    pur_code = "2";

                }
            }else if(purchaseCode.equals("1")){

                if (toggle.getId() == R.id.toggle_left) {
                    toggle_button_layout.setToggled(R.id.toggle_right, true);
                    Toasty.success(getApplicationContext(), "You are already subscribed to Tier 1", Toast.LENGTH_SHORT).show();
                    tvListsCount.setText(getString(R.string.create_up_to_20_to_do_lists));
                    purchaseProductId = "tier2";
                    pur_code = "2";

                } else if (toggle.getId() == R.id.toggle_right) {
                    tvListsCount.setText(getString(R.string.create_up_to_20_to_do_lists));
                    purchaseProductId = "tier2";
                    pur_code = "2";

                }
            }else{
//                    IN Tier2
            }
            return null;
        });


    }

    public void setPurchaseCodeInDatabase(String product_Id) {

//        Toast.makeText(this, "set purchase code in db", Toast.LENGTH_SHORT).show();

        Map<String, String> purchaseCodeMap = new HashMap<>();
        if(product_Id.equals("tier1")){
            pur_code="1";
        }else if(product_Id.equals("tier2")){
            pur_code="2";
        }
        purchaseCodeMap.put("purchase_code", pur_code);

        db.collection("Users").document(userID).set(purchaseCodeMap, SetOptions.merge()).addOnSuccessListener(aVoid -> {
            purchaseCode=pur_code;
            setPurchaseCode();
//                Toast.makeText(AppUpgradeActivity2.this, "on success", Toast.LENGTH_LONG).show();
        });

    }

    public void setPurchaseCode() {

//        Toast.makeText(this, "set purchase", Toast.LENGTH_SHORT).show();

        db.collection("Users").document(userID).get().addOnSuccessListener(documentSnapshot -> {
            purchaseCode = documentSnapshot.get("purchase_code").toString();
            db.collection("UserTiers").document(purchaseCode).get().addOnSuccessListener(documentSnapshot1 -> {
                Log.i("purchasecode", "purchase code :" + purchaseCode);
                Log.i("purchasecode", "new :" + documentSnapshot1.get("masterListLimit").toString());
                userAccountDetails.add(0, documentSnapshot1.get("masterListLimit").toString());
                userAccountDetails.add(1, documentSnapshot1.get("todoItemLimit").toString());
                Toasty.success(getApplicationContext(), "Package Upgraded", Toast.LENGTH_SHORT).show();
                loadingProgressBarUpgrade.setVisibility(View.GONE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                Intent intent = new Intent(AppUpgradeActivity2.this, MasterTodoListActivity.class);
                startActivity(intent);

            });
        }).addOnFailureListener(e -> {

        });
//        this.recreate();
    }

    @Override
    public void onBackPressed() {
        if (mInterstitialAd != null) {
            mInterstitialAd.show(AppUpgradeActivity2.this);
        }
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }

    @Override
    public void onBillingInitialized() {

        Log.d(TAG, "onBillingInitialized: ");
        ArrayList<String> productIdList = new ArrayList<>();
        productIdList.add("tier1");
        productIdList.add("tier2");

        purchaseTransactionDetails = bp.getSubscriptionListingDetails(productIdList);
        buttonUpgrade.setOnClickListener(v -> {
            if (bp.isSubscriptionUpdateSupported()) {
                bp.subscribe(AppUpgradeActivity2.this, purchaseProductId);
            } else {
                Log.d("MainActivity", "onBillingInitialized: Subscription updated is not supported");
            }
        });

    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        Log.d(TAG, "onProductPurchased: "+productId);
        loadingProgressBarUpgrade.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        setPurchaseCodeInDatabase(productId);

    }

    @Override
    public void onPurchaseHistoryRestored() {
        Log.d(TAG, "onPurchaseHistoryRestored: ");
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        Log.d(TAG, "onBillingError: "+error);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void loadFullScreenAd() {
            AdRequest adRequest = new AdRequest.Builder().build();
//ca-app-pub-3111421321050812/5967628112 our
            //test ca-app-pub-3940256099942544/1033173712
            InterstitialAd.load(this, "ca-app-pub-3111421321050812/5967628112", adRequest,
                    new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            // The mInterstitialAd reference will be null until
                            // an ad is loaded.
                            mInterstitialAd = interstitialAd;

                            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    // Called when fullscreen content is dismissed.
                                    Log.d("TAG", "The ad was dismissed.");
                                }

                                @Override
                                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                    // Called when fullscreen content failed to show.
                                    Log.d("TAG", "The ad failed to show.");
                                }

                                @Override
                                public void onAdShowedFullScreenContent() {
                                    // Called when fullscreen content is shown.
                                    // Make sure to set your reference to null so you don't
                                    // show it a second time.
                                    mInterstitialAd = null;
                                    Log.d("TAG", "The ad was shown.");
                                }
                            });
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error
                            mInterstitialAd = null;
                        }
                    });
        }
}






