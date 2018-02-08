package com.github.genderquery.usmbalance.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

public class Preferences {

  private static final String USERNAME = "username";
  private static final String PASSWORD = "password";
  private static final String LINE = "lineId";
  private static final String SERVICE_ENDS_DATE = "serviceEndDate";
  private static final String TALK_REMAINING = "talkRemaining";
  private static final String TALK_PERCENT = "talkPercent";
  private static final String TEXT_REMAINING = "textRemaining";
  private static final String TEXT_PERCENT = "textPercent";
  private static final String DATA_REMAINING = "dataRemaining";
  private static final String DATA_PERCENT = "dataPercent";
  private static final String LAST_UPDATED = "lastUpdated";
  private static final String SYNC_FREQUENCY = "syncFrequency";
  private static final String SYNC_UNMETERED_ONLY = "syncUnmeteredOnly";

  public static void clear(@NonNull Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    preferences.edit()
        .clear()
        .apply();
  }

  public static boolean isLoginRequired(@NonNull Context context) {
    return getPassword(context).isEmpty();
  }

  public static String getUsername(@NonNull Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getString(USERNAME, "");
  }

  public static String getPassword(@NonNull Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getString(PASSWORD, "");
  }

  public static void setCredentials(@NonNull Context context, String username, String password) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    preferences.edit()
        .putString(USERNAME, username)
        .putString(PASSWORD, password)
        .apply();
  }

  public static String getLineId(@NonNull Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getString(LINE, "");
  }

  public static void setLineId(@NonNull Context context, String lineId) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    preferences.edit()
        .putString(LINE, lineId)
        .apply();
  }

  public static UsmBalance getBalance(@NonNull Context context) {
    UsmBalance balance = new UsmBalance();
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    balance.serviceEndDate = preferences.getLong(SERVICE_ENDS_DATE, 0);
    balance.talkRemaining = preferences.getInt(TALK_REMAINING, 0);
    balance.talkPercent = preferences.getInt(TALK_PERCENT, 0);
    balance.textRemaining = preferences.getInt(TEXT_REMAINING, 0);
    balance.textPercent = preferences.getInt(TEXT_PERCENT, 0);
    balance.dataRemaining = preferences.getInt(DATA_REMAINING, 0);
    balance.dataPercent = preferences.getInt(DATA_PERCENT, 0);
    return balance;
  }

  public static void setBalance(@NonNull Context context, @NonNull UsmBalance balance) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    preferences.edit()
        .putLong(SERVICE_ENDS_DATE, balance.serviceEndDate)
        .putInt(TALK_REMAINING, balance.talkRemaining)
        .putInt(TALK_PERCENT, balance.talkPercent)
        .putInt(TEXT_REMAINING, balance.textRemaining)
        .putInt(TEXT_PERCENT, balance.textPercent)
        .putInt(DATA_REMAINING, balance.dataRemaining)
        .putInt(DATA_PERCENT, balance.dataPercent)
        .putLong(LAST_UPDATED, System.currentTimeMillis())
        .apply();
  }

  public static long getLastUpdated(@NonNull Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getLong(LAST_UPDATED, 0);
  }

  public static int getSyncFrequency(@NonNull Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return Integer.parseInt(preferences.getString(SYNC_FREQUENCY, "180"));
  }

  public static boolean getSyncUnmeteredOnly(@NonNull Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getBoolean(SYNC_UNMETERED_ONLY, true);
  }
}
