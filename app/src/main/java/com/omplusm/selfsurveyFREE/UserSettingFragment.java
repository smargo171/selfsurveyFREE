package com.omplusm.selfsurveyFREE;

/**
 * Created by s3rg on 25/03/14.
 */
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;


public class UserSettingFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String KEY_PREF_EXP_DIR = "prefExportDir";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        // Get a reference to the application default shared preferences.
        SharedPreferences sp = getPreferenceScreen().getSharedPreferences();

        //get value
        //String Exdir = sp.getString("prefExportDir", "NULL");

        EditTextPreference exportDirPref =
                (EditTextPreference) findPreference(KEY_PREF_EXP_DIR);
        exportDirPref.setSummary(sp.getString(KEY_PREF_EXP_DIR, ""));


    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_PREF_EXP_DIR)) {
            Preference connectionPref = findPreference(key);

            // Set summary to be the user-description for the selected value
            connectionPref.setSummary(sharedPreferences.getString(key, ""));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }
}