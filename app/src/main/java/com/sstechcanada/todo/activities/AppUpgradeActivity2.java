package com.sstechcanada.todo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.savvyapps.togglebuttonlayout.Toggle;
import com.savvyapps.togglebuttonlayout.ToggleButtonLayout;
import com.sstechcanada.todo.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import kotlin.Unit;
import kotlin.jvm.functions.Function3;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_upgrade);

        buttonUpgrade = findViewById(R.id.buttonUpgrade);
        fabBack = findViewById(R.id.fabBack);
        toggle_button_layout = findViewById(R.id.toggle_button_layout);
        tvListsCount = findViewById(R.id.tvListsCount);

//        bp = new BillingProcessor(this, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnDatsVXJEFzzwnOEBiE5wSffxr+dEazc3zbf5t5jK1NKYPlfBbeN2M8ZEA38YRt0pQ0WfnXGcJ0mauXH/0xtXdo9Hv6uyzn3W73W6RxTbc5fk2950Tn0fqHkTh6wZoEJBaLn5OnhUy6GE0Yf5VM4oj3HeY5li6ESi8PggUMeYmMcvLzcOsQ8rh4G2KBWqXcYOTMREyfFXp6jJLXHDrJqeeSAEnP/aGLPPyi2NRy5S7dp8qPIkjDYt6yU+FICSBcDAPPWO1jNZrWH43ObcDF4KNdp5CAf/HT5GLcwZv+CUvQGgtuOyiN193NE9wpV5jpA2BgV7FxENqe9T1NIPk8AMwIDAQAB", null, new BillingProcessor.IBillingHandler() {
//            @Override
//            public void onProductPurchased(String productId, TransactionDetails details) {
//
//
//                String pur_code = purchaseCode;
//
//                if (productId.equals("1")) {
//                    pur_code = "1";
//                } else if (productId.equals("2")) {
//                    pur_code = "2";
//                }
//
//
//                Map<String, String> purchaseCode = new HashMap<>();
//                purchaseCode.put("purchase_code", pur_code);
//
//                db.collection("Users").document(userID).set(purchaseCode).addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        setPurchaseCode();
//                        Toasty.error(getApplicationContext(), "Package Upgraded", Toast.LENGTH_SHORT).show();
//
//                    }
//                });
//
//
//            }
//
//            @Override
//            public void onPurchaseHistoryRestored() {
//
//            }
//
//            @Override
//            public void onBillingError(int errorCode, Throwable error) {
//
//                Toasty.error(getApplicationContext(), "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show();
//
//            }
//
//            @Override
//            public void onBillingInitialized() {
//
//            }
//        });

        bp = new BillingProcessor(this, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnDatsVXJEFzzwnOEBiE5wSffxr+dEazc3zbf5t5jK1NKYPlfBbeN2M8ZEA38YRt0pQ0WfnXGcJ0mauXH/0xtXdo9Hv6uyzn3W73W6RxTbc5fk2950Tn0fqHkTh6wZoEJBaLn5OnhUy6GE0Yf5VM4oj3HeY5li6ESi8PggUMeYmMcvLzcOsQ8rh4G2KBWqXcYOTMREyfFXp6jJLXHDrJqeeSAEnP/aGLPPyi2NRy5S7dp8qPIkjDYt6yU+FICSBcDAPPWO1jNZrWH43ObcDF4KNdp5CAf/HT5GLcwZv+CUvQGgtuOyiN193NE9wpV5jpA2BgV7FxENqe9T1NIPk8AMwIDAQAB", AppUpgradeActivity2.this);
        bp.initialize();


        fabBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppUpgradeActivity2.super.onBackPressed();
            }
        });
        setupPriceToggle();
    }

    private void setupPriceToggle() {
        toggle_button_layout.setToggled(R.id.toggle_left, true);

        toggle_button_layout.setOnToggledListener(new Function3<ToggleButtonLayout, Toggle, Boolean, Unit>() {
            @Override
            public Unit invoke(ToggleButtonLayout toggleButtonLayout, Toggle toggle, Boolean aBoolean) {
                if (toggle.getId() == R.id.toggle_left) {
                    tvListsCount.setText(getString(R.string.create_up_to_3_to_do_lists));
                    purchaseProductId = "tier1";
                    pur_code="1";

                } else if (toggle.getId() == R.id.toggle_right) {
                    tvListsCount.setText(getString(R.string.create_up_to_3_to_do_lists));
                    purchaseProductId = "tier2";
                    pur_code="2";

                }
                return null;
            }
        });


    }

    public void setPurchaseCodeInDatabase(String product_Id) {

        Toast.makeText(this, "set purchase code in db", Toast.LENGTH_SHORT).show();

        Map<String, String> purchaseCode = new HashMap<>();
        if(product_Id.equals("tier1")){
            pur_code="1";
        }else if(product_Id.equals("tier2")){
            pur_code="2";
        }
        purchaseCode.put("purchase_code", pur_code);

        db.collection("Users").document(userID).set(purchaseCode, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                setPurchaseCode();
                Toast.makeText(AppUpgradeActivity2.this, "on success", Toast.LENGTH_LONG).show();
            }
        });

    }

    public void setPurchaseCode() {

        Toast.makeText(this, "set purchase", Toast.LENGTH_SHORT).show();

        db.collection("Users").document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                purchaseCode = documentSnapshot.get("purchase_code").toString();
                db.collection("UserTiers").document(purchaseCode).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Log.i("purchasecode", "purchase code :" + purchaseCode);
                        Log.i("purchasecode", "new :" + documentSnapshot.get("masterListLimit").toString());
                        userAccountDetails.add(0, documentSnapshot.get("masterListLimit").toString());
                        userAccountDetails.add(1, documentSnapshot.get("todoItemLimit").toString());
                        Toasty.success(getApplicationContext(), "Package Upgraded", Toast.LENGTH_SHORT).show();

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
//        this.recreate();
    }

    @Override
    public void onBackPressed() {
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
        buttonUpgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bp.isSubscriptionUpdateSupported()) {
                    bp.subscribe(AppUpgradeActivity2.this, purchaseProductId);
                } else {
                    Log.d("MainActivity", "onBillingInitialized: Subscription updated is not supported");
                }
            }
        });

    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        Log.d(TAG, "onProductPurchased: "+productId);

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
}






