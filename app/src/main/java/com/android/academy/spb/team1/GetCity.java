package com.android.academy.spb.team1;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GetCity {
    @GET("api/geocode/json?sensor=true")
    Call<CityResult> getCity(@Query("latlng") String latlng);
}
