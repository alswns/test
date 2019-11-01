package com.example.test_api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface input {
    @Headers({"Authorization:Bearer 957ea642f45647e98a071eaacd6b73bf","Content-Type:application/json; charset=utf-8"})
    @GET("query")
    Call<art> get_Weather_retrofit(@Query("v") String v, @Query("query") String query, @Query("lang") String lang, @Query("sessionId") String sessionId);

}
