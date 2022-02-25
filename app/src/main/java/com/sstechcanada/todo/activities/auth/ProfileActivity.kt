package com.sstechcanada.todo.activities.auth

import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.sstechcanada.todo.BuildConfig
import com.sstechcanada.todo.R
import com.sstechcanada.todo.activities.AboutActivity
import com.sstechcanada.todo.activities.MasterTodoListActivity
import com.sstechcanada.todo.utils.SaveSharedPreference
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_profile.*
import java.lang.Exception

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
        cardAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
        cardShare.setOnClickListener { shareApp() }
        cardRate.setOnClickListener { openGooglePlayForRating() }
        cardDelete.setOnClickListener { showDeleteWarning() }
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

    private fun showDeleteWarning() {
        val alert = AlertDialog.Builder(this)
        alert.setTitle(R.string.delete_account)
        alert.setMessage(R.string.delete_account_desc)
        alert.setPositiveButton(
            R.string.yes
        ) { _: DialogInterface?, _: Int -> deleteUserAccount() }
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

    private fun deleteUserAccount() {
        mAuth?.currentUser?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("TAG", "User account deleted.")
                Toasty.success(this, "Account Deleted", Toast.LENGTH_SHORT).show()
                SaveSharedPreference.saveLimit(applicationContext, 0)
                SaveSharedPreference.setUserLogIn(this, "false")
                finishAffinity()
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
                Toasty.error(this, "Technical Error", Toast.LENGTH_SHORT).show()
            }

        }

    }

    private fun openGooglePlayForRating() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.sstechcanada.todo")))
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.sstechcanada.todo")))
        }
    }

    private fun shareApp() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Sorted To-Do List Maker")
            var shareMessage = "Let me recommend you this application.......\n\nJon Please Help me with this content\n\n"
            shareMessage =
                """${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}""".trimIndent()
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "choose one"))
        } catch (e: Exception) {

        }
    }



}