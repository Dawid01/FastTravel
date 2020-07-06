package com.szczepaniak.fasttravel;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


class PermissionManager{

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static boolean mLocationPermissionsGranted;

    static void getLocationPermission(MainActivity mainActivity){
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(mainActivity, FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(mainActivity, COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                mainActivity.init();

            }else {
                ActivityCompat.requestPermissions((Activity) mainActivity, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else {
            ActivityCompat.requestPermissions((Activity) mainActivity, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    public static String getFineLocation() {
        return FINE_LOCATION;
    }

    public static String getCoarseLocation() {
        return COARSE_LOCATION;
    }

    public static int getLocationPermissionRequestCode() {
        return LOCATION_PERMISSION_REQUEST_CODE;
    }

    public Boolean getmLocationPermissionsGranted() {
        return mLocationPermissionsGranted;
    }

    public void setmLocationPermissionsGranted(Boolean mLocationPermissionsGranted) {
        this.mLocationPermissionsGranted = mLocationPermissionsGranted;
    }
}
