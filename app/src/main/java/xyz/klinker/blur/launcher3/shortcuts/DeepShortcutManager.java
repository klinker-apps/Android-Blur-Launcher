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

package xyz.klinker.blur.launcher3.shortcuts;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.LauncherApps;
import android.content.pm.LauncherApps.ShortcutQuery;
import android.content.pm.ShortcutInfo;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import xyz.klinker.blur.launcher3.ItemInfo;
import xyz.klinker.blur.launcher3.LauncherSettings;
import xyz.klinker.blur.launcher3.Utilities;
import xyz.klinker.blur.launcher3.compat.UserHandleCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Performs operations related to deep shortcuts, such as querying for them, pinning them, etc.
 */
public class DeepShortcutManager {
    private static final String TAG = "DeepShortcutManager";

    // TODO: Replace this with platform constants when the new sdk is available.
    public static final int FLAG_MATCH_DYNAMIC = 1 << 0;
    public static final int FLAG_MATCH_MANIFEST = 1 << 3;
    public static final int FLAG_MATCH_PINNED = 1 << 1;

    private static final int FLAG_GET_ALL =
            FLAG_MATCH_DYNAMIC | FLAG_MATCH_PINNED | FLAG_MATCH_MANIFEST;

    private final LauncherApps mLauncherApps;
    private boolean mWasLastCallSuccess;

    public DeepShortcutManager(Context context, ShortcutCache shortcutCache) {
        mLauncherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
    }

    public static boolean supportsShortcuts(ItemInfo info) {
        return info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
    }

    public boolean wasLastCallSuccess() {
        return mWasLastCallSuccess;
    }

    public void onShortcutsChanged(List<ShortcutInfoCompat> shortcuts) {
        // mShortcutCache.removeShortcuts(shortcuts);
    }

    /**
     * Queries for the shortcuts with the package name and provided ids.
     *
     * This method is intended to get the full details for shortcuts when they are added or updated,
     * because we only get "key" fields in onShortcutsChanged().
     */
    public List<ShortcutInfoCompat> queryForFullDetails(String packageName,
            List<String> shortcutIds, UserHandleCompat user) {
        return query(FLAG_GET_ALL, packageName, null, shortcutIds, user);
    }

    /**
     * Gets all the manifest and dynamic shortcuts associated with the given package and user,
     * to be displayed in the shortcuts container on long press.
     */
    public List<ShortcutInfoCompat> queryForShortcutsContainer(ComponentName activity,
            List<String> ids, UserHandleCompat user) {
        return query(FLAG_MATCH_MANIFEST | FLAG_MATCH_DYNAMIC,
                activity.getPackageName(), activity, ids, user);
    }

    /**
     * Removes the given shortcut from the current list of pinned shortcuts.
     * (Runs on background thread)
     */
    @TargetApi(25)
    public void unpinShortcut(final ShortcutKey key) {
        if (Utilities.isNycMR1OrAbove()) {
            String packageName = key.componentName.getPackageName();
            String id = key.getId();
            UserHandleCompat user = key.user;
            List<String> pinnedIds = extractIds(queryForPinnedShortcuts(packageName, user));
            pinnedIds.remove(id);
            try {
                mLauncherApps.pinShortcuts(packageName, pinnedIds, user.getUser());
                mWasLastCallSuccess = true;
            } catch (SecurityException|IllegalStateException e) {
                Log.w(TAG, "Failed to unpin shortcut", e);
                mWasLastCallSuccess = false;
            }
        }
    }

    /**
     * Adds the given shortcut to the current list of pinned shortcuts.
     * (Runs on background thread)
     */
    @TargetApi(25)
    public void pinShortcut(final ShortcutKey key) {
        if (Utilities.isNycMR1OrAbove()) {
            String packageName = key.componentName.getPackageName();
            String id = key.getId();
            UserHandleCompat user = key.user;
            List<String> pinnedIds = extractIds(queryForPinnedShortcuts(packageName, user));
            pinnedIds.add(id);
            try {
                mLauncherApps.pinShortcuts(packageName, pinnedIds, user.getUser());
                mWasLastCallSuccess = true;
            } catch (SecurityException|IllegalStateException e) {
                Log.w(TAG, "Failed to pin shortcut", e);
                mWasLastCallSuccess = false;
            }
        }
    }

