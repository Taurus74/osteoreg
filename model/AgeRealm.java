package com.aconst.spinareg.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Age extends RealmObject {
    @PrimaryKey
    private int ageId;
    private String age;

    public int getAgeId() {
        return ageId;
    }

    public void setAgeId(int ageId) {
        this.ageId = ageId;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }
}
