/*
 * Copyright 2014 Klinker Apps Inc.
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

package com.klinker.android.launcher.calc_page;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.TypedValue;
import android.view.Display;

/**
 * Commonly used utilities
 */
public class Utils {

    // Used when creating the resource helper. technically, this should be com.klinker.android.launcher.info_page, but
    // since we are using it as a module in the app, it needs to use the launcher's package since it doesn't have it's
    // own once compiled on the device
    public static final String PACKAGE_NAME = "com.klinker.android.launcher";

    /**
     * Gets the nav bar height
     * @param context context of the activity where nav bar is displayed
     * @return the height in pixels
     */
    public static int getNavBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * Check if device has nav bar available
     * @param context context of the activity where nav bar is displayed
     * @return true if nav bar is on the screen
     */
    public static boolean hasNavBar(Context context) {
        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        Point realSize = new Point();
        display.getSize(size);
        display.getRealSize(realSize);

        return Math.max(size.x, size.y) < Math.max(realSize.x, realSize.y) &&
                (context.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) &&
                !isInImmersiveMode(context);
    }

    /**
     * Returns the height of the screen
     * @param context context of the activity
     * @return the height of the screen
     */
    public static int getScreenHeight(Context context) {
        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    /**
     * Returns the width of the screen
     * @param context context of the activity
     * @return the height of the screen
     */
    public static int getScreenWidth(Context context) {
        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    /**
     * Check for immersive mode in some custom roms such as PA
     * @param context context of the activity
     * @return true if in immersive mode
     */
    public static boolean isInImmersiveMode(Context context) {
        try {
            int immersive = android.provider.Settings.System.getInt(context.getContentResolver(), "immersive_mode");

            if (immersive == 1) {
                return true;
            }
        } catch (Exception e) {
            // setting not found, so they dont have it
        }

        return false;
    }

    /**
     * Converts pixesl to dips
     * @param context context of fragment
     * @param px pixel value to convert
     * @return value in dips
     */
    public static int toDP(Context context, int px) {
        try {
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, context.getResources().getDisplayMetrics());
        } catch (Exception e) {
            return px;
        }
    }
}
