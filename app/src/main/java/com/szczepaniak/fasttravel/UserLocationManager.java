package com.szczepaniak.fasttravel;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;

public class UserLocationManager {

    private LocationEngine locationEngine;
    private long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    private Context context;
    private  MapActivity activity;
//    private LocationEngineCallback callback = new LocationEngineCallback() {
//        @Override
//        public void onSuccess(Object result) {
//
////
////            if (activity != null) {
////                Location location = result.getLastLocation();
////
////                if (location == null) {
////                    return;
////                }
////
////// Create a Toast which displays the new location's coordinates
////
////// Pass the new location to the Maps SDK's LocationComponent
////                if (activity.mapboxMap != null && result.getLastLocation\() != null) {
////                    activity.mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
////                }
//            }
//        }
//
//        @Override
//        public void onFailure(@NonNull Exception exception) {
//
//        }
//    };


    public UserLocationManager(Context context) {
        this.context = context;
        initLocationEngine();
    }

    private void initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(context);

        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

//            locationEngine.requestLocationUpdates(request, callback, context.getMainLooper());
//            locationEngine.getLastLocation(callback);
            return;
        }

    }
}


