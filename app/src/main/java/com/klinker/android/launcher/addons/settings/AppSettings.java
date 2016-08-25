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

package com.klinker.android.launcher.addons.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.klinker.android.launcher.R;
import com.klinker.android.launcher.addons.utils.Utils;

public class AppSettings {

    private static AppSettings settings;
    public static AppSettings getInstance(Context context) {
        if (settings != null) {
            return settings;
        } else {
            AppSettings.settings = new AppSettings(context);
            return AppSettings.settings;
        }
    }
    public static AppSettings assumeNotNull() {
        return settings;
    }
    public static void invalidate() {
        AppSettings.settings = null;
    }
    public static void invalidate(Context context) {
        AppSettings.settings = new AppSettings(context);
    }

    public static final int WHITE_UNREAD_BACK = 0;
    public static final int BLACK_UNREAD_BACK = 1;

    public static final int NO_EXTRA_PAGE = 0;
    public static final int BLUR_INFO = 1;
    public static final int VERTICAL_DRAWER = 2;

    public boolean showDock;
    public boolean showPageIndicators;
    public boolean showSearchBar;
    public boolean showIconNames;
    public boolean shouldPersist;
    public boolean useUnread;
    public boolean showPredictedApps;

    public int colCount;
    public int rowCount;
    public int colCountAllApps;
    public int widthMargin;
    public int heightMargin;
    public int dockItems;
    public int unreadBack;
    public int extraPage;

    public float iconScale;
    public float dockScale;

    public String iconPack;

    public AppSettings(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        this.showDock = sharedPrefs.getBoolean("show_dock", true);
        this.showPageIndicators = sharedPrefs.getBoolean("show_page_indicator", true);
        this.showPredictedApps = sharedPrefs.getBoolean("show_predicted_apps", true);
        this.showSearchBar = sharedPrefs.getBoolean("show_search_bar", true);
        this.showIconNames = sharedPrefs.getBoolean("show_icon_names", true);
        this.shouldPersist = sharedPrefs.getBoolean("keep_running", false);

        // need to know if it is selected as well as them having unread app installed
        this.useUnread = sharedPrefs.getBoolean("use_unread", false) &&
                Utils.isPackageInstalled(context, "com.klinker.android.blur_unread");

        this.colCount = Integer.parseInt(sharedPrefs.getString("col_count", context.getResources().getInteger(R.integer.default_col_count) + ""));
        this.rowCount = Integer.parseInt(sharedPrefs.getString("row_count", context.getResources().getInteger(R.integer.default_row_count) + ""));
        this.colCountAllApps = Integer.parseInt(sharedPrefs.getString("col_count_all_apps", context.getResources().getInteger(R.integer.default_col_count) + ""));
        this.widthMargin = sharedPrefs.getInt("width_margin", 0);
        this.heightMargin = sharedPrefs.getInt("height_margin", 0);
        this.dockItems = Integer.parseInt(sharedPrefs.getString("dock_count", context.getResources().getInteger(R.integer.default_dock_items) + ""));
        this.unreadBack = Integer.parseInt(sharedPrefs.getString("unread_back", "1"));
        this.extraPage = sharedPrefs.getInt("extra_page", NO_EXTRA_PAGE);

        this.iconScale = Float.parseFloat(sharedPrefs.getString("icon_scale", "1.0"));
        this.dockScale = Float.parseFloat(sharedPrefs.getString("dock_icon_scale", "1.0"));

        this.iconPack = sharedPrefs.getString("icon_pack", "");

        setUpGestures(sharedPrefs);
    }

    public static final String[] gestureTitles = {
            "home_button_action",
            "back_button_action",
            "swipe_up_action",
            "swipe_down_action",
            "double_tap_action"
    };

    public static final int HOME_BUTTON = 0;
    public static final int BACK_BUTTON = 1;
    public static final int SWIPE_UP = 2;
    public static final int SWIPE_DOWN = 3;
    public static final int DOUBLE_TAP = 4;

    public static final int NOTHING = 0;
    public static final int OPEN_PAGES = 1;
    public static final int OPEN_ALL_APPS = 2;
    public static final int OPEN_NOTIFICATIONS = 3;
    public static final int SLEEP_DEVICE = 4;
    public static final int OPEN_RECENT_APPS = 5;

    // this will be used to store the actions for the gestures or buttons
    public int[] gestureActions = new int[gestureTitles.length];

    public void setUpGestures(SharedPreferences sp) {
        // with blur 2, we removed the extra page, so this removes it from the gestures
        // if they had that gesture, it will set it to NOTHING
        if (sp.getBoolean("blur2", true)) {
            for (int i = 0; i < gestureActions.length; i++) {
                int val = Integer.parseInt(sp.getString(gestureTitles[i], (i + 1) + ""));
                if (val > 1) {
                    sp.edit().putString(gestureTitles[i], (val - 1) + "").commit();
                } else if (val == 1) {
                    sp.edit().putString(gestureTitles[i], NOTHING + "").commit();
                }
            }

            sp.edit().putBoolean("blur2", false).commit();
        }

        for (int i = 0; i < gestureActions.length; i++) {
            String key = gestureTitles[i];

            // plus one because the default is to have nothing at index 0
            gestureActions[i] = Integer.parseInt(sp.getString(key, (i + 1) + ""));
        }
    }
}
