package com.aconst.spinareg.model;

import java.util.Arrays;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

// Услуги - отображение результата запроса по услугам в локальный кэш

public class ServiceItemRealm extends RealmObject {
    @PrimaryKey
    private int servID;         // Первичный ключ, ID записи
    private int specId;         // ID специалиста. При добавлении услуги до регистрации присваивается -1,
    // при загрузке с сервера в этом свойстве передается 0.
    // При создании профиля на сервер передаются все услуги (т.к. у них specId != 0)
    // При обновлении профиля на сервер передаются только услуги, у которых specId == -1
    // После регистрации запись услуг на сервер выполняется непосредственно из вкладки Услуги Профиля
    private String title;       // Название услуги
    private String description; // Описание
    private int duration;       // Продолжительность в минутах
    private float price;        // Стоимость
    private String currency;    // Валюта

    public int getServID() {
        return servID;
    }

    public void setServID(int servID) {
        this.servID = servID;
    }

    public int getSid() {
        return specId;
    }

    public void setSid(int specId) {
        this.specId = specId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public int getCurrencyCode(String[] currencies) {
        return Arrays.asList(currencies).indexOf(currency) + 1;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

}
