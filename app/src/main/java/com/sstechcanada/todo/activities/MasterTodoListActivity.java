package com.sstechcanada.todo.activities;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.sstechcanada.todo.R;
import com.sstechcanada.todo.activities.auth.LoginActivity;
import com.sstechcanada.todo.activities.auth.ProfileActivity;
import com.sstechcanada.todo.adapters.MasterListFirestoreAdapter;
import com.sstechcanada.todo.adapters.MasterListGridViewAdapter;
import com.sstechcanada.todo.custom_views.MasterIconGridItemView;
import com.sstechcanada.todo.models.List;
import com.sstechcanada.todo.utils.Constants;
import com.sstechcanada.todo.utils.SaveSharedPreference;
import com.sstechcanada.todo.utils.SwipeController;
import com.sstechcanada.todo.utils.SwipeControllerActions;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import hotchemi.android.rate.AppRate;

import static com.sstechcanada.todo.activities.auth.LoginActivity.SHAREDPREF;
import static com.sstechcanada.todo.activities.auth.LoginActivity.userAccountDetails;

public class MasterTodoListActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {

    private static final String TAG = MasterTodoListActivity.class.getSimpleName();
    public static int list_cnt = 0;
    //    ArrayList<String>
    public static Integer[] listDrawable;
    public static String listId, listName;
    public static String purchaseCode = "0";
    String userID;
    ImageView placeholderImage;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersColRef = db.collection("Users");
    private final Handler mHandler = new Handler();
    private int list_limit = 15;
    private RecyclerView mRecyclerView;
    //    private MasterTodoListActivity mBinding;
    private SharedPreferences mSharedPreferences, ll;
    private AppCompatImageView toolbar_profile;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private AdView adView;
    private DatabaseReference databaseReference;
    ProgressBar progressBar, loadingProgressBar;
    //    String
    int selectedDrawable, sdrawable;
    private MasterListFirestoreAdapter masterListFirestoreAdapter;
    private GridView gridView;
    String purchaseProductId;
    java.util.List<String> alreadyPurchasedList;
    String pur_code;
    BillingClient billingClient;
    AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener;
    private Button buttonTapTargetView;
    private InterstitialAd mInterstitialAd;

    private MasterListGridViewAdapter gridAdapter;
    FloatingActionButton fab;
    private boolean doubleBackToExitPressedOnce;
    BillingProcessor bp;
    SharedPreferences.Editor editor;
    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            doubleBackToExitPressedOnce = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master_todo_list);

        mRecyclerView = findViewById(R.id.rv_todo_list);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        fab = findViewById(R.id.fab);
        buttonTapTargetView = findViewById(R.id.buttonTapTargetView);
        SharedPreferences prefs = getSharedPreferences(SHAREDPREF, MODE_PRIVATE);
        editor = getSharedPreferences(SHAREDPREF, MODE_PRIVATE).edit();

        listDrawable = new Integer[]{
                R.drawable.master_list_default_icon, R.drawable.idea, R.drawable.ic_lock, R.drawable.ic_to_do_list,
                R.drawable.circle_per_item, R.drawable.sport, R.drawable.movie, R.drawable.globe, R.drawable.music,
                R.drawable.heart, R.drawable.diet, R.drawable.book,
                R.drawable.shopping_cart,
        };


        openRatingPopup();
        fetchIntent();
//        placeholderImage=findViewById(R.id.placeholderImage);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if (user != null) {
            userID = user.getUid();
        }

        getPurchaseCode();

