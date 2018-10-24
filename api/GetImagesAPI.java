package com.aconst.spinareg.api;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface GetImageAPI {
    @GET("storage/sessionsdoc/{imageName}")
    Single<ResponseBody> getSessionImage(@Path("imageName") String imageName);

    @GET("storage/portfolio/{imageName}")
    Single<QueryResponse> getPortfolioImage(@Path("imageName") String imageName);

    @GET("storage/avatars/{imageName}")
    Single<QueryResponse> getAvatar(@Path("imageName") String imageName);
}
