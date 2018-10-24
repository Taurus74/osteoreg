package com.aconst.spinareg;

import com.aconst.spinareg.model.WeekDay;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CalendarHelper {
    private static final int FIRST_DAY_OF_WEEK = Calendar.MONDAY;
    private static final long MSEC_OF_DAY = 86400000L;

    public static int[] getMonthArray(int mYear, int mMonth) {
        int[] result = new int[42];
        for (int i = 0; i < 42; i++)
            result[i] = 0;
        Calendar calendar = Calendar.getInstance();
        calendar.set(mYear, mMonth, 1, 0, 0, 0);
        calendar.setFirstDayOfWeek(FIRST_DAY_OF_WEEK);

        while (calendar.get(Calendar.DAY_OF_WEEK) != FIRST_DAY_OF_WEEK) {
            calendar.add(Calendar.DATE, -1);
        }
        if (calendar.get(Calendar.DAY_OF_MONTH) == 1)
            calendar.add(Calendar.DATE, -7);
        for (int i = 0; i < 42; i++) {
            result[i] = calendar.get(Calendar.DAY_OF_MONTH);
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        return result;
    }

    public static ArrayList<WeekDay> getWeekDays(int mYear, int mMonth, int mDay) {
        ArrayList<WeekDay> result = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(weekStart(mYear, mMonth, mDay));
        for (int i = 0; i < 7; i++) {
            WeekDay weekDay = new WeekDay(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH),
                    i);
            result.add(weekDay);
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        return result;
    }

    public static Date weekStart(int mYear, int mMonth, int mDay) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(mYear, mMonth, mDay, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return weekStart(calendar.getTime());
    }

    public static Date weekEnd(int mYear, int mMonth, int mDay) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(weekStart(mYear, mMonth, mDay));
        return weekEnd(calendar.getTime());
    }

    public static Date weekStart(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getDayStart(date));
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.setFirstDayOfWeek(FIRST_DAY_OF_WEEK);
        while (calendar.get(Calendar.DAY_OF_WEEK) != FIRST_DAY_OF_WEEK) {
            calendar.add(Calendar.DATE, -1);
        }
        return calendar.getTime();
    }

    public static Date weekEnd(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(weekStart(getDayEnd(date)));
        calendar.add(Calendar.DATE, 7);
        calendar.add(Calendar.SECOND, -1);
        return calendar.getTime();
    }

    public static Date monthStart(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTime();
    }

    public static Date monthEnd(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return calendar.getTime();
    }

    public static Date yearStart(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        return calendar.getTime();
    }

    public static Date yearEnd(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
        return calendar.getTime();
    }

    public static String dateToString(Date date, String format) {
        return dateToString(date.getTime(), format);
    }

    public static String dateToString(long timestamp, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        Date date = new Date();
        date.setTime(timestamp);
        return firstUpperCase(sdf.format(date));
    }

    private static String firstUpperCase(String s) {
        if (s == null || s.isEmpty())
            return "";
        else
            return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static long addMonth(long timestamp, int inc) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.add(Calendar.MONTH, inc);
        return calendar.getTimeInMillis();
    }

    public static long addDay(long timestamp, int inc) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp + 24*60*60*1000 * inc);
        return calendar.getTimeInMillis();
    }

    public static Date addDay(Date date, int inc) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH,inc);
        return calendar.getTime();
    }

    public static Date getDate(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return calendar.getTime();
    }

    public static long getDate(int mYear, int mMonth, int mDay) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(mYear, mMonth, mDay);
        return calendar.getTimeInMillis();
    }

    public static Date getDate(String s, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        try {
            return sdf.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean dateInPast(Date date, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime());
        calendar.set(Calendar.HOUR_OF_DAY, minute / 60);
        calendar.set(Calendar.MINUTE, minute - calendar.get(Calendar.HOUR_OF_DAY) * 60);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis() < Calendar.getInstance().getTimeInMillis();
    }

    public static int getMinute(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
    }

    public static String getMinute(int minute) {
        int hour = Math.round(minute / 60);
        return String.format(Locale.getDefault(), "%d:%02d", hour, minute - hour * 60);
    }

    public static String getMinuteWithSeconds(int minute) {
        int hour = Math.round(minute / 60);
        return String.format(Locale.getDefault(), "%d:%02d", hour, minute - hour * 60);
    }

    public static String getMinutes(int start, int stop) {
        int hourStart = Math.round(start / 60);
        int hourStop = Math.round(stop / 60);
        return String.format(Locale.getDefault(),
                "%d:%02d - %d:%02d",
                hourStart, start - hourStart * 60, hourStop, stop - hourStop * 60);
    }

    public static int getWeekDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(FIRST_DAY_OF_WEEK);
        calendar.setTime(date);
        int res = calendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
        return (res >= 0? res: res + 7);
    }

    public static Date getDayStart(Date day) {
        Date result = new Date();
        result.setTime((day.getTime() + getDiffFromGMT()) / MSEC_OF_DAY * MSEC_OF_DAY - getDiffFromGMT());
        return result;
    }

    public static Date getDayEnd(Date day) {
        Date result = new Date();
        result.setTime(((day.getTime() + getDiffFromGMT()) / MSEC_OF_DAY + 1) * MSEC_OF_DAY - getDiffFromGMT() - 1000);
        return result;
    }

    private static int getDiffFromGMT() {
        return getDiffFromGMT(TimeZone.getDefault());
    }

    private static int getDiffFromGMT(TimeZone tz) {
        return tz.getOffset(new Date().getTime());
    }

    public static boolean isToday(int mYear, int mMonth, int mDay) {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) == mYear
                && calendar.get(Calendar.MONTH) == mMonth
                && calendar.get(Calendar.DAY_OF_MONTH) == mDay;
    }

    public static boolean isToday(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return isToday(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
    }

    public static boolean isEqualDates(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
                && cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    public static boolean isYesterday(Date date) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);
        cal1.add(Calendar.DAY_OF_YEAR, 1);
        return isEqualDates(cal1.getTime(), Calendar.getInstance().getTime());
    }

    public static String getAge(Date date, Date now) {
        return Common.numberInCase(
                getAgeYears(date, now), new String[] {"Менее года", "год", "года", "лет"});
    }

    private static int getAgeYears(Date date, Date now) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(now);
        int diffYear = cal2.get(Calendar.YEAR) - cal1.get(Calendar.YEAR);
        int diffMonth = cal2.get(Calendar.MONTH) - cal1.get(Calendar.MONTH);
        int diffDay = cal2.get(Calendar.DAY_OF_MONTH) - cal1.get(Calendar.DAY_OF_MONTH);
        if (diffMonth == 0) {
            if (diffDay < 0)
                diffYear--;
        } else if (diffMonth < 0)
            diffYear--;
        return  (diffYear < 0? 0: diffYear);
    }

    public static int getDaysDiff(Date dateStart, Date dateStop) {
        Calendar calStart = Calendar.getInstance();
        calStart.setTime(getDayStart(dateStart));
        Calendar calStop = Calendar.getInstance();
        calStop.setTime(getDayEnd(dateStop));

        int daysDiff = 0;
        while (calStart.before(calStop)) {
            calStart.add(Calendar.DAY_OF_MONTH, 1);
            daysDiff++;
        }
        return daysDiff;
    }
}
