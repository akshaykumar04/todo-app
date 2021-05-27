package com.sstechcanada.todo.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sstechcanada.todo.R
import com.sstechcanada.todo.activities.auth.LoginActivity
import com.sstechcanada.todo.utils.SaveSharedPreference
import es.dmoral.toasty.Toasty

class SplashActivity : AppCompatActivity() {
    private var TIME_OUT: Long = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Handler().postDelayed({
            checkUserState()
        }, TIME_OUT)

    }

    private fun checkUserState() {
        if (SaveSharedPreference.getUserLogin(this).equals("true")) {
            startActivity(Intent(this, MasterTodoListActivity::class.java))
            finish()
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            Toasty.info(this, getString(R.string.please_login), Toast.LENGTH_LONG, true).show()

            finish()
        }
    }
}