package com.aconst.spinareg.model;

import java.util.Date;
import java.util.List;

public class EventItem {
    private Date date;
    private int startTime;
    private int stopTime;
    private Client client;
    private List<ServiceItemRealm> serviceItem;

    public EventItem(Date date, int startTime, int stopTime, Client client,
                     List<ServiceItemRealm> serviceItem) {
        this.date = date;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.client = client;
        this.serviceItem = serviceItem;
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

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public List<ServiceItemRealm> getServiceItem() {
        return serviceItem;
    }

    public void setServiceItem(List<ServiceItemRealm> serviceItem) {
        this.serviceItem = serviceItem;
    }
}