//        lottieAnimationView = findViewById(R.id.placeholderImage);

        setUpFirestoreRecyclerView();

        if (prefs.getBoolean("flagMasterListFirstRun", true)) {
            buttonTapTargetView.setVisibility(View.INVISIBLE);

            callWalkThrough();
        }


        //Limit Set

        setValue();


        toolbar_profile = findViewById(R.id.profile_toolbar);
        Glide.with(this).load(mAuth.getCurrentUser().getPhotoUrl()).into(toolbar_profile);
        toolbar_profile.setOnClickListener(view -> startActivity(new Intent(MasterTodoListActivity.this, ProfileActivity.class)));


        fab.setOnClickListener(view -> {
            if (Integer.parseInt(userAccountDetails.get(0)) > masterListFirestoreAdapter.getItemCount()) {
                Log.i("purchasecode", "masterlist limit :" + (userAccountDetails.get(0)));
                Log.i("purchasecode", "masterlist items :" + (masterListFirestoreAdapter.getItemCount()));
                setValue();
                if (isLogin()) {
                    addNewListAlert();
                }
            } else {
                if (isLogin()) {
                    if (!purchaseCode.equals("2")) {
                        Toasty.info(getApplicationContext(), getString(R.string.upgrade_master_list), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MasterTodoListActivity.this, AppUpgradeActivity.class);
//                        intent.putExtra(getString(R.string.intent_adding_or_editing_key), getString(R.string.add_new_task));
                        startActivity(intent);
                    } else if (purchaseCode.equals("2")) {
                        Toasty.warning(getApplicationContext(), "Sorry, You cannot add more to-do list. You have reached the max-limit!", Toast.LENGTH_LONG).show();
                    }

                }

            }
        });

        adView = findViewById(R.id.adView);


    }


    private void getPurchaseCode() {
        showProgressBar();
        usersColRef.document(userID).get().addOnSuccessListener(documentSnapshot -> {
            purchaseCode = documentSnapshot.get("purchase_code").toString();
            db.collection("UserTiers").document(purchaseCode).get().addOnSuccessListener(documentSnapshot1 -> {
                Log.i("purchasecode", "purchase code :" + purchaseCode);
                Log.i("purchasecode", "new :" + documentSnapshot1.get("masterListLimit").toString());
                userAccountDetails.add(0, documentSnapshot1.get("masterListLimit").toString());
                userAccountDetails.add(1, documentSnapshot1.get("todoItemLimit").toString());

                if (!purchaseCode.equals("0")) {
                    bp = new BillingProcessor(MasterTodoListActivity.this, getString(R.string.license_key), MasterTodoListActivity.this);
                    bp.initialize();
                    SaveSharedPreference.setAdsEnabled(this, false);
                } else {
                    hideProgressBar();
                    adView.setVisibility(View.VISIBLE);
                    SaveSharedPreference.setAdsEnabled(this, true);
                    if (SaveSharedPreference.getAdsEnabled(this)) {
                        loadFullScreenAds();
                        AdRequest adRequest = new AdRequest.Builder().build();
                        adView.loadAd(adRequest);
                    } else {
                        adView.setVisibility(View.GONE);
                    }
                }
            });
        }).addOnFailureListener(e -> {

        });

//        this.recreate();
    }

    public void addNewListAlert() {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.add_list_dialog, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Add List");
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        gridView = alertLayout.findViewById(R.id.grid_view_alert);
//        addMoreCategories = alertLayout.findViewById(R.id.addMoreCategoriesLayout);
        progressBar = alertLayout.findViewById(R.id.progress_circular);

        AdView bannerAd = alertLayout.findViewById(R.id.adView);
        loadImages(0);
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false);
        alert.setNegativeButton("Cancel", (dialog, which) -> {
//                Toast.makeText(getBaseContext(), "Cancel clicked", Toast.LENGTH_SHORT).show();
        });
        alert.setPositiveButton("Done", (dialog, which) -> {
            sdrawable = selectedDrawable;
//            int imageResource = getResources().getIdentifier(sdrawable, null, getPackageName());
            String name = ((EditText) alertLayout.findViewById(R.id.editTextListName)).getText().toString();
//            String description = ((EditText) alertLayout.findViewById(R.id.editTextListDescription)).getText().toString();

            usersColRef.document(userID).collection("Lists");

            Map<String, Object> newList = new HashMap<>();
            newList.put("ListName", name);
            newList.put("positionImage", sdrawable);
//            newList.put("ListDescription", description);

            usersColRef.document(userID).collection("Lists").document().set(newList).addOnSuccessListener(aVoid -> Toasty.success(MasterTodoListActivity.this, "New List Successfully Added")).addOnFailureListener(e -> Toasty.error(MasterTodoListActivity.this, "Something went wrong"));
        });

        if (SaveSharedPreference.getAdsEnabled(this)) {
            bannerAd.loadAd(new AdRequest.Builder().build());
        } else {
            bannerAd.setVisibility(View.GONE);
        }

        AlertDialog dialog = alert.create();
        dialog.show();

    }

    public void editListAlert(String oldListName, int oldListIconPosition, String documentSnapshotId) {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.add_list_dialog, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Edit List");
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        gridView = alertLayout.findViewById(R.id.grid_view_alert);
//        addMoreCategories = alertLayout.findViewById(R.id.addMoreCategoriesLayout);
        progressBar = alertLayout.findViewById(R.id.progress_circular);

        AdView bannerAd = alertLayout.findViewById(R.id.adView);

//        gridAdapter.selectedPosition=oldListIconPosition;
        loadImages(oldListIconPosition);
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false);
        alert.setNegativeButton("Cancel", (dialog, which) -> {
//                Toast.makeText(getBaseContext(), "Cancel clicked", Toast.LENGTH_SHORT).show();
        });
        alert.setPositiveButton("Done", (dialog, which) -> {
            sdrawable = selectedDrawable;
//            int imageResource = getResources().getIdentifier(sdrawable, null, getPackageName());
            String name = ((EditText) alertLayout.findViewById(R.id.editTextListName)).getText().toString();
//            String description = ((EditText) alertLayout.findViewById(R.id.editTextListDescription)).getText().toString();

            usersColRef.document(userID).collection("Lists");

            Map<String, Object> list = new HashMap<>();
            list.put("ListName", name);
            list.put("positionImage", sdrawable);
//            list.put("ListDescription", description);

            usersColRef.document(userID).collection("Lists").document(documentSnapshotId).update(list).addOnSuccessListener(aVoid -> Toasty.success(MasterTodoListActivity.this, "List Successfully Edited")).addOnFailureListener(e -> Toasty.error(MasterTodoListActivity.this, "Something went wrong"));
        });

        if (SaveSharedPreference.getAdsEnabled(this)) {
            bannerAd.loadAd(new AdRequest.Builder().build());
        } else {
            bannerAd.setVisibility(View.GONE);
        }


        AlertDialog dialog = alert.create();
        dialog.show();

        EditText listNameEditText = dialog.findViewById(R.id.editTextListName);
        listNameEditText.setText(oldListName);
