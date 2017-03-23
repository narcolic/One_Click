package com.example.narco.one_click;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.narco.one_click.model.GeoRssLocation;
import com.example.narco.one_click.model.GooglePlace;
import com.example.narco.one_click.model.GooglePlaceList;
import com.example.narco.one_click.model.GooglePlacesUtility;
import com.example.narco.one_click.model.MyItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.List;


public class Mapfragment extends SupportMapFragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMyLocationButtonClickListener {
    private GeoRssLocation location;
    private GooglePlaceList nearbyGooglePlaceList;
    Double latitude;
    Double longitude;
    private List<String> interestList;
    private List<Double> latlong;
    List<String> urlList;
    Location userLocation;
    private static GoogleMap mMap;
    FirebaseUser user;
    String placesKey;
    List<MyItem> markers = new ArrayList<>();
    private ClusterManager<MyItem> mClusterManager;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;



    public Mapfragment() {
        super();
        location = new GeoRssLocation();
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
        placesKey = this.getResources().getString(R.string.places_key);
        interestList = new ArrayList<>();
        latlong = new ArrayList<>();
        userLocation = new Location("");
        nearbyGooglePlaceList = null;
        user = FirebaseAuth.getInstance().getCurrentUser();
        this.getMapAsync(this);
        return v;
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
        mClusterManager = new ClusterManager<>(getContext(), mMap);
        UiSettings UiSettings = googleMap.getUiSettings();
        UiSettings.setZoomControlsEnabled(true);
        ImageView northIc = new ImageView(getActivity());
        ImageView southIc = new ImageView(getActivity());
        ImageView eastIc = new ImageView(getActivity());
        ImageView westIc = new ImageView(getActivity());
        if (user != null) {
            DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference().child(user.getUid());
            readData(ref1, new Mapfragment.OnGetDataListener() {
                @Override
                public void onSuccess(DataSnapshot dataSnapshot) {
                    Log.d("ONSUCCESS", "Success");
                    if (dataSnapshot.child("location").exists()) {
                        location.setLat((Double) dataSnapshot.child("location").child("0").getValue());
                        location.setLng((Double) dataSnapshot.child("location").child("1").getValue());
                        googleMap.getUiSettings().setZoomControlsEnabled(true);
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location.getLatLon(), 17));
                        createMarker();
                    }
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
        //calculate the angle on off-screen markers
        LatLngBounds currentScreen = mMap.getProjection().getVisibleRegion().latLngBounds;
        for (MyItem marker : markers) {
            if (currentScreen.contains(marker.getPosition())) {
                // marker inside visible region
                Log.i("MARKER", "On screen");
            } else {// marker outside visible region
                LatLng c = currentScreen.getCenter();
                double angle = angleFromLatLng(c.latitude,c.longitude,marker.getPosition().latitude,marker.getPosition().longitude);
                //0=north, 90=right, etc.
                if (angle>45 && angle<135){
                //Marker is on right of screen
                }
                else if (angle>136 && angle<225){
                    //Marker on bottom of screen
                }else if (angle>226 && angle<315){
                    //Marker on left of screen
                }else{
                    //Marker on top of screen
                }

            }
        }

        mMap.setOnMarkerClickListener(mClusterManager);
        mClusterManager.setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<MyItem>() {
            @Override
            public void onClusterItemInfoWindowClick(MyItem item) {
                Log.i("CHECK", item.getTitle());
                Intent i = new Intent(getActivity(), PlaceDetailActivity.class);
                i.putExtra("PLACE", item.getmGooglePlace());

                startActivity(i);
            }
        });
        mMap.setOnInfoWindowClickListener(mClusterManager);
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(getActivity(), LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        //Toast.makeText(getActivity(), "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            boolean mPermissionDenied = true;
        }
    }



    private double angleFromLatLng(double lat1, double long1, double lat2, double long2) {

        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;
        brng = 360 - brng;

        return brng;
    }

    private void createMarker() {

        if (user != null) {
            DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference().child(user.getUid());
            readData(ref1, new Mapfragment.OnGetDataListener() {
                @Override
                public void onSuccess(DataSnapshot dataSnapshot) {

                    for (DataSnapshot postSnapshot : dataSnapshot.child("interest").getChildren()) {
                        String interests = postSnapshot.getValue(String.class);
                        interestList.add(interests);
                    }
                    for (DataSnapshot postSnapshot : dataSnapshot.child("location").getChildren()) {
                        Double loc = postSnapshot.getValue(Double.class);
                        latlong.add(loc);
                    }

                    latitude = (Double) dataSnapshot.child("location").child("0").getValue();
                    longitude = (Double) dataSnapshot.child("location").child("1").getValue();
                    int radius;
                    if (dataSnapshot.child("radius").exists()) {
                        radius = dataSnapshot.child("radius").getValue(Integer.class);
                    } else {
                        radius = 1500;
                    }
                    urlList = generateUrlList(interestList, longitude, latitude, radius, placesKey);
                    PlacesReadFeed process = new PlacesReadFeed();
                    process.execute(urlList);
                }

                @Override
                public void onStart() {
                    //Whatever you want to do on start
                    Log.d("ONSTART", "Started2");
                }

                @Override
                public void onFailure() {
                    //Whatever you want to do in case of failure
                    Log.d("ONFAILURE", "Failure2");
                }
            });
        }
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
                Log.d("ONSUCCESS1", "" + urllistia.size());
                Log.d("ONSUCCESS1", "" + urllistia.get(0));
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
            this.dialog.setMessage("Loading nearby places...");
            this.dialog.show();
        }

        @Override
        protected void onPostExecute(List<GooglePlaceList> googlePlaceList) {
            this.dialog.dismiss();
            reportBack(googlePlaceList);
        }
    }

    protected void reportBack(List<GooglePlaceList> nearbyGooglePlaceList) {
        for (GooglePlaceList gpl : nearbyGooglePlaceList) {
            if (this.nearbyGooglePlaceList == null) {
                this.nearbyGooglePlaceList = gpl;

            } else {
                this.nearbyGooglePlaceList.getResults().addAll(gpl.getResults());
            }

            for (final GooglePlace place : gpl.getResults()) {
                final String name = place.getName();

                //List<String> types = place.getTypes();
                String address = place.getVicinity();
                GooglePlace.Geometry geometry = place.getGeometry();
                if (geometry != null) {
                    GooglePlace.Geometry.Location location = geometry.getLocation();
                    if (location != null) {
                        MyItem infoWindowItem = new MyItem(location.getLat(), location.getLng(), name, address,place);
                        Log.i("CHECK", infoWindowItem.getTitle());

                        mClusterManager.addItem(infoWindowItem);



                        markers.add(infoWindowItem);
                        /*mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(location.getLat(), location.getLng()))
                                .title(name)
                                .snippet(address)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));*/
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
