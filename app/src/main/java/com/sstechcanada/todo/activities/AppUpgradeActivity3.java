package com.sstechcanada.todo.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import kotlin.Unit;
import kotlin.jvm.functions.Function3;

import static com.android.billingclient.api.BillingClient.SkuType.SUBS;
import static com.sstechcanada.todo.activities.MasterTodoListActivity.purchaseCode;
import static com.sstechcanada.todo.activities.auth.LoginActivity.userAccountDetails;

//import com.anjlab.android.iab.v3.BillingProcessor;
//import com.anjlab.android.iab.v3.TransactionDetails;

public class AppUpgradeActivity3 extends AppCompatActivity implements PurchasesUpdatedListener {

    LottieAnimationView buttonUpgrade;
    FloatingActionButton fabBack;
    ToggleButtonLayout toggle_button_layout;
    TextView tvListsCount;
    AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener;
    //    BillingProcessor bp;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    String userID = mAuth.getCurrentUser().getUid();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String purchaseProductId;
    List<String> alreadyPurchasedList;
    String pur_code;

    BillingClient billingClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_upgrade);

        buttonUpgrade = findViewById(R.id.buttonUpgrade);
        fabBack = findViewById(R.id.fabBack);
        toggle_button_layout = findViewById(R.id.toggle_button_layout);
        tvListsCount = findViewById(R.id.tvListsCount);

        billingClient = BillingClientSetup.getInstance(this,
                this);

        buttonUpgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpBillingClient();

            }
        });


        fabBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppUpgradeActivity3.super.onBackPressed();
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
                    purchaseProductId = "0";
                } else if (toggle.getId() == R.id.toggle_right) {
                    tvListsCount.setText(getString(R.string.create_up_to_20_to_do_lists));
                    purchaseProductId = "1";
                }
                return null;
            }
        });
    }

    private boolean verifyValidSignature(String signedData, String signature) {
        try {
            // To get key go to Developer Console > Select your app > Development Tools > Services & APIs.
            String base64Key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnDatsVXJEFzzwnOEBiE5wSffxr+dEazc3zbf5t5jK1NKYPlfBbeN2M8ZEA38YRt0pQ0WfnXGcJ0mauXH/0xtXdo9Hv6uyzn3W73W6RxTbc5fk2950Tn0fqHkTh6wZoEJBaLn5OnhUy6GE0Yf5VM4oj3HeY5li6ESi8PggUMeYmMcvLzcOsQ8rh4G2KBWqXcYOTMREyfFXp6jJLXHDrJqeeSAEnP/aGLPPyi2NRy5S7dp8qPIkjDYt6yU+FICSBcDAPPWO1jNZrWH43ObcDF4KNdp5CAf/HT5GLcwZv+CUvQGgtuOyiN193NE9wpV5jpA2BgV7FxENqe9T1NIPk8AMwIDAQAB";

            return Security.verifyPurchase(base64Key, signedData, signature);
        } catch (IOException e) {
            return false;
        }
    }

    public void setUpBillingClient() {

//        acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
//            @Override
//            public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
//                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
//                    Toast.makeText(AppUpgradeActivity3.this, "acknowlegment inside setup billing cliend", Toast.LENGTH_SHORT).show();
//                    purchaseProductId="0";
//                    setPurchaseCodeInDatabase();
//
////                    AppUpgradeActivity3.this.recreate();
//                } else {
//                Toast.makeText(AppUpgradeActivity3.this,"Not able to acknowledge the purchase", Toast.LENGTH_SHORT).show();
//            }
//            }
//        };

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    List<Purchase> purchases = billingClient.queryPurchases(SUBS).getPurchasesList();

                    assert purchases != null;
                    if (purchases.size() > 0) {

                        for (Purchase purchase : purchases) {

                            alreadyPurchasedList = new ArrayList<>();
                            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {

                                Toast.makeText(AppUpgradeActivity3.this, "SKUS billing setup finished" + purchase.getSkus().toString(), Toast.LENGTH_LONG).show();


//                                if (purchase.getSkus().get(0).equals("tier1") || purchase.getOrderId().equals("0")) {
//                                    pur_code = "1";
//
//                                } else if (purchase.getSkus().get(0).equals("tier2") || purchase.getOrderId().equals("tier2")) {
//                                    pur_code = "2";
//
//                                }

                                if (!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
                                    // Invalid purchase
                                    // show error to user
                                    Toast.makeText(getApplicationContext(), "Error : invalid Purchase", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if (!purchase.isAcknowledged()) {

                                    AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                                            .setPurchaseToken(purchase.getPurchaseToken())
                                            .build();

                                    Toast.makeText(AppUpgradeActivity3.this, "billing setup finished :is not  ackn if", Toast.LENGTH_SHORT).show();

                                    Toast.makeText(AppUpgradeActivity3.this, "purcode " + pur_code.toString(), Toast.LENGTH_LONG).show();

                                    billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
                                } else {
                                    // Grant entitlement to the user on item purchase
                                    // restart activity
                                    Toast.makeText(AppUpgradeActivity3.this, "billing setup finished is ackno else", Toast.LENGTH_SHORT).show();
//                                    setPurchaseCodeInDatabase();
                                    handleItemAlreadyPurchase(purchases);

                                }
                            }
                        }

                    } else {
                        loadAllSubscribePackage();
                    }

                } else {
                    Log.e("Error connecting to billing client", String.valueOf(billingResult.getResponseCode()));
                    Toasty.error(AppUpgradeActivity3.this, "Error connecting to billing client", Toast.LENGTH_SHORT).show();

                }
            }



            @Override
            public void onBillingServiceDisconnected() {
                Toast.makeText(AppUpgradeActivity3.this, "You are disconnected from Billing service ", Toast.LENGTH_SHORT).show();
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });

        acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Toast.makeText(AppUpgradeActivity3.this, "acknowlegment  listener inside setup billing cliend", Toast.LENGTH_SHORT).show();
                    purchaseProductId="0";
                    setPurchaseCodeInDatabase();

