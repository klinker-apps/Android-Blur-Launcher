package com.klinker.android.launcher.addons.utils;

import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.widget.Toast;
import com.klinker.android.launcher.R;
import com.klinker.android.launcher.addons.receivers.AdminReceiver;
import com.klinker.android.launcher.addons.settings.AppSettings;
import com.klinker.android.launcher.launcher3.Launcher;

import java.lang.reflect.Method;

/**
 * Created by lucasklinker on 8/28/14.
 */
public class GestureUtils {

    public static void openNotifications(Context context) {
        try {
            Object sbservice = context.getSystemService("statusbar");
            Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
            Method showsb;
            if (Build.VERSION.SDK_INT >= 17) {
                showsb = statusbarManager.getMethod("expandNotificationsPanel");
            } else {
                showsb = statusbarManager.getMethod("expand");
            }
            showsb.invoke(sbservice);
        } catch (Exception e) {
            // this will work for most, but it is something that can be changed by the manufacturer
            // there is no offical sdk method for this.
            Toast.makeText(context, "Sorry! Your device doesn't support this!", Toast.LENGTH_SHORT).show();
        }
    }

    public static void openRecents(Context context) {
        try {
            Class serviceManagerClass = Class.forName("android.os.ServiceManager");
            Method getService = serviceManagerClass.getMethod("getService", String.class);
            IBinder retbinder = (IBinder) getService.invoke(serviceManagerClass, "statusbar");
            Class statusBarClass = Class.forName(retbinder.getInterfaceDescriptor());
            Object statusBarObject = statusBarClass.getClasses()[0].getMethod("asInterface", IBinder.class).invoke(null, new Object[] { retbinder });
            Method clearAll = statusBarClass.getMethod("toggleRecentApps");
            clearAll.setAccessible(true);
            clearAll.invoke(statusBarObject);
        } catch (Exception e) {
            // this will work for most, but it is something that can be changed by the manufacturer
            // there is no offical sdk method for this.
            Toast.makeText(context, "Sorry! Your device doesn't support this!", Toast.LENGTH_SHORT).show();
        }
    }

    public static void putToSleep(final Context context) {
        DevicePolicyManager policyManager = (DevicePolicyManager) context
                .getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName adminReceiver = new ComponentName(context,
                AdminReceiver.class);
        boolean admin = policyManager.isAdminActive(adminReceiver);
        if (admin) {
            policyManager.lockNow();
        } else {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.device_admin)
                    .setMessage(R.string.device_admin_message)
                    .setPositiveButton(R.string.enable, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Launch the activity to have the user enable our admin.
                            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                                    new ComponentName(context, AdminReceiver.class));
                            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                    context.getString(R.string.device_admin_description));
                            context.startActivity(intent);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .create()
                    .show();
        }
    }

    public static boolean runGesture(Context context, Launcher mLauncher, int type) {
        AppSettings settings = AppSettings.getInstance(context);
        switch (settings.gestureActions[type]) {
            case AppSettings.NOTHING:
                return false;
            case AppSettings.OPEN_PAGES:
                mLauncher.getLauncherDrawer().openDrawer(Gravity.LEFT);
                return true;
            case AppSettings.OPEN_ALL_APPS:
                mLauncher.getAllAppsButton().performClick();
                return true;
            case AppSettings.OPEN_NOTIFICATIONS:
                openNotifications(context);
                return true;
            case AppSettings.SLEEP_DEVICE:
                putToSleep(context);
                return true;
            case AppSettings.OPEN_RECENT_APPS:
                openRecents(context);
                return true;
            default:
                return false;
        }
    }
}
