package com.szczepaniak.fasttravel;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;

import com.google.android.gms.maps.model.LatLng;
import com.szczepaniak.fasttravel.Adapters.PlaceTipsAdapter;

import java.util.List;

public class AutoTipsManager {

    private AutoCompleteTextView text;
    private Context context;


    public AutoTipsManager(final AutoCompleteTextView text, Context context) {
        this.text = text;
        this.context = context;
        text.setAdapter(new PlaceTipsAdapter(context,android.R.layout.simple_list_item_1));

        text.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                LatLng latLng=getLatLngFromAddress(text.getText().toString());
                if(latLng!=null) {
                    Address address = getAddressFromLatLng(latLng);
                    text.setText(address.toString());
                }
            }
        });

    }

    private LatLng getLatLngFromAddress(String address){

        Geocoder geocoder=new Geocoder(context);
        List<Address> addressList;

        try {
            addressList = geocoder.getFromLocationName(address, 1);
            if(addressList!=null){
                Address singleaddress=addressList.get(0);
                LatLng latLng=new LatLng(singleaddress.getLatitude(),singleaddress.getLongitude());
                return latLng;
            }
            else{
                return null;
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }

    private Address getAddressFromLatLng(LatLng latLng){
        Geocoder geocoder=new Geocoder(context);
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 5);
            if(addresses!=null){
                Address address=addresses.get(0);
                return address;
            }
            else{
                return null;
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }
}
