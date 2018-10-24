package com.aconst.spinareg.model;

import com.aconst.spinareg.CalendarHelper;
import com.aconst.spinareg.Common;
import com.aconst.spinareg.controllers.ServiceController;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

// Сеансы - отображение результата запроса по сеансам в локальный кэш
public class SessionItemRealm extends RealmObject {
    @PrimaryKey
    private int id;                 // ID записи
    @SerializedName("cid")
    private int clientId;           // ID клиента (0 если там перерыв)
    private int cardID;             // ID карточки клиента (сеанс может быть назначен как для самого
                                    // пользователя, так и для кого-то из его близких)
    @SerializedName("sid")
    private int specId;             // ID специалиста
    private String service;         // ID услуги (более одной – через запятую или 0 - перерыв)
    private Date sessionDate;       // Дата сеанса
    private String sessionTime;     // Назначенное время
    private int duration;           // Продолжительность в минутах
    private String clientComment;   // Комментарий клиента, виден только ему
    private String specComment;     // Комментарий специалиста, виден только ему
    private float cost;             // Стоимость сеанса, берется из описания услуги,
                                    // но может быть изменена вручную специалистом при редактировании
    private String report;          // Отчет
    private String files;           // Имена файлов с документами и фотографиями - через запятую
    private String description;     // Описание файлов с документами и фотографиями
    private String status;          // статус сеанса (enum: 'wait','complete', 'canceled', по умолчанию  – 'wait'
    private long googleId;          // Идентификатор события в Google calendar

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public int getCardID() {
        return cardID;
    }

    public void setCardID(int cardID) {
        this.cardID = cardID;
    }

    public int getSpecId() {
        return specId;
    }

    public void setSpecId(int specId) {
        this.specId = specId;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Date getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(Date sessionDate) {
        this.sessionDate = sessionDate;
    }

    public String getSessionTime() {
        return sessionTime;
    }

    public void setSessionTime(String sessionTime) {
        this.sessionTime = sessionTime;
    }

    public void setSessionTime(int sessionTime) {
        int hour = sessionTime / 60;
        setSessionTime(
                String.format(Locale.getDefault(), "%02d:%02d:00", hour, sessionTime - hour * 60));
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getStartTime() {
        if (sessionTime == null || sessionTime.isEmpty())
            return 0;
        else {
            String periods[] = sessionTime.split(":");
            return Integer.parseInt(periods[0]) * 60 + Integer.parseInt(periods[1]);
        }
    }

    public int getStopTime() {
        return getStartTime() + getDuration();
    }

    public String getClientComment() {
        return clientComment;
    }

    public void setClientComment(String clientComment) {
        this.clientComment = clientComment;
    }

    public String getSpecComment() {
        return specComment;
    }

    public void setSpecComment(String specComment) {
        this.specComment = specComment;
    }

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public String getFiles() {
        return files;
    }

    public void setFiles(String files) {
        this.files = files;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getGoogleId() {
        return googleId;
    }

    public void setGoogleId(long googleId) {
        this.googleId = googleId;
    }

    public String toString() {
        return "" + getId() + ": " + CalendarHelper.dateToString(getSessionDate(), Common.DATE_FORMAT)
                + " " + CalendarHelper.getMinutes(getStartTime(), getStopTime());
    }

    public List<ServiceItemRealm> getServices() {
        List<ServiceItemRealm> result = new LinkedList<>();
        if (service != null && !service.isEmpty()) {
            String[] data = service.split(",");
            ServiceController controller = new ServiceController();
            for (String s : data) {
                ServiceItemRealm serviceItemRealm =
                        controller.getService(Integer.parseInt(s)).blockingGet();
                result.add(serviceItemRealm);
            }
        }
        return result;
    }

    public String getServiceNames() {
        StringBuilder builder = new StringBuilder();
        String[] services = service.split(",");
        ServiceController controller = new ServiceController();
        for (String s : services) {
            if (!s.isEmpty()) {
                ServiceItemRealm service = controller.getService(Integer.parseInt(s.trim())).blockingGet();
                if (builder.length() == 0)
                    builder.append(service.getTitle());
                else {
                    builder.append(", ");
                    builder.append(service.getTitle().toLowerCase());
                }
            }
        }
        return builder.toString();
    }

}
