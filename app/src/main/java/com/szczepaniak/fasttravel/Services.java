package com.szczepaniak.fasttravel;

import com.mapbox.geojson.Feature;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface Services {

    @GET("/geocoding/v5/mapbox.places/{lon},{lat}.json")
    Call<List<Feature>> getNearbyLocations(@Path("lon") double lon, @Path("lat") double lat, @Query("access_token") String token);

    @GET("/geocoding/v5/mapbox.places/{proximity}.json")
    Call<List<Feature>> getNearbyPoi(@Path("proximity") String proximity, @Query("types") String type, @Query("access_token") String access_token);
}
