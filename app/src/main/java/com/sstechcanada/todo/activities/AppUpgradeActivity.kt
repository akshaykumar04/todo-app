package com.sstechcanada.todo.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.savvyapps.togglebuttonlayout.Toggle
import com.savvyapps.togglebuttonlayout.ToggleButtonLayout
import com.sstechcanada.todo.R
import kotlinx.android.synthetic.main.activity_app_upgrade.*


class AppUpgradeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_upgrade)

        fabBack.setOnClickListener { super.onBackPressed() }
        setupPriceToggle()

    }

    private fun setupPriceToggle() {
        toggle_button_layout.setToggled(R.id.toggle_right, true)
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