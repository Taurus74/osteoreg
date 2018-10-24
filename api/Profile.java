package com.aconst.spinareg.api;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.aconst.spinareg.Common;
import com.aconst.spinareg.WelcomeActivity;
import com.aconst.spinareg.controllers.PortfolioController;
import com.aconst.spinareg.profile.PortfolioItem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
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

public class Portfolio {
    private static final String TAG = "portfolio";
    private static final String BASE_URL = Common.BASE_URL;
    private PortfolioAPI portfolioAPI;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Context context;

    public Portfolio(Context context) {
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

        portfolioAPI = retrofit.create(PortfolioAPI.class);
    }

    public void selfCard(String token, String firstName, String secondName, String lastName,
                         String city, String street, String station, String building, String flat,
                         double longitude, double latitude, String about, String options) {
        compositeDisposable.add(portfolioAPI.selfCard(token, firstName, secondName, lastName,
                city, street, station, building, flat, longitude, latitude, about, options)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(selfCardObserver()));
    }

    private DisposableSingleObserver<QueryResponse> selfCardObserver() {
        return new DisposableSingleObserver<QueryResponse>() {
            @Override
            public void onSuccess(QueryResponse queryResponse) {
                String status = queryResponse.getStatus();
                if (status.equalsIgnoreCase("ok")) {
                    Intent intent = new Intent(context, WelcomeActivity.class);
                    context.startActivity(intent);
                    Log.d(TAG, "Карточка записана");

                }
                else if (status.equalsIgnoreCase("error"))
                    Toast.makeText(context, "Ошибка " + queryResponse.getDescription(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.toString());
            }
        };
    }

    public void setAvatar(String token, String filename) {
        File file = new File(filename);
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("avatar", filename, requestFile);

        compositeDisposable.add(portfolioAPI.setAvatar(token, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(setAvatarObserver()));
    }

    private DisposableSingleObserver<QueryResponse> setAvatarObserver() {
        return new DisposableSingleObserver<QueryResponse>() {
            @Override
            public void onSuccess(QueryResponse queryResponse) {
                String status = queryResponse.getStatus();
                if (status.equalsIgnoreCase("ok"))
                    Log.d(TAG, "Аватар загружен");
                else
                    Log.e(TAG, "Ошибка загрузки: " + queryResponse.getDescription());
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.toString());
            }
        };
    }

    public void getPortfolio(String token, int id) {
        compositeDisposable.add(portfolioAPI.getPortfolio(token, id)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeWith(getPortfolioObserver()));
    }

    private DisposableSingleObserver<List<PortfolioItem>> getPortfolioObserver() {
        return new DisposableSingleObserver<List<PortfolioItem>>() {
            @Override
            public void onSuccess(List<PortfolioItem> portfolioItems) {
                PortfolioController controller = new PortfolioController();
                controller.updatePortfolio(portfolioItems).subscribe();
            }

            @Override
            public void onError(Throwable e) {
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
