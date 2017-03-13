package com.example.narco.one_click;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.narco.one_click.model.GeoRssLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;


public class Mapfragment extends SupportMapFragment implements OnMapReadyCallback {
    private static final String LOC = "LOCATION";
    private GeoRssLocation location;

    public Mapfragment() {
        super();
        location = new GeoRssLocation();
        // location of Aston University
        location.setLat(52.487144);
        location.setLng(-1.886977);
    }

    public static Mapfragment newInstance() {
        Mapfragment mf = new Mapfragment();
        return mf;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Map showing location");
        this.setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, parent, savedInstanceState);
        /*
        View v = inflater.inflate(R.layout.content_main, parent, false);
        TextView title = (TextView) v.findViewById(R.id.text);
        title.setText(location.toString());
        */
        /*
        GoogleMap googleMap = this.getMap();
        //googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location.getLatLon(), 9));
        */
        this.getMapAsync(this);
        /* */
        return v;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location.getLatLon(), 9));
    }
}
