package com.aconst.spinareg.api;

import com.aconst.spinareg.model.ServiceItemRealm;

import java.util.List;

import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ServiceAPI {
    // Добавление предоставляемой услуги
    @POST("{token}/services/add")
    @FormUrlEncoded
    Single<QueryResponse> addService(@Path("token") String token,
                                     @Field("title") String title,
                                     @Field("description") String description,
                                     @Field("duration") int duration,
                                     @Field("price") float price,
                                     @Field("currency") int currency);

    // Обновление предоставляемой услуги
    @POST("{token}/services/upd/{id}")
    @FormUrlEncoded
    Single<QueryResponse> updService(@Path("token") String token,
                                     @Path("id") int servId,
                                     @Field("title") String title,
                                     @Field("description") String description,
                                     @Field("duration") int duration,
                                     @Field("price") float price,
                                     @Field("currency") int currency);


    // Услуги, предоставляемые данным специалистом
    @GET("{token}/services")
    Single<List<ServiceItemRealm>> getServices(@Path("token") String token);

    // Удаление предоставляемой услуги
    @GET("{token}/services/del/{id}")
    Single<QueryResponse> deleteService(@Path("token") String token,
                                                 @Path("id") int id);
}
