package com.szczepaniak.fasttravel;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;


public class MainActivity extends FragmentActivity {

    private PermissionManager permissionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        permissionManager = new PermissionManager();
        PermissionManager.getLocationPermission(MainActivity.this);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionManager.setmLocationPermissionsGranted(false);

        int request_code = PermissionManager.getLocationPermissionRequestCode();

        if (requestCode == request_code) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        permissionManager.setmLocationPermissionsGranted(false);
                        return;
                    }
                }
                permissionManager.setmLocationPermissionsGranted(true);
                startActivity(new Intent(MainActivity.this, MapActivity.class));
                finish();

            }
        }
    }
}
