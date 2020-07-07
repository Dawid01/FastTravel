package com.szczepaniak.fasttravel;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
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


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;



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
                if(mainMarker != null){
                    mainMarker.remove();
                    mainMarker = null;
                }
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
            }
        });

        bikeBtm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                walkBtm.setBackground(getResources().getDrawable(R.drawable.small_icon_type_bg));
                bikeBtm.setBackground(getResources().getDrawable(R.drawable.small_icon_selected));
                carBtm.setBackground(getResources().getDrawable(R.drawable.small_icon_type_bg));
                directionProfile = DirectionsCriteria.PROFILE_CYCLING;
            }
        });

        carBtm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                walkBtm.setBackground(getResources().getDrawable(R.drawable.small_icon_type_bg));
                bikeBtm.setBackground(getResources().getDrawable(R.drawable.small_icon_type_bg));
                carBtm.setBackground(getResources().getDrawable(R.drawable.small_icon_selected));
                directionProfile = DirectionsCriteria.PROFILE_DRIVING;
            }
        });

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

                        if(touchMarker != null){
                            touchMarker.remove();
                            touchMarker = null;
                        }

                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.setTitle("Touch position \n" + point.getLatitude() + " : " + point.getLongitude());
                        markerOptions.setPosition(point);
                        touchMarker = mapboxMap.addMarker(markerOptions);

                        if(mainMarker != null){

                            Point origin = Point.fromLngLat(mainMarker.getPosition().getLongitude(), mainMarker.getPosition().getLatitude());
                            Point destination = Point.fromLngLat(point.getLongitude(), point.getLatitude());
                            directionManager.DrawDirection(origin, destination, directionProfile);
                        }
                        return false;
                    }
                });
            }
        });
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


                }
            }
        }
    }

}
