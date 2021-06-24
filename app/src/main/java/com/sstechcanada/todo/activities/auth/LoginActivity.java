package com.sstechcanada.todo.activities.auth;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.sstechcanada.todo.R;
import com.sstechcanada.todo.activities.MasterTodoListActivity;
import com.sstechcanada.todo.utils.SaveSharedPreference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

import static com.sstechcanada.todo.activities.MasterTodoListActivity.purchaseCode;

public class LoginActivity extends AppCompatActivity {

    public static final String SHAREDPREF = "UserSharedPrefFile";
    public static final String LIST_LIMIT = "LIST_LIMIT";
    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 9001;
    public static ArrayList<String> userAccountDetails =new ArrayList<>();
    SignInButton googleSignInButton;
    CardView profileCard;
    ImageView placeHolder, dp;
    TextView userName, userType, userEmail;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    int list_limit;
    //Shared Preference
    SharedPreferences sharedpreferences;
    private FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ProgressDialog pDialog;
    private GoogleSignInClient mGoogleSignInClient;
    private ProgressBar progressBar;
    private Button signOutButton;
    private FirebaseUser user;
    private FloatingActionButton fabBack;
    private AdView bannerAd;
    public static Boolean flagMasterListFirstRun=false;
    public static Boolean flagTodoListFirstRun=false;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        googleSignInButton = findViewById(R.id.sign_in_button);
        signOutButton = findViewById(R.id.sign_out_button);
        progressBar = findViewById(R.id.progressBar2);
        profileCard = findViewById(R.id.myCardView);
        placeHolder = findViewById(R.id.imageView);
        dp = findViewById(R.id.roundedImage);
        userName = findViewById(R.id.tv_userName);
        userType = findViewById(R.id.tv_userType);
        userEmail = findViewById(R.id.tv_userEmail);
        fabBack = findViewById(R.id.fabBack);

        bannerAd = findViewById(R.id.adView);
        if(purchaseCode.equals("0")){
            bannerAd.loadAd(new AdRequest.Builder().build());
        }else{
            bannerAd.setVisibility(View.GONE);
        }
//        setUpSharedPref();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        pDialog = new ProgressDialog(this);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(LoginActivity.this, gso);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        googleSignInButton.setOnClickListener(v -> signIn());
        signOutButton.setOnClickListener(v -> showSignOutDialog());

        fabBack.setOnClickListener(view -> {
            super.onBackPressed();
        });

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String data = bundle.getString("key");
            if (data == null) {
                signOut();
            }
        }
        checkUserStatus();
    }

    private void showSignOutDialog() {
        androidx.appcompat.app.AlertDialog.Builder alert = new androidx.appcompat.app.AlertDialog.Builder(this);
        alert.setTitle(R.string.sign_out);
        alert.setMessage(R.string.are_you_sure);
        alert.setPositiveButton(
                R.string.yes,
                (dialog, id) -> signOut());

        alert.setNegativeButton(
                R.string.no,
                (dialog, id) -> dialog.dismiss());
        alert.show();
    }

    /**
     * Display Progress bar while Logging in through Google
     */
    private void displayProgressDialog() {
        pDialog.setMessage("Logging In.. Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        user = mAuth.getCurrentUser();
        updateUI(user);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        displayProgressDialog();
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            SaveSharedPreference.setUserLogIn(LoginActivity.this, "true");
//                            startActivity(new Intent(LoginActivity.this, TodoListActivity2.class));
                            Toasty.success(getApplicationContext(), "Sign in complete", Toast.LENGTH_SHORT).show();
                            hideProgressDialog();
//                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
//                            databaseReference.child("Users").child(firebaseUser.getUid()).child("Email").setValue(firebaseUser.getEmail());
//                            databaseReference.child(firebaseUser.getUid()).child("Email").setValue(firebaseUser.getEmail());
                            updateUserPackage();
                            checkUserStatus();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
//                            Toasty.error(getApplicationContext(), "Login Failed: ", Toast.LENGTH_LONG).show();
                        }

                        hideProgressDialog();
                    }

                });
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
    }

    private void hideProgressDialog() {
        pDialog.dismiss();
    }

    private void signOut() {
        // Firebase sign out

        mAuth.signOut();
        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                task -> {
                    updateUI(null);
                    SaveSharedPreference.saveLimit(getApplicationContext(), 0);
                });
        checkUserStatus();
        SaveSharedPreference.setUserLogIn(LoginActivity.this, "false");
        finishAffinity();
        startActivity(new Intent(LoginActivity.this, LoginActivity.class));
    }

    @SuppressLint("RestrictedApi")
    private void checkUserStatus() {
        FirebaseUser User = mAuth.getCurrentUser();
        if (User != null) {
            googleSignInButton.setVisibility(View.GONE);
            signOutButton.setVisibility(View.VISIBLE);
            profileCard.setVisibility(View.VISIBLE);
            dp.setVisibility(View.VISIBLE);
            placeHolder.setVisibility(View.GONE);
            userName.setVisibility(View.VISIBLE);
            userName.setText(User.getDisplayName());
            userEmail.setVisibility(View.VISIBLE);
            userEmail.setText(User.getEmail());
            userType.setVisibility(View.VISIBLE);
            fabBack.setVisibility(View.VISIBLE);
            bannerAd.setVisibility(View.VISIBLE);
            list_limit = SaveSharedPreference.loadLimit(this);
            if (purchaseCode.equals("0")) {
                userType.setText(R.string.free_user);
            } else {
                userType.setText(R.string.premium_user);
            }
            updateUI(User);
        } else {
            googleSignInButton.setVisibility(View.VISIBLE);
            signOutButton.setVisibility(View.GONE);
            profileCard.setVisibility(View.GONE);
            placeHolder.setVisibility(View.VISIBLE);
            userName.setVisibility(View.GONE);
            userType.setVisibility(View.GONE);
            userEmail.setVisibility(View.GONE);
        }
    }

    private void updateUserPackage() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        DocumentReference documentReferenceCurrentReference=db.collection("Users").document(firebaseUser.getUid());

