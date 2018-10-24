package com.aconst.spinareg.api;

import java.util.List;

import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface AuthorizationAPI {
    String appId = "7d487847639c07f09c1d565a";

    // Запрос на регистрацию
    @POST("{app_id}/reg/spec")
    @FormUrlEncoded
    Single<QueryResponse> registration(@Path("app_id") String appId,
                                       @Field("phone") String phone,
                                       @Field("email") String email,
                                       @Field("password") String password,
                                       @Field("password2") String password2);

    // Запрос кода верификации номера телефона
    @GET("{app_id}/getcode/{phone}")
    Single<QueryResponse> getPhoneVerifyCode(@Path("app_id") String appId,
                                             @Path("phone") String phoneNumber);

    // Запрос проверки кода верификации телефона
    @GET("{app_id}/verify/{code}")
    Single<QueryResponse> checkPhoneVerifyCode(@Path("app_id") String appId,
                                               @Path("code") String code);

    // Запрос подтверждения электронной почты
    @GET("{app_id}/getconfirm/{phone}")
    Single<QueryResponse> getEMailVerifyCode(@Path("app_id") String appId,
                                             @Path("phone") String phone);

    // Запрос на аутентификацию
    @POST("{app_id}/auth")
    @FormUrlEncoded
    Single<QueryResponse> authentication(@Path("app_id") String appId,
                                         @Field("phone") String phone,
                                         @Field("password") String password,
                                         @Field("pushToken") String pushToken);

    // Запрос выхода из учетной записи
    @GET("{app_id}/logout/{token}")
    Single<QueryResponse> logout(@Path("app_id") String appId,
                                 @Path("token") String token);

    // Запрос на смену пароля
    @POST("{app_id}/changepassw")
    @FormUrlEncoded
    Single<QueryResponse> changePasswd(@Path("app_id") String appId,
                                       @Field("phone") String phone,
                                       @Field("password") String password,
                                       @Field("newpass") String newpass,
                                       @Field("newpass2") String newpass2);

    // Запрос на восстановление  пароля
    @GET("{app_id}/resetpasswrequest/{phone}")
    Single<QueryResponse> resetPasswdRequest(@Path("app_id") String appId,
                                             @Path("phone") String phone);

    // Данные карточки
    @GET("{token}/user/card/personal/self")
    Single<List<CardResponse>> getCard(@Path("token") String token);
}

