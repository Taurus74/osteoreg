package com.aconst.spinareg.model;

import com.google.gson.annotations.SerializedName;

import io.realm.annotations.PrimaryKey;

public class Specialties {
    @PrimaryKey
    private int specID;         // Первичный ключ, ID записи
    @SerializedName("sid")
    private int specId;         // ID специалиста
    private int code;           // ID (код специальности)
    private boolean verified;   // Специальность подтверждена – необходимо для «Остеопракта»

    public int getSpecID() {
        return specID;
    }

    public void setSpecID(int specID) {
        this.specID = specID;
    }

    public int getSpecId() {
        return specId;
    }

    public void setSpecId(int specId) {
        this.specId = specId;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}
