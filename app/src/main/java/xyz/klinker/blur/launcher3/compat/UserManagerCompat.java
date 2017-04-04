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

import android.content.Context;

import xyz.klinker.blur.launcher3.Utilities;

import java.util.List;

public abstract class UserManagerCompat {
    protected UserManagerCompat() {
    }

    private static final Object sInstanceLock = new Object();
    private static UserManagerCompat sInstance;

    public static UserManagerCompat getInstance(Context context) {
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                if (Utilities.isNycMR1OrAbove()) {
                    sInstance = new UserManagerCompatVNMr1(context.getApplicationContext());
                } else if (Utilities.isNycOrAbove()) {
                    sInstance = new UserManagerCompatVN(context.getApplicationContext());
                } else if (Utilities.ATLEAST_MARSHMALLOW) {
                    sInstance = new UserManagerCompatVM(context.getApplicationContext());
                } else if (Utilities.ATLEAST_LOLLIPOP) {
                    sInstance = new UserManagerCompatVL(context.getApplicationContext());
                } else if (Utilities.ATLEAST_JB_MR1) {
                    sInstance = new UserManagerCompatV17(context.getApplicationContext());
                } else {
                    sInstance = new UserManagerCompatV16();
                }
            }
            return sInstance;
        }
    }

    /**
     * Creates a cache for users.
     */
    public abstract void enableAndResetCache();

    public abstract List<UserHandleCompat> getUserProfiles();
    public abstract long getSerialNumberForUser(UserHandleCompat user);
    public abstract UserHandleCompat getUserForSerialNumber(long serialNumber);
    public abstract CharSequence getBadgedLabelForUser(CharSequence label, UserHandleCompat user);
    public abstract long getUserCreationTime(UserHandleCompat user);
    public abstract boolean isQuietModeEnabled(UserHandleCompat user);
    public abstract boolean isUserUnlocked(UserHandleCompat user);

    public abstract boolean isDemoUser();
}
