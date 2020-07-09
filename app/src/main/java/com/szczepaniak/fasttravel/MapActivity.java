package com.szczepaniak.fasttravel;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.UiSettings;
import com.mapbox.mapboxsdk.plugins.building.BuildingPlugin;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;


import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MapActivity extends AppCompatActivity implements  OnMapReadyCallback{

    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private MapView mapView;
    public MapboxMap mapboxMap;
    private BuildingPlugin buildingPlugin;
    private Bundle savedInstanceState;
    private Marker mainMarker;
    private Marker touchMarker;

    private TextView searchText;
    private ImageView clearIcon;
    private ImageView gps;
    private String geojsonSourceLayerId = "geojsonSourceLayerId";

    private String directionProfile = DirectionsCriteria.PROFILE_CYCLING;
    private DirectionManager directionManager;
    private View walkBtm, bikeBtm, carBtm;
    private TextView distanceText;

    private Button startButton;

    private Retrofit.Builder builder;
    private Retrofit retrofit;
    private Services service;

    private MapboxDirections client;


    private  String[] filterTypes = {"park", "museum", "rest area", "mountain", "lake" , "zoo", "restaurant", "pizza", "pools", "theatre"};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        initMap();
        setContentView(R.layout.activity_map);
        initMap();
        searchText = findViewById(R.id.input_search);
        clearIcon = findViewById(R.id.ic_clear);
        gps = findViewById(R.id.ic_gps);
        distanceText = findViewById(R.id.distance_text);

        builder = new Retrofit.Builder().baseUrl("https://api.mapbox.com").addConverterFactory(GsonConverterFactory.create());
        retrofit = builder.build();
        service = retrofit.create(Services.class);

        searchText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) ||
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                }
                return false;
            }
        });

        searchText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new PlaceAutocomplete.IntentBuilder()
                        .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.access_token))
                        .placeOptions(PlaceOptions.builder()
                                .backgroundColor(Color.parseColor("#EEEEEE"))
                                .limit(10)
                                .build(PlaceOptions.MODE_CARDS))
                        .build(MapActivity.this);
                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
            }
        });


        gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                mapboxMap.clear();
                mainMarker = null;
                touchMarker = null;
                distanceText.setText("");
                distanceText.setVisibility(View.GONE);
                directionManager.clearDirections();

            }
        });

        ImageView search = findViewById(R.id.ic_magnify);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //geoLocate();
            }
        });

        walkBtm = findViewById(R.id.walk);
        bikeBtm = findViewById(R.id.bike);
        carBtm = findViewById(R.id.car);

        walkBtm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                walkBtm.setBackground(getResources().getDrawable(R.drawable.small_icon_selected));
                bikeBtm.setBackground(getResources().getDrawable(R.drawable.small_icon_type_bg));
                carBtm.setBackground(getResources().getDrawable(R.drawable.small_icon_type_bg));
                directionProfile = DirectionsCriteria.PROFILE_WALKING;
                DrawDirection();
            }
        });

        bikeBtm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                walkBtm.setBackground(getResources().getDrawable(R.drawable.small_icon_type_bg));
                bikeBtm.setBackground(getResources().getDrawable(R.drawable.small_icon_selected));
                carBtm.setBackground(getResources().getDrawable(R.drawable.small_icon_type_bg));
                directionProfile = DirectionsCriteria.PROFILE_CYCLING;
                DrawDirection();
            }
        });

        carBtm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                walkBtm.setBackground(getResources().getDrawable(R.drawable.small_icon_type_bg));
                bikeBtm.setBackground(getResources().getDrawable(R.drawable.small_icon_type_bg));
                carBtm.setBackground(getResources().getDrawable(R.drawable.small_icon_selected));
                directionProfile = DirectionsCriteria.PROFILE_DRIVING;
                DrawDirection();
            }
        });

        startButton = findViewById(R.id.start_button);



    }


    private void DrawDirection(){

        if(mainMarker != null && touchMarker != null){

            Point origin = Point.fromLngLat(mainMarker.getPosition().getLongitude(), mainMarker.getPosition().getLatitude());
            Point destination = Point.fromLngLat(touchMarker.getPosition().getLongitude(), touchMarker.getPosition().getLatitude());
            directionManager.drawDirection(origin, destination, directionProfile);
        }
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                buildingPlugin = new BuildingPlugin(mapView, mapboxMap, style);
                buildingPlugin.setMinZoomLevel(15f);
                buildingPlugin.setVisibility(true);

                UiSettings uiSettings = mapboxMap.getUiSettings();
                uiSettings.setCompassMargins(0, 160, 30, 0);
                directionManager = new DirectionManager(mapView, mapboxMap,MapActivity.this);

                mapboxMap.addOnMapLongClickListener(new MapboxMap.OnMapLongClickListener() {
                    @Override
                    public boolean onMapLongClick(@NonNull LatLng point) {

                        if(mainMarker == null){
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.setTitle("Touch position");
                            markerOptions.setSnippet(point.getLatitude() + " : " + point.getLongitude());
                            markerOptions.setPosition(point);
                            mainMarker = mapboxMap.addMarker(markerOptions);
                            searchText.setText("Touch position");
                        }else {

                            if (touchMarker != null) {
                                touchMarker.remove();
                                touchMarker = null;
                            }

                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.setTitle("Touch position");
                            markerOptions.setSnippet(point.getLatitude() + " : " + point.getLongitude());
                            markerOptions.setPosition(point);
                            touchMarker = mapboxMap.addMarker(markerOptions);
                        }
                        DrawDirection();
                        return false;
                    }
                });

                startButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(mainMarker != null){

                            LatLng latLng = new LatLng();
                            latLng.setLatitude(mainMarker.getPosition().getLatitude());
                            latLng.setLongitude(mainMarker.getPosition().getLongitude());
                            PolygonOptions polygonOptions = directionManager.generatePerimeter(latLng, 5, 6);
                            mapboxMap.addPolygon(polygonOptions);

                            List<LatLng> latLngs = directionManager.getLatLangByDistance(latLng, 5, 6);
                            List<LatLng> places = new ArrayList<>();
                            for(LatLng position : latLngs){

                                for(String type : filterTypes){

                                    findPlacesByType(type, position, places, latLng);
                                }
                            }

                        }

                    }
                });
            }
        });
    }


    private void findPlacesByType(String type, LatLng position, List<LatLng> places, LatLng origin){

        @SuppressLint("Range") MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
                .accessToken(getString(R.string.access_token))
                .mode(GeocodingCriteria.MODE_PLACES)
                .proximity(Point.fromLngLat(position.getLongitude(), position.getLatitude()))
                .query(type)
                .geocodingTypes(GeocodingCriteria.TYPE_POI_LANDMARK)
                .limit(1)
                .fuzzyMatch(false)
                .build();


        mapboxGeocoding.enqueueCall(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {

                if(response.isSuccessful()){

                    for (CarmenFeature carmenFeature : response.body().features()){

                        LatLng position = new LatLng();
                        position.setLatitude(carmenFeature.center().latitude());
                        position.setLongitude(carmenFeature.center().longitude());

                        if(!places.contains(position)) {

                            places.add(position);

                            client = MapboxDirections.builder()
                                    .origin(Point.fromLngLat(origin.getLongitude(), origin.getLatitude()))
                                    .destination(Point.fromLngLat(position.getLongitude(), position.getLatitude()))
                                    .overview(DirectionsCriteria.OVERVIEW_FULL)
                                    .profile(directionProfile)
                                    .accessToken(getString(R.string.access_token))
                                    .build();

                            client.enqueueCall(new Callback<DirectionsResponse>() {
                                @Override
                                public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {

                                    if(response.isSuccessful()){

                                        try {
                                            double distance = response.body().routes().get(0).distance();
                                            if(Math.abs(distance - 5000) <= 1000) {

                                                MarkerOptions markerOptions = new MarkerOptions();
                                                markerOptions.setTitle(carmenFeature.placeName());
                                                markerOptions.setPosition(position);
                                                markerOptions.setIcon(drawableToIcon(MapActivity.this, R.drawable.blue_marker));
                                                mapboxMap.addMarker(markerOptions);
                                            }
                                        }catch (NullPointerException e){

                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<DirectionsResponse> call, Throwable t) {

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

    public void initMap(){
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_map);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }




    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);

            if (mapboxMap != null) {
                Style style = mapboxMap.getStyle();
                if (style != null) {
                    GeoJsonSource source = style.getSourceAs(geojsonSourceLayerId);
                    if (source != null) {
                        source.setGeoJson(FeatureCollection.fromFeatures(
                                new Feature[] {Feature.fromJson(selectedCarmenFeature.toJson())}));
                    }

                    LatLng latLng = new LatLng(((Point) Objects.requireNonNull(selectedCarmenFeature.geometry())).latitude(),
                            ((Point) selectedCarmenFeature.geometry()).longitude());
                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(latLng)
                                    .zoom(14)
                                    .build()), 4000);
                    searchText.setText(selectedCarmenFeature.text());
                    MarkerOptions newMark  = new MarkerOptions();
                    newMark.setPosition(latLng);
                    newMark.setTitle(selectedCarmenFeature.text());
                    if(mainMarker != null){
                        mainMarker.remove();
                    }
                    mainMarker = mapboxMap.addMarker(newMark);
                    DrawDirection();

                }
            }
        }
    }

}
