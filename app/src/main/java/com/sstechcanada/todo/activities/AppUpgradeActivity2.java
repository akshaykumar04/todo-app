package com.sstechcanada.todo.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.savvyapps.togglebuttonlayout.Toggle;
import com.savvyapps.togglebuttonlayout.ToggleButtonLayout;
import com.sstechcanada.todo.R;
import java.util.HashMap;
import java.util.Map;
import es.dmoral.toasty.Toasty;
import kotlin.Unit;
import kotlin.jvm.functions.Function3;
import static com.sstechcanada.todo.activities.MasterTodoListActivity.purchaseCode;
import static com.sstechcanada.todo.activities.auth.LoginActivity.userAccountDetails;
public class AppUpgradeActivity2 extends AppCompatActivity {

    LottieAnimationView buttonUpgrade;
    FloatingActionButton fabBack;
    ToggleButtonLayout toggle_button_layout;
    TextView tvListsCount;
    BillingProcessor bp;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    String userID=mAuth.getCurrentUser().getUid();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String purchaseProductId="1";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_upgrade);

        buttonUpgrade = findViewById(R.id.buttonUpgrade);
        fabBack = findViewById(R.id.fab);
        toggle_button_layout=findViewById(R.id.toggle_button_layout);
        tvListsCount=findViewById(R.id.tvListsCount);

        bp = new BillingProcessor(this, "YOUR LICENSE KEY FROM GOOGLE PLAY CONSOLE HERE", null, new BillingProcessor.IBillingHandler() {
            @Override
            public void onProductPurchased(String productId, TransactionDetails details) {
                

         String pur_code = purchaseCode;
                
                if(productId.equals("1")) {
                    pur_code="1";
                }else if(productId.equals("2")) {
                    pur_code="2";
                }


                    

                Map<String, String> purchaseCode = new HashMap<>();
                purchaseCode.put("purchase_code", pur_code);
                
                db.collection("Users").document(userID).set(purchaseCode).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        setPurchaseCode();
                        Toasty.error(getApplicationContext(), "Package Upgraded", Toast.LENGTH_SHORT).show();
                      
                    }
                });


            }

            @Override
            public void onPurchaseHistoryRestored() {

            }

            @Override
            public void onBillingError(int errorCode, Throwable error) {

                Toasty.error(getApplicationContext(), "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onBillingInitialized() {

            }
        });



        buttonUpgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bp.purchase(AppUpgradeActivity2.this,purchaseProductId);

//                try {
//                    startActivity( new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$premium_app")));
//                } catch (ActivityNotFoundException e) {
//                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$premium_app")));
//                }

            }
        });


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
                if (toggle.getId()==R.id.toggle_left) {
                    tvListsCount.setText(getString(R.string.create_up_to_3_to_do_lists));
                    purchaseProductId="1";

                }else if(toggle.getId()==R.id.toggle_right){
                    tvListsCount.setText(getString(R.string.create_up_to_3_to_do_lists));
                    purchaseProductId="2";

                }


                return null;
            }
        });


    }

    public void setPurchaseCode() {
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

    @Override
    public void onDestroy() {
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }


}






