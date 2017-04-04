/*
 * Copyright (C) 2015 The Android Open Source Project
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

import xyz.klinker.blur.launcher3.FolderInfo;
import xyz.klinker.blur.launcher3.ItemInfo;
import xyz.klinker.blur.launcher3.LauncherAppState;
import xyz.klinker.blur.launcher3.LauncherFiles;
import xyz.klinker.blur.launcher3.LauncherModel;
import xyz.klinker.blur.launcher3.MainThreadExecutor;
import xyz.klinker.blur.R;
import xyz.klinker.blur.launcher3.ShortcutInfo;
import xyz.klinker.blur.launcher3.Utilities;
import xyz.klinker.blur.launcher3.compat.LauncherActivityInfoCompat;
import xyz.klinker.blur.launcher3.shortcuts.ShortcutInfoCompat;
import xyz.klinker.blur.launcher3.compat.UserHandleCompat;
import xyz.klinker.blur.launcher3.compat.UserManagerCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Handles addition of app shortcuts for managed profiles.
 * Methods of class should only be called on {@link LauncherModel#sWorkerThread}.
 */
public class ManagedProfileHeuristic {

    /**
     * Maintain a set of packages installed per user.
     */
    private static final String INSTALLED_PACKAGES_PREFIX = "installed_packages_for_user_";

    private static final String USER_FOLDER_ID_PREFIX = "user_folder_";

    /**
     * Duration (in milliseconds) for which app shortcuts will be added to work folder.
     */
    private static final long AUTO_ADD_TO_FOLDER_DURATION = 8 * 60 * 60 * 1000;

    public static ManagedProfileHeuristic get(Context context, UserHandleCompat user) {
        if (Utilities.ATLEAST_LOLLIPOP && !UserHandleCompat.myUserHandle().equals(user)) {
            return new ManagedProfileHeuristic(context, user);
        }
        return null;
    }

    private final Context mContext;
    private final LauncherModel mModel;
    private final UserHandleCompat mUser;

    private ManagedProfileHeuristic(Context context, UserHandleCompat user) {
        mContext = context;
        mUser = user;
        mModel = LauncherAppState.getInstance().getModel();
    }

    public void processPackageRemoved(String[] packages) {
        Preconditions.assertWorkerThread();
        ManagedProfilePackageHandler handler = new ManagedProfilePackageHandler();
        for (String pkg : packages) {
            handler.onPackageRemoved(pkg, mUser);
        }
    }

    public void processPackageAdd(String[] packages) {
        Preconditions.assertWorkerThread();
        ManagedProfilePackageHandler handler = new ManagedProfilePackageHandler();
        for (String pkg : packages) {
            handler.onPackageAdded(pkg, mUser);
        }
    }

    public void processUserApps(List<LauncherActivityInfoCompat> apps) {
        Preconditions.assertWorkerThread();
        new ManagedProfilePackageHandler().processUserApps(apps, mUser);
    }

    private class ManagedProfilePackageHandler extends CachedPackageTracker {

        private ManagedProfilePackageHandler() {
            super(mContext, LauncherFiles.MANAGED_USER_PREFERENCES_KEY);
        }

        protected void onLauncherAppsAdded(
                List<LauncherActivityInstallInfo> apps, UserHandleCompat user, boolean userAppsExisted) {
            ArrayList<ShortcutInfo> workFolderApps = new ArrayList<>();
            ArrayList<ShortcutInfo> homescreenApps = new ArrayList<>();

            int count = apps.size();
            long folderCreationTime =
                    mUserManager.getUserCreationTime(user) + AUTO_ADD_TO_FOLDER_DURATION;

            for (int i = 0; i < count; i++) {
                LauncherActivityInstallInfo info = apps.get(i);

                ShortcutInfo si = new ShortcutInfo(info.info, mContext);
                ((info.installTime <= folderCreationTime) ? workFolderApps : homescreenApps).add(si);
            }

            finalizeWorkFolder(user, workFolderApps, homescreenApps);

            // Do not add shortcuts on the homescreen for the first time. This prevents the launcher
            // getting filled with the managed user apps, when it start with a fresh DB (or after
            // a very long time).
            if (userAppsExisted && !homescreenApps.isEmpty()) {
                mModel.addAndBindAddedWorkspaceItems(mContext, homescreenApps);
            }
        }

        @Override
        protected void onLauncherPackageRemoved(String packageName, UserHandleCompat user) {
        }

