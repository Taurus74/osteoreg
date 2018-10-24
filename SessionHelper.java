package com.aconst.spinareg;

import android.content.Context;

import com.aconst.spinareg.PrefsHelper;
import com.aconst.spinareg.model.EventItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class SessionHelper {
    private int dayBegin;
    private int dayEnd;
    private int interval;

    public SessionHelper(Context context) {
        PrefsHelper prefsHelper = new PrefsHelper(context);
        dayBegin = prefsHelper.getPref("dayBegin", 8 * 60);
        dayEnd = prefsHelper.getPref("dayEnd", 24 * 60);
        interval = prefsHelper.getPref("interval", 15);
    }

    public int getDayBegin() {
        return dayBegin;
    }

    public int getDayEnd() {
        return dayEnd;
    }

    public int getInterval() {
        return interval;
    }

    public List<EventItem> getEventItemList(Date date) {
        ArrayList<EventItem> eventItems = new ArrayList<>();
        ArrayList<Integer> intervals = getIntervals(dayBegin, dayEnd, interval);
        for (int begin : intervals) {
            EventItem eventItem = new EventItem(date, begin, begin + interval,
                    null, null);
            eventItems.add(eventItem);
        }
        return eventItems;
    }

    public ArrayList<Integer> getIntervals() {
        return getIntervals(dayBegin, dayEnd, interval);
    }

    public ArrayList<Integer> getIntervals(int dayBegin, int dayEnd, int interval) {
        ArrayList<Integer> result = new ArrayList<>();
        int begin = dayBegin;
        while (begin < dayEnd) {
            result.add(begin);
            begin += interval;
        }
        return result;
    }
}
