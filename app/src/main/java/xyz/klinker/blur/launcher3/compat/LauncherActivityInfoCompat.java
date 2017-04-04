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

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import xyz.klinker.blur.addons.utils.IconPackHelper;

public abstract class LauncherActivityInfoCompat {

    LauncherActivityInfoCompat() {
    }

    public abstract ComponentName getComponentName();
    public abstract UserHandleCompat getUser();
    public abstract CharSequence getLabel();
    public abstract Drawable getIcon(int density, IconPackHelper helper);
    public abstract ApplicationInfo getApplicationInfo();
    public abstract long getFirstInstallTime();
    public abstract boolean isThemed();

    /**
     * Creates a LauncherActivityInfoCompat for the primary user.
     */
    public static LauncherActivityInfoCompat fromResolveInfo(ResolveInfo info, Context context) {
        return new LauncherActivityInfoCompatV16(context, info);
    }
}
