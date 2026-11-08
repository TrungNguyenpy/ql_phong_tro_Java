package com.example.manager_room.util;

import android.content.Context;
import android.content.SharedPreferences;

public class LandlordInfo {
    public static final String NAME = "name", CCCD = "cccd", PHONE = "phone",
            ADDRESS = "address", PROPERTY = "property",
            PAY_DAY = "pay_day", ELEC = "elec_rate", WATER = "water_rate";

    private final SharedPreferences pref;

    public LandlordInfo(Context context) {
        pref = context.getSharedPreferences("LandlordInfo", Context.MODE_PRIVATE);
    }

    public String get(String key) {
        return pref.getString(key, "");
    }

    public void put(String key, String value) {
        pref.edit().putString(key, value).apply();
    }
}