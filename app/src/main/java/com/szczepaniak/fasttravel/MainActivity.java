package com.szczepaniak.fasttravel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.navigation.NavigationView;
import com.szczepaniak.fasttravel.Adapters.PlaceTipsAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private static int AUTOCOMPLETE_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private PermissionManager permissionManager;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private AutoCompleteTextView searchText;
    private ImageView clearIcon;
    private ImageView gps;
    private NavigationView nav_view;

    List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.map_key), Locale.US);
        }
        searchText = findViewById(R.id.input_search);
        clearIcon = findViewById(R.id.ic_clear);
        gps = findViewById(R.id.ic_gps);
        //new AutoTipsManager(searchText, this);
        searchText.setAdapter(new PlaceTipsAdapter(MainActivity.this, android.R.layout.simple_list_item_1));
        permissionManager = new PermissionManager();
        PermissionManager.getLocationPermission(MainActivity.this);

        nav_view = findViewById(R.id.nav_view);

        nav_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){

                    case R.id.normal:
                        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(MainActivity.this, R.raw.normal));
                        break;
                    case R.id.silver:
                        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(MainActivity.this, R.raw.silver));
                        break;
                    case R.id.retro:
                        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(MainActivity.this, R.raw.retro));
                        break;
                    case R.id.dark:
                        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(MainActivity.this, R.raw.dark));
                        break;
                    case R.id.night:
                        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(MainActivity.this, R.raw.night));
                        break;
                    case R.id.aubergine:
                        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(MainActivity.this, R.raw.aubergine));
                        break;
                    case R.id.otherMap:

                        Intent otherMap = new Intent(MainActivity.this, MapActivity.class);
                        startActivity(otherMap);

                        break;
                }
                return false;
            }
        });

    }

    public void init() {

        searchText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) ||
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    geoLocate();
                }
                return false;
            }
        });

        searchText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startAutocomplereActivity(v);
                return false;
            }
        });


        gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getDeviceLocation();
            }
        });


        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(searchText.getText().toString().length() > 0){

                    clearIcon.setVisibility(View.VISIBLE);
                }else {

                    clearIcon.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        clearIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                searchText.setText("");
                mMap.clear();
            }
        });

        ImageView search = findViewById(R.id.ic_magnify);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                geoLocate();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        hideSoftKeyboard();
    }

    private void geoLocate(){

        String searchString = searchText.getText().toString();

        Geocoder geocoder = new Geocoder(MainActivity.this);
        List<Address> list = new ArrayList<>();

        try {
            list = geocoder.getFromLocationName(searchString, 1);

        }catch (IOException e){}

        if(list.size() > 0){
            Address address = list.get(0);
            moveMapCamera(new LatLng(address.getLatitude(), address.getLongitude()), 15f, address.getAddressLine(0));
            searchText.setText(address.getAddressLine(0));
        }

    }


    public boolean isServicesOK() {

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (permissionManager.getmLocationPermissionsGranted()) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            MapStyleOptions mapStyleOptions = MapStyleOptions.loadRawResourceStyle(this, R.raw.normal);
            mMap.setMapStyle(mapStyleOptions);
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.setPadding(0, 145, 0, 0);

        }

    }

    private  void getDeviceLocation(){

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if(permissionManager.getmLocationPermissionsGranted()){

                final Task<Location> location = fusedLocationProviderClient.getLastLocation();
                assert location != null;
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Location currentLocation = (Location) task.getResult();
                            if(currentLocation != null) {
                                moveMapCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15f, "Your last location");
                            }
                        }else {
                            Toast.makeText(MainActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException ignored){

        }
    }

    private void  moveMapCamera(LatLng latLng, float zoom, String title){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title(title);
        mMap.clear();
        mMap.addMarker(options);
        hideSoftKeyboard();
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionManager.setmLocationPermissionsGranted(false);

        int request_code = PermissionManager.getLocationPermissionRequestCode();

        if(requestCode == request_code){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                for (int i = 0; i < grantResults.length; i++){
                    if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                        permissionManager.setmLocationPermissionsGranted(false);
                        return;
                    }
                }
                permissionManager.setmLocationPermissionsGranted(true);
                if(isServicesOK()){
                    init();
                }
            }
        }

    }

    public void startAutocomplereActivity(View view){
//        Intent intent = new Autocomplete.IntentBuilder(
//                AutocompleteActivityMode.OVERLAY,
//                Arrays.asList(Place.Field.ID, Place.Field.NAME))
//                .setTypeFilter(TypeFilter.ESTABLISHMENT)
//                .build(this);
//        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i("Places", "Place: " + place.getName() + ", " + place.getId());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("Places", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }}