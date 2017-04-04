/*
 * Copyright (C) 2014 The Android Open Source Project
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

package xyz.klinker.blur.launcher3.compat;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.UserHandle;

import xyz.klinker.blur.launcher3.shortcuts.ShortcutInfoCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class LauncherAppsCompatVL extends LauncherAppsCompatV16 {

    protected LauncherApps mLauncherApps;

    private Map<OnAppsChangedCallbackCompat, WrappedCallback> mCallbacks
            = new HashMap<OnAppsChangedCallbackCompat, WrappedCallback>();

    LauncherAppsCompatVL(Context context) {
        super(context);
        mLauncherApps = (LauncherApps) context.getSystemService("launcherapps");
    }

    public List<LauncherActivityInfoCompat> getActivityList(String packageName,
            UserHandleCompat user) {
        List<LauncherActivityInfo> list = mLauncherApps.getActivityList(packageName,
                user.getUser());
        if (list.size() == 0) {
            return Collections.emptyList();
        }
        ArrayList<LauncherActivityInfoCompat> compatList =
                new ArrayList<LauncherActivityInfoCompat>(list.size());
        for (LauncherActivityInfo info : list) {
            compatList.add(new LauncherActivityInfoCompatVL(info));
        }
        return compatList;
    }

    public LauncherActivityInfoCompat resolveActivity(Intent intent, UserHandleCompat user) {
        LauncherActivityInfo activity = mLauncherApps.resolveActivity(intent, user.getUser());
        if (activity != null) {
            return new LauncherActivityInfoCompatVL(activity);
        } else {
            return null;
        }
    }

    public void startActivityForProfile(ComponentName component, UserHandleCompat user,
            Rect sourceBounds, Bundle opts) {
        mLauncherApps.startMainActivity(component, user.getUser(), sourceBounds, opts);
    }

    public void showAppDetailsForProfile(ComponentName component, UserHandleCompat user) {
        mLauncherApps.startAppDetailsActivity(component, user.getUser(), null, null);
    }

    public void addOnAppsChangedCallback(LauncherAppsCompat.OnAppsChangedCallbackCompat callback) {
        WrappedCallback wrappedCallback = new WrappedCallback(callback);
        synchronized (mCallbacks) {
            mCallbacks.put(callback, wrappedCallback);
        }
        mLauncherApps.registerCallback(wrappedCallback);
    }

    public void removeOnAppsChangedCallback(
            LauncherAppsCompat.OnAppsChangedCallbackCompat callback) {
        WrappedCallback wrappedCallback = null;
        synchronized (mCallbacks) {
            wrappedCallback = mCallbacks.remove(callback);
        }
        if (wrappedCallback != null) {
            mLauncherApps.unregisterCallback(wrappedCallback);
        }
    }

    public boolean isPackageEnabledForProfile(String packageName, UserHandleCompat user) {
        return mLauncherApps.isPackageEnabled(packageName, user.getUser());
    }

    public boolean isActivityEnabledForProfile(ComponentName component, UserHandleCompat user) {
        return mLauncherApps.isActivityEnabled(component, user.getUser());
    }

    public boolean isPackageSuspendedForProfile(String packageName, UserHandleCompat user) {
        return false;
    }

    private static class WrappedCallback extends LauncherApps.Callback {
        private LauncherAppsCompat.OnAppsChangedCallbackCompat mCallback;

        public WrappedCallback(LauncherAppsCompat.OnAppsChangedCallbackCompat callback) {
            mCallback = callback;
        }

        public void onPackageRemoved(String packageName, UserHandle user) {
            mCallback.onPackageRemoved(packageName, UserHandleCompat.fromUser(user));
        }

        public void onPackageAdded(String packageName, UserHandle user) {
            mCallback.onPackageAdded(packageName, UserHandleCompat.fromUser(user));
        }

        public void onPackageChanged(String packageName, UserHandle user) {
            mCallback.onPackageChanged(packageName, UserHandleCompat.fromUser(user));
        }

        public void onPackagesAvailable(String[] packageNames, UserHandle user, boolean replacing) {
            mCallback.onPackagesAvailable(packageNames, UserHandleCompat.fromUser(user), replacing);
        }

        public void onPackagesUnavailable(String[] packageNames, UserHandle user,
                boolean replacing) {
            mCallback.onPackagesUnavailable(packageNames, UserHandleCompat.fromUser(user),
                    replacing);
        }

        public void onPackagesSuspended(String[] packageNames, UserHandle user) {
            mCallback.onPackagesSuspended(packageNames, UserHandleCompat.fromUser(user));
        }

        public void onPackagesUnsuspended(String[] packageNames, UserHandle user) {
            mCallback.onPackagesUnsuspended(packageNames, UserHandleCompat.fromUser(user));
        }

        @Override
        public void onShortcutsChanged(String packageName, List<ShortcutInfo> shortcuts,
                UserHandle user) {
            List<ShortcutInfoCompat> shortcutInfoCompats = new ArrayList<>(shortcuts.size());
            for (ShortcutInfo shortcutInfo : shortcuts) {
                shortcutInfoCompats.add(new ShortcutInfoCompat(shortcutInfo));
            }

            mCallback.onShortcutsChanged(packageName, shortcutInfoCompats,
                    UserHandleCompat.fromUser(user));
        }
    }
}

