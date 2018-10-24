package com.aconst.spinareg;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsHelper {
    SharedPreferences sp;

    public PrefsHelper(Context context) {
        this.sp = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
    }

    public String getPref(String prefName) {
        return sp.getString(prefName, "");
    }

    public int getPref(String prefName, int defValue) {
        return sp.getInt(prefName, defValue);
    }

    public long getPrefLong(String prefName, long defValue) {
        return sp.getLong(prefName, defValue);
    }

    public float getPref(String prefName, float defValue) {
        return sp.getFloat(prefName, defValue);
    }

    public boolean getPref(String prefName, boolean defValue) {
        return sp.getBoolean(prefName, defValue);
    }

    public void setPref(String prefName, String value) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(prefName, value);
        editor.apply();
    }

    public void setPref(String prefName, int value) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(prefName, value);
        editor.apply();
    }

    public void setPref(String prefName, boolean value) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(prefName, value);
        editor.apply();
    }

    public void setPref(String prefName, float value) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putFloat(prefName, value);
        editor.apply();
    }

    public void setPrefLong(String prefName, long value) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(prefName, value);
        editor.apply();
    }
}
