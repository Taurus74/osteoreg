package com.aconst.calendarwidget.calendar;

// https://developer.android.com/guide/topics/providers/calendar-provider.html?hl=ru

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.util.LongSparseArray;

public class Calendars {
    // Projection array. Creating indices for this array instead of doing
    // dynamic lookups improves performance.
    private static final String[] CALENDAR_PROJECTION = new String[]{
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 1
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 2
            CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
    };

    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_DISPLAY_NAME_INDEX = 1;
    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 2;
    private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;

    private Cursor cur = null;

    public Calendars(Context context) {
        // Run query
        ContentResolver cr = context.getContentResolver();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;

        // Submit the query and get a Cursor object back.
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        cur = cr.query(uri, CALENDAR_PROJECTION, null, null, null);
    }

    public LongSparseArray<String> getCalendars() {
        return getCalendars(PROJECTION_DISPLAY_NAME_INDEX);
    }

    private LongSparseArray<String> getCalendars(int index) {
        LongSparseArray<String> result = new LongSparseArray<>();

        // Допустимые индексы
        if (index != PROJECTION_DISPLAY_NAME_INDEX
                && index != PROJECTION_ACCOUNT_NAME_INDEX
                && index != PROJECTION_OWNER_ACCOUNT_INDEX)
            index = PROJECTION_DISPLAY_NAME_INDEX;

        // Use the cursor to step through the returned records
        if (cur != null) {
            while (cur.moveToNext()) {
                long calID = cur.getLong(PROJECTION_ID_INDEX);
                String displayName = cur.getString(index);

                result.append(calID, displayName);
            }
            cur.close();
        }
        return result;
    }

}