package com.erank.koletsionpods.utils.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SharedPrefsHelper {
    private static SharedPrefsHelper instance;
    private final SharedPreferences prefs;

    private SharedPrefsHelper(Context context) {
        this.prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
    }

    public static SharedPrefsHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefsHelper(context);
        }
        return instance;
    }

    public <T> void save(Keys key, T value) {
        SharedPreferences.Editor editor = prefs.edit();

        String k = key.value;
        if (value instanceof Boolean) {
            editor.putBoolean(k, (Boolean) value);
        } else if (value instanceof Set) {
            editor.putStringSet(k, (Set<String>) value);
        } else if (value instanceof String) {
            editor.putString(k, (String) value);
        } else if (value instanceof Float) {
            editor.putFloat(k, (Float) value);
        } else if (value instanceof Long) {
            editor.putLong(k, (Long) value);
        } else if (value instanceof Integer) {
            editor.putInt(k, (Integer) value);
        }

        editor.apply();
    }


    public <T> T get(Keys key, T defaultValue) {

        String k = key.value;

        if (defaultValue instanceof Boolean) {
            Boolean ret = prefs.getBoolean(k, (Boolean) defaultValue);
            return (T) ret;
        } else if (defaultValue instanceof Collection) {
            Set<String> result = prefs.getStringSet(k, new HashSet<>());
            return (T) result;
        } else if (defaultValue instanceof String) {
            String ret = prefs.getString(k, (String) defaultValue);
            return (T) ret;
        } else if (defaultValue instanceof Float) {
            Float result = prefs.getFloat(k, (Float) defaultValue);
            return (T) result;
        } else if (defaultValue instanceof Long) {
            Long result = prefs.getLong(k, (Long) defaultValue);
            return (T) result;
        } else if (defaultValue instanceof Integer) {
            Integer result = prefs.getInt(k, (Integer) defaultValue);
            return (T) result;
        }

        return null;
    }

    public enum Keys {
        USER_NAME("username");

        private String value;

        Keys(String value) {
            this.value = value;
        }
    }
}
