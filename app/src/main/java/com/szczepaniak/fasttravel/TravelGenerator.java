package com.szczepaniak.fasttravel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
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
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    public int callCount = 0;
    private int findedPlacesCount = 0;

    private List<LatLng> polygon_points = new ArrayList<>();
    private int removedPositions = 0;
    private int featureIndex = 0;

    public TravelGenerator(MapboxMap mapboxMap, Context context, String directionProfile) {
        this.mapboxMap = mapboxMap;
        this.context = context;
        this.directionProfile = directionProfile;
    }

    public void findPlacesByType(String type, LatLng position, List<LatLng> places, LatLng origin, float distance, boolean last, Marker mainMarker){

        this.dist = distance;

        @SuppressLint("Range") MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
                .accessToken(context.getString(R.string.access_token))
                .mode(GeocodingCriteria.MODE_PLACES)
                .proximity(Point.fromLngLat(position.getLongitude(), position.getLatitude()))
                .query(type)
                .geocodingTypes(GeocodingCriteria.TYPE_POI)
                .limit(5)
                .build();


        callCount++;
        mapboxGeocoding.enqueueCall(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {

                if(response.isSuccessful()){
                    assert response.body() != null;
                    List<CarmenFeature> features = response.body().features();
                    for (CarmenFeature carmenFeature : response.body().features()){

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

                            callCount++;
                            onTravelGeneratorFinish();

                            client.enqueueCall(new Callback<DirectionsResponse>() {
                                @Override
                                public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {

                                    if(response.isSuccessful()){
                                        try {
                                            assert response.body() != null;
                                                double distance = response.body().routes().get(0).distance();

                                                boolean distanceIsOK;

                                                if(distance <= 3000){
                                                    distanceIsOK = Math.abs(distance - dist) <= 100;
                                                }else {
                                                    distanceIsOK = Math.abs(distance - dist) <= 500;

                                                }

                                                if (distanceIsOK) {

                                                    MarkerOptions markerOptions = new MarkerOptions();
                                                    markerOptions.setTitle(type + ": " + carmenFeature.placeName());
                                                    markerOptions.setPosition(position);
                                                    markerOptions.setIcon(drawableToIcon(context, R.drawable.blue_marker));
                                                    mapboxMap.addMarker(markerOptions);
                                                    findedPlacesCount ++;
                                                    polygon_points.add(position);

                                                }


                                            }catch (IndexOutOfBoundsException e){

                                             onTravelGeneratorFinish();

                                        }

                                        if(last) {

                                            featureIndex ++;
//                                                    int featureIndex = features.indexOf(carmenFeature);
                                            int size = features.size();
                                            if (featureIndex == size) {
                                                removedPositions = 0;
                                                featureIndex = 0;
                                                Toast.makeText(context, "liczba zapytań: " + callCount, Toast.LENGTH_SHORT).show();
                                                callCount = 0;
                                                onTravelGeneratorFinish();


                                                if (findedPlacesCount <= 5) {

                                                    findCities(mainMarker.getPosition());
                                                    findedPlacesCount = 0;
                                                }

                                                polygon_points.clear();


                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<DirectionsResponse> call, Throwable t) {

                                        onTravelGeneratorFinish();
                                        removedPositions = 0;
                                        featureIndex = 0;

                                }
                            });


                        }else {

                            if(last) {
                                removedPositions++;
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<GeocodingResponse> call, Throwable t) {

            }
        });
    }

    private void findCities(LatLng startPosition){

        @SuppressLint("Range") MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
                .accessToken(context.getString(R.string.access_token))
                .mode(GeocodingCriteria.MODE_PLACES)
                .proximity(Point.fromLngLat(startPosition.getLongitude(), startPosition.getLatitude()))
                .query(Point.fromLngLat(startPosition.getLongitude(), startPosition.getLatitude()))
                .geocodingTypes(GeocodingCriteria.TYPE_PLACE)
                .limit(5)
                .build();


        mapboxGeocoding.enqueueCall(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {

                if(response.isSuccessful()){

                    assert response.body() != null;
                    for (CarmenFeature carmenFeature : response.body().features()){

                        List<CarmenFeature> features = response.body().features();
                        LatLng position = new LatLng();
                        position.setLatitude(Objects.requireNonNull(carmenFeature.center()).latitude());
                        position.setLongitude(carmenFeature.center().longitude());
                        

                            client = MapboxDirections.builder()
                                    .origin(Point.fromLngLat(startPosition.getLongitude(), startPosition.getLatitude()))
                                    .destination(Point.fromLngLat(position.getLongitude(), position.getLatitude()))
                                    .overview(DirectionsCriteria.OVERVIEW_FULL)
                                    .profile(directionProfile)
                                    .accessToken(context.getString(R.string.access_token))
                                    .build();

                            callCount++;

                            client.enqueueCall(new Callback<DirectionsResponse>() {
                                @Override
                                public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {

                                    if(response.isSuccessful()){
                                        try {
                                            assert response.body() != null;
                                            double distance = response.body().routes().get(0).distance();

                                            boolean distanceIsOK;

                                            if(distance <= 3000){
                                                distanceIsOK = Math.abs(distance - dist) <= 100;
                                            }else {
                                                distanceIsOK = Math.abs(distance - dist) <= 500;

                                            }

                                            if (distanceIsOK) {

                                                MarkerOptions markerOptions = new MarkerOptions();
                                                markerOptions.setTitle("city/vilage" + ": " + carmenFeature.placeName());
                                                markerOptions.setPosition(position);
                                                markerOptions.setIcon(drawableToIcon(context, R.drawable.blue_marker));
                                                mapboxMap.addMarker(markerOptions);
                                                findedPlacesCount ++;

                                            }
                                            onTravelGeneratorFinish();

                                        }catch (IndexOutOfBoundsException e){
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


    private List<LatLng> sortPsitionsToDrawPolygon(List<LatLng> latLngList){

        List<LatLng> sortedList = new ArrayList<>();
        sortedList.add(latLngList.get(0));
        latLngList.remove(0);
        LatLng checkedLatLang = sortedList.get(0);

        while (latLngList.size() != 0) {
            double minDist = 1000000000;
            LatLng miniLatLang = null;
            for (LatLng latLng : latLngList) {

                if(checkedLatLang != null && latLng != null) {
                    double distance = Math.sqrt(Math.pow(latLng.getLongitude() - checkedLatLang.getLongitude(), 2) - Math.pow(latLng.getLatitude() - checkedLatLang.getLatitude(), 2));
                    if (distance <= minDist) {

                        minDist = distance;
                        miniLatLang = latLng;
                    }
                }
            }
            sortedList.add(miniLatLang);
            if(miniLatLang != null) {
                latLngList.remove(miniLatLang);
            }
            checkedLatLang = miniLatLang;
        }

        return sortedList;

    }

}