    @TargetApi(25)
    public void startShortcut(String packageName, String id, Rect sourceBounds,
          Bundle startActivityOptions, UserHandleCompat user) {
        if (Utilities.isNycMR1OrAbove()) {
            try {
                mLauncherApps.startShortcut(packageName, id, sourceBounds,
                        startActivityOptions, user.getUser());
                mWasLastCallSuccess = true;
            } catch (SecurityException|IllegalStateException e) {
                Log.e(TAG, "Failed to start shortcut", e);
                mWasLastCallSuccess = false;
            }
        }
    }

    @TargetApi(25)
    public Drawable getShortcutIconDrawable(ShortcutInfoCompat shortcutInfo, int density) {
        if (Utilities.isNycMR1OrAbove()) {
            try {
                Drawable icon = mLauncherApps.getShortcutIconDrawable(
                        shortcutInfo.getShortcutInfo(), density);
                mWasLastCallSuccess = true;
                return icon;
            } catch (SecurityException|IllegalStateException e) {
                Log.e(TAG, "Failed to get shortcut icon", e);
                mWasLastCallSuccess = false;
            }
        }
        return null;
    }

    /**
     * Returns the id's of pinned shortcuts associated with the given package and user.
     *
     * If packageName is null, returns all pinned shortcuts regardless of package.
     */
    public List<ShortcutInfoCompat> queryForPinnedShortcuts(String packageName,
            UserHandleCompat user) {
        return query(FLAG_MATCH_PINNED, packageName, null, null, user);
    }

    public List<ShortcutInfoCompat> queryForAllShortcuts(UserHandleCompat user) {
        return query(FLAG_GET_ALL, null, null, null, user);
    }

    private List<String> extractIds(List<ShortcutInfoCompat> shortcuts) {
        List<String> shortcutIds = new ArrayList<>(shortcuts.size());
        for (ShortcutInfoCompat shortcut : shortcuts) {
            shortcutIds.add(shortcut.getId());
        }
        return shortcutIds;
    }

    /**
     * Query the system server for all the shortcuts matching the given parameters.
     * If packageName == null, we query for all shortcuts with the passed flags, regardless of app.
     *
     * TODO: Use the cache to optimize this so we don't make an RPC every time.
     */
    @TargetApi(25)
    private List<ShortcutInfoCompat> query(int flags, String packageName,
            ComponentName activity, List<String> shortcutIds, UserHandleCompat user) {
        if (Utilities.isNycMR1OrAbove()) {
            ShortcutQuery q = new ShortcutQuery();
            q.setQueryFlags(flags);
            if (packageName != null) {
                q.setPackage(packageName);
                q.setActivity(activity);
                q.setShortcutIds(shortcutIds);
            }
            List<ShortcutInfo> shortcutInfos = null;
            try {
                shortcutInfos = mLauncherApps.getShortcuts(q, user.getUser());
                mWasLastCallSuccess = true;
            } catch (SecurityException|IllegalStateException e) {
                Log.e(TAG, "Failed to query for shortcuts", e);
                mWasLastCallSuccess = false;
            }
            if (shortcutInfos == null) {
                return Collections.EMPTY_LIST;
            }
            List<ShortcutInfoCompat> shortcutInfoCompats = new ArrayList<>(shortcutInfos.size());
            for (ShortcutInfo shortcutInfo : shortcutInfos) {
                shortcutInfoCompats.add(new ShortcutInfoCompat(shortcutInfo));
            }
            return shortcutInfoCompats;
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    @TargetApi(25)
    public boolean hasHostPermission() {
        if (Utilities.isNycMR1OrAbove()) {
            try {
                return mLauncherApps.hasShortcutHostPermission();
            } catch (SecurityException|IllegalStateException e) {
                Log.e(TAG, "Failed to make shortcut manager call", e);
            }
        }
        return false;
    }
}
