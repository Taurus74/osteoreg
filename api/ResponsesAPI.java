package com.aconst.spinareg.api;

import io.reactivex.Single;
import retrofit2.http.GET;

public interface ResponcesAPI {
    // Чтение отзыва к сеансу
    @GET("{token}/sessions/response/{session}")
    Single<>
}
