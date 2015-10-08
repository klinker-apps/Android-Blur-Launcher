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

package com.klinker.android.launcher.vertical_app_page;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import com.klinker.android.launcher.api.ResourceHelper;

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
     *
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
     *
     * @param context context of the activity where nav bar is displayed
     * @return true if nav bar is on the screen
     */
    public static boolean hasNavBar(Context context) {
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
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
     *
     * @param context context of the activity
     * @return the height of the screen
     */
    public static int getScreenHeight(Context context) {
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    /**
     * Returns the width of the screen
     *
     * @param context context of the activity
     * @return the height of the screen
     */
    public static int getScreenWidth(Context context) {
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    /**
     * Check for immersive mode in some custom roms such as PA
     *
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
     *
     * @param context context of fragment
     * @param px      pixel value to convert
     * @return value in dips
     */
    public static int toDP(Context context, int px) {
        try {
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, context.getResources().getDisplayMetrics());
        } catch (Exception e) {
            return px;
        }
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private static int sIconWidth = -1;
    private static int sIconHeight = -1;
    public static int sIconTextureWidth = -1;
    public static int sIconTextureHeight = -1;

    private static final Canvas sCanvas = new Canvas();
    static {
        sCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                Paint.FILTER_BITMAP_FLAG));
    }
    static int sColors[] = { 0xffff0000, 0xff00ff00, 0xff0000ff };
    static int sColorIndex = 0;
    private static final Rect sOldBounds = new Rect();
    private static void initStatics(Context context, ResourceHelper helper) {
        final Resources resources = context.getResources();
        final DisplayMetrics metrics = resources.getDisplayMetrics();
        final float density = metrics.density;

        sIconWidth = sIconHeight = (int) helper.getDimension("app_icon_size");
        sIconTextureWidth = sIconTextureHeight = sIconWidth;


        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0.2f);
    }

    /**
     * Returns a bitmap suitable for the all apps view.
     */
    static Bitmap createIconBitmap(Drawable icon, Context context, Drawable iconBack,
                                   Drawable iconMask, Drawable iconUpon, float scale, ResourceHelper helper) {
        synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                initStatics(context, helper);
            }

            int width = sIconWidth;
            int height = sIconHeight;

            if (icon instanceof PaintDrawable) {
                PaintDrawable painter = (PaintDrawable) icon;
                painter.setIntrinsicWidth(width);
                painter.setIntrinsicHeight(height);
            } else if (icon instanceof BitmapDrawable) {
                // Ensure the bitmap has a density.
                BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                if (bitmap.getDensity() == Bitmap.DENSITY_NONE) {
                    bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
                }
            }
            int sourceWidth = icon.getIntrinsicWidth();
            int sourceHeight = icon.getIntrinsicHeight();
            if (sourceWidth > 0 && sourceHeight > 0) {
                // Scale the icon proportionally to the icon dimensions
                final float ratio = (float) sourceWidth / sourceHeight;
                if (sourceWidth > sourceHeight) {
                    height = (int) (width / ratio);
                } else if (sourceHeight > sourceWidth) {
                    width = (int) (height * ratio);
                }
            }

            // no intrinsic size --> use default size
            int textureWidth = sIconTextureWidth;
            int textureHeight = sIconTextureHeight;

            if (sIconTextureHeight <= 0 || sIconTextureWidth <= 0) {
                initStatics(context, helper);
            }

            Bitmap bitmap = Bitmap.createBitmap(textureWidth, textureHeight,
                    Bitmap.Config.ARGB_8888);
            final Canvas canvas = sCanvas;
            canvas.setBitmap(bitmap);

            final int left = (textureWidth-width) / 2;
            final int top = (textureHeight-height) / 2;

            @SuppressWarnings("all") // suppress dead code warning
            final boolean debug = false;
            if (debug) {
                // draw a big box for the icon for debugging
                canvas.drawColor(sColors[sColorIndex]);
                if (++sColorIndex >= sColors.length) sColorIndex = 0;
                Paint debugPaint = new Paint();
                debugPaint.setColor(0xffcccc00);
                canvas.drawRect(left, top, left+width, top+height, debugPaint);
            }

            sOldBounds.set(icon.getBounds());
            icon.setBounds(left, top, left+width, top+height);
            canvas.save();
            canvas.scale(scale, scale, width / 2, height/2);
            icon.draw(canvas);
            canvas.restore();
            if (iconMask != null) {
                iconMask.setBounds(icon.getBounds());
                ((BitmapDrawable) iconMask).getPaint().setXfermode(
                        new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
                iconMask.draw(canvas);
            }
            if (iconUpon != null) {
                iconUpon.draw(canvas);
                canvas.drawBitmap(((BitmapDrawable)iconUpon).getBitmap(), null, icon.getBounds(), new Paint());
            }
            if (iconBack != null) {
                canvas.setBitmap(null);
                Bitmap finalBitmap = Bitmap.createBitmap(textureWidth, textureHeight,
                        Bitmap.Config.ARGB_8888);
                canvas.setBitmap(finalBitmap);
                iconBack.setBounds(icon.getBounds());
                iconBack.draw(canvas);
                canvas.drawBitmap(bitmap, null, icon.getBounds(), null);
                bitmap = finalBitmap;
            }
            icon.setBounds(sOldBounds);
            canvas.setBitmap(null);

            return bitmap;
        }
    }
}