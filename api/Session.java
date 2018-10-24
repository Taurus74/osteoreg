package com.aconst.spinareg.api;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.aconst.spinareg.CalendarHelper;
import com.aconst.spinareg.Common;
import com.aconst.spinareg.PrefsHelper;
import com.aconst.spinareg.controllers.SessionController;
import com.aconst.spinareg.model.SessionItemRealm;
import com.aconst.spinareg.model.Vacation;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.Date;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class Session {
    private static final String TAG = "session";
    private static final String BASE_URL = Common.BASE_URL;
    private SessionAPI sessionAPI;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Context context;

    public Session(Context context) {
        this.context = context;

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        Gson gson = new GsonBuilder()
                .setLenient()
                .setDateFormat(Common.DATE_FORMAT)
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();

        sessionAPI = retrofit.create(SessionAPI.class);
    }

    public void newSession(String token, int clientId, int cardID, int specId, String service,
                           String sessionDate, String sessionTime, int duration,
                           String comment, float cost, List<String> files) {
        if (periodAvailable(sessionDate, sessionTime, duration, 0))
            compositeDisposable.add(sessionAPI.newSession(token, clientId, cardID, specId, service,
                    sessionDate, sessionTime, duration, comment, cost, null)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(newSessionObserver(token, files)));
    }

    private DisposableSingleObserver<QueryResponse> newSessionObserver(
            final String token, final List<String> files) {
        return new DisposableSingleObserver<QueryResponse>() {
            @Override
            public void onSuccess(QueryResponse queryResponse) {
                String status = queryResponse.getStatus();
                if (status.equalsIgnoreCase("ok")) {
                    int sessionId = Integer.parseInt(queryResponse.getId());
                    saveFiles(token, sessionId, files);
                    Log.d(TAG, "newSession id = " + sessionId);

                    getSessionById(token, sessionId);

                } else if (status.equalsIgnoreCase("error")) {
                    Log.d(TAG, "newSession error " + queryResponse.getDescription());
                    Toast.makeText(context, "Ошибка создания сеанса", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.toString());
                Toast.makeText(context, "Ошибка создания сеанса", Toast.LENGTH_SHORT).show();
            }
        };
    }

    public void getSessions(String token, int from, int count) {
        compositeDisposable.add(sessionAPI.getSessions(token, from, count)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getSessionsObserver(token, from, count)));
    }

    private DisposableSingleObserver<List<SessionItemRealm>> getSessionsObserver(
            final String token, final int from, final int count) {
        return new DisposableSingleObserver<List<SessionItemRealm>>() {
            @Override
            public void onSuccess(List<SessionItemRealm> queryResponse) {
                if (queryResponse.size() > 0) {
                    new SessionController().updateSessions(queryResponse).subscribe();

                    GetImages getImages = new GetImages(context);
                    for (SessionItemRealm itemRealm : queryResponse)
                        getImages.getSessionImages(itemRealm.getFiles());

                    if (queryResponse.size() == count) {
                        // Рекурсивный вызов для загрузки сеансов
                        getSessions(token, from + count, count);

                    } else {
                        // Оповестить о завершении загрузки
                        updateUI();
                    }

                } else {
                    // Оповестить о завершении загрузки
                    updateUI();
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "Sessions error (getSessions): " + e.toString());

                // Оповестить о завершении загрузки
                updateUI();
            }
        };
    }

    public void getSessionsInterval(String token, String from, String to) {
        compositeDisposable.add(sessionAPI.getSessionsInterval(token, from, to)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getSessionsIntervalObserver()));
    }

    private DisposableSingleObserver<List<SessionItemRealm>> getSessionsIntervalObserver() {
        return new DisposableSingleObserver<List<SessionItemRealm>>() {
            @Override
            public void onSuccess(List<SessionItemRealm> queryResponse) {
                if (queryResponse.size() > 0) {
                    new SessionController().updateSessions(queryResponse).subscribe();

                    GetImages getImages = new GetImages(context);
                    for (SessionItemRealm itemRealm : queryResponse)
                        getImages.getSessionImages(itemRealm.getFiles());
                }
                // Оповестить о завершении загрузки
                updateUI();
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "Sessions error (getSessionsInterval): " + e.toString());

                // Оповестить о завершении загрузки
                updateUI();
            }
        };
    }


    public void getSessionById(String token, int sessionId) {
        compositeDisposable.add(sessionAPI.getSessionById(token, sessionId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getSessionByIdObserver()));
    }

    private DisposableSingleObserver<SessionItemRealm> getSessionByIdObserver() {
        return new DisposableSingleObserver<SessionItemRealm>() {
            @Override
            public void onSuccess(SessionItemRealm itemRealm) {
                new SessionController().updateSession(itemRealm).subscribe();
                updateUI();
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.toString());
            }
        };
    }

    public void updSession(String token, int id, int clientId, int cardID, int specId,
                           String service, String sessionDate, String sessionTime, int duration,
                           String specComment, float cost, String status, List<String> files) {
        if (periodAvailable(sessionDate, sessionTime, duration, id))
            compositeDisposable.add(sessionAPI.updSession(token, id, clientId, cardID, specId, service,
                    sessionDate, sessionTime, duration, specComment, cost, status)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(updSessionObserver(token, id, files)));
    }

    private DisposableSingleObserver<QueryResponse> updSessionObserver(
            final String token, final int sessionId, final List<String> files) {
        return new DisposableSingleObserver<QueryResponse>() {
            @Override
            public void onSuccess(QueryResponse queryResponse) {
                String status = queryResponse.getStatus();
                if (status.equalsIgnoreCase("ok")) {
                    saveFiles(token, sessionId, files);
                    Log.d(TAG, "updSession id = " + sessionId);

                    getSessionById(token, sessionId);
                    updateUI();

                } else if (status.equalsIgnoreCase("error")) {
                    Log.d(TAG, "updSession error " + queryResponse.getDescription());
                    Toast.makeText(context, "Ошибка редактирования сеанса",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.toString());
            }
        };
    }

    public void copySession(String token, int id, String dates[]) {
        compositeDisposable.add(sessionAPI.copySession(token, id, dates)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(copySessionObserver()));
    }

    private DisposableSingleObserver<QueryResponse> copySessionObserver() {
        return new DisposableSingleObserver<QueryResponse>() {
            @Override
            public void onSuccess(QueryResponse queryResponse) {

            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.toString());
            }
        };
    }

    public void changeStatus(String token, int id, String status) {
        compositeDisposable.add(sessionAPI.changeStatus(token, id, status)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(changeStatusObserver()));

    }

    private DisposableSingleObserver<QueryResponse> changeStatusObserver() {
        return new DisposableSingleObserver<QueryResponse>() {
            @Override
            public void onSuccess(QueryResponse queryResponse) {

            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.toString());
            }
        };
    }

    public void delSession(String token, int sessionId, boolean updateUI) {
        compositeDisposable.add(sessionAPI.delSession(token, sessionId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(delSessionObserver(sessionId, updateUI)));

    }

    private DisposableSingleObserver<QueryResponse> delSessionObserver(
            final int sessionId, final boolean updateUI) {
        return new DisposableSingleObserver<QueryResponse>() {
            @Override
            public void onSuccess(QueryResponse queryResponse) {
                new SessionController().deleteSession(sessionId).subscribe();
                if (updateUI)
                    updateUI();
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.toString());
            }
        };
    }

    private void saveFiles(String token, int sessionId, List<String> files) {
        if (files != null)
            for (int i = 0; i < files.size(); i++)
                // ToDo - description
                uploadFile(token, sessionId, files.get(i), "-");
    }

    private void uploadFile(String token, int sessionId, String fileUpload, String description) {
        if (!fileUpload.isEmpty()) {
            File file = new File(fileUpload);
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            MultipartBody.Part body =
                    MultipartBody.Part.createFormData("fileUpload", fileUpload, requestFile);
            RequestBody descr = RequestBody.create(MediaType.parse("multipart/form-data"), description);

            compositeDisposable.add(sessionAPI.uploadFile(token, sessionId, body, descr)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(uploadFileObserver(sessionId, fileUpload)));
        }
    }

    private DisposableSingleObserver<QueryResponse> uploadFileObserver(
            final int sessionId, final String filenameSrc) {
        return new DisposableSingleObserver<QueryResponse>() {
            @Override
            public void onSuccess(QueryResponse queryResponse) {
                String status = queryResponse.getStatus();
                if (status.equalsIgnoreCase("ok")) {
                    String filenames = queryResponse.getFiles();
                    String cacheDir = context.getCacheDir().getPath() + "/";
                    new SessionController().updateFilenames(sessionId, filenames).subscribe();
                    if (renameFile(filenameSrc, filenames, cacheDir))
                        Log.d(TAG, "File " + filenames + " uploaded successfully");
                    else
                        Log.d(TAG, "File " + filenames + " uploaded, but not renamed");

                } else if (status.equalsIgnoreCase("error")) {
                    String description = queryResponse.getDescription();
                    if (description.contains("empty")) {
                        description = "Поле " + description.replace("empty", "пустое");
                    }
                    Log.d(TAG, "Error: " + description);
                    Toast.makeText(context, description, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.toString());
            }
        };
    }

    private boolean renameFile(String oldName, String newNames, String dir) {
        File from = new File(oldName);
        String[] newNameList = newNames.split(",");
        File to = new File(dir + newNameList[newNameList.length - 1]);
        return from.renameTo(to);
    }

    private void replaceFile(String token, int id, String fileUpload, String description) {
        File file = new File(fileUpload);
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body =
                MultipartBody.Part.createFormData(fileUpload, fileUpload, requestFile);

        compositeDisposable.add(sessionAPI.replaceFile(token, id, body, description)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(replaceFileObserver()));

    }

    private DisposableSingleObserver<QueryResponse> replaceFileObserver() {
        return new DisposableSingleObserver<QueryResponse>() {
            @Override
            public void onSuccess(QueryResponse queryResponse) {

            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.toString());
            }
        };
    }

    public void deleteFile(String token, int id, int fileID) {
        compositeDisposable.add(sessionAPI.deleteFile(token, id, fileID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(deleteFileObserver()));
    }

    private DisposableSingleObserver<QueryResponse> deleteFileObserver() {
        return new DisposableSingleObserver<QueryResponse>() {
            @Override
            public void onSuccess(QueryResponse queryResponse) {

            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.toString());
            }
        };
    }

    public void addVacation(Vacation vacation) {
        PrefsHelper prefsHelper = new PrefsHelper(context);
        String token = prefsHelper.getPref("token");
        int specId = prefsHelper.getPref("specId", 0);
        Date date = CalendarHelper.getDayStart(vacation.getDateFrom());

        if (periodAvailable(date, 0, vacation.getNumOfDays() * 24 * 60, 0))
            newSession(token, 0, 1, specId, "0",
                    CalendarHelper.dateToString(date, "yyyy-MM-dd"),
                    "0:00:00", vacation.getNumOfDays() * 24 * 60,
                    "Отпуск", 0, null);
    }

    public void editVacation(Vacation vacation) {
        PrefsHelper prefsHelper = new PrefsHelper(context);
        String token = prefsHelper.getPref("token");
        int specId = prefsHelper.getPref("specId", 0);
        Date date = CalendarHelper.getDayStart(vacation.getDateFrom());

        if (periodAvailable(
                date, 0, vacation.getNumOfDays() * 24 * 60, vacation.getSessionId()))
            updSession(token, vacation.getSessionId(), 0, 1, specId, "0",
                    CalendarHelper.dateToString(date, "yyyy-MM-dd"),
                    "0:00:00", vacation.getNumOfDays() * 24 * 60,
                    "Отпуск", 0, "wait", null);
    }

    public void dispose() {
        if (compositeDisposable != null && !compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
    }

    private void updateUI() {
        Intent intent = new Intent();
        intent.setAction("com.aconst.spinareg.updateSession");
        context.sendBroadcast(intent);
    }

    // Проверить занятость времени сеанса другими сеансами за этот день
    private boolean periodAvailable(String sDate, String sTime, int duration, int sessionId) {
        Date date = CalendarHelper.getDate(sDate, Common.DATE_FORMAT);
        String[] s = sTime.split(":");
        int time = Integer.parseInt(s[0]) * 60 + Integer.parseInt(s[1]);
        return periodAvailable(date, time, duration, sessionId);
    }

    // Проверить занятость времени сеанса другими сеансами за этот день
    private boolean periodAvailable(Date date, int time, int duration, int sessionId) {
        Date dateStop = CalendarHelper.addDay(
                CalendarHelper.getDayEnd(date), (duration - 1) / 24 / 60);
        List<SessionItemRealm> itemRealmList = new SessionController()
                .getPeriodSessions(date, dateStop, SessionController.SESSION_FULLTIME).blockingGet();
        for (SessionItemRealm itemRealm : itemRealmList) {
            if (itemRealm.getId() != sessionId) {
                // Если начало нового сеанса попадает на существующий сеанс:
                if (time >= itemRealm.getStartTime() && time < itemRealm.getStopTime()
                        // или окончание нового сеанса попадает на существующий сеанс:
                        || time + duration > itemRealm.getStartTime()
                        && time + duration <= itemRealm.getStopTime()
                        // или новый сеанс перекрывает существующий сеанс:
                        || time <= itemRealm.getStartTime()
                        && time + duration >= itemRealm.getStopTime()) {
                    // то есть пересечение
                    String message = "Невозможно добавить событие на "
                            + CalendarHelper.dateToString(date, Common.DATE_FORMAT_VIEW)
                            + ", время " + CalendarHelper.getMinutes(time, time + duration)
                            + ", т.к. на это время назначено другое событие";
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }
        return true;
    }
}
