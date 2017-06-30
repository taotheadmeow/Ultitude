package com.teamsmokeweed.ultitude;

import java.util.Map;

import retrofit2.Call;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Created by tao_s on 29 Jun 2017.
 */

public interface Interface {
     @GET("/weather?lat={lat}&lon={lon}&appid=f1711bf6fbee00fd83c6de3a48dcbcc5")
     Call<Main> getUser(@Path("username") String username);

     @GET("weather")
    Call<WeatherApi> getApi (@QueryMap Map<String,String> params);
}
