package com.aconst.spinareg.model;

import java.util.Date;

public class EmptyPeriod {
    private Date date;
    private int startTime;
    private int stopTime;

    public EmptyPeriod(Date date, int startTime, int stopTime) {
        this.date = date;
        this.startTime = startTime;
        this.stopTime = stopTime;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getStopTime() {
        return stopTime;
    }

    public void setStopTime(int stopTime) {
        this.stopTime = stopTime;
    }
}
