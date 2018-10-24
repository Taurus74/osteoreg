package com.aconst.spinareg.api;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.aconst.spinareg.Common;
import com.aconst.spinareg.PrefsHelper;
import com.aconst.spinareg.authorization.RegisterActivity;
import com.aconst.spinareg.controllers.ServiceController;
import com.aconst.spinareg.model.ServiceItemRealm;
import com.aconst.spinareg.profile.Profile2Fragment;
import com.aconst.spinareg.profile.ProfileActivity;
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

public class OsteoService {
    private static final String TAG = "osteoService";
    private static final String BASE_URL = Common.BASE_URL;
    private ServiceAPI serviceAPI;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Context context;

    public OsteoService(Context context) {
        this.context = context;

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

        serviceAPI = retrofit.create(ServiceAPI.class);
    }

    public void addService(String token,
            String title, String description, int duration, float price, int currency) {
        compositeDisposable.add(serviceAPI.addService(token, title, description, duration, price, currency)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(addServiceObserver(token)));
    }

    private DisposableSingleObserver<QueryResponse> addServiceObserver(final String token) {
        return new DisposableSingleObserver<QueryResponse>() {
            @Override
            public void onSuccess(QueryResponse response) {
                String status = response.getStatus();
                if (status.equalsIgnoreCase("ok")) {
                    String servId = response.getId();

                    PrefsHelper prefsHelper = new PrefsHelper(context);
                    prefsHelper.setPref("servId", servId);

                    // Обновление Realm-кэша
                    getServices(token);

                } else if (status.equalsIgnoreCase("error")) {
                    String description = response.getDescription();
                    if (description.endsWith("empty")) {
                        description = "Поле "
                                + description.replace("empty", "пустое");
                    }
                    Toast.makeText(context, description, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.toString());
            }
        };
    }

    public void updService(String token, int servId,
                           String title, String description, int duration, float price, int currency) {
        compositeDisposable.add(serviceAPI.updService(
                token, servId, title, description, duration, price, currency)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(updServiceObserver(token)));
    }

    private DisposableSingleObserver<QueryResponse> updServiceObserver(final String token) {
        return new DisposableSingleObserver<QueryResponse>() {
            @Override
            public void onSuccess(QueryResponse response) {
                String status = response.getStatus();
                if (status.equalsIgnoreCase("ok")) {
                    String servId = response.getId();

                    PrefsHelper prefsHelper = new PrefsHelper(context);
                    prefsHelper.setPref("servId", servId);

                    // Обновление Realm-кэша
                    getServices(token);

                } else if (status.equalsIgnoreCase("error")) {
                    String description = response.getDescription();
                    if (description.endsWith("empty")) {
                        description = "Поле "
                                + description.replace("empty", "пустое");
                    }
                    Toast.makeText(context, description, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.toString());
            }
        };
    }

    public void getServices(String token) {
        compositeDisposable.add(serviceAPI.getServices(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getServicesObserver()));
    }

    private DisposableSingleObserver<List<ServiceItemRealm>> getServicesObserver() {
        return new DisposableSingleObserver<List<ServiceItemRealm>>() {
            @Override
            public void onSuccess(List<ServiceItemRealm> serviceItemRealms) {
                ServiceController controller = new ServiceController();
                controller.updateServices(serviceItemRealms).subscribe();

                updateUI();
            }

            @Override
            public void onError(Throwable e) {
                if (e instanceof HttpException) {
                    int code = ((HttpException) e).code();
                    if (code == 403) {
                        // Неверный id приложения
                        PrefsHelper prefsHelper = new PrefsHelper(context);
                        prefsHelper.setPref("token", "");

                        Intent intent = new Intent(context, RegisterActivity.class);
                        context.startActivity(intent);

                    } else if (code == 404) {
                        // Услуги не добавлены. Очищаем кэш
                        ServiceController controller = new ServiceController();
                        controller.deleteServices().subscribe();
                        Toast.makeText(context, "Услуг не найдено. Добавьте услуги в профиле", Toast.LENGTH_SHORT).show();

                    } else
                        Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                } else
                    Log.e(TAG, e.toString());
            }
        };
    }

    public void deleteService(String token, int id) {
        compositeDisposable.add(serviceAPI.deleteService(token, id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(deleteServiceObserver()));
    }

    private DisposableSingleObserver<QueryResponse> deleteServiceObserver() {
        return new DisposableSingleObserver<QueryResponse>() {
            @Override
            public void onSuccess(QueryResponse queryResponse) {
                String status = queryResponse.getStatus();
                if (status.equalsIgnoreCase("ok"))
                    updateUI();
                else
                    Toast.makeText(context, "При удалении услуги произошла ошибка",
                            Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(context, "При удалении услуги произошла ошибка: "
                        + e.toString(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, e.toString());
            }
        };
    }

    public void dispose() {
        if (compositeDisposable != null && !compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
    }

    private void updateUI() {
        Intent intent = new Intent();
        intent.setAction("com.aconst.spinareg.updateService");
        context.sendBroadcast(intent);
    }
}