//        long creationTimestamp = mAuth.getCurrentUser().getMetadata().getCreationTimestamp();
//        long lastSignInTimestamp = mAuth.getCurrentUser().getMetadata().getLastSignInTimestamp();
        db.collection("Users").whereEqualTo("Email",firebaseUser.getEmail()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
               if(queryDocumentSnapshots.size()==0){
                   Map<String, String> profile = new HashMap<>();
                   profile.put("Email", firebaseUser.getEmail());
                   profile.put("purchase_code", "0");
//                   flagMasterListFirstRun=true;
//                   flagTodoListFirstRun=true;
                   editor.putBoolean("flagMasterListFirstRun", true);
                   editor.putBoolean("flagTodoListFirstRun", true);

                   editor.apply();
                   documentReferenceCurrentReference.set(profile).addOnSuccessListener(new OnSuccessListener<Void>() {
                       @Override
                       public void onSuccess(Void aVoid) {
//                           startActivity(new Intent(LoginActivity.this, MasterTodoListActivity.class));
                           Toasty.success(getApplicationContext(), "Profile creation complete", Toast.LENGTH_SHORT).show();
                           startActivity(new Intent(LoginActivity.this, MasterTodoListActivity.class));

                       }
                   }).addOnFailureListener(new OnFailureListener() {
                       @Override
                       public void onFailure(@NonNull Exception e) {
                           Toasty.error(getApplicationContext(), "Error in profile creation", Toast.LENGTH_SHORT).show();
                       }
                   });

               }else{
                    startActivity(new Intent(LoginActivity.this, MasterTodoListActivity.class));
               }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

        /////CreationTimestamp logic not working properly because of firebase migration
//        if (creationTimestamp==lastSignInTimestamp) {
//            Map<String, String> profile = new HashMap<>();
//            profile.put("Email", firebaseUser.getEmail());
//            profile.put("purchase_code", "0");
//            documentReferenceCurrentReference.set(profile).addOnSuccessListener(new OnSuccessListener<Void>() {
//                @Override
//                public void onSuccess(Void aVoid) {
//                    startActivity(new Intent(LoginActivity.this, MasterTodoListActivity.class));
//                    Toasty.success(getApplicationContext(), "Profile Updation Failed: ", Toast.LENGTH_LONG).show();
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    Toasty.error(getApplicationContext(), "Profile Updation Failed: ", Toast.LENGTH_LONG).show();
//                }
//            });
//        }else{ startActivity(new Intent(LoginActivity.this, MasterTodoListActivity.class));}


//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(firebaseUser.getUid());
//        databaseReference.child(firebaseUser.getUid()).child("Email").setValue(firebaseUser.getEmail());
//        databaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                String ans = snapshot.child("purchase_code").getValue(String.class);
//                String limit = snapshot.child("").getValue(String.class);
//
//                if (!snapshot.child("purchase_code").exists()) {
//                    databaseReference.child("purchase_code").setValue("0");
//                    databaseReference.child("item_limit").setValue("15");
//                    SaveSharedPreference.saveLimit(getApplicationContext(), 15);
//                } else {
//                    list_limit = Integer.parseInt(ans);
//                    if (list_limit != 0) {
//                        userType.setText(R.string.premium_user);
//                    } else {
//                        userType.setText(R.string.free_user);
//                    }
//                    SaveSharedPreference.saveLimit(getApplicationContext(), 15);
////                    Toast.makeText(LoginActivity.this, ""+list_limit, Toast.LENGTH_SHORT).show();
//                }
//                if (!snapshot.child("purchase_type").exists()) {
//                    databaseReference.child("purchase_type").setValue("Free User");
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
    }
    public void setUpSharedPref(){
        editor = getSharedPreferences(SHAREDPREF, MODE_PRIVATE).edit();
        editor.putBoolean("flagMasterListFirstRun", false);
        editor.putBoolean("flagTodoListFirstRun", false);
        editor.apply();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(user!=null){
            startActivity(new Intent(LoginActivity.this,MasterTodoListActivity.class));
        }
    }

    public void setValue() {
        if (user != null) {
            list_limit = SaveSharedPreference.loadLimit(this);
        }
//        Toast.makeText(this, list_limit + " " + db_cnt, Toast.LENGTH_SHORT).show();
    }
}