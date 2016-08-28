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
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import xyz.klinker.blur.addons.utils.IconPackHelper;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class LauncherActivityInfoCompatVL extends LauncherActivityInfoCompat {
    private LauncherActivityInfo mLauncherActivityInfo;
    private boolean isThemed;

    LauncherActivityInfoCompatVL(LauncherActivityInfo launcherActivityInfo) {
        super();
        mLauncherActivityInfo = launcherActivityInfo;
    }

    public ComponentName getComponentName() {
        return mLauncherActivityInfo.getComponentName();
    }

    public UserHandleCompat getUser() {
        return UserHandleCompat.fromUser(mLauncherActivityInfo.getUser());
    }

    public CharSequence getLabel() {
        return mLauncherActivityInfo.getLabel();
    }

    public Drawable getIcon(int density, IconPackHelper helper) {
        if (helper != null && helper.isIconPackLoaded()) {
            int iconId = helper.getResourceIdForActivityIcon(mLauncherActivityInfo.getComponentName().getPackageName(),
                    mLauncherActivityInfo.getComponentName().getClassName());
            if (iconId != 0) {
                isThemed = true;
                return helper.getIconPackResources().getDrawableForDensity(iconId, density);
            }
        }

        return mLauncherActivityInfo.getIcon(density);
    }

    public ApplicationInfo getApplicationInfo() {
        return mLauncherActivityInfo.getApplicationInfo();
    }

    public long getFirstInstallTime() {
        return mLauncherActivityInfo.getFirstInstallTime();
    }

    @Override
    public boolean isThemed() {
        return isThemed;
    }
}