//        EditText ListDescriptionEditText = dialog.findViewById(R.id.editTextListDescription);
//        ListDescriptionEditText.setText(oldListDescription);

    }

    public void loadImages(int iconPosition) {
        gridAdapter = new MasterListGridViewAdapter(listDrawable, MasterTodoListActivity.this);
        gridView.setAdapter(gridAdapter);
        gridView.setVisibility(View.VISIBLE);

        gridAdapter.selectedPosition = iconPosition;

        gridView.setOnItemClickListener((parent, v, position, id) -> {
//            gridView.setAdapter(null);
//            gridView.setAdapter(gridAdapter);

            Log.i("gridView", "on click");

            int selectedIndex = gridAdapter.selectedPosition;

            if (selectedIndex == position) {
                ((MasterIconGridItemView) v).display(false);

                gridAdapter.selectedPosition = -1;
                selectedDrawable = iconPosition;
            } else {
                Log.i("gridView", String.valueOf(position));

                if (gridAdapter.selectedPosition != -1) {
                    ((MasterIconGridItemView) gridView.getChildAt(gridAdapter.selectedPosition)).display(false);

                }

                selectedDrawable = position;
                ((MasterIconGridItemView) v).display(true);
                gridAdapter.selectedPosition = position;

            }

        });
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void setUpFirestoreRecyclerView() {

        Query query = usersColRef.document(userID).collection("Lists");
        FirestoreRecyclerOptions<List> options = new FirestoreRecyclerOptions.Builder<List>().setQuery(query, List.class).build();
        masterListFirestoreAdapter = new MasterListFirestoreAdapter(options, this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        SwipeController swipeController = new SwipeController(this, new SwipeControllerActions() {
            @Override
            public void onRightClicked(int position) {
                Log.i("cluck", "right");

                new AlertDialog.Builder(MasterTodoListActivity.this)
                        .setIcon(android.R.drawable.ic_menu_delete)
                        .setTitle("Confirm Delete")
                        .setMessage("Are you sure you want to delete this list?")
                        .setPositiveButton("Yes", (dialog, which) -> {

                            if (SaveSharedPreference.getAdsEnabled(getApplicationContext())) {
                                if (mInterstitialAd != null) {
                                    mInterstitialAd.show(MasterTodoListActivity.this);
                                    DocumentSnapshot documentSnapshot = masterListFirestoreAdapter.getSnapshots().getSnapshot(position);
                                    String id = documentSnapshot.getId();
                                    usersColRef.document(userID).collection("Lists").document(id).delete();
                                } else {
                                    DocumentSnapshot documentSnapshot = masterListFirestoreAdapter.getSnapshots().getSnapshot(position);
                                    String id = documentSnapshot.getId();
                                    usersColRef.document(userID).collection("Lists").document(id).delete();
                                }

                            } else {
                                DocumentSnapshot documentSnapshot = masterListFirestoreAdapter.getSnapshots().getSnapshot(position);
                                String id = documentSnapshot.getId();
                                usersColRef.document(userID).collection("Lists").document(id).delete();
                                Toasty.error(MasterTodoListActivity.this, "List Deleted", Toast.LENGTH_SHORT).show();
                            }


                        })
                        .setNegativeButton("No", null)
                        .show();

            }

            @Override
            public void onLeftClicked(int position) {
                Log.i("cluck", "left");
                DocumentSnapshot documentSnapshot = masterListFirestoreAdapter.getSnapshots().getSnapshot(position);
                List list = documentSnapshot.toObject(List.class);
//                List list=masterListFirestoreAdapter.getItem(position);
                String oldListName = list.getListName();
//                String oldListDescription = list.getListDescription();
                int oldListIconPosition = list.getPositionImage();

                editListAlert(oldListName, oldListIconPosition, documentSnapshot.getId());
            }
        });
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(mRecyclerView);
        mRecyclerView.setAdapter(masterListFirestoreAdapter);

        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });


        list_cnt = masterListFirestoreAdapter.getItemCount();

    }

    private void loadFullScreenAds() {
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
                                Toasty.error(MasterTodoListActivity.this, "List Deleted", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onStart() {
        super.onStart();
        masterListFirestoreAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        masterListFirestoreAdapter.stopListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.todo_list_menu, menu);
        //For 3 Dot menu
        return false;
    }


//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
////        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            updateWidget();
        }
    }

    private void updateWidget() {
        // let the widget know there's been a database or sort order change
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        sendBroadcast(intent);
    }

    @Override
    protected void onResume() {

        super.onResume();
        // This is so that if we've edited a task directly from the widget, the widget will still
        // get updated when we come to this activity after clicking UPDATE TASK in adOrEditTaskActivity
        updateWidget();

    }

    public boolean isLogin() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toasty.warning(this, getString(R.string.login_first), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MasterTodoListActivity.this, LoginActivity.class));
            return false;
        } else {
            return true;
        }
