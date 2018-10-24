package com.aconst.spinareg.api;

import com.aconst.spinareg.model.Profile;
import com.aconst.spinareg.profile.PortfolioItem;

import java.io.File;
import java.util.List;

import io.reactivex.Single;
import okhttp3.MultipartBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface PortfolioAPI {
    String appId = "7d487847639c07f09c1d565a";

    // Чтение профиля пользователя (в т.ч. опций учетной записи)
    @GET("{token}/user")
    Single<Profile> getUser(@Path("token") String token);

    // Изменение профиля пользователя
    @POST("{token}/user")
    @Multipart
    Single<QueryResponse> setUser(@Path("token") String token,
                                  @Field("") );

    // Создание/обновление карточки специалиста
    @POST("{token}/user/card/self")
    @FormUrlEncoded
    Single<QueryResponse> selfCard(@Path("token") String token,
                                   @Field("firstName") String firstName,
                                   @Field("secondName") String secondName,
                                   @Field("lastName") String lastName,
                                   @Field("city") String city,
                                   @Field("street") String street,
                                   @Field("station") String station,
                                   @Field("building") String building,
                                   @Field("flat") String flat,
                                   @Field("longitude") double longitude,
                                   @Field("latitude") double latitude,
                                   @Field("about") String about,
                                   @Field("options") String options);

    // Загрузка/обновление аватара для карточки
    @POST("{token}/user/avatar/self")
    @Multipart
    Single<QueryResponse> setAvatar(@Path("token") String token,
                                    @Part MultipartBody.Part avatar);

    // Добавление записи в портфолио
    @POST("{token}/portfolio/add")
    @Multipart
    Single<QueryResponse> addPortfolio(@Path("token") String token,
                                       @Part("type") String type,
                                       @Part("description") String description,
                                       @Part MultipartBody.Part photo,
                                       @Part("eventDate") String eventDate);

    // Обновления записи в портфолио
    @POST("{token}/portfolio/upd/{id}")
    @Multipart
    Single<QueryResponse> updPortfolio(@Path("token") String token,
                                       @Path("id") int id,
                                       @Part("type") String type,
                                       @Part("description") String description,
                                       @Part MultipartBody.Part photo,
                                       @Part("eventDate") String eventDate);

//    Запрос портфолио
    @GET("{token}/get/{id}")
    Single<List<PortfolioItem>> getPortfolio(@Path("token") String token,
                                             @Path("id") int id);

    @GET("{token}/get/{id}/{type}")
    Single<List<PortfolioItem>> getPortfolioType(@Path("token") String token,
                                                 @Path("id") int id,
                                                 @Path("type") String type);
}
