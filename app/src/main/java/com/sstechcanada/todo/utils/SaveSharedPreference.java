package com.sstechcanada.todo.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class SaveSharedPreference {

    static final String PREF_USER_STATE = "user_state";
    static final String PREF_USER_NAME = "username";
    static final String PREF_USER_TYPE = "user_type";
    public static final String PREF = "USER DATA";
    public static final String LIST_LIMIT = "LIST_LIMIT";
    static final String PREF_ADS_ENABLED = "isAdsEnabled";
    static final String PREF_IS_LOGGED_IN = "isLoggedIn";

    static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void setUserName(Context ctx, String userName) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER_NAME, userName);
        editor.apply();
    }

    public static String getUserName(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_USER_NAME, "");
    }

    public static void setUserLogIn(Context ctx, Boolean isLoggedIn) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putBoolean(PREF_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    public static Boolean getUserLogin(Context ctx) {
        return getSharedPreferences(ctx).getBoolean(PREF_IS_LOGGED_IN, false);
    }

    public static void saveLimit(Context context, int limit) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt(LIST_LIMIT, limit);
        editor.apply();
    }

    public static int loadLimit(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        return sharedpreferences.getInt(LIST_LIMIT, 0);
    }

    public static void setAdsEnabled(Context ctx, Boolean isAdsEnabled) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putBoolean(PREF_ADS_ENABLED, isAdsEnabled);
        editor.apply();
    }

    public static Boolean getAdsEnabled(Context ctx) {
        return getSharedPreferences(ctx).getBoolean(PREF_ADS_ENABLED, true);
    }

}
