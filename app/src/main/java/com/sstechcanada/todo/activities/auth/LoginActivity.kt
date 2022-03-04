package com.sstechcanada.todo.activities.auth

import com.google.android.gms.common.SignInButton
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.app.ProgressDialog
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseUser
import android.os.Bundle
import com.sstechcanada.todo.R
import com.sstechcanada.todo.activities.MasterTodoListActivity
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import android.content.Intent
import android.util.Log
import android.widget.*
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider

import com.sstechcanada.todo.utils.SaveSharedPreference
import es.dmoral.toasty.Toasty
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.FieldPath
import kotlinx.android.synthetic.main.activity_login.*
import java.lang.Exception
import java.util.ArrayList
import java.util.HashMap

class LoginActivity : AppCompatActivity() {

    var firebaseDatabase: FirebaseDatabase? = null
    var databaseReference: DatabaseReference? = null

    private var mAuth: FirebaseAuth? = null
    var db = FirebaseFirestore.getInstance()
    private var pDialog: ProgressDialog? = null
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var user: FirebaseUser? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //        setUpSharedPref();
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase?.reference
        pDialog = ProgressDialog(this)

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this@LoginActivity, gso)
        mAuth = FirebaseAuth.getInstance()
        user = mAuth?.currentUser
        sign_in_button.setSize(SignInButton.SIZE_WIDE)
        sign_in_button.setOnClickListener { signIn() }
    }


    /**
     * Display Progress bar while Logging in through Google
     */
    private fun displayProgressDialog() {
        pDialog?.setMessage("Logging In.. Please wait...")
        pDialog?.isIndeterminate = false
        pDialog?.setCancelable(false)
        pDialog?.show()
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        user = mAuth?.currentUser
        hideProgressDialog()
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient?.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                // ...
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount?) {
        displayProgressDialog()
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct?.id)
        val credential = GoogleAuthProvider.getCredential(acct?.idToken, null)
        mAuth?.signInWithCredential(credential)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    SaveSharedPreference.setUserLogIn(this@LoginActivity, true)
                    //                            startActivity(new Intent(LoginActivity.this, TodoListActivity2.class));
                    Toasty.success(applicationContext, "Sign in complete", Toast.LENGTH_SHORT)
                        .show()
                    hideProgressDialog()
                    //                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
//                            databaseReference.child("Users").child(firebaseUser.getUid()).child("Email").setValue(firebaseUser.getEmail());
//                            databaseReference.child(firebaseUser.getUid()).child("Email").setValue(firebaseUser.getEmail());
                    updateUserPackage()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    //                            Toasty.error(getApplicationContext(), "Login Failed: ", Toast.LENGTH_LONG).show();
                }
                hideProgressDialog()
            }
    }

    private fun hideProgressDialog() {
        pDialog?.dismiss()
    }

    private fun updateUserPackage() {
        val firebaseUser = mAuth?.currentUser
        val documentReferenceCurrentReference = firebaseUser?.uid?.let {
            db.collection("Users").document(it)
        }
        db.collection("Users").whereEqualTo(FieldPath.documentId(), firebaseUser?.uid).get()
            .addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
                if (queryDocumentSnapshots.size() == 0) {
                    val profile: MutableMap<String, String?> = HashMap()
                    profile["Email"] = firebaseUser?.email
                    profile["purchase_code"] = "0"
                    documentReferenceCurrentReference?.set(profile)
                        ?.addOnSuccessListener {
                            Log.d("Usercreation", "Usercreation:success")
                            Toasty.success(
                                applicationContext,
                                "Profile creation complete",
                                Toast.LENGTH_SHORT
                            ).show()
                            startActivity(
                                Intent(
                                    this@LoginActivity,
                                    MasterTodoListActivity::class.java
                                )
                            )
                        }?.addOnFailureListener {
                            Log.d("Usercreation", "Usercreation:success")
                            Toasty.error(
                                applicationContext,
                                "Error in profile creation",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    Log.d("Usercreation", "Usercreation:already complete")
                    startActivity(Intent(this@LoginActivity, MasterTodoListActivity::class.java))
                }
            }.addOnFailureListener { e: Exception? -> }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (user != null) {
            startActivity(Intent(this@LoginActivity, MasterTodoListActivity::class.java))
        }
    }

    companion object {
        const val SHAREDPREF = "UserSharedPrefFile"
        private const val TAG = "MainActivity"
        private const val RC_SIGN_IN = 9001

        @JvmField
        var userAccountDetails = ArrayList<String>()
    }
}