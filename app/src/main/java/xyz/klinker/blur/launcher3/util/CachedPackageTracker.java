/*
 * Copyright (C) 2016 The Android Open Source Project
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

package xyz.klinker.blur.launcher3.util;

import android.content.Context;
import android.content.SharedPreferences;

import xyz.klinker.blur.launcher3.Utilities;
import xyz.klinker.blur.launcher3.compat.LauncherActivityInfoCompat;
import xyz.klinker.blur.launcher3.compat.LauncherAppsCompat;
import xyz.klinker.blur.launcher3.compat.LauncherAppsCompat.OnAppsChangedCallbackCompat;
import xyz.klinker.blur.launcher3.compat.UserHandleCompat;
import xyz.klinker.blur.launcher3.compat.UserManagerCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class to track list of installed packages. It persists the list so that apps
 * installed/uninstalled while Launcher was dead can also be handled properly.
 */
public abstract class CachedPackageTracker implements OnAppsChangedCallbackCompat {

    protected static final String INSTALLED_PACKAGES_PREFIX = "installed_packages_for_user_";

    protected final SharedPreferences mPrefs;
    protected final UserManagerCompat mUserManager;
    protected final LauncherAppsCompat mLauncherApps;

    public CachedPackageTracker(Context context, String preferenceFileName) {
        mPrefs = context.getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE);
        mUserManager = UserManagerCompat.getInstance(context);
        mLauncherApps = LauncherAppsCompat.getInstance(context);
    }

    /**
     * Checks the list of user apps, and generates package event accordingly.
     * {@see #onLauncherAppsAdded}, {@see #onLauncherPackageRemoved}
     */
    public void processUserApps(List<LauncherActivityInfoCompat> apps, UserHandleCompat user) {
        String prefKey = INSTALLED_PACKAGES_PREFIX + mUserManager.getSerialNumberForUser(user);
        HashSet<String> oldPackageSet = new HashSet<>();
        final boolean userAppsExisted = getUserApps(oldPackageSet, prefKey);

        HashSet<String> packagesRemoved = new HashSet<>(oldPackageSet);
        HashSet<String> newPackageSet = new HashSet<>();
        ArrayList<LauncherActivityInstallInfo> packagesAdded = new ArrayList<>();

        for (LauncherActivityInfoCompat info : apps) {
            String packageName = info.getComponentName().getPackageName();
            newPackageSet.add(packageName);
            packagesRemoved.remove(packageName);

            if (!oldPackageSet.contains(packageName)) {
                oldPackageSet.add(packageName);
                packagesAdded.add(new LauncherActivityInstallInfo(
                        info, info.getFirstInstallTime()));
            }
        }

        if (!packagesAdded.isEmpty() || !packagesRemoved.isEmpty()) {
            mPrefs.edit().putStringSet(prefKey, newPackageSet).apply();

            if (!packagesAdded.isEmpty()) {
                Collections.sort(packagesAdded);
                onLauncherAppsAdded(packagesAdded, user, userAppsExisted);
            }

            if (!packagesRemoved.isEmpty()) {
                for (String pkg : packagesRemoved) {
                    onLauncherPackageRemoved(pkg, user);
                }
            }
        }
    }

    /**
     * Reads the list of user apps which have already been processed.
     * @return false if the list didn't exist, true otherwise
     */
    private boolean getUserApps(HashSet<String> outExistingApps, String prefKey) {
        Set<String> userApps = mPrefs.getStringSet(prefKey, null);
        if (userApps == null) {
            return false;
        } else {
            outExistingApps.addAll(userApps);
            return true;
        }
    }

    @Override
    public void onPackageRemoved(String packageName, UserHandleCompat user) {
        String prefKey = INSTALLED_PACKAGES_PREFIX + mUserManager.getSerialNumberForUser(user);
        HashSet<String> packageSet = new HashSet<>();
        if (getUserApps(packageSet, prefKey) && packageSet.remove(packageName)) {
            mPrefs.edit().putStringSet(prefKey, packageSet).apply();
        }

        onLauncherPackageRemoved(packageName, user);
    }

    @Override
    public void onPackageAdded(String packageName, UserHandleCompat user) {
        String prefKey = INSTALLED_PACKAGES_PREFIX + mUserManager.getSerialNumberForUser(user);
        HashSet<String> packageSet = new HashSet<>();
        final boolean userAppsExisted = getUserApps(packageSet, prefKey);
        if (!packageSet.contains(packageName)) {
            List<LauncherActivityInfoCompat> activities =
                    mLauncherApps.getActivityList(packageName, user);
            if (!activities.isEmpty()) {
                LauncherActivityInfoCompat activityInfo = activities.get(0);

                packageSet.add(packageName);
                mPrefs.edit().putStringSet(prefKey, packageSet).apply();
                onLauncherAppsAdded(Arrays.asList(
                        new LauncherActivityInstallInfo(activityInfo, System.currentTimeMillis())),
                        user, userAppsExisted);
            }
        }
    }

    @Override
    public void onPackageChanged(String packageName, UserHandleCompat user) { }

    @Override
    public void onPackagesAvailable(
            String[] packageNames, UserHandleCompat user, boolean replacing) { }

    @Override
    public void onPackagesUnavailable(
            String[] packageNames, UserHandleCompat user, boolean replacing) { }

    @Override
    public void onPackagesSuspended(String[] packageNames, UserHandleCompat user) { }

    @Override
    public void onPackagesUnsuspended(String[] packageNames, UserHandleCompat user) { }

    /**
     * Called when new launcher apps are added.
     * @param apps list of newly added activities. Only one entry per package is sent.
     * @param user the user for this event. All activities in {@param apps} will belong to
     *             the same user.
     * @param userAppsExisted false if the list was processed for the first time, like in case
     *                        when Launcher was newly installed or a new user was added.
     */
    protected abstract void onLauncherAppsAdded(List<LauncherActivityInstallInfo> apps,
            UserHandleCompat user, boolean userAppsExisted);

    /**
     * Called when apps are removed from the system.
     */
    protected abstract void onLauncherPackageRemoved(String packageName, UserHandleCompat user);

    public static class LauncherActivityInstallInfo
            implements Comparable<LauncherActivityInstallInfo> {
        public final LauncherActivityInfoCompat info;
        public final long installTime;

        public LauncherActivityInstallInfo(LauncherActivityInfoCompat info, long installTime) {
            this.info = info;
            this.installTime = installTime;
        }

        @Override
        public int compareTo(LauncherActivityInstallInfo another) {
            return Utilities.longCompare(installTime, another.installTime);
        }
    }
}
