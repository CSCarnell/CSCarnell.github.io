/**
 * Christopher Carnell
 * CS-360
 *
 * This utility class manages user preferences using SharedPreferences, maybe move this to the database later
 * It handles storing and retrieving whether a user has opted in for SMS notifications.
 * It provides methods to set and get the SMS opt-in status for a user.
 */


package com.cs360.weightwatcher;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceUtils {

    private static final String PREFS_NAME = "WeightWatcherPrefs";
    private static final String KEY_SMS_OPT_IN_PREFIX = "sms_opt_in_user_";
    private static final String KEY_SMS_SETUP_COMPLETED_PREFIX = "sms_setup_completed_user_";


    public static void setSmsOptIn(Context context, long userId, boolean optIn) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit()
                .putBoolean(KEY_SMS_OPT_IN_PREFIX + userId, optIn)
                .apply();
    }

    public static boolean isSmsOptIn(Context context, long userId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_SMS_OPT_IN_PREFIX + userId, false);
    }

    public static void setSmsSetupCompleted(Context context, long userId, boolean completed) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit()
                .putBoolean(KEY_SMS_SETUP_COMPLETED_PREFIX + userId, completed)
                .apply();
    }

    public static boolean isSmsSetupCompleted(Context context, long userId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_SMS_SETUP_COMPLETED_PREFIX + userId, false);
    }

}
