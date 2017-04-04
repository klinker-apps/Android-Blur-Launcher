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

package xyz.klinker.blur.launcher3;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import xyz.klinker.blur.R;
import xyz.klinker.blur.launcher3.util.Thunk;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * A class that parses content values corresponding to some common app types.
 */
public class CommonAppTypeParser implements AutoInstallsLayout.LayoutParserCallback {
    private static final String TAG = "CommonAppTypeParser";

    // Including TARGET_NONE
    public static final int SUPPORTED_TYPE_COUNT = 7;

    private static final int RESTORE_FLAG_BIT_SHIFT = 4;

    public static final int TARGET_PHONE = 1;
    public static final int TARGET_MESSENGER = 2;
    public static final int TARGET_EMAIL = 3;
    public static final int TARGET_BROWSER = 4;
    public static final int TARGET_GALLERY = 5;
    public static final int TARGET_CAMERA = 6;

    private final long mItemId;
    @Thunk
    final int mResId;
    @Thunk final Context mContext;

    ContentValues parsedValues;
    Intent parsedIntent;
    String parsedTitle;

    public CommonAppTypeParser(long itemId, int itemType, Context context) {
        mItemId = itemId;
        mContext = context;
        mResId = getResourceForItemType(itemType);
    }

    @Override
    public long generateNewItemId() {
        return mItemId;
    }

    @Override
    public long insertAndCheck(SQLiteDatabase db, ContentValues values) {
        parsedValues = values;

        // Remove unwanted values
        values.put(LauncherSettings.Favorites.ICON_PACKAGE, (String) null);
        values.put(LauncherSettings.Favorites.ICON_RESOURCE, (String) null);
        values.put(LauncherSettings.Favorites.ICON, (byte[]) null);
        return 1;
    }

    /**
     * Tries to find a suitable app to the provided app type.
     */
    public boolean findDefaultApp() {
        if (mResId == 0) {
            return false;
        }

        parsedIntent = null;
        parsedValues = null;
        new MyLayoutParser().parseValues();
        return (parsedValues != null) && (parsedIntent != null);
    }

    private class MyLayoutParser extends DefaultLayoutParser {

        public MyLayoutParser() {
            super(CommonAppTypeParser.this.mContext, null, CommonAppTypeParser.this,
                    CommonAppTypeParser.this.mContext.getResources(), mResId, TAG_RESOLVE);
        }

        @Override
        protected long addShortcut(String title, Intent intent, int type) {
            if (type == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) {
                parsedIntent = intent;
                parsedTitle = title;
            }
            return super.addShortcut(title, intent, type);
        }

        public void parseValues() {
            XmlResourceParser parser = mSourceRes.getXml(mLayoutId);
            try {
                beginDocument(parser, mRootTag);
                new ResolveParser().parseAndAdd(parser);
            } catch (IOException | XmlPullParserException e) {
                Log.e(TAG, "Unable to parse default app info", e);
            }
            parser.close();
        }
    }

    public static int getResourceForItemType(int type) {
        switch (type) {
            case TARGET_PHONE:
                return R.xml.app_target_phone;

            case TARGET_MESSENGER:
                return R.xml.app_target_messenger;

            case TARGET_EMAIL:
                return R.xml.app_target_email;

            case TARGET_BROWSER:
                return R.xml.app_target_browser;

            case TARGET_GALLERY:
                return R.xml.app_target_gallery;

            case TARGET_CAMERA:
                return R.xml.app_target_camera;

            default:
                return 0;
        }
    }

    public static int encodeItemTypeToFlag(int itemType) {
        return itemType << RESTORE_FLAG_BIT_SHIFT;
    }

    public static int decodeItemTypeFromFlag(int flag) {
        return (flag & ShortcutInfo.FLAG_RESTORED_APP_TYPE) >> RESTORE_FLAG_BIT_SHIFT;
    }

}
