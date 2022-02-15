package com.sstechcanada.todo.utils

import android.content.Context
import android.widget.Toast

object ViewUtils {

    fun showToast(context: Context, text: String) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }

}