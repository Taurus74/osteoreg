package com.aconst.spinareg.model;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Client extends RealmObject {
    @PrimaryKey
    private int id;             // id клиента
    private int cardID;         // Номер карточки клиента. Первичный ключ - на сервере
    private String lastName;    // Фамилия
    private String firstName;   // Имя
    private String secondName;  // Отчество
    private String avatar;      // Фото
    private Date birthday;
    private String sex;         // Пол
    private String phone;
    private String email;
    private String city;
    private String street;
    private String station;
    private String building;
    private String flat;
    private String added;       // Дата добавления, формат "2018-01-01 10:20:30"
    private String complaint;   // Жалобы
    private String anamnesis;   // Анамнез
    @SerializedName("note")
    private String comment;
    private String documents;
    @SerializedName("groupID")
    private int group;          // Группа, в которую входит клиент (def = 1)

    public int getCardID() {
        return cardID;
    }

    public void setCardID(int cardID) {
        this.cardID = cardID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public String getFullName() {
        String name = (lastName == null? "": lastName);
        name += (name.length() == 0? "": " ") + (firstName == null? "": firstName);
        name += (name.length() == 0? "": " ") + (secondName == null? "": secondName);
        if (name.isEmpty())
            return "id = " + id;
        else
            return name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getFlat() {
        return flat;
    }

    public void setFlat(String flat) {
        this.flat = flat;
    }

    public String getAdded() {
        return added;
    }

    public void setAdded(String added) {
        this.added = added;
    }

    public String getComplaint() {
        return complaint;
    }

    public void setComplaint(String complaint) {
        this.complaint = complaint;
    }

    public String getAnamnesis() {
        return anamnesis;
    }

    public void setAnamnesis(String anamnesis) {
        this.anamnesis = anamnesis;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDocuments() {
        return documents;
    }

    public void setDocuments(String documents) {
        this.documents = documents;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    @Override
    public String toString() {
        return firstName + " " + lastName;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Client && id == ((Client) obj).id;
    }

    public void makeCall(Context context) {
        if (phone != null && !phone.isEmpty()) {
            String dial = "tel:" + getPhone();
            context.startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(dial)));
        }
    }
}
