package com.klinker.android.launcher.addons.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class UpdateUtils {
    public static void checkUpdate(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        if (sharedPreferences.getBoolean("version_3_0_1", true)) {
            sharedPreferences.edit().putBoolean("version_3_0_1", false)
                    .remove("launcher_package_name_1").remove("launcher_class_path_1").remove("launcher_title_1")
                    .remove("launcher_package_name_2").remove("launcher_class_path_2").remove("launcher_title_2")
                    .remove("launcher_package_name_3").remove("launcher_class_path_3").remove("launcher_title_3")
                    .remove("launcher_package_name_4").remove("launcher_class_path_4").remove("launcher_title_4")
                    .remove("launcher_package_name_5").remove("launcher_class_path_5").remove("launcher_title_5")
                    .commit();
        }
    }
}