//        return true;
    }

    public void setValue() {
        if (user != null) {
            list_limit = 15;
        }
    }

    private void showProgressBar() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void hideProgressBar() {
        if (loadingProgressBar.getVisibility() == View.VISIBLE) {
            loadingProgressBar.setVisibility(View.INVISIBLE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
        }
    }

    @Override
    public void onBackPressed() {

//            if(getSupportFragmentManager().findFragmentById(R.id.fragment_container).getParentFragment()==R.layout.fragment_profile){
        if (doubleBackToExitPressedOnce) {
            this.finishAffinity();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toasty.info(this, "Please press back again to exit", Toast.LENGTH_SHORT).show();
        mHandler.postDelayed(mRunnable, 2000);

    }

    public void callWalkThrough() {

        new TapTargetSequence(this)
                .targets(
                        TapTarget.forView(fab, "Add Button", "Click here to add a new list")
                                .outerCircleColor(R.color.chip_5)
                                .outerCircleAlpha(0.85f)
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
                                .targetRadius(80),
                        TapTarget.forView(buttonTapTargetView, "List", "1: Swipe right and click on the pencil icon to update a list. \n2: Swipe left and click on the garbage can icon to delete a list.")
                                .outerCircleColor(R.color.chip_5)
                                .outerCircleAlpha(0.85f)
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
                                .targetRadius(60)).listener(new TapTargetSequence.Listener() {
            @Override
            public void onSequenceFinish() {

//                Toast.makeText(MasterTodoListActivity.this,"Sequence Finished",Toast.LENGTH_SHORT).show();
                Toasty.success(MasterTodoListActivity.this, "Awesome!", Toast.LENGTH_SHORT).show();

//                flagMasterListFirstRun = false;
                buttonTapTargetView.setVisibility(View.GONE);
                editor.putBoolean("flagMasterListFirstRun", false);
                editor.apply();
            }

            @Override
            public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {


            }

            @Override
            public void onSequenceCanceled(TapTarget lastTarget) {
                buttonTapTargetView.setVisibility(View.GONE);
                editor.putBoolean("flagMasterListFirstRun", false);
                editor.apply();
            }
        }).start();
    }


    public void isUserSubscribed(String purchaseCode) {
        try {
            boolean purchaseResult = bp.loadOwnedPurchasesFromGoogle();


//            Toast.makeText(MasterTodoListActivity.this, "Inside billing: "+purchaseResult+ " "+purchaseCode, Toast.LENGTH_SHORT).show();
            String purchaseID = "";
            if (user != null) {
                if (purchaseCode.equals("1")) {
                    purchaseID = "tier1";
                } else if (purchaseCode.equals("2")) {
                    purchaseID = "tier2";
                } else if (purchaseCode.equals("3")) {
                    purchaseID = "adfree";
                }
                if (purchaseResult) {
                    TransactionDetails subscriptionTransactionDetails = bp.getSubscriptionTransactionDetails(purchaseID);
                    if (subscriptionTransactionDetails != null) {
                        //User is still subscribed
//                        Toast.makeText(MasterTodoListActivity.this, "Inside billing+ user is still subscribed in", Toast.LENGTH_SHORT).show();
                    } else {
                        //Not subscribed
                        refreshPurchaseCodeInDatabase();
//                        Toast.makeText(MasterTodoListActivity.this, "Inside billing+ user is not subscribed", Toast.LENGTH_SHORT).show();
                    }
                }

//                Toast.makeText(MasterTodoListActivity.this, "Inside billing +user iobject not null" + purchaseID, Toast.LENGTH_SHORT).show();
            }
            hideProgressBar();
        } catch (Exception e) {
//            Toast.makeText(MasterTodoListActivity.this, "Inside billing +Exception in "+e, Toast.LENGTH_SHORT).show();
            hideProgressBar();
        }

    }

    public void refreshPurchaseCodeInDatabase() {

//        Toast.makeText(this, "set purchase code in db", Toast.LENGTH_SHORT).show();

        Map<String, String> purchaseCode = new HashMap<>();
        purchaseCode.put("purchase_code", "0");

        db.collection("Users").document(userID).set(purchaseCode,
                SetOptions.merge()).addOnSuccessListener(aVoid -> {
            getPurchaseCode();
//                Toast.makeText(MasterTodoListActivity.this, "on success", Toast.LENGTH_LONG).show();
        });

    }

    public void openRatingPopup() {
        // callback listener.
        AppRate.with(this)
                .setInstallDays(1) // default 10, 0 means install day.
                .setLaunchTimes(25) // default 10
                .setRemindInterval(2) // default 1
                .setShowLaterButton(true)
                .setTitle(getString(R.string.will_you_rate_us_5_stars))// default true
                .setDebug(false) // default false
                .setOnClickButtonListener(which -> Log.d(MasterTodoListActivity.class.getName(), Integer.toString(which)))
                .monitor();

        AppRate.showRateDialogIfMeetsConditions(this);

        //Show Dialog Instantly
        //AppRate.with(this).showRateDialog(this);

    }

    private void fetchIntent() {
        if (getIntent().hasExtra(Constants.TODO_RATE_APP)) {
            AppRate.with(this).showRateDialog(this);
        }
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {

    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {

    }

    @Override
    public void onBillingInitialized() {
        isUserSubscribed(purchaseCode);
    }
}






