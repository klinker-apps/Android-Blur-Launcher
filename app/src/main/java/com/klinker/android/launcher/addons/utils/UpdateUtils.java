package com.klinker.android.launcher.addons.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.klinker.android.launcher.R;

public class UpdateUtils {
    public static void checkUpdate(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        if (sharedPreferences.getBoolean("version_3", true)) {
            sharedPreferences.edit().putBoolean("version_3", false)
                    .putString("launcher_package_name_4", "com.klinker.android.launcher").putString("launcher_class_path_4", ".weather_page.LauncherFragment").putString("launcher_title_4", context.getString(R.string.weather_page))
                    .putString("launcher_package_name_3", "com.klinker.android.launcher").putString("launcher_class_path_3", ".calendar_page.LauncherFragment").putString("launcher_title_3", context.getString(R.string.calendar_page))
                    .putString("launcher_package_name_2", "com.klinker.android.launcher").putString("launcher_class_path_2", ".calc_page.LauncherFragment").putString("launcher_title_2", context.getString(R.string.calculator_page))
                    .remove("launcher_package_name_1").remove("launcher_class_path_1").remove("launcher_title_1")
                    .remove("launcher_package_name_0").remove("launcher_class_path_0").remove("launcher_title_0")
                    .commit();
        }
    }
}
