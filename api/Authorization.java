package com.aconst.spinareg.api;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.aconst.spinareg.Common;
import com.aconst.spinareg.PrefsHelper;
import com.aconst.spinareg.StartActivity;
import com.aconst.spinareg.WelcomeActivity;
import com.aconst.spinareg.authorization.RegisterActivity;
import com.aconst.spinareg.calendar.CalendarActivity;
import com.aconst.spinareg.controllers.ClientsController;
import com.aconst.spinareg.controllers.ServiceController;
import com.aconst.spinareg.controllers.SessionController;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class Authorization {
    private static final String TAG = "authorization";
    private static final String BASE_URL = Common.BASE_URL;
    private AuthorizationAPI authorizationAPI;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Context context;

    private String phone = "";
    private String password = "";

    public interface SaveProfile {
        void saveProfile(String token, boolean isNew, boolean avatarUpdated);
    }
    private SaveProfile saveProfile;

    public Authorization(Context context) {
        this.context = context;
        setAuthorizationAPI();
    }

    public Authorization(Context context, SaveProfile saveProfile) {
        this.context = context;
        this.saveProfile = saveProfile;
        setAuthorizationAPI();
    }

    private void setAuthorizationAPI() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();

        authorizationAPI = retrofit.create(AuthorizationAPI.class);
    }

    public void registration(String phone, String email, String password, String password2) {
        this.phone = phone;
        this.password = password;

        compositeDisposable.add(authorizationAPI.registration(authorizationAPI.appId,
                phone, email, password, password2)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(registrationObserver(phone)));
    }

    private DisposableSingleObserver<QueryResponse> registrationObserver(final String phone) {
        return new DisposableSingleObserver<QueryResponse>() {
            @Override
            public void onSuccess(QueryResponse response) {
                String status = response.getStatus();
                if (status.equalsIgnoreCase("ok")) {
                    Log.d(TAG, "Registration status: " + status);
                    getPhoneVerifyCode(phone);

                } else if (status.equalsIgnoreCase("error")) {
                    String description = response.getDescription();
                    Log.e(TAG, status + ": " + description);
                    switch (description) {
                        case "phone incorrect" :
                            Toast.makeText(context, "Неправильный номер телефона: " + phone,
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case "password mismatch" :
                            Toast.makeText(context, "Registration error: " + description,
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case "user already exist" :
                            getPhoneVerifyCode(phone);
                            break;
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "Registration error: " + e.toString());
                if (e instanceof HttpException) {
                    int code = ((HttpException) e).code();
                    if (code == 403)
                        Toast.makeText(context, "Неверный id приложения", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            }
        };
    }

    public void authentication(String phone, String password, String pushToken) {
        compositeDisposable.add(authorizationAPI.authentication(authorizationAPI.appId,
                phone, password, pushToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(authenticationObserver()));
    }

    private DisposableSingleObserver<QueryResponse> authenticationObserver() {
        return new DisposableSingleObserver<QueryResponse>() {
            @Override
            public void onSuccess(QueryResponse response) {
                String status = response.getStatus();
                Log.d(TAG, "Authentication status: " + status);
                if (status.equalsIgnoreCase("ok")) {
                    String token = response.getToken();
                    Log.d(TAG, "Token: " + token);

                    PrefsHelper prefsHelper = new PrefsHelper(context);
                    prefsHelper.setPref("token", token);

                    // Получить/обновить данные личной карточки, в т.ч. specId
                    getCard(token);

                    if (phone.isEmpty() && password.isEmpty()) {
                        // Норм. вход
                        Intent intent = new Intent(context, CalendarActivity.class);
                        context.startActivity(intent);

                    } else {
                        // Попытка входа после неудачной регистрации
                        phone = "";
                        password = "";
                        Intent intent = new Intent(context, WelcomeActivity.class);
                        context.startActivity(intent);
                    }

                } else if (status.equalsIgnoreCase("error")) {
                    String description = response.getDescription();
                    Log.e(TAG, description);
                    if (description.equalsIgnoreCase("not verified")) {
                        Toast.makeText(context, "Error: " + description, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "Authentication error: " + e.toString());
                if (e instanceof HttpException) {
                    int code = ((HttpException) e).code();
                    switch (code) {
                        case 401:
                            Toast.makeText(context, "Неверный логин/пароль", Toast.LENGTH_SHORT).show();
                            break;
                        case 403:
                            Toast.makeText(context, "Неверный id приложения", Toast.LENGTH_SHORT).show();
                            break;
                        case 404:
                            Toast.makeText(context, "Неверные учетные данные", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Log.e(TAG, e.toString());
                            break;
                    }
                }
                else
                    Log.e(TAG, e.toString());

                Intent intent = new Intent(context, StartActivity.class);
                context.startActivity(intent);
            }
        };
    }

    public void getPhoneVerifyCode(String phoneNumber) {
        compositeDisposable.add(authorizationAPI.getPhoneVerifyCode(authorizationAPI.appId, phoneNumber)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getPhoneVerifyCodeObserver()));
    }

    private DisposableSingleObserver<QueryResponse> getPhoneVerifyCodeObserver() {
        return new DisposableSingleObserver<QueryResponse>() {
            @Override
            public void onSuccess(QueryResponse response) {
                String status = response.getStatus();
                Log.d(TAG, "Verification status: " + status);
                if (status.equalsIgnoreCase("ok")) {
                    String code = response.getCode();
                    Log.d(TAG, "Verification code: " + code);

                    checkPhoneVerifyCode(code);

                } else if (status.equalsIgnoreCase("error")) {
                    String description = response.getDescription();
                    Log.e(TAG, status + ": " + description);

                    if (!phone.isEmpty() && !password.isEmpty()) {
                        PrefsHelper prefsHelper = new PrefsHelper(context);
                        authentication(phone, password, prefsHelper.getPref("pushToken"));

                    } else
                        Toast.makeText(context, "Ошибка регистрации", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "Verification error: " + e.toString());
                if (e instanceof HttpException) {
                    int code = ((HttpException) e).code();
                    switch (code) {
                        case 403:
                            Toast.makeText(context, "Неверный id приложения", Toast.LENGTH_SHORT).show();
                            break;
                        case 404:
                            Toast.makeText(context, "Номер телефона не найден или уже прошел верификацию",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
                else
                    Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            }
        };
    }

    public void checkPhoneVerifyCode(String code) {
        compositeDisposable.add(authorizationAPI.checkPhoneVerifyCode(authorizationAPI.appId, code)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getCheckPhoneVerifyCode()));
    }

    private DisposableSingleObserver<QueryResponse> getCheckPhoneVerifyCode() {
        return new DisposableSingleObserver<QueryResponse>() {
            @Override
            public void onSuccess(QueryResponse response) {
                String status = response.getStatus();
                Log.d(TAG, "CheckPhoneVerifyCode status: " + status);
                if (status.equalsIgnoreCase("ok")) {
                    String token = response.getToken();
                    Log.d(TAG, "Token: " + token);

                    PrefsHelper prefsHelper = new PrefsHelper(context);
                    prefsHelper.setPref("token", token);

                    // Получить/обновить данные личной карточки, в т.ч. specId
                    getCard(token);

                    saveProfile.saveProfile(token, true, true);

                } else {
                    Log.e(TAG, "CheckPhoneVerifyCode status: " + status);
                    Toast.makeText(context, "Check code status: " + status, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "CheckPhoneVerifyCode error: " + e.toString());
                if (e instanceof HttpException) {
                    int code = ((HttpException) e).code();
                    switch (code) {
                        case 403:
                            Toast.makeText(context, "Неверный id приложения", Toast.LENGTH_SHORT).show();
                            break;
                        case 404:
                            Toast.makeText(context, "Неверный код", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
                else
                    Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            }
        };
    }

    public void getEMailVerifyCode(String phone) {
        compositeDisposable.add(authorizationAPI.getEMailVerifyCode(authorizationAPI.appId, phone)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getEMailVerifyCodeObserver()));
    }

    private DisposableSingleObserver<QueryResponse> getEMailVerifyCodeObserver() {
        return new DisposableSingleObserver<QueryResponse>() {
            @Override
            public void onSuccess(QueryResponse response) {
                String status = response.getStatus();
                Log.d(TAG, "GetEMailVerifyCode status: " + status);
                if (status.equalsIgnoreCase("ok"))
                    Toast.makeText(context, "e-mail подтвержден", Toast.LENGTH_SHORT).show();

                else if (status.equalsIgnoreCase("error")) {
                    String description = response.getDescription();
                    Toast.makeText(context, description, Toast.LENGTH_SHORT).show();
                    if (description != null) {
                        switch (description) {
                            case "not verified":
                                Log.e(TAG, description);
                                break;
                            case "email confirmed":
                                Log.e(TAG, description);
                                break;
                            default:
                                Log.e(TAG, description);
                        }
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "GetEMailVerifyCode error: " + e.toString());
                if (e instanceof HttpException) {
                    int code = ((HttpException) e).code();
                    switch (code) {
                        case 403:
                            Toast.makeText(context, "Неверный id приложения", Toast.LENGTH_SHORT).show();
                            break;
                        case 404:
                            Toast.makeText(context, "Номер телефона не найден или уже прошел верификацию",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
                else
                    Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            }
        };
    }

    public void logout(String token) {
        compositeDisposable.add(authorizationAPI.logout(authorizationAPI.appId, token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(logoutObserver()));
    }

    private DisposableSingleObserver<QueryResponse> logoutObserver() {
        return new DisposableSingleObserver<QueryResponse>() {
            @Override
            public void onSuccess(QueryResponse response) {
                String status = response.getStatus();
                Log.d(TAG, "Logout status: " + status);
                if (status.equalsIgnoreCase("ok")) {
                    // При успешном выходе
                    PrefsHelper prefsHelper = new PrefsHelper(context);
                    prefsHelper.setPref("token", "");
                    prefsHelper.setPref("specId", 0);

                    // Очистить кэш
                    new ServiceController().deleteServices().subscribe();
                    new SessionController().deleteSessions().subscribe();
                    new ClientsController().deleteClients().subscribe();

                    Intent intent = new Intent(context, RegisterActivity.class);
                    context.startActivity(intent);
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "Logout error: " + e.toString());
                if (e instanceof HttpException) {
                    int code = ((HttpException) e).code();
                    switch (code) {
                        case 403:
                            Toast.makeText(context, "Неверный id приложения", Toast.LENGTH_SHORT).show();
                            break;
                        case 404:
                            Toast.makeText(context, "Токен не найден", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
                else
                    Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            }
        };
    }

    public void changePasswd(String phone, String password, String newpass, String newpass2) {
        compositeDisposable.add(authorizationAPI.changePasswd(authorizationAPI.appId,
                phone, password, newpass, newpass2)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(changePasswdObserver()));
    }

    private DisposableSingleObserver<QueryResponse> changePasswdObserver() {
        return new DisposableSingleObserver<QueryResponse>() {
            @Override
            public void onSuccess(QueryResponse response) {
                String status = response.getStatus();
                Log.d(TAG, "ChangePasswd status: " + status);
                if (status.equalsIgnoreCase("ok"))
                    Toast.makeText(context, "Пароль успешно изменен", Toast.LENGTH_SHORT).show();
                else {
                    String description = response.getDescription();
                    Toast.makeText(context, status + ": " + description, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "ChangePasswd error: " + e.toString());
                if (e instanceof HttpException) {
                    int code = ((HttpException) e).code();
                    switch (code) {
                        case 401:
                            Toast.makeText(context, "Неверный логин/пароль", Toast.LENGTH_SHORT).show();
                            break;
                        case 403:
                            Toast.makeText(context, "Неверный id приложения", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
                else
                    Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            }
        };
    }

    public void resetPasswdRequest(String phone) {
        compositeDisposable.add(authorizationAPI.resetPasswdRequest(authorizationAPI.appId, phone)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(resetPasswdRequestObserver()));
    }

    private DisposableSingleObserver<QueryResponse> resetPasswdRequestObserver() {
        return new DisposableSingleObserver<QueryResponse>() {
            @Override
            public void onSuccess(QueryResponse response) {
                String status = response.getStatus();
                Log.d(TAG, "ResetPasswRequest status: " + status);
                if (status.equalsIgnoreCase("ok"))
                    Toast.makeText(context, "Пароль отправлен на адрес эл. почты,"
                            + " указанный при регистрации", Toast.LENGTH_SHORT).show();
                else if (status.equalsIgnoreCase("error")) {
                    String description = response.getDescription();
                    if (description != null) {
                        switch (description) {
                            case "email not confirmed" :
                                Toast.makeText(context, "Для восстановления пароля "
                                        + "подтвердите адрес эл. почты, указанный при регистрации",
                                        Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                Log.e(TAG, description);
                        }
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "ResetPasswRequest error: " + e.toString());
                if (e instanceof HttpException) {
                    int code = ((HttpException) e).code();
                    switch (code) {
                        case 401:
                            Toast.makeText(context, "Неверный логин/пароль", Toast.LENGTH_SHORT).show();
                            break;
                        case 403:
                            Toast.makeText(context, "Неверный id приложения", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
                else
                    Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            }
        };
    }

    public void getCard(String token) {
        compositeDisposable.add(authorizationAPI.getCard(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getCardObserver()));
    }

    private DisposableSingleObserver<List<CardResponse>> getCardObserver() {
        return new DisposableSingleObserver<List<CardResponse>>() {
            @Override
            public void onSuccess(List<CardResponse> cardResponse) {
                Intent intent;
                if (cardResponse.size() > 0) {
                    CardResponse card = cardResponse.get(0);
                    PrefsHelper prefsHelper = new PrefsHelper(context);
                    prefsHelper.setPref("specId", card.getId());
                    prefsHelper.setPref("options", card.getOptions());
                    intent = new Intent(context, CalendarActivity.class);

                } else {
                    intent = new Intent(context, RegisterActivity.class);
                }
                context.startActivity(intent);
            }

            @Override
            public void onError(Throwable e) {
                Intent intent = new Intent(context, RegisterActivity.class);
                context.startActivity(intent);
                Log.e(TAG, e.toString());
            }
        };
    }

    public void dispose() {
        if (compositeDisposable != null && !compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
    }
}
