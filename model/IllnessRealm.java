package com.aconst.spinareg.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Illness extends RealmObject {
    @PrimaryKey
    private int illnID;
    private String aid;
    private String illName;

    public int getIllnID() {
        return illnID;
    }

    public void setIllnID(int illnID) {
        this.illnID = illnID;
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getIllName() {
        return illName;
    }

    public void setIllName(String illName) {
        this.illName = illName;
    }
}
