package com.sstechcanada.todo.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.make
import com.savvyapps.togglebuttonlayout.Toggle
import com.savvyapps.togglebuttonlayout.ToggleButtonLayout
import com.sstechcanada.todo.R
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_app_upgrade.*


class AppUpgradeActivity : AppCompatActivity() {

    val premium_app = "com.sstechcanada.todo_plus"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_upgrade)

        buttonUpgrade.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$premium_app")))
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$premium_app")))
            }
        }

        fabBack.setOnClickListener { super.onBackPressed() }
        setupPriceToggle()

    }

    private fun setupPriceToggle() {
        toggle_button_layout.setToggled(R.id.toggle_left, true)
        toggle_button_layout.onToggledListener = { toggleButton: ToggleButtonLayout, toggle: Toggle, b: Boolean ->

            when (toggle.id) {
                R.id.toggle_left -> {
                    tvListsCount.text = getString(R.string.create_up_to_3_to_do_lists)
                }
                R.id.toggle_right -> {
                    tvListsCount.text = getString(R.string.create_up_to_20_to_do_lists)
                }
            }
        }

    }

}