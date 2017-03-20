package com.example.narco.one_click;


import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.narco.one_click.model.GeoRssLocation;
import com.example.narco.one_click.model.GooglePlace;
import com.example.narco.one_click.model.GooglePlaceList;
import com.example.narco.one_click.model.GooglePlacesUtility;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Mapfragment extends SupportMapFragment implements OnMapReadyCallback,GoogleMap.OnMarkerClickListener {
    private static final String LOC = "LOCATION";
    private GeoRssLocation location;
    //private GoogleMap googleMap;
    private GooglePlaceList nearbyGooglePlaceList;
    LinearLayout linearLayout;
    private HashMap<Marker, GooglePlace> nearby;
    Double latitude;
    Double longitude;
    private List<String> interestList;
    private List<Double> latlong;
    List<String> urlList;

    public Mapfragment() {
        super();
        location = new GeoRssLocation();
        // location of Aston University
        location.setLat(52.487144);
        location.setLng(-1.886977);
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
        final String placesKey = this.getResources().getString(R.string.places_key);
        interestList = new ArrayList<>();
        latlong = new ArrayList<>();
        nearbyGooglePlaceList = null;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        //googleMap = getMap();
        //googleMap.setOnMarkerClickListener(this);
        //placePin(item, true);
        //googleMap.setMyLocationEnabled(true);
        //googleMap.getUiSettings().setZoomControlsEnabled(true);
        //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(item.getLocation().getLatLon(), 9));

        if (user != null) {
            DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference().child(user.getUid());
            readData(ref1, new Mapfragment.OnGetDataListener() {
                @Override
                public void onSuccess(DataSnapshot dataSnapshot) {
                    Log.d("ONSUCCESS", "Success");
                    for (DataSnapshot postSnapshot : dataSnapshot.child("interest").getChildren()) {
                        String interests = postSnapshot.getValue(String.class);
                        interestList.add(interests);
                    }
                    for (DataSnapshot postSnapshot : dataSnapshot.child("location").getChildren()) {
                        Double loc = postSnapshot.getValue(Double.class);
                        latlong.add(loc);
                    }
                    dataSnapshot.child("radius").getValue();
                    latitude = latlong.get(0);
                    longitude = latlong.get(1);
                    int radius;
                    if (dataSnapshot.child("radius").exists()) {
                        radius = dataSnapshot.child("radius").getValue(Integer.class);
                    }else{
                        radius=1500;
                    }
                    urlList = generateUrlList(interestList, longitude, latitude, radius, placesKey);
                    PlacesReadFeed process = new PlacesReadFeed();
                    process.execute(urlList);
                }

                @Override
                public void onStart() {
                    //Whatever you want to do on start
                    Log.d("ONSTART", "Started");
                }

                @Override
                public void onFailure() {
                    //Whatever you want to do in case of failure
                    Log.d("ONFAILURE", "Failure");
                }
            });
        }

        this.getMapAsync(this);
        return v;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location.getLatLon(), 9));
    }

    private Marker createMarker(LatLng ll, String title, String description, float hue) {
        Marker marker = googleMap.addMarker(new MarkerOptions().position(ll).icon(BitmapDescriptorFactory.defaultMarker(hue)));
        marker.setTitle(title);
        marker.setSnippet(description);

        return marker;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    private class PlacesReadFeed extends AsyncTask<List<String>, Void, List<GooglePlaceList>> {
        private final ProgressDialog dialog = new ProgressDialog(getActivity());


        @SafeVarargs
        @Override
        protected final List<GooglePlaceList> doInBackground(List<String>... urls) {
            try {
                List<String> urllistia = new ArrayList<>(urls[0]);
                List<GooglePlaceList> result = new ArrayList<>();
                for (int counter = 0; counter < urllistia.size(); counter++) {
                    String input = GooglePlacesUtility.readGooglePlaces(urllistia.get(counter), null);
                    Gson gson = new Gson();
                    GooglePlaceList googlePlaceList = gson.fromJson(input, GooglePlaceList.class);
                    result.add(googlePlaceList);
                }
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("PLACES_EXAMPLE", e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Getting nearbyGooglePlaceList places...");
            this.dialog.show();
        }

        @Override
        protected void onPostExecute(List<GooglePlaceList> places) {
            this.dialog.dismiss();
            reportBack(places);
        }


    }
    protected void reportBack(List<GooglePlaceList> nearbyGooglePlaceList) {
        for (GooglePlaceList gpl :  nearbyGooglePlaceList) {
            if (this.nearbyGooglePlaceList == null) {
                this.nearbyGooglePlaceList = gpl;

            } else {
                this.nearbyGooglePlaceList.getResults().addAll(gpl.getResults());
            }
            for (GooglePlace place : gpl.getResults()) {
                String name = place.getName();
                List<String> types = place.getTypes();
                GooglePlace.Geometry geometry = place.getGeometry();
                if (geometry != null) {
                    GooglePlace.Geometry.Location location = geometry.getLocation();
                    if (location != null) {

                        nearby.put(createMarker(new LatLng(location.getLat(), location.getLng()),
                                types.toString(), name, BitmapDescriptorFactory.HUE_BLUE),
                                place);
                    }
                }
            }
        }
    }

    public List<String> generateUrlList(List<String> interestList, Double longitude, Double latitude, int radius, String placesKey) {
        List<String> urlList = new ArrayList<>();
        String url;
        for (String interest : interestList) {
            switch (interest) {
                case "Art":
                    url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latitude + "," + longitude + "&radius=" + radius + "&type=art_gallery|movie_theater" + "&key=" + placesKey;
                    urlList.add(url);
                    break;
                case "Animals":
                    url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latitude + "," + longitude + "&radius=" + radius + "&type=zoo" + "&key=" + placesKey;
                    urlList.add(url);
                    break;
                case "Books":
                    url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latitude + "," + longitude + "&radius=" + radius + "&type=library|book_store" + "&key=" + placesKey;
                    urlList.add(url);
                    break;
                case "Food":
                    url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latitude + "," + longitude + "&radius=" + radius + "&type=restaurant|bakery" + "&key=" + placesKey;
                    urlList.add(url);
                    break;
                case "History":
                    url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latitude + "," + longitude + "&radius=" + radius + "&type=museum" + "&key=" + placesKey;
                    urlList.add(url);
                    break;
                case "Music":
                    url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latitude + "," + longitude + "&radius=" + radius + "&type=bar|night_club" + "&key=" + placesKey;
                    urlList.add(url);
                    break;
                case "Nature":
                    url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latitude + "," + longitude + "&radius=" + radius + "&type=campground|park|rv_park" + "&key=" + placesKey;
                    urlList.add(url);
                    break;
                case "Shopping":
                    url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latitude + "," + longitude + "&radius=" + radius + "&type=shopping_mall|clothing_store" + "&key=" + placesKey;
                    urlList.add(url);
                    break;
                case "Sport":
                    url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latitude + "," + longitude + "&radius=" + radius + "&type=bowling_alley|stadium" + "&key=" + placesKey;
                    urlList.add(url);
                    break;
            }
        }
        return urlList;
    }

    public void readData(DatabaseReference ref, final Mapfragment.OnGetDataListener listener) {
        listener.onStart();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listener.onSuccess(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onFailure();
            }
        });

    }

    interface OnGetDataListener {
        //make new interface for call back
        void onSuccess(DataSnapshot dataSnapshot);

        void onStart();

        void onFailure();
    }

}
