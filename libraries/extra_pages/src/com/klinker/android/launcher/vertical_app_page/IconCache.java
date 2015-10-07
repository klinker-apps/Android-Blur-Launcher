/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.klinker.android.launcher.vertical_app_page;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import com.klinker.android.launcher.api.ResourceHelper;

import java.util.HashMap;

/**
 * Cache of application icons.  Icons can be made from any thread.
 */
public class IconCache {
    @SuppressWarnings("unused")
    private static final String TAG = "Launcher.IconCache";

    private static final int INITIAL_ICON_CACHE_CAPACITY = 50;
    private IconPackHelper mIconPackHelper;
    private ResourceHelper resHelper;

    private String iconPack = "";

    private static class CacheEntry {
        public Bitmap icon;
        public String title;
    }

    private final Bitmap mDefaultIcon;
    private final Context mContext;
    private final PackageManager mPackageManager;
    private final HashMap<ComponentName, CacheEntry> mCache =
            new HashMap<ComponentName, CacheEntry>(INITIAL_ICON_CACHE_CAPACITY);
    private int mIconDpi;

    public IconCache(Context context) {
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        resHelper = new ResourceHelper(context, Utils.PACKAGE_NAME);

        iconPack = PreferenceManager.getDefaultSharedPreferences(context).getString("icon_pack", "");
        mContext = context;
        mPackageManager = context.getPackageManager();
        mIconDpi = activityManager.getLauncherLargeIconDensity();

        // need to set mIconDpi before getting default icon
        mDefaultIcon = makeDefaultIcon();

        mIconPackHelper = new IconPackHelper(context);
        loadIconPack();
    }

    public Drawable getFullResDefaultActivityIcon() {
        return getFullResIcon(Resources.getSystem(),
                android.R.mipmap.sym_def_app_icon);
    }

    public Drawable getFullResIcon(Resources resources, int iconId) {
        Drawable d;
        try {
            if (Build.VERSION.SDK_INT >= 15) {
                d = resources.getDrawableForDensity(iconId, mIconDpi);
            } else {
                d = resources.getDrawable(iconId);
            }
        } catch (Throwable e) {
            d = null;
        }

        return (d != null) ? d : getFullResDefaultActivityIcon();
    }

    public Drawable getFullResIcon(ResolveInfo info) {
        return getFullResIcon(info.activityInfo);
    }

    public Drawable getFullResIcon(ActivityInfo info) {

        Resources resources;
        try {
            resources = mPackageManager.getResourcesForApplication(
                    info.applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            resources = null;
        }
        if (resources != null) {
            int iconId = 0;
            if (mIconPackHelper != null && mIconPackHelper.isIconPackLoaded()) {
                iconId = mIconPackHelper.getResourceIdForActivityIcon(info);
                if (iconId != 0) {
                    return getFullResIcon(mIconPackHelper.getIconPackResources(), iconId);
                }
            }
            iconId = info.getIconResource();
            if (iconId != 0 && !TextUtils.isEmpty(iconPack)) {
                return new BitmapDrawable(Utils.createIconBitmap(getFullResIcon(resources, iconId),
                        mContext, getIconBack(), getIconMask(), getIconUpon(), getScale(), resHelper));//getFullResIcon(resources, iconId);
            } else if (iconId != 0) {
                return getFullResIcon(resources, iconId);
            }
        }
        if (!TextUtils.isEmpty(iconPack)) {
            return new BitmapDrawable(Utils.createIconBitmap(getFullResDefaultActivityIcon(),
                    mContext, getIconBack(), getIconMask(), getIconUpon(), getScale(), resHelper));//getFullResIcon(resources, iconId);

        } else {
            return getFullResDefaultActivityIcon();
        }
    }

    private Bitmap makeDefaultIcon() {
        Drawable d = getFullResDefaultActivityIcon();
        Bitmap b = Bitmap.createBitmap(Math.max(d.getIntrinsicWidth(), 1),
                Math.max(d.getIntrinsicHeight(), 1),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        d.setBounds(0, 0, b.getWidth(), b.getHeight());
        d.draw(c);
        c.setBitmap(null);
        return b;
    }

    private void loadIconPack() {
        mIconPackHelper.unloadIconPack();
        if (!TextUtils.isEmpty(iconPack)) {
            mIconPackHelper = new IconPackHelper(mContext);
            mIconPackHelper.loadIconPack(iconPack);
        }
    }

    /**
     * Remove any records for the supplied ComponentName.
     */
    public void remove(ComponentName componentName) {
        synchronized (mCache) {
            mCache.remove(componentName);
        }
    }

    /**
     * Empty out the cache.
     */
    public void flush() {
        synchronized (mCache) {
            mCache.clear();
        }
        loadIconPack();
    }

    public Drawable getIconUpon() {
        return mIconPackHelper.getIconUpon();
    }

    public Drawable getIconMask() {
        return mIconPackHelper.getIconMask();
    }

    public Drawable getIconBack() {
        return mIconPackHelper.getIconBack();
    }

    public float getScale() {
        return mIconPackHelper.getIconScale();
    }
}