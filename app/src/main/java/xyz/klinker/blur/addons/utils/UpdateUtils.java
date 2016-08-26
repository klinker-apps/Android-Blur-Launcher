package xyz.klinker.blur.addons.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import xyz.klinker.blur.R;

public class UpdateUtils {
    public static void checkUpdate(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        if (sharedPreferences.getBoolean("version_3_0_8", true)) {
            sharedPreferences.edit().putBoolean("version_3_0_8", false)
                    .putString("launcher_package_name_4", "xyz.klinker.blur").putString("launcher_class_path_4", ".extra_pages.weather_page.LauncherFragment").putString("launcher_title_4", context.getString(R.string.weather_page))
                    .putString("launcher_package_name_3", "xyz.klinker.blur").putString("launcher_class_path_3", ".extra_pages.calendar_page.LauncherFragment").putString("launcher_title_3", context.getString(R.string.calendar_page))
                    .putString("launcher_package_name_2", "xyz.klinker.blur").putString("launcher_class_path_2", ".extra_pages.calc_page.LauncherFragment").putString("launcher_title_2", context.getString(R.string.calculator_page))
                    .remove("launcher_package_name_1").remove("launcher_class_path_1").remove("launcher_title_1")
                    .remove("launcher_package_name_0").remove("launcher_class_path_0").remove("launcher_title_0")
                    .commit();
        }
    }
}
