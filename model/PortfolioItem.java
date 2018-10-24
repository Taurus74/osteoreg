package com.aconst.spinareg.profile;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class PortfolioItem extends RealmObject {
    @PrimaryKey
    private int prtfID;         // Первичный ключ, ID записи
    private int specId;         // ID специалиста
    private String type;        // Тип записи (строка, содержимое – portfolio, certificate, education)
    private String description; // Описание
    private String photo;       // Фото/документ
    private String eventDate;   // Дата

    public int getPrtfID() {
        return prtfID;
    }

    public void setPrtfID(int prtfID) {
        this.prtfID = prtfID;
    }

    public int getSid() {
        return specId;
    }

    public void setSid(int specId) {
        this.specId = specId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }
}
