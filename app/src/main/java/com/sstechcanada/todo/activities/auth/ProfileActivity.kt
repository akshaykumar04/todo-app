package com.sstechcanada.todo.activities.auth

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.sstechcanada.todo.R
import com.sstechcanada.todo.activities.MasterTodoListActivity
import com.sstechcanada.todo.utils.SaveSharedPreference
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var mGoogleSignInClient: GoogleSignInClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        setupFirebaseLogin()
        initViews()
        initOnClicks()
        setupAds()

    }


    private fun initViews() {
        userEmail.text = mAuth?.currentUser?.email
        userName.text = mAuth?.currentUser?.displayName
        Glide.with(this).load(mAuth?.currentUser?.photoUrl).into(roundedImage)
        if (MasterTodoListActivity.purchaseCode == "0") {
            userType?.setText(R.string.free_user)
        } else {
            userType?.setText(R.string.premium_user)
        }
    }

    private fun initOnClicks() {
        fabBack.setOnClickListener { onBackPressed() }
        cardSignOut.setOnClickListener { showSignOutDialog() }
    }

    private fun setupFirebaseLogin() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        mAuth = FirebaseAuth.getInstance()
    }

    private fun setupAds() {
        if (MasterTodoListActivity.purchaseCode == "0") {
            adView.loadAd(AdRequest.Builder().build())
            adView.visibility = View.VISIBLE
        } else {
            adView.visibility = View.GONE
        }

    }

    private fun showSignOutDialog() {
        val alert = AlertDialog.Builder(this)
        alert.setTitle(R.string.sign_out)
        alert.setMessage(R.string.are_you_sure)
        alert.setPositiveButton(
            R.string.yes
        ) { _: DialogInterface?, _: Int -> signOut() }
        alert.setNegativeButton(
            R.string.no
        ) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
        alert.show()
    }

    private fun signOut() {
        mAuth?.signOut()
        mGoogleSignInClient?.signOut()?.addOnCompleteListener(
            this
        ) {
            SaveSharedPreference.saveLimit(applicationContext, 0)
        }
        SaveSharedPreference.setUserLogIn(this, "false")
        finishAffinity()
        startActivity(Intent(this, LoginActivity::class.java))
    }

}