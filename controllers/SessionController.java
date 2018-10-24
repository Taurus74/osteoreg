package com.aconst.spinareg.controllers;

import com.aconst.spinareg.CalendarHelper;
import com.aconst.spinareg.model.SessionItemRealm;
import com.aconst.spinareg.model.Vacation;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.functions.Action;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;

public class SessionController {
    // Только рабочие сеансы - без перерывов и отпусков
    // Применяется для отчетов
    public final static int SESSION_WORKTIME = 0;
    // Рабочие сеансы и перерывы, без отпусков
    // Применяется в сетке календаря
    public final static int SESSION_WO_VACATIONS = 1;
    // Все сеансы - с перерывами и отпусками
    // Для проверки занятости интервалов времени
    public final static int SESSION_FULLTIME = 2;

    private RealmConfiguration realmConfiguration;

    public SessionController() {
        this.realmConfiguration = Realm.getDefaultConfiguration();
    }

    public Single<List<SessionItemRealm>> getSessions(boolean reverseOrder) {
        Realm realm = Realm.getInstance(realmConfiguration);
        List<SessionItemRealm> sessions;
        if (reverseOrder)
            sessions = realm.copyFromRealm(realm.where(SessionItemRealm.class)
                    .findAll().sort(
                            "sessionDate", Sort.DESCENDING,
                            "sessionTime", Sort.DESCENDING));
        else
            sessions = realm.copyFromRealm(realm.where(SessionItemRealm.class).findAll());
        realm.close();
        return Single.just(sessions);
    }

    public Single<Boolean> hasSessionsWithService(int servId) {
        Realm realm = Realm.getInstance(realmConfiguration);
        List<SessionItemRealm> sessions = realm.copyFromRealm(realm.where(SessionItemRealm.class)
                .contains("service", String.valueOf(servId))
                .findAll());
        boolean result = sessions.size() > 0;
        realm.close();
        return Single.just(result);
    }

    public Single<List<SessionItemRealm>> getClientSessions(int clientId, boolean reverseOrder) {
        Realm realm = Realm.getInstance(realmConfiguration);
        List<SessionItemRealm> sessions;
        if (reverseOrder)
            sessions = realm.copyFromRealm(realm.where(SessionItemRealm.class)
                    .equalTo("clientId", clientId)
                    .findAll().sort(
                            "sessionDate", Sort.DESCENDING,
                            "sessionTime", Sort.DESCENDING));
        else
            sessions = realm.copyFromRealm(realm.where(SessionItemRealm.class)
                    .equalTo("clientId", clientId)
                    .findAll());
        realm.close();
        return Single.just(sessions);
    }

    public Single<SessionItemRealm> getSession(int sessionId) {
        Realm realm = Realm.getInstance(realmConfiguration);
        SessionItemRealm result = realm.where(
                SessionItemRealm.class).equalTo("id", sessionId).findFirst();
        SessionItemRealm session;
        if (result == null) {
            session = new SessionItemRealm();
            session.setId(sessionId);
        }
        else
            session = realm.copyFromRealm(result);
        realm.close();
        return Single.just(session);
    }

    public Single<List<SessionItemRealm>> getDaySessions(Date date) {
        Date dateStart = CalendarHelper.getDayStart(date);
        Date dateEnd = CalendarHelper.getDayEnd(date);
        return getPeriodSessions(dateStart, dateEnd, SESSION_WO_VACATIONS);
    }

    public Single<List<SessionItemRealm>> getPeriodSessions(
            Date dateStart, Date dateEnd, int sessionType) {
        Realm realm = Realm.getInstance(realmConfiguration);
        RealmResults<SessionItemRealm> results;
        switch (sessionType) {
            case SESSION_WORKTIME :
                results = realm.where(SessionItemRealm.class)
                        .greaterThanOrEqualTo("sessionDate", dateStart)
                        .lessThanOrEqualTo("sessionDate", dateEnd)
                        .sort(
                                "sessionDate", Sort.ASCENDING,
                                "sessionTime", Sort.ASCENDING)
                        .notEqualTo("clientId", 0)
                        .notEqualTo("service", "0")
                        .findAll();
                break;
            case SESSION_WO_VACATIONS :
                results = realm.where(SessionItemRealm.class)
                        .greaterThanOrEqualTo("sessionDate", dateStart)
                        .lessThanOrEqualTo("sessionDate", dateEnd)
                        .sort(
                                "sessionDate", Sort.ASCENDING,
                                "sessionTime", Sort.ASCENDING)
                        .lessThan("duration", 24 * 60)
                        .findAll();
                break;
            case SESSION_FULLTIME :
            default:
                results = realm.where(SessionItemRealm.class)
                        .greaterThanOrEqualTo("sessionDate", dateStart)
                        .lessThanOrEqualTo("sessionDate", dateEnd)
                        .sort(
                                "sessionDate", Sort.ASCENDING,
                                "sessionTime", Sort.ASCENDING)
                        .findAll();
                break;
        }
        List<SessionItemRealm> sessions = realm.copyFromRealm(results);
        realm.close();
        return Single.just(sessions);
    }

