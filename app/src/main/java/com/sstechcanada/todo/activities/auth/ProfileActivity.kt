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
import android.view.Gravity

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color

import android.graphics.drawable.ColorDrawable

import android.view.ViewGroup
import android.view.Window
import android.widget.CheckBox

import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.preference.PreferenceManager
import com.google.android.material.button.MaterialButton
import com.sstechcanada.todo.activities.SplashActivity

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
        cardDelete.setOnClickListener { deleteWarningDialog() }
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
            clearPrefs()
        }
        finishAffinity()
        startActivity(Intent(this, SplashActivity::class.java))
    }

    private fun deleteUserAccount() {
        mAuth = FirebaseAuth.getInstance()
        mAuth?.currentUser?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("TAG", "User account deleted.")
                Toasty.success(this, "Account Deleted", Toast.LENGTH_SHORT).show()
                signOut()
                finishAffinity()
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
                Toasty.error(this, "Server Error, Please try login in again", Toast.LENGTH_SHORT).show()
            }

        }

    }

    private fun openGooglePlayForRating() {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=com.sstechcanada.todo")
                )
            )
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=com.sstechcanada.todo")
                )
            )
        }
    }

    private fun shareApp() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Sorted To-Do List Maker")
            var shareMessage =
                "Check out this list maker. It automatically sorts your list based on the number of benefits you assign to each item:\n\n"
            shareMessage =
                """${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}""".trimIndent()
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "choose one"))
        } catch (e: Exception) {

        }
    }

    private fun deleteWarningDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottomsheet)
        val btnDelete: MaterialButton = dialog.findViewById(R.id.btnDelete)
        val userText: AppCompatTextView = dialog.findViewById(R.id.tvUser)
        val tvUserDes: AppCompatTextView = dialog.findViewById(R.id.tvUserDes)
        val checkBox: CheckBox = dialog.findViewById(R.id.checkDelete)

        userText.text = "Sorry to see you go, ${mAuth?.currentUser?.displayName}"
        tvUserDes.text = "If you're experiencing any issue, please give us the opportunity to help you by sending us your Feedback.\n\nIf you continue, your account will be deleted immediately and all the associated data with your account will be deleted forever.\n\nPost account deletion, you will be able to create a new account. However, your previous data will be inaccessible."

        checkBox.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                btnDelete.isEnabled = true
                btnDelete.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.btnRed))
            } else {
                btnDelete.isEnabled = false
                btnDelete.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.discp_border))
            }
        }

        btnDelete.setOnClickListener {
            if (checkBox.isChecked) {
                deleteUserAccount()
            } else {
                Toasty.info(this, "Please check the checkbox to continue", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)
    }

    private fun clearPrefs() {
        val editor = PreferenceManager.getDefaultSharedPreferences(this)
            editor.edit().clear().commit()
    }

}