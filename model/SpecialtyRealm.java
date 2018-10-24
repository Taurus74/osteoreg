package com.aconst.spinareg.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Specialty extends RealmObject {
    @PrimaryKey
    private int spcatID;
    private String name;

    public int getSpcatID() {
        return spcatID;
    }

    public void setSpcatID(int spcatID) {
        this.spcatID = spcatID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
