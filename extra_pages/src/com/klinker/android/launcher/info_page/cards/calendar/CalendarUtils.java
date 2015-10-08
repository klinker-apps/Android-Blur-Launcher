package com.klinker.android.launcher.info_page.cards.calendar;

import android.content.Context;
import android.database.Cursor;
import android.provider.CalendarContract;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

public class CalendarUtils {

    public interface EventsQuery {
        String[] PROJECTION = {
                CalendarContract.Instances.EVENT_ID,
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.END,
                CalendarContract.Instances.TITLE,
                CalendarContract.Instances.EVENT_LOCATION,
                CalendarContract.Instances.ALL_DAY,
        };

        int EVENT_ID = 0;
        int BEGIN = 1;
        int END = 2;
        int TITLE = 3;
        int EVENT_LOCATION = 4;
        int ALL_DAY = 5;
    }

    private interface CalendarsQuery {
        String[] PROJECTION = {
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.VISIBLE,
        };

        int ID = 0;
        int VISIBLE = 1;
    }

    public static List<Pair<String, Boolean>> getAllCalendars(Context context) {
        // Only return calendars that are marked as synced to device.
        // (This is different from the display flag)
        List<Pair<String, Boolean>> calendars = new ArrayList<Pair<String, Boolean>>();

        try {
            Cursor cursor = context.getContentResolver().query(
                    CalendarContract.Calendars.CONTENT_URI,
                    CalendarsQuery.PROJECTION,
                    CalendarContract.Calendars.SYNC_EVENTS + "=1",
                    null,
                    null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    calendars.add(new Pair<String, Boolean>(
                            cursor.getString(CalendarsQuery.ID),
                            cursor.getInt(CalendarsQuery.VISIBLE) == 1));

                }

                cursor.close();
            }

        } catch (SecurityException e) {
            Log.v("launcher_calendar", e.getMessage());
            return null;
        }

        return calendars;
    }

    public static long getCurrentTimestamp() {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();
    }

    private static final String SQL_TAUTOLOGY = "1=1";

    public static final long MILLIS_NANOS = 1000000l; // 1 ms = 1,000,000 nanos
    public static final int SECONDS_MILLIS = 1000; // 1 second is 1000 ms
    public static final int MINUTES_MILLIS = 60 * SECONDS_MILLIS; // 1 minute = 60 sec
    public static final int HOURS_MILLIS = 60 * MINUTES_MILLIS; // 1 hour = 60 min

    public static final long NOW_BUFFER_TIME_MILLIS = 3 * MINUTES_MILLIS;

    public static Cursor tryOpenEventsCursor(Context context, int lookAheadHours) {

        boolean showAllDay = true;

        // Filter out all day events unless the user expressly requested to show all day events
        String allDaySelection = SQL_TAUTOLOGY;
        if (!showAllDay) {
            allDaySelection = CalendarContract.Instances.ALL_DAY + "=0";
        }

        // Only filter on visible calendars if there isn't custom visibility
        String visibleCalendarsSelection = SQL_TAUTOLOGY;
        allDaySelection = CalendarContract.Instances.VISIBLE + "!=0";

        String calendarSelection = generateCalendarSelection(context);
        Set<String> calendarSet = getSelectedCalendars(context);
        String[] calendarsSelectionArgs = calendarSet.toArray(new String[calendarSet.size()]);

        long now = getCurrentTimestamp();

        try {
            return context.getContentResolver().query(
                    CalendarContract.Instances.CONTENT_URI.buildUpon()
                            .appendPath(Long.toString(now - NOW_BUFFER_TIME_MILLIS))
                            .appendPath(Long.toString(now + lookAheadHours * HOURS_MILLIS))
                            .build(),
                    EventsQuery.PROJECTION,
                    allDaySelection + " AND "
                            + CalendarContract.Instances.SELF_ATTENDEE_STATUS + "!="
                            + CalendarContract.Attendees.ATTENDEE_STATUS_DECLINED + " AND "
                            + "IFNULL(" + CalendarContract.Instances.STATUS + ",0)!="
                            + CalendarContract.Instances.STATUS_CANCELED + " AND "
                            + visibleCalendarsSelection + " AND ("
                            + calendarSelection + ")",
                    calendarsSelectionArgs,
                    CalendarContract.Instances.BEGIN);

        } catch (Exception e) {
            Log.v("launcher_calendar", e.getMessage());
            return null;
        }
    }

    public static String generateCalendarSelection(Context context) {
        Set<String> calendars = getSelectedCalendars(context);
        int count = calendars.size();

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < count; i++) {
            if (i != 0) {
                sb.append(" OR ");
            }

            sb.append(CalendarContract.Events.CALENDAR_ID);
            sb.append(" = ?");
        }

        if (sb.length() == 0) {
            sb.append(SQL_TAUTOLOGY); // constant expression to prevent returning null
        }

        return sb.toString();
    }


    public static Set<String> getSelectedCalendars(Context context) {

        Set<String> selectedCalendars = null;
        final List<Pair<String, Boolean>> allCalendars = getAllCalendars(context);

        // Build a set of all visible calendars in case we don't have a selection set in
        // the preferences.
        selectedCalendars = new HashSet<String>();
        for (Pair<String, Boolean> pair : allCalendars) {
            if (pair.second) {
                selectedCalendars.add(pair.first);
            }
        }

        return selectedCalendars;
    }


}
