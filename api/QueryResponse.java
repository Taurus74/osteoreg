package com.aconst.spinareg.api;

public class QueryResponse {
    private String status;
    private String code;
    private String description;
    private String access_token;
    private String id;
    private String files;
    private int prtfID;
    private int servID;
    private int specID;
    private int spzID;

    public String getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getToken() {
        return access_token;
    }

    public String getAccess_token() {
        return access_token;
    }

    public String getId() {
        return id;
    }

    public String getFiles() {
        return files;
    }

    public int getPrtfID() {
        return prtfID;
    }

    public int getServID() {
        return servID;
    }

    public int getSpecID() {
        return specID;
    }

    public int getSpzID() {
        return spzID;
    }
}
