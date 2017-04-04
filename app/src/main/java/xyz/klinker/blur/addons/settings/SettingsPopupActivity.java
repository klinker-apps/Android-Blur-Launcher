/*
 * Copyright 2014 Klinker Apps Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.klinker.blur.addons.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.*;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;
import xyz.klinker.blur.R;
import xyz.klinker.blur.addons.utils.IOUtils;
import xyz.klinker.blur.addons.utils.IconPackHelper;
import xyz.klinker.blur.addons.utils.Utils;

import java.io.File;

public class SettingsPopupActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setUpWindow();

        super.onCreate(savedInstanceState);

        context = this;

        setPrefs();
        setXML();
    }

    private void setPrefs() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (Build.VERSION.SDK_INT < 18 ||
                !Utils.isPackageInstalled(this, "com.klinker.android.blur_unread")) {
            prefs.edit().putBoolean("use_unread", false).commit();
        }

        if (Build.VERSION.SDK_INT < 16) {
            prefs.edit().putBoolean("scroll_wallpaper", false).commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        } catch (Exception e) { }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        } catch (Exception e) { }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        SettingsActivity.prefChanged = true;

        // clear any custom icons when you switch packs
        if (s.equals("icon_pack")) {
            sharedPreferences.edit().remove("custom_icons").commit();
        }
    }

    public void setUpWindow() {
        //requestWindowFeature(Window.FEATURE_ACTION_BAR);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        // Params for the window.
        // You can easily set the alpha and the dim behind the window from here
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.alpha = .85f;    // lower than one makes it more transparent
        params.dimAmount = .6f;  // set it higher if you want to dim behind the window
        getWindow().setAttributes(params);
        getListView().setAlpha(.85f);

        // Gets the display size so that you can set the window to a percent of that
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        // You could also easily used an integer value from the shared preferences to set the percent
        if (height > width) {
            getWindow().setLayout((int) (width * .85), (int) (height * .66));
        } else {
            getWindow().setLayout((int) (width * .5), (int) (height * .8));
        }
    }

    @SuppressWarnings("deprecation")
    public void setXML() {
        switch (getIntent().getIntExtra("page", 1)) {
            case SettingsActivity.PAGE_LAYOUT:
                addPreferencesFromResource(R.xml.layout_settings);
                getActionBar().setTitle(R.string.layout);
                setUpLayout();
                break;
            case SettingsActivity.PAGE_VISUALS:
                addPreferencesFromResource(R.xml.visual_settings);
                getActionBar().setTitle(R.string.visuals);
                setUpVisuals();
                break;
            case SettingsActivity.PAGE_DOCK:
                addPreferencesFromResource(R.xml.dock_settings);
                getActionBar().setTitle(R.string.dock);
                break;
            case SettingsActivity.PAGE_EXPERIMENTAL:
                addPreferencesFromResource(R.xml.advanced_settings);
                getActionBar().setTitle(getString(R.string.experimental_settings));
                setUpExperimental();
                break;
            case SettingsActivity.PAGE_BACKUP:
                addPreferencesFromResource(R.xml.backup_settings);
                getActionBar().setTitle(getString(R.string.backup_and_restore));
                setUpBackup();
                break;
        }
    }

    public void setUpLayout() {
        Preference gestureSettings = findPreference("buttons_and_gestures");
        gestureSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(context, ButtonsGesturesActivity.class));
                return false;
            }
        });

        Preference hiddenApps = findPreference("hidden_apps");
        hiddenApps.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(context, HiddenAppsActivity.class));
                return false;
            }
        });
    }

    public void setUpVisuals() {
        Preference iconPack = findPreference("ui_general_iconpack");
        iconPack.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                IconPackHelper.pickIconPack(context);
                return false;
            }
        });
    }

    public void setUpExperimental() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        Preference runningAbout = findPreference("running_about");
        runningAbout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                builder.setTitle(R.string.about_running);
                builder.setMessage(R.string.keep_running_about);
                builder.show();
                return false;
            }
        });

        /*if (Build.VERSION.SDK_INT >= 18) {
            Preference unreadAbout = findPreference("unread_about");
            unreadAbout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    builder.setTitle(R.string.unread_badges);
                    builder.setMessage(R.string.about_unread_summary);
                    builder.show();
                    return false;
                }
            });

            Preference unreadBugs = findPreference("unread_bugs");
            unreadBugs.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    builder.setTitle(R.string.known_bugs);
                    builder.setMessage(R.string.unread_bugs_summary);
                    builder.show();
                    return false;
                }
            });
        }

        if (Build.VERSION.SDK_INT >= 19) {
            Preference hotword = findPreference("hotword_about");
            hotword.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    builder.setTitle(R.string.ok_google_hotword);
                    builder.setMessage(R.string.hotword_about);
                    builder.show();
                    return false;
                }
            });
        }*/
    }

    public void setUpBackup() {
        Preference backupSettings = findPreference("backup_settings");
        backupSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                File des = new File(Environment.getExternalStorageDirectory() + "/Blur/backup.prefs");

                if (IOUtils.saveSharedPreferencesToFile(des, context)) {
                    Toast.makeText(context, R.string.backup_complete, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, R.string.backup_failed, Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

        Preference restoreSettings = findPreference("restore_settings");
        restoreSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                File des = new File(Environment.getExternalStorageDirectory() + "/Blur/backup.prefs");

                if (IOUtils.loadSharedPreferencesFromFile(des, context)) {
                    Toast.makeText(context, R.string.restore_complete, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, R.string.restore_failed, Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
    }
}