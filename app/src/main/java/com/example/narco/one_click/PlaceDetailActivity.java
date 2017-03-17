package com.example.narco.one_click;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.narco.one_click.model.GooglePlace;
import com.example.narco.one_click.model.GooglePlacesUtility;
import com.example.narco.one_click.model.PlaceDetail;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.koushikdutta.ion.Ion;

import java.util.List;
import java.util.Objects;


public class PlaceDetailActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GooglePlace place;
    private Context context;
    Double longitude;
    Double latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.poi_details);
        this.context = getApplicationContext();

        Log.i("PLACE EXAMPLE", "Place Detail Activity");
        place = (GooglePlace) this.getIntent().getExtras().getSerializable("PLACE");
        Log.i("PLACE EXAMPLE", "got place " + place.toString());
        //this.setHasOptionsMenu(true);
        setTitle(place.getName());
        String placesKey = getResources().getString(R.string.places_key);
        longitude=place.getGeometry().getLocation().getLng();
        latitude=place.getGeometry().getLocation().getLat();
        String placesRequest = "https://maps.googleapis.com/maps/api/place/details/json?" +
                "key=" + placesKey + "&reference=" + place.getReference();
        PlacesDetailReadFeed detailTask = new PlacesDetailReadFeed();
        detailTask.execute(new String[]{placesRequest});
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng latLng = new LatLng(latitude, longitude);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
    }

    private class PlacesDetailReadFeed extends AsyncTask<String, Void, PlaceDetail> {
        private final ProgressDialog dialog = new ProgressDialog(PlaceDetailActivity.this);

        @Override
        protected PlaceDetail doInBackground(String... urls) {
            try {
                //dialog.setMessage("Fetching Places Data");
                String referer = null;
                //dialog.setMessage("Fetching Places Data");
                if (urls.length == 1) {
                    referer = null;
                } else {
                    referer = urls[1];
                }
                String input = GooglePlacesUtility.readGooglePlaces(urls[0], referer);
                Gson gson = new Gson();
                PlaceDetail place = gson.fromJson(input, PlaceDetail.class);
                Log.i("PLACES EXAMPLE", "Place found is " + place.toString());
                return place;
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("PLACES EXAMPLE", e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Getting nearby places...");
            this.dialog.show();

        }

        @Override
        protected void onPostExecute(PlaceDetail placeDetail) {
            place = placeDetail.getResult();
            fillInLayout(place);
            this.dialog.dismiss();
        }
    }

    private void fillInLayout(GooglePlace place) {
        // Title
        TextView title = (TextView) findViewById(R.id.name);
        title.setText(place.getName());
        //Rating
        float floatRating = place.getRating();
        String textRating = Float.toString(floatRating);
        TextView rating = (TextView) findViewById(R.id.rating);
        rating.setText("User Rating: " + textRating);
        //Open Hours
        TextView openhrs = (TextView) findViewById(R.id.openhrs);
        if (Objects.equals(place.getOpening_hours().getOpen_now(), "true")) {
            openhrs.setText("Availability: Open Now!");
        }else {
            openhrs.setText("Availability: Closed now..");
        }
        //Images
        if(!place.getPhotos().get(0).getPhoto_reference().isEmpty()){
            ImageView image1 = (ImageView) findViewById(R.id.i1);
            Ion.with(image1)
                    .placeholder(R.drawable.ic_postcard_new)
                    .load("https://maps.googleapis.com/maps/api/place/photo?maxwidth=500&photoreference=" + place.getPhotos().get(0).getPhoto_reference() + "&sensor=false&key=AIzaSyD79S9Un0Ti8tDT_el4ko7ItRJz3KapOLA");
            image1.setScaleType(ImageView.ScaleType.FIT_XY);
            image1.setBackgroundResource(R.drawable.shadow);
            image1.setMinimumHeight(100);
            image1.setMinimumWidth(150);
        }
        if(!place.getPhotos().get(1).getPhoto_reference().isEmpty()){
            ImageView image2 = (ImageView) findViewById(R.id.i2);
            Ion.with(image2)
                    .placeholder(R.drawable.ic_postcard_new)
                    .load("https://maps.googleapis.com/maps/api/place/photo?maxwidth=500&photoreference=" + place.getPhotos().get(1).getPhoto_reference() + "&sensor=false&key=AIzaSyD79S9Un0Ti8tDT_el4ko7ItRJz3KapOLA");
            image2.setScaleType(ImageView.ScaleType.FIT_XY);
            image2.setBackgroundResource(R.drawable.shadow);
            image2.setMinimumHeight(100);
            image2.setMinimumWidth(150);
        }
        if(!place.getPhotos().get(2).getPhoto_reference().isEmpty()){
            ImageView image3 = (ImageView) findViewById(R.id.i3);
            Ion.with(image3)
                    .placeholder(R.drawable.ic_postcard_new)
                    .load("https://maps.googleapis.com/maps/api/place/photo?maxwidth=500&photoreference=" + place.getPhotos().get(2).getPhoto_reference() + "&sensor=false&key=AIzaSyD79S9Un0Ti8tDT_el4ko7ItRJz3KapOLA");
            image3.setScaleType(ImageView.ScaleType.FIT_XY);
            image3.setBackgroundResource(R.drawable.shadow);
            image3.setMinimumHeight(100);
            image3.setMinimumWidth(150);
        }

        //Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_on_details);
        mapFragment.getMapAsync(this);
        //Review
        TextView reviews = (TextView) findViewById(R.id.userreviews);
        List<GooglePlace.Review> reviewsData = place.getReviews();
        if (reviewsData != null) {
            StringBuilder sb = new StringBuilder();
            for (GooglePlace.Review r : reviewsData) {
                sb.append(r.getAuthor_name());
                sb.append(" : \"");
                sb.append(r.getText());
                sb.append("\n\n");
            }
            String reviewText = sb.toString();
            reviews.setText(reviewText);
        } else {
            reviews.setText("There have not been any reviews!");
        }
        Log.i("PLACES EXAMPLE", "Setting rating to: " + reviews.getText());
    }

}
