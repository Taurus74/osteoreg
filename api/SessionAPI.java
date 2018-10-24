package com.aconst.spinareg.api;

import com.aconst.spinareg.model.SessionItemRealm;

import java.util.List;

import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface SessionAPI {
    // Создание нового сеанса
    @POST("{token}/sessions/new")
    @FormUrlEncoded
    Single<QueryResponse> newSession(@Path("token") String token,
                                     @Field("cid") int clientId,
                                     @Field("cardID") int cardID,
                                     @Field("sid") int specId,
                                     @Field("service") String service,
                                     @Field("sessionDate") String sessionDate,
                                     @Field("sessionTime") String sessionTime,
                                     @Field("duration") int duration,
                                     @Field("specComment") String specComment,
                                     @Field("cost") float cost,
                                     @Field("files") List<String> files);

    // Чтение существующих сеансов
    @GET("{token}/sessions/get/{from}/{count}")
    Single<List<SessionItemRealm>> getSessions(@Path("token") String token,
                                               @Path("from") int from,
                                               @Path("count") int count);

    // Чтение существующих сеансов в заданном интервале дат
    @GET("{token}/sessions/getinterval/{from}/{to}")
    Single<List<SessionItemRealm>> getSessionsInterval(@Path("token") String token,
                                                       @Path("from") String from,
                                                       @Path("to") String to);

    // Чтение сеанса по id
    @GET("{token}/sessions/getbyid/{id}")
    Single<SessionItemRealm> getSessionById(@Path("token") String token,
                                            @Path("id") int id);

    // Изменение назначенного сеанса
    @POST("{token}/sessions/upd/{id}")
    @FormUrlEncoded
    Single<QueryResponse> updSession(@Path("token") String token,
                                     @Path("id") int id,
                                     @Field("cid") int clientId,
                                     @Field("cardID") int cardID,
                                     @Field("sid") int specId,
                                     @Field("service") String service,
                                     @Field("sessionDate") String sessionDate,
                                     @Field("sessionTime") String sessionTime,
                                     @Field("duration") int duration,
                                     @Field("specComment") String specComment,
                                     @Field("cost") float cost,
                                     @Field("status") String status);

    // Копирование назначенного сеанса
    @POST("{token}/sessions/copy/{id}")
    @FormUrlEncoded
    Single<QueryResponse> copySession(@Path("token") String token,
                                      @Path("id") int id,
                                      @Field("dates") String[] dates);    // ???

    // Изменение состояния назначенного сеанса
    @GET("{token}/sessions/chngstatus/{id}/{status}")
    Single<QueryResponse> changeStatus(@Path("token") String token,
                                       @Path("id") int id,
                                       @Path("status") String status);

    // Удаление назначенного сеанса
    @GET("{token}/sessions/del/{id}/")
    Single<QueryResponse> delSession(@Path("token") String token,
                                     @Path("id") int id);

    // Обработка файлов для сеанса
    // - добавление файла и описания к списку
    @POST("{token}/sessions/upload/add/{id}")
    @Multipart
    Single<QueryResponse> uploadFile(@Path("token") String token,
                                     @Path("id") int id,
                                     @Part MultipartBody.Part fileUpload,
                                     @Part("description") RequestBody description);

    // - замена файла и описания к списку
    @POST("{token}/sessions/upload/replace/{id}")
    @Multipart
    Single<QueryResponse> replaceFile(@Path("token") String token,
                                      @Path("id") int id,
                                      @Part MultipartBody.Part fileUpload,
                                      @Part("description") String description);

    // - удаление указанного файла
    @POST("{token}/sessions/upload/del/{id}")
    @FormUrlEncoded
    Single<QueryResponse> deleteFile(@Path("token") String token,
                                     @Path("id") int id,
                                     @Field("fileID") int fileID);
}
