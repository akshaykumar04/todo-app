package com.sstechcanada.todo.data;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefConfig {
    public static final String PREF = "USER DATA";
    public static final String LIST_LIMIT = "LIST_LIMIT";
    public static void saveLimit(Context context, int limit){
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt(LIST_LIMIT, limit);
        editor.apply();
    }

    public static int loadLimit(Context context){
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        return sharedpreferences.getInt(LIST_LIMIT,0);
    }
}
