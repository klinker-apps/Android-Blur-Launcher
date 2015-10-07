package com.klinker.android.launcher.addons.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.text.TextUtils;


public class IconChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String packageName = intent.getStringExtra("package_name");

        if (!TextUtils.isEmpty(packageName)) {
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putString("icon_pack", packageName)
                    .putBoolean("icons_changed", true)
                    .commit();
        }
    }
}
