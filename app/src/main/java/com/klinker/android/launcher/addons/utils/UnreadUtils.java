package com.klinker.android.launcher.addons.utils;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class UnreadUtils {

    /**
     * Get the unread count for the given shortcut info
     * @param c cursor holding the unread counts
     * @param packageName the package we are looking for
     * @return int of the unread count
     */
    public static int getUnreadCount(Cursor c, String packageName) {
        int count = 0;

        try {
            if (c.moveToFirst()) {
                String currName = null;
                do {
                    currName = c.getString(c.getColumnIndex("package_name"));
                    if (currName.equals(packageName)) {
                        count = c.getInt(c.getColumnIndex("count"));
                    }
                } while (c.moveToNext());
            }
        } catch (Exception e) {

        }

        return count;
    }

    static final String PROVIDER_NAME = "com.klinker.android.provider.blur_unread";
    static final String URL = "content://" + PROVIDER_NAME + "/counts";
    public static final Uri UNREAD_URI = Uri.parse(URL);

    // throws a security exception with "no permission to query"
    public static Cursor getBlurUnread(Activity activity) {
        // Run query
        String[] projection = new String[] { "package_name", "count" };
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = null;

        return activity.getContentResolver().query(UNREAD_URI, projection, selection, selectionArgs, sortOrder);
    }
}
