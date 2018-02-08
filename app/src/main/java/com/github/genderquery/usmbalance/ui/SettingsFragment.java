package com.github.genderquery.usmbalance.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.TwoStatePreference;
import android.support.annotation.Nullable;
import com.github.genderquery.usmbalance.R;
import com.github.genderquery.usmbalance.data.Preferences;
import com.github.genderquery.usmbalance.sync.SyncJobService;

public class SettingsFragment extends PreferenceFragment implements
    OnSharedPreferenceChangeListener {

  /**
   * Indicates if the sync job needs to be rescheduled due to changes in the sync settings.
   */
  private boolean isJobRescheduleRequired = false;

  public SettingsFragment() {
    // Required empty public constructor
  }

  /**
   * Update the summary for the specified preference. For {@link ListPreference}, the summary will
   * be set to the respective entry value. For {@link TwoStatePreference}, and it's subclasses, no
   * summary will be set. For all other Preferences, the summary will be the string value.
   *
   * @param preference the preference whose summary will be updated
   * @param newValue the value of the preference
   * @see ListPreference#setEntries(CharSequence[])
   */
  private static void updateSummary(Preference preference, Object newValue) {
    String stringValue = newValue.toString();
    if (preference instanceof ListPreference) {
      ListPreference listPreference = (ListPreference) preference;
      int indexOfValue = listPreference.findIndexOfValue(stringValue);
      if (indexOfValue >= 0) {
        CharSequence entry = listPreference.getEntries()[indexOfValue];
        preference.setSummary(entry);
      }
    } else if (!(preference instanceof TwoStatePreference)) {
      preference.setSummary(stringValue);
    }
  }

  /**
   * Recursively update the summary for all of the preferences in preferenceGroup.
   *
   * @param sharedPreferences SharedPreferences instance
   * @param preferenceGroup usually a PreferenceScreen or PreferenceCategory
   */
  private static void initializeSummaries(SharedPreferences sharedPreferences,
      PreferenceGroup preferenceGroup) {
    int preferenceCount = preferenceGroup.getPreferenceCount();
    for (int i = 0; i < preferenceCount; i++) {
      Preference preference = preferenceGroup.getPreference(i);
      if (preference instanceof PreferenceGroup) {
        initializeSummaries(sharedPreferences, (PreferenceGroup) preference);
      } else if (!(preference instanceof TwoStatePreference)) {
        String value = sharedPreferences.getString(preference.getKey(), "");
        updateSummary(preference, value);
      }
    }
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);

    PreferenceScreen preferenceScreen = getPreferenceScreen();
    SharedPreferences sharedPreferences = preferenceScreen.getSharedPreferences();
    initializeSummaries(sharedPreferences, preferenceScreen);
  }

  @Override
  public void onStart() {
    super.onStart();
    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onStop() {
    super.onStop();
    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

    if (isJobRescheduleRequired) {
      isJobRescheduleRequired = false;
      Context context = getActivity();
      if (Preferences.getSyncFrequency(context) == -1) {
        // never sync
        SyncJobService.cancel(context);
      } else {
        SyncJobService.schedule(context);
      }
    }
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    switch (key) {
      case "syncFrequency":
      case "syncUnmeteredOnly":
        isJobRescheduleRequired = true;
        break;
    }

    Preference preference = findPreference(key);
    String value = sharedPreferences.getString(key, "");
    updateSummary(preference, value);
  }
}
