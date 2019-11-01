package com.example.test_api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkHelper {
    private static Retrofit retrofit = null;

    public static input getInstence(){
        if (retrofit==null){
            retrofit = new Retrofit.Builder().baseUrl("https://api.dialogflow.com/v1/").addConverterFactory(GsonConverterFactory.create()).build();
        }
        return retrofit.create(input.class);
    }

}
