package com.example.narco.one_click;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.narco.one_click.model.GooglePlace;
import com.example.narco.one_click.model.GooglePlacesUtility;
import com.example.narco.one_click.model.PlaceDetail;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.koushikdutta.ion.Ion;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;


public class PlaceDetailActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GooglePlace place;
    private Context context;
    private Button takePictureButton;
    private Button favoriteButton;
    private Button goButton;
    Double longitude;
    Double latitude;
    private List<Double> latlong;
    private List<String> favoriteIDList;
    private List<String> favoriteKeyList;
    private Boolean isFavorite = false;
    FirebaseUser user;
    private String placeUrlBuyReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.poi_details);
        this.context = getApplicationContext();

        takePictureButton = (Button) findViewById(R.id.postcard_button);
        favoriteButton = (Button) findViewById(R.id.favorite_button);
        goButton = (Button) findViewById(R.id.go_there);

        user = FirebaseAuth.getInstance().getCurrentUser();
        favoriteIDList = new ArrayList<>();
        favoriteKeyList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            takePictureButton.setEnabled(false);
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

        Log.i("PLACE EXAMPLE", "Place Detail Activity");
        place = (GooglePlace) this.getIntent().getExtras().getSerializable("PLACE");
        Log.i("PLACE EXAMPLE", "got place " + place.toString());
        //this.setHasOptionsMenu(true);
        setTitle(place.getName());
        String placesKey = getResources().getString(R.string.places_key);
        longitude = place.getGeometry().getLocation().getLng();
        latitude = place.getGeometry().getLocation().getLat();
        placeUrlBuyReference = "https://maps.googleapis.com/maps/api/place/details/json?" +
                "key=" + placesKey + "&reference=" + place.getReference();
        PlacesDetailReadFeed detailTask = new PlacesDetailReadFeed();
        detailTask.execute(new String[]{placeUrlBuyReference});


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                takePictureButton.setEnabled(true);
            }
        }
    }

    public void takePicture(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri file = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", getOutputMediaFile());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, file);

        startActivityForResult(intent, 100);
    }

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "One_Click_Pics");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("Camera", "failed to create directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Pictured saved on My Postcards!", Toast.LENGTH_LONG).show();
            }
        }
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

    private void fillInLayout(final GooglePlace place) {

        if (user != null) {
            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(user.getUid());
            readData(reference, new PlaceDetailActivity.OnGetDataListener() {
                @Override
                public void onSuccess(final DataSnapshot dataSnapshot) {
                    Log.d("ONSUCCESS", "Success");
                    //Favorites Button
                    int favoritePositionOnList = 0;
                    for (DataSnapshot postSnapshot : dataSnapshot.child("favorites").getChildren()) {
                        String favoritesID = postSnapshot.child("1").getValue(String.class);
                        favoriteIDList.add(favoritesID);
                        String favoritesKey = postSnapshot.getKey();
                        favoriteKeyList.add(favoritesKey);
                    }
                    final List<String> placeRequestAndPlaceID = new ArrayList<String>();
                    placeRequestAndPlaceID.add(placeUrlBuyReference);
                    placeRequestAndPlaceID.add(place.getPlace_id());
                    //Check if place already in favorite list fill heart button
                    int index = 0;
                    for (String placeId : favoriteIDList) {
                        if (Objects.equals(placeId, place.getPlace_id())) {
                            favoriteButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fav_full, 0, 0, 0);
                            isFavorite = true;
                            favoritePositionOnList = index;
                        }
                        index++;
                    }
                    final int finalFavoriteKey = favoritePositionOnList;
                    favoriteButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            if (!isFavorite) {
                                reference.child("favorites").push().setValue(placeRequestAndPlaceID);
                                favoriteButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fav_full, 0, 0, 0);
                                isFavorite = true;
                            } else {
                                reference.child("favorites").child(favoriteKeyList.get(finalFavoriteKey)).removeValue();
                                favoriteButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fav, 0, 0, 0);
                                isFavorite = false;
                            }
                        }
                    });


                    // Title
                    TextView title = (TextView) findViewById(R.id.name);
                    String upToNCharacters = place.getName().substring(0, Math.min(place.getName().length(), 25));
                    title.setText("  " + upToNCharacters);
                    //Rating
                    TextView rating = (TextView) findViewById(R.id.rating);
                    if (place.getRating() != 0.0f) {
                        float floatRating = place.getRating();
                        String textRating = Float.toString(floatRating);
                        rating.setText("User Rating: " + textRating);
                    } else {
                        rating.setText("User Rating Not Available.");
                    }
                    //Open Hours
                    TextView openhrs = (TextView) findViewById(R.id.openhrs);
                    if (place.getOpening_hours() != null) {
                        if (Objects.equals(place.getOpening_hours().getOpen_now(), "true")) {
                            openhrs.setText("Availability: Open Now!");
                        } else {
                            openhrs.setText("Availability: Closed now..");
                        }
                    } else {
                        openhrs.setText("Availability: Unknown..");
                    }
                    //Images
                    if (!place.getPhotos().isEmpty()) {
                        if (!place.getPhotos().get(0).getPhoto_reference().isEmpty()) {
                            ImageView image1 = (ImageView) findViewById(R.id.i1);
                            Ion.with(image1)
                                    .placeholder(R.drawable.ic_postcard_new)
                                    .load("https://maps.googleapis.com/maps/api/place/photo?maxwidth=500&photoreference=" + place.getPhotos().get(0).getPhoto_reference() + "&sensor=false&key=AIzaSyD79S9Un0Ti8tDT_el4ko7ItRJz3KapOLA");
                            image1.setScaleType(ImageView.ScaleType.FIT_XY);
                            image1.setBackgroundResource(R.drawable.shadow);
                            image1.setMinimumHeight(450);
                            image1.setMinimumWidth(600);
                        }
                        if (!place.getPhotos().get(1).getPhoto_reference().isEmpty()) {
                            ImageView image2 = (ImageView) findViewById(R.id.i2);
                            Ion.with(image2)
                                    .placeholder(R.drawable.ic_postcard_new)
                                    .load("https://maps.googleapis.com/maps/api/place/photo?maxwidth=500&photoreference=" + place.getPhotos().get(1).getPhoto_reference() + "&sensor=false&key=AIzaSyD79S9Un0Ti8tDT_el4ko7ItRJz3KapOLA");
                            image2.setScaleType(ImageView.ScaleType.FIT_XY);
                            image2.setBackgroundResource(R.drawable.shadow);
                            image2.setMinimumHeight(450);
                            image2.setMinimumWidth(600);
                        }
                        if (!place.getPhotos().get(2).getPhoto_reference().isEmpty()) {
                            ImageView image3 = (ImageView) findViewById(R.id.i3);
                            Ion.with(image3)
                                    .placeholder(R.drawable.ic_postcard_new)
                                    .load("https://maps.googleapis.com/maps/api/place/photo?maxwidth=500&photoreference=" + place.getPhotos().get(2).getPhoto_reference() + "&sensor=false&key=AIzaSyD79S9Un0Ti8tDT_el4ko7ItRJz3KapOLA");
                            image3.setScaleType(ImageView.ScaleType.FIT_XY);
                            image3.setBackgroundResource(R.drawable.shadow);
                            image3.setMinimumHeight(450);
                            image3.setMinimumWidth(600);
                        }
                    }

                    //Go Button
                    final Double userLatitude;
                    final Double userLongitude;
                    userLatitude = (Double) dataSnapshot.child("location").child("0").getValue();
                    userLongitude = (Double) dataSnapshot.child("location").child("1").getValue();

                    goButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                    Uri.parse("http://maps.google.com/maps?saddr="+userLatitude+","+userLongitude+"&daddr="+latitude+","+longitude));
                            startActivity(intent);
                        }
                    });

                    //Map
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_on_details);
                    mapFragment.getMapAsync(PlaceDetailActivity.this);
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

    }


    public void readData(DatabaseReference ref, final PlaceDetailActivity.OnGetDataListener listener) {
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
