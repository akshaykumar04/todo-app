package com.sstechcanada.todo.activities.auth;

import android.app.ProgressDialog;
import android.content.Intent;
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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sstechcanada.todo.R;
import com.sstechcanada.todo.models.Users;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 9001;
    SignInButton googleSignInButton;
    private FirebaseAuth mAuth;
    private ProgressDialog pDialog;
    private GoogleSignInClient mGoogleSignInClient;
    private ProgressBar progressBar;
    private Button signOutButton;
    CardView profileCard;
    ImageView placeHolder, dp;
    TextView userName, userType, userEmail;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    int list_limit;

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

        googleSignInButton.setOnClickListener(v -> signIn());
        signOutButton.setOnClickListener(v -> signOut());

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
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
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
                            hideProgressDialog();
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
//                            databaseReference.child("Users").child(firebaseUser.getUid()).child("Email").setValue(firebaseUser.getEmail());
                            databaseReference.child("Users").child(firebaseUser.getUid()).child("Email").setValue(firebaseUser.getEmail());
                            updateUserPackage(firebaseUser);
                            checkUserStatus();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Login Failed: ", Toast.LENGTH_SHORT).show();
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
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
        checkUserStatus();
    }

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
    private void updateUserPackage(FirebaseUser firebaseUser){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.child("purchase_code").exists()){
                    databaseReference.child("purchase_code").setValue("1");
                }else{
                    String ans = snapshot.child("purchase_code").getValue(String.class);
                    list_limit = Integer.parseInt(ans);
                    if(list_limit != 1){
                        userType.setText("Premium User");
                    }else{
                        userType.setText("Free User");
                    }
                    Toast.makeText(LoginActivity.this, ""+list_limit, Toast.LENGTH_SHORT).show();
                }
                if(!snapshot.child("purchase_type").exists()){
                    databaseReference.child("purchase_type").setValue("Free User");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}