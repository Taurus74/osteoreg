package com.aconst.spinareg.model;

import com.google.gson.annotations.SerializedName;

public class SessionResponce {
    private int id;
    @SerializedName("session")
    private int sessionId;  // id сеанса
    @SerializedName("sid")
    private int specId;     // id специалиста
    @SerializedName("cid")
    private int clientId;   // id клиента
    private String response;
    private String sentDate;
    private String answer;
    private String answerDate;
    private float 
}