        /**
         * Adds and binds shortcuts marked to be added to the work folder.
         */
        private void finalizeWorkFolder(
                UserHandleCompat user, final ArrayList<ShortcutInfo> workFolderApps,
                ArrayList<ShortcutInfo> homescreenApps) {
            if (workFolderApps.isEmpty()) {
                return;
            }
            // Try to get a work folder.
            String folderIdKey = USER_FOLDER_ID_PREFIX + mUserManager.getSerialNumberForUser(user);
            if (mPrefs.contains(folderIdKey)) {
                long folderId = mPrefs.getLong(folderIdKey, 0);
                final FolderInfo workFolder = mModel.findFolderById(folderId);

                if (workFolder == null || !workFolder.hasOption(FolderInfo.FLAG_WORK_FOLDER)) {
                    // Could not get a work folder. Add all the icons to homescreen.
                    homescreenApps.addAll(0, workFolderApps);
                    return;
                }
                saveWorkFolderShortcuts(folderId, workFolder.contents.size(), workFolderApps);

                // FolderInfo could already be bound. We need to add shortcuts on the UI thread.
                new MainThreadExecutor().execute(new Runnable() {

                    @Override
                    public void run() {
                        for (ShortcutInfo info : workFolderApps) {
                            workFolder.add(info, false);
                        }
                    }
                });
            } else {
                // Create a new folder.
                final FolderInfo workFolder = new FolderInfo();
                workFolder.title = mContext.getText(R.string.work_folder_name);
                workFolder.setOption(FolderInfo.FLAG_WORK_FOLDER, true, null);

                // Add all shortcuts before adding it to the UI, as an empty folder might get deleted.
                for (ShortcutInfo info : workFolderApps) {
                    workFolder.add(info, false);
                }

                // Add the item to home screen and DB. This also generates an item id synchronously.
                ArrayList<ItemInfo> itemList = new ArrayList<ItemInfo>(1);
                itemList.add(workFolder);
                mModel.addAndBindAddedWorkspaceItems(mContext, itemList);
                mPrefs.edit().putLong(folderIdKey, workFolder.id).apply();

                saveWorkFolderShortcuts(workFolder.id, 0, workFolderApps);
            }
        }

        @Override
        public void onShortcutsChanged(String packageName, List<ShortcutInfoCompat> shortcuts,
                UserHandleCompat user) {
            // Do nothing
        }
    }

    /**
     * Add work folder shortcuts to the DB.
     */
    private void saveWorkFolderShortcuts(
            long workFolderId, int startingRank, ArrayList<ShortcutInfo> workFolderApps) {
        for (ItemInfo info : workFolderApps) {
            info.rank = startingRank++;
            LauncherModel.addItemToDatabase(mContext, info, workFolderId, 0, 0, 0);
        }
    }


    /**
     * Verifies that entries corresponding to {@param users} exist and removes all invalid entries.
     */
    public static void processAllUsers(List<UserHandleCompat> users, Context context) {
        if (!Utilities.ATLEAST_LOLLIPOP) {
            return;
        }
        UserManagerCompat userManager = UserManagerCompat.getInstance(context);
        HashSet<String> validKeys = new HashSet<String>();
        for (UserHandleCompat user : users) {
            addAllUserKeys(userManager.getSerialNumberForUser(user), validKeys);
        }

        SharedPreferences prefs = context.getSharedPreferences(
                LauncherFiles.MANAGED_USER_PREFERENCES_KEY,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        for (String key : prefs.getAll().keySet()) {
            if (!validKeys.contains(key)) {
                editor.remove(key);
            }
        }
        editor.apply();
    }

    private static void addAllUserKeys(long userSerial, HashSet<String> keysOut) {
        keysOut.add(INSTALLED_PACKAGES_PREFIX + userSerial);
        keysOut.add(USER_FOLDER_ID_PREFIX + userSerial);
    }

    /**
     * For each user, if a work folder has not been created, mark it such that the folder will
     * never get created.
     */
    public static void markExistingUsersForNoFolderCreation(Context context) {
        UserManagerCompat userManager = UserManagerCompat.getInstance(context);
        UserHandleCompat myUser = UserHandleCompat.myUserHandle();

        SharedPreferences prefs = null;
        for (UserHandleCompat user : userManager.getUserProfiles()) {
            if (myUser.equals(user)) {
                continue;
            }

            if (prefs == null) {
                prefs = context.getSharedPreferences(
                        LauncherFiles.MANAGED_USER_PREFERENCES_KEY,
                        Context.MODE_PRIVATE);
            }
            String folderIdKey = USER_FOLDER_ID_PREFIX + userManager.getSerialNumberForUser(user);
            if (!prefs.contains(folderIdKey)) {
                prefs.edit().putLong(folderIdKey, ItemInfo.NO_ID).apply();
            }
        }
    }
}
