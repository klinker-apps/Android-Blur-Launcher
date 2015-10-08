package com.klinker.android.launcher.addons.settings;

import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import com.klinker.android.launcher.R;

public class ButtonsGesturesActivity extends SettingsPopupActivity {


    @Override
    public void setXML() {
        addPreferencesFromResource(R.xml.gesture_settings);

        setUpSummaries();
    }

    public void setUpSummaries() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        for (String s : AppSettings.gestureTitles) {
            Preference p = findPreference(s);
            int value = getValue(sp, s);
            switch (value) {
                case AppSettings.NOTHING:
                    p.setSummary(getString(R.string.no_action));
                    break;
                case AppSettings.OPEN_PAGES:
                    p.setSummary(getString(R.string.open_pages));
                    break;
                case AppSettings.OPEN_ALL_APPS:
                    p.setSummary(getString(R.string.open_all_apps));
                    break;
                case AppSettings.OPEN_NOTIFICATIONS:
                    p.setSummary(getString(R.string.open_notifications));
                    break;
                case AppSettings.SLEEP_DEVICE:
                    p.setSummary(getString(R.string.sleep_device));
                    break;
                case AppSettings.OPEN_RECENT_APPS:
                    p.setSummary(R.string.open_recents);
                    break;
            }
        }
    }

    public int getValue(SharedPreferences sp, String key) {
        for (int i = 0; i < AppSettings.gestureTitles.length; i++) {
            if (AppSettings.gestureTitles[i].equals(key)) {
                return Integer.parseInt(sp.getString(key, (i + 1) + ""));
            }
        }

        return Integer.parseInt(sp.getString(key, "1"));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        SettingsActivity.prefChanged = true;
        AppSettings.invalidate();

        // invalidate the summaries to notify the user
        setUpSummaries();
    }
}
