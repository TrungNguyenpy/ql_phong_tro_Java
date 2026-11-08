package com.example.manager_room.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_ROLE = "role";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    // Lưu thông tin khi đăng nhập thành công
    public void saveSession(long userId, String role) {
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_ROLE, role);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public long getUserId() {
        return pref.getLong(KEY_USER_ID, -1);
    }

    public String getRole() {
        return pref.getString(KEY_ROLE, "");
    }

    // Xóa phiên khi người dùng chọn Đăng xuất
    public void clearSession() {
        editor.clear();
        editor.apply();
    }

    private static final String KEY_REMEMBER = "remember";

    public void setRemember(boolean remember) {
        editor.putBoolean(KEY_REMEMBER, remember);
        editor.apply();
    }

    public boolean isRemember() {
        return pref.getBoolean(KEY_REMEMBER, false);
    }
}