package com.example.narco.one_click;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.narco.one_click.model.GooglePlace;
import com.example.narco.one_click.model.GooglePlaceList;
import com.example.narco.one_click.model.GooglePlacesUtility;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.List;

public class Suggestionsfragment extends Fragment implements AdapterView.OnItemClickListener {

    private GooglePlaceList nearby;
    LinearLayout linearLayout;
    Double latitude;
    Double longitude;
    private List<String> interestList;
    private List<Double> latlong;
    List<String> urlList;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.suggestions, container, false);

        //Get Current location
        Intent i = new Intent(getActivity(), LocationTrackerActivity.class);
        getActivity().startActivity(i);

        String placesKey = this.getResources().getString(R.string.places_key);
        interestList = new ArrayList<>();
        latlong = new ArrayList<>();
        nearby = null;
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        linearLayout = (LinearLayout) v.findViewById(R.id.main_layout);
        FirebaseUser user = firebaseAuth.getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        if (user != null) {

            DatabaseReference ref1 = databaseReference.child(user.getUid()).child("interest");
            readData(ref1, new OnGetDataListener() {
                @Override
                public void onSuccess(DataSnapshot dataSnapshot) {
                    Log.d("ONSUCCESS", "Success");
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        String interests = postSnapshot.getValue(String.class);
                        interestList.add(interests);
                    }
                }
                @Override
                public void onStart() {
                    //whatever you need to do onStart
                    Log.d("ONSTART", "Started");
                }

                @Override
                public void onFailure() {
                    Log.d("ONFAILURE", "Failure");
                }
            });
            databaseReference.child(user.getUid());
            databaseReference.child("location");
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d("ONDATACHANGED", "listener2 entered");
                    try {
                        if (dataSnapshot.getValue() != null) {
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                Double loc = postSnapshot.getValue(Double.class);
                                latlong.add(loc);
                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        latitude = latlong.get(0);
        longitude = latlong.get(1);

        int radius = 500;
        urlList = generateUrlList(interestList, longitude, latitude, radius, placesKey);
        if (placesKey.equals("PUT YOUR KEY HERE"))
            Toast.makeText(getActivity(), "You haven't entered your Google Places Key into the strings file.  Dont forget to set a referer too.", Toast.LENGTH_LONG).show();
        else {
            PlacesReadFeed process = new PlacesReadFeed();
            process.execute(urlList);

        }

        return v;
    }

    protected void reportBack(List<GooglePlaceList> nearby) {

        for (GooglePlaceList gpl : nearby) {

            if (this.nearby == null) {
                this.nearby = gpl;

            } else {
                this.nearby.getResults().addAll(gpl.getResults());
            }

            LinearLayout ll = new LinearLayout(getActivity());
            TextView boxTitle = new TextView(getActivity());
            HorizontalScrollView hsv = new HorizontalScrollView(getActivity());
            RelativeLayout relativeLayout = new RelativeLayout(getActivity());

            ll.setOrientation(LinearLayout.VERTICAL);
            boxTitle.setText(" Interest");
            ll.addView(boxTitle);

            ll.setBackgroundResource(R.drawable.bg_round_rect);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(10, 20, 10, 20);

            HorizontalScrollView.LayoutParams hsvparams = new HorizontalScrollView.LayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 450));

            boxTitle.setTextColor(Color.WHITE);
            boxTitle.setTextSize(25);
            ll.setBackgroundResource(R.drawable.bg_round_rect);


            for (int i = 0; i < gpl.getResults().size(); i++) {
                ImageView imageView = new ImageView(getActivity());
                Ion.with(imageView)
                        .placeholder(R.drawable.ic_postcard)
                        .load(gpl.getPhotoUrl().get(i));
                imageView.setId(i);
                imageView.setPadding(2, 2, 2, 2);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setBackgroundResource(R.drawable.shadow);
                imageView.setMinimumHeight(450);
                imageView.setMinimumWidth(600);
                imageView.isClickable();
                final GooglePlace place = gpl.getResults().get(i);
                imageView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        Intent ok = new Intent(getActivity(), PlaceDetailActivity.class);
                        ok.putExtra("PLACE", place);
                        startActivity(ok);
                    }
                });
                TextView textView = new TextView(getActivity());
                String upToNCharacters = place.getName().substring(0, Math.min(place.getName().length(), 17));
                textView.setText("  " + upToNCharacters);
                textView.setTextColor(Color.WHITE);

                if (i == 0) {
                    RelativeLayout.LayoutParams rlp1 = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
                    RelativeLayout.LayoutParams rlp2 = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
                    rlp2.addRule(RelativeLayout.ALIGN_BOTTOM, imageView.getId());
                    relativeLayout.addView(imageView, rlp1);
                    relativeLayout.addView(textView, rlp2);
                } else {
                    RelativeLayout.LayoutParams rlp1 = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
                    RelativeLayout.LayoutParams rlp2 = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
                    rlp1.addRule(RelativeLayout.RIGHT_OF, imageView.getId() - 1);
                    rlp2.addRule(RelativeLayout.RIGHT_OF, imageView.getId() - 1);
                    rlp2.addRule(RelativeLayout.ALIGN_BOTTOM, imageView.getId() - 1);
                    relativeLayout.addView(imageView, rlp1);
                    relativeLayout.addView(textView, rlp2);
                }
            }
            hsv.addView(relativeLayout);
            ll.addView(hsv, hsvparams);
            linearLayout.addView(ll, layoutParams);
        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.i("PLACE EXAMPLE", "Clicked " + parent.getItemAtPosition(position));
        GooglePlace place = nearby.getResults().get(position);
        Log.i("PLACE EXAMPLE", "Place is " + place.toString());
        Intent i = new Intent(this.getActivity(), PlaceDetailActivity.class);
        i.putExtra("PLACE", place);
        startActivity(i);
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
                    GooglePlaceList places = gson.fromJson(input, GooglePlaceList.class);
                    result.add(places);
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
            this.dialog.setMessage("Getting nearby places...");
            this.dialog.show();
        }

        @Override
        protected void onPostExecute(List<GooglePlaceList> places) {
            this.dialog.dismiss();
            reportBack(places);
        }


    }

    public List<String> generateUrlList(List<String> interestList, Double longitude, Double latitude, int radius, String placesKey) {
        List<String> urlList = new ArrayList<>();
        String url;
        for (String interest : interestList) {
            switch (interest) {
                case "Art":
                    url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latitude + "," + longitude + "&radius=" + radius + "&type=art_gallery" + "&key=" + placesKey;
                    urlList.add(url);
                    break;
                case "Animals":
                    url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latitude + "," + longitude + "&radius=" + radius + "&type=zoo" + "&key=" + placesKey;
                    urlList.add(url);
                    break;
                case "Books":
                    url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latitude + "," + longitude + "&radius=" + radius + "&type=library" + "&key=" + placesKey;
                    urlList.add(url);
                    break;
                case "Food":
                    url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latitude + "," + longitude + "&radius=" + radius + "&type=restaurant" + "&key=" + placesKey;
                    urlList.add(url);
                    break;
                case "History":
                    url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latitude + "," + longitude + "&radius=" + radius + "&type=museum" + "&key=" + placesKey;
                    urlList.add(url);
                    break;
                case "Music":
                    url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latitude + "," + longitude + "&radius=" + radius + "&type=bar" + "&key=" + placesKey;
                    urlList.add(url);
                    break;
                case "Nature":
                    url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latitude + "," + longitude + "&radius=" + radius + "&type=campground" + "&key=" + placesKey;
                    urlList.add(url);
                    break;
                case "Shopping":
                    url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latitude + "," + longitude + "&radius=" + radius + "&type=shopping_mall" + "&key=" + placesKey;
                    urlList.add(url);
                    break;
                case "Sport":
                    url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latitude + "," + longitude + "&radius=" + radius + "&type=bowling_alley" + "&key=" + placesKey;
                    urlList.add(url);
                    break;
            }
        }
        return urlList;
    }

    public void readData(DatabaseReference ref, final OnGetDataListener listener) {
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
