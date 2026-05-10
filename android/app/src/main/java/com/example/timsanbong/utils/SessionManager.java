package com.example.timsanbong.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        this.prefs = context.getSharedPreferences(Constants.PREFS_AUTH, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        prefs.edit().putString(Constants.KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return prefs.getString(Constants.KEY_TOKEN, null);
    }

    public void saveUserJson(String userJson) {
        prefs.edit().putString(Constants.KEY_USER_JSON, userJson).apply();
    }

    public String getUserJson() {
        return prefs.getString(Constants.KEY_USER_JSON, null);
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }
}