    public Single<List<SessionItemRealm>> getFreeSessions() {
        Realm realm = Realm.getInstance(realmConfiguration);
        RealmResults<SessionItemRealm> results = realm.where(SessionItemRealm.class)
                .equalTo("clientId", 0)
                .lessThan("duration", 24 * 60)
                .sort("sessionDate", Sort.DESCENDING)
                .findAll();
        List<SessionItemRealm> sessions = realm.copyFromRealm(results);
        realm.close();
        return Single.just(sessions);
    }

    public Single<List<Vacation>> getVacations() {
        Realm realm = Realm.getInstance(realmConfiguration);
        RealmResults<SessionItemRealm> results = realm.where(SessionItemRealm.class)
                .equalTo("clientId", 0)
                .greaterThanOrEqualTo("duration", 24 * 60)
                .sort("sessionDate")
                .findAll();
        List<SessionItemRealm> sessions = realm.copyFromRealm(results);
        realm.close();

        List<Vacation> vacations = new LinkedList<>();
        for (SessionItemRealm session : sessions) {
            vacations.add(new Vacation(session.getId(),
                    CalendarHelper.getDayStart(session.getSessionDate()),
                    session.getDuration()));
        }

        return Single.just(vacations);
    }

    public Single<List<SessionItemRealm>> getVacation(Vacation vacation) {
        Realm realm = Realm.getInstance(realmConfiguration);
        RealmResults<SessionItemRealm> results = realm.where(SessionItemRealm.class)
                .equalTo("clientId", 0)
                .greaterThanOrEqualTo("duration", 24 * 60)
                .greaterThanOrEqualTo("sessionDate", vacation.getDateFrom())
                .lessThanOrEqualTo("sessionDate", vacation.getDateTo())
                .findAll();
        List<SessionItemRealm> sessions = realm.copyFromRealm(results);
        realm.close();
        return Single.just(sessions);
    }

    public Single<Integer> getSessionCount(boolean withoutFree) {
        Realm realm = Realm.getInstance(realmConfiguration);
        RealmResults<SessionItemRealm> results;
        if (withoutFree)
            results = realm.where(SessionItemRealm.class)
                    .notEqualTo("clientId", 0)
                    .notEqualTo("service", "0")
                .findAll();
        else
            results = realm.where(SessionItemRealm.class)
                    .findAll();
        int result = results.size();
        realm.close();
        return Single.just(result);
    }

    public Completable updateSessions(final List<SessionItemRealm> sessionItemRealms) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(realmConfiguration);
                realm.beginTransaction();
                realm.insertOrUpdate(sessionItemRealms);
                realm.commitTransaction();
                realm.close();
                Realm.compactRealm(realmConfiguration);
            }
        });
    }

    public Completable updateSession(final SessionItemRealm sessionItemRealm) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(realmConfiguration);
                realm.beginTransaction();
                realm.insertOrUpdate(sessionItemRealm);
                realm.commitTransaction();
                realm.close();
                Realm.compactRealm(realmConfiguration);
            }
        });
    }

    public Completable updateFilenames(final int sessionId, final String filenames) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(realmConfiguration);
                realm.beginTransaction();
                SessionItemRealm itemRealm = getSession(sessionId).blockingGet();
                itemRealm.setFiles(filenames);
                realm.insertOrUpdate(itemRealm);
                realm.commitTransaction();
                realm.close();
                Realm.compactRealm(realmConfiguration);
            }
        });
    }

    public Completable deleteSessions() {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(realmConfiguration);
                RealmResults<SessionItemRealm> sessionItemRealms =
                        realm.where(SessionItemRealm.class).findAll();
                realm.beginTransaction();
                sessionItemRealms.deleteAllFromRealm();
                realm.commitTransaction();
                realm.close();
                Realm.compactRealm(realmConfiguration);
            }
        });
    }

    public Completable deleteSession(final int sessionId) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(realmConfiguration);
                SessionItemRealm sessionItemRealms =
                        realm.where(SessionItemRealm.class).equalTo("id", sessionId)
                                .findFirst();
                if (sessionItemRealms != null) {
                    realm.beginTransaction();
                    sessionItemRealms.deleteFromRealm();
                    realm.commitTransaction();
                }
                realm.close();
                Realm.compactRealm(realmConfiguration);
            }
        });
    }
}
