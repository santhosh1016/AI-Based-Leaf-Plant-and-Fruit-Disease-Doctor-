package com.app.diseaseapp;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import java.util.Locale;

public class LanguageManager {

    private static final String PREFS_NAME = "language_prefs";
    private static final String KEY_LANGUAGE = "key_language";
    private static final String DEFAULT_LANGUAGE = "en";

    public static void applyAppLocale(Context context) {
        String language = getSavedLanguage(context);
        setLocale(context, language, false);
    }

    public static void setLocale(Context context, String language, boolean persist) {
        LocaleListCompat localeList = LocaleListCompat.create(new Locale(language));
        AppCompatDelegate.setApplicationLocales(localeList);

        if (persist) {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(KEY_LANGUAGE, language).apply();
        }
    }

    public static String getSavedLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE);
    }
}
