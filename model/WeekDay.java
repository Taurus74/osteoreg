package com.aconst.spinareg.model;

public class WeekDay {
    private int year;
    private int month;
    private int day;
    private int weekDay;

    public WeekDay(int year, int month, int day, int weekDay) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.weekDay = weekDay;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getWeekDay() {
        return weekDay;
    }

    public void setWeekDay(int weekDay) {
        this.weekDay = weekDay;
    }
}
