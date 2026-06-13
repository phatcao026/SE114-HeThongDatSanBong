package com.example.timsanbong.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

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

    public String getUserRole() {
        String json = getUserJson();
        if (json == null) return "PLAYER";
        try {
            JSONObject obj = new JSONObject(json);
            return obj.optString("role", "PLAYER");
        } catch (JSONException e) {
            return "PLAYER";
        }
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(getUserRole());
    }

    public boolean isOwner() {
        return "OWNER".equalsIgnoreCase(getUserRole());
    }
}
