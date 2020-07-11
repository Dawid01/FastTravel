package com.szczepaniak.fasttravel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class TravelGenerator {

    private MapboxMap mapboxMap;
    private Context context;
    private MapboxDirections client;
    private String directionProfile;
    private float dist;
    private int callCount = 0;


    public TravelGenerator(MapboxMap mapboxMap, Context context, String directionProfile) {
        this.mapboxMap = mapboxMap;
        this.context = context;
        this.directionProfile = directionProfile;
    }

    public void findPlacesByType(String type, LatLng position, List<LatLng> places, LatLng origin, float distance){

        this.dist = distance;

        @SuppressLint("Range") MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
                .accessToken(context.getString(R.string.access_token))
                .mode(GeocodingCriteria.MODE_PLACES)
                .proximity(Point.fromLngLat(position.getLongitude(), position.getLatitude()))
                .query(type)
                .geocodingTypes(GeocodingCriteria.TYPE_POI)
                .limit(5)
                .build();


        mapboxGeocoding.enqueueCall(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {

                if(response.isSuccessful()){

                    callCount++;
                    assert response.body() != null;
                    for (CarmenFeature carmenFeature : response.body().features()){

                        List<CarmenFeature> features = response.body().features();
                        LatLng position = new LatLng();
                        position.setLatitude(Objects.requireNonNull(carmenFeature.center()).latitude());
                        position.setLongitude(carmenFeature.center().longitude());

                        if(!places.contains(position)) {

                            places.add(position);

                            client = MapboxDirections.builder()
                                    .origin(Point.fromLngLat(origin.getLongitude(), origin.getLatitude()))
                                    .destination(Point.fromLngLat(position.getLongitude(), position.getLatitude()))
                                    .overview(DirectionsCriteria.OVERVIEW_FULL)
                                    .profile(directionProfile)
                                    .accessToken(context.getString(R.string.access_token))
                                    .build();


                            client.enqueueCall(new Callback<DirectionsResponse>() {
                                @Override
                                public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {

                                    if(response.isSuccessful()){
                                        callCount++;
                                        try {
                                            assert response.body() != null;
                                                double distance = response.body().routes().get(0).distance();
                                                if (Math.abs(distance - dist) <= 500) {

                                                    MarkerOptions markerOptions = new MarkerOptions();
                                                    markerOptions.setTitle(type + ": " + carmenFeature.placeName());
                                                    markerOptions.setPosition(position);
                                                    markerOptions.setIcon(drawableToIcon(context, R.drawable.blue_marker));
                                                    mapboxMap.addMarker(markerOptions);

                                                }

                                            if(features.indexOf(carmenFeature) == features.size()-1){

                                                onTravelGeneratorFinish();
                                                Toast.makeText(context, "liczba zapytań: " + callCount , Toast.LENGTH_SHORT).show();
                                            }

                                            }catch (IndexOutOfBoundsException e){

                                                if(features.indexOf(carmenFeature) == features.size()-1){

                                                    onTravelGeneratorFinish();
                                                }
                                            }
                                    }
                                }

                                @Override
                                public void onFailure(Call<DirectionsResponse> call, Throwable t) {

                                    if(features.indexOf(carmenFeature) == features.size()-1){

                                        onTravelGeneratorFinish();
                                    }
                                }
                            });


                        }

                    }
                }
            }

            @Override
            public void onFailure(Call<GeocodingResponse> call, Throwable t) {

            }
        });
    }

    public static com.mapbox.mapboxsdk.annotations.Icon drawableToIcon(@NonNull Context context, @DrawableRes int id) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(context.getResources(), id, context.getTheme());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.scale(0.7f, 0.7f);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return IconFactory.getInstance(context).fromBitmap(bitmap);
    }

    public void setDirectionProfile(String directionProfile) {
        this.directionProfile = directionProfile;
    }

    public abstract void onTravelGeneratorFinish();

}