//                    AppUpgradeActivity3.this.recreate();
                } else {
                    Toast.makeText(AppUpgradeActivity3.this,"Not able to acknowledge the purchase", Toast.LENGTH_SHORT).show();
                }
            }
        };

    }

    private void loadAllSubscribePackage() {

        if (billingClient.isReady()) {
            SkuDetailsParams params = SkuDetailsParams.newBuilder()
                    .setSkusList(Arrays.asList("tier1", "tier2"))
                    .setType(SUBS)
                    .build();

            billingClient.querySkuDetailsAsync(params, new SkuDetailsResponseListener() {
                @Override
                public void onSkuDetailsResponse(@NonNull BillingResult billingResult, @Nullable List<SkuDetails> list) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {

                        assert list != null;
                        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                .setSkuDetails(list.get(Integer.parseInt(purchaseProductId)))
                                .build();
                        int responseCode = billingClient.launchBillingFlow(AppUpgradeActivity3.this, billingFlowParams).getResponseCode();

                        switch (responseCode) {
                            case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE:
                                Toast.makeText(AppUpgradeActivity3.this, "BILLING UNAVAILABLE", Toast.LENGTH_SHORT).show();
                                break;
                            case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
                                Toast.makeText(AppUpgradeActivity3.this, "Error", Toast.LENGTH_SHORT).show();
                                break;
                            case BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED:
                                Toast.makeText(AppUpgradeActivity3.this, "FEATURE NOT SUPPORTED", Toast.LENGTH_SHORT).show();
                                break;
                            case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
                                Toast.makeText(AppUpgradeActivity3.this, "ITEM ALREADY OWNED", Toast.LENGTH_SHORT).show();
                                break;
                            case BillingClient.BillingResponseCode.SERVICE_DISCONNECTED:
                                Toast.makeText(AppUpgradeActivity3.this, "SERVICE DISCONNECTED", Toast.LENGTH_SHORT).show();
                                break;
                            case BillingClient.BillingResponseCode.SERVICE_TIMEOUT:
                                Toast.makeText(AppUpgradeActivity3.this, "SERVICE TIMEOUT", Toast.LENGTH_SHORT).show();
                                break;
                            case BillingClient.BillingResponseCode.ITEM_UNAVAILABLE:
                                Toast.makeText(AppUpgradeActivity3.this, "ITEM UNAVAILABLE", Toast.LENGTH_SHORT).show();
                                break;
                            case BillingClient.BillingResponseCode.OK:

                                break;
                            default:
                                break;
                        }
                    } else {
                        Log.e("Error connecting to billing client", String.valueOf(billingResult.getResponseCode()));
                        Toast.makeText(AppUpgradeActivity3.this, "Error connecting to billing client", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void handleItemAlreadyPurchase(List<Purchase> purchases) {
        pur_code="0";

        for (Purchase purchase : purchases) {
            alreadyPurchasedList = new ArrayList<>();
            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {

                Toast.makeText(this, "already purchases SKUS" + purchase.getSkus().toString(), Toast.LENGTH_LONG).show();

                if (purchase.getSkus().contains("tier1") || purchase.getOrderId().equals("tier1")) {
                    alreadyPurchasedList.add("1");
                    pur_code = "1";

                } else if (purchase.getSkus().contains("tier2") || purchase.getOrderId().equals("tier2")) {
                    alreadyPurchasedList.add("2");
                    pur_code = "2";

                }

                if (!purchase.isAcknowledged()) {
                    AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.getPurchaseToken())
                            .build();

                    billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
                }else{

                    if(!purchaseCode.equals(pur_code)){
                        setPurchaseCodeInDatabase();
                    }
                }

                Toast.makeText(this, "already orderID" + purchase.getOrderId().toString(), Toast.LENGTH_SHORT).show();
            }
        }

        if (alreadyPurchasedList.contains(purchaseProductId)) {
            Toast.makeText(AppUpgradeActivity3.this, "You already have the selected subscription", Toast.LENGTH_SHORT).show();

        } else {
            loadAllSubscribePackage();
        }

        acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Toast.makeText(AppUpgradeActivity3.this, "acknowledgment listener inside handlepurchase", Toast.LENGTH_SHORT).show();
                    purchaseProductId="0";
                    setPurchaseCodeInDatabase();

//                    AppUpgradeActivity3.this.recreate();
                } else {
                    Toast.makeText(AppUpgradeActivity3.this,"Not able to acknowledge the purchase", Toast.LENGTH_SHORT).show();
                }
            }
        };

    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
        Toast.makeText(this, "onPurchasesUpdated " , Toast.LENGTH_LONG).show();

        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
            Toast.makeText(this, "onPurchasesUpdated OK" , Toast.LENGTH_LONG).show();

//------ ---
            pur_code = purchaseCode;
//            if(purchaseProductId.equals("1")) {
//                pur_code="1";
//            }else if(purchaseProductId.equals("2")) {
//                pur_code="2";
//            }
//------ ---

            for (Purchase purchase : list) {
                alreadyPurchasedList = new ArrayList<>();
                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {

                    Toast.makeText(this, "onPurchasesUpdated SKUS" + purchase.getSkus().toString(), Toast.LENGTH_LONG).show();

                    if (purchase.getSkus().get(0).equals("tier1") || purchase.getOrderId().equals("0")) {
                        pur_code = "1";
                    } else if (purchase.getSkus().get(0).equals("tier2") || purchase.getOrderId().equals("1")) {
                        pur_code = "2";
                    }

                    if (!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
                        // Invalid purchase
                        // show error to user
                        Toast.makeText(getApplicationContext(), "Error : invalid Purchase", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Toast.makeText(this, "orderID" + purchase.getOrderId().toString(), Toast.LENGTH_SHORT).show();

                    if (!purchase.isAcknowledged()) {
                        AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();

                        Toast.makeText(this, "onPurchasesUpdated is not  ackn if", Toast.LENGTH_SHORT).show();

                        Toast.makeText(this, "onPurchasesUpdated purcode " + pur_code.toString(), Toast.LENGTH_LONG).show();

                        billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
                    } else {
                        // Grant entitlement to the user on item purchase
                        // restart activity
                        Toast.makeText(this, "onPurchasesUpdated is ackno else", Toast.LENGTH_SHORT).show();
                        setPurchaseCodeInDatabase();


                    }


                }
            }


        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            Purchase.PurchasesResult queryAlreadyPurchasesResult = billingClient.queryPurchases(SUBS);
            List<Purchase> alreadyPurchases = queryAlreadyPurchasesResult.getPurchasesList();
            if (alreadyPurchases != null) {

                for (Purchase purchase : list) {
                    if (purchase.getSkus().get(0).equals("tier1") || purchase.getOrderId().equals("0")) {
                        pur_code = "1";
                    } else if (purchase.getSkus().get(0).equals("tier2") || purchase.getOrderId().equals("tier2")) {
                        pur_code = "2";
                    }

                    if (!purchase.isAcknowledged()) {
                        AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();

                        Toast.makeText(this, "onPurchasesUpdated ITEM_ALREADY_OWNED is not  ackn if", Toast.LENGTH_SHORT).show();

                        Toast.makeText(this, "onPurchasesUpdated ITEM_ALREADY_OWNED purcode " + pur_code.toString(), Toast.LENGTH_LONG).show();

                        billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);


                    } else {
                        // Grant entitlement to the user on item purchase
                        // restart activity
                        Toast.makeText(this, "is ackno else", Toast.LENGTH_SHORT).show();

                        if(!purchaseCode.equals(pur_code)) {
                            setPurchaseCodeInDatabase();
                        }


                    }
                }

            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Toast.makeText(AppUpgradeActivity3.this, "The request was cancelled", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(AppUpgradeActivity3.this, "Error " + billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
        }
        acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Toast.makeText(AppUpgradeActivity3.this, "acknowledgment inside on purchase updated ", Toast.LENGTH_SHORT).show();
                    purchaseProductId="0";
                    setPurchaseCodeInDatabase();

//                    AppUpgradeActivity3.this.recreate();
                } else {
                    Toast.makeText(AppUpgradeActivity3.this,"Not able to acknowledge the purchase", Toast.LENGTH_SHORT).show();
                }
            }
        };

    }
//    AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
//        @Override
//        public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
//            if(billingResult.getResponseCode()==BillingClient.BillingResponseCode.OK){
//                //if purchase is acknowledged
//                // Grant entitlement to the user. and restart activity
//                        Toast.makeText(AppUpgradeActivity3.this,"is ackno listener",Toast.LENGTH_SHORT).show();
//                setPurchaseCodeInDatabase();
//            }
//        }
//    };

    public void setPurchaseCodeInDatabase() {

        Toast.makeText(this, "set purchase code in db", Toast.LENGTH_SHORT).show();

        Map<String, String> purchaseCode = new HashMap<>();
        purchaseCode.put("purchase_code", pur_code);

        db.collection("Users").document(userID).set(purchaseCode, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                setPurchaseCode();
                Toast.makeText(AppUpgradeActivity3.this, "on success", Toast.LENGTH_LONG).show();
            }
        });
        this.recreate();

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
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    protected void onDestroy() {
        super.onDestroy();
        if (billingClient != null) {
            billingClient.endConnection();
        }
    }


}






