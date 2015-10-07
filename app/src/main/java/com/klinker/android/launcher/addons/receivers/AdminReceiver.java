package com.klinker.android.launcher.addons.receivers;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Receiver class which shows notifications when the Device Administrator status
 * of the application changes.
 */
public class AdminReceiver extends DeviceAdminReceiver {

    @Override
    public void onEnabled(Context context, Intent intent) {

    }

    @Override
    public void onDisabled(Context context, Intent intent) {

    }

}
