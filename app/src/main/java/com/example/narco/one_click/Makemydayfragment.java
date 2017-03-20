package com.example.narco.one_click;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;
import com.baoyz.widget.SmartisanDrawable;
import com.example.narco.one_click.model.GooglePlace;
import com.example.narco.one_click.model.GooglePlaceListSingleItem;
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
import java.util.Objects;

public class Makemydayfragment extends Fragment {

    private Location userLocation;
    private List<Location> favoriteLocations;
    protected List<Location> locationsSortedByDistance;
    private List<String> favoriteKeyList;
    private List<String> favoriteRefList;
    private List<GooglePlace> placeList;
    protected List<GooglePlace> placeListSorted;
    private List<Float> distanceOnSortedPlaces;
    private List<String> isViewedList;
    PullRefreshLayout layout;
    int radius;
    DatabaseReference reference;
    LinearLayout linearLayout;
    private GooglePlaceListSingleItem nearby;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.makemyday, container, false);

        linearLayout = (LinearLayout) v.findViewById(R.id.main_layout);
        userLocation = new Location("");
        locationsSortedByDistance = new ArrayList<>();
        favoriteRefList = new ArrayList<>();
        placeList = new ArrayList<>();
        favoriteLocations = new ArrayList<>();
        placeListSorted = new ArrayList<>();
        favoriteKeyList = new ArrayList<>();
        distanceOnSortedPlaces = new ArrayList<>();
        isViewedList = new ArrayList<>();
        nearby = null;
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        layout = (PullRefreshLayout) v.findViewById(R.id.swipeRefreshLayout);
        layout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                layout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        layout.setRefreshing(false);
                    }
                }, 3000);
            }
        });
        layout.setColorSchemeColors(Color.GRAY);
        layout.setRefreshDrawable(new SmartisanDrawable(getActivity(), layout));

        if (user != null) {
            reference = FirebaseDatabase.getInstance().getReference().child(user.getUid());
            readData(reference, new Makemydayfragment.OnGetDataListener() {
                @Override
                public void onSuccess(DataSnapshot dataSnapshot) {
                    Log.d("ONSUCCESS", "Success");
                    //Get User Current Location
                    for (DataSnapshot postSnapshot : dataSnapshot.child("location").getChildren()) {
                        Double loc = postSnapshot.getValue(Double.class);
                        userLocation.setLatitude(loc);
                    }
                    userLocation.setLatitude((Double) dataSnapshot.child("location").child("0").getValue());
                    userLocation.setLongitude((Double) dataSnapshot.child("location").child("1").getValue());
                    Log.e("USER LOC", "" + userLocation.getLatitude());

                    //Get Radius
                    if (dataSnapshot.child("radius").exists()) {
                        radius = dataSnapshot.child("radius").getValue(Integer.class);
                    }
                    //Get Favorite items Ref ID
                    for (DataSnapshot postSnapshot : dataSnapshot.child("favorites").getChildren()) {
                        String favoritesID = postSnapshot.child("0").getValue(String.class);
                        favoriteRefList.add(favoritesID);
                        String isViewed = postSnapshot.child("2").getValue(String.class);
                        isViewedList.add(isViewed);
                        String favoritesKey = postSnapshot.getKey();
                        favoriteKeyList.add(favoritesKey);
                    }

                    //RefID to Google Place
                    if (!favoriteRefList.isEmpty()) {
                        Makemydayfragment.PlacesReadFeed process = new Makemydayfragment.PlacesReadFeed();
                        process.execute(favoriteRefList);
                    }
                }

                @Override
                public void onStart() {
                }

                @Override
                public void onFailure() {
                }
            });
        }
        return v;
    }

    private class PlacesReadFeed extends AsyncTask<List<String>, Void, List<GooglePlaceListSingleItem>> {
        private final ProgressDialog dialog = new ProgressDialog(getActivity());


        @SafeVarargs
        @Override
        protected final List<GooglePlaceListSingleItem> doInBackground(List<String>... urls) {
            try {
                List<String> urllistia = new ArrayList<>(urls[0]);
                List<GooglePlaceListSingleItem> result = new ArrayList<>();
                for (int counter = 0; counter < urllistia.size(); counter++) {
                    String input = GooglePlacesUtility.readGooglePlaces(urllistia.get(counter), null);
                    Gson gson = new Gson();
                    GooglePlaceListSingleItem places = gson.fromJson(input, GooglePlaceListSingleItem.class);
                    //Log.i("PLACES_EXAMPLE", places.getResult().get(0).getName());
                    result.add(places);
                }
                //Log.i("PLACES_EXAMPLE", result.get(0).getPlaceNames().get(0));
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("PLACES_EXAMPLE", e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Getting favorite list...");
            this.dialog.show();
        }

        @Override
        protected void onPostExecute(List<GooglePlaceListSingleItem> places) {
            this.dialog.dismiss();
            reportBack(places);
        }


    }

    protected void reportBack(List<GooglePlaceListSingleItem> nearby) {
        for (GooglePlaceListSingleItem gpl : nearby) {
            if (this.nearby == null) {
                this.nearby = gpl;
            }
            GooglePlace place = gpl.getResult();
            Location tempLocation = new Location("");
            placeList.add(place);
            tempLocation.setLatitude(place.getGeometry().getLocation().getLat());
            tempLocation.setLongitude(place.getGeometry().getLocation().getLng());
            favoriteLocations.add(tempLocation);
        }

        Location currentPlaceLocation = userLocation;
        do {
            int nextPlaceLocationI = 0;
            float distance = currentPlaceLocation.distanceTo(favoriteLocations.get(0));
            Location nextPlaceLocation = favoriteLocations.get(0);
            GooglePlace nextPlace = placeList.get(0);
            for (int i = 1; i < favoriteLocations.size(); i++) {
                if (distance < currentPlaceLocation.distanceTo(favoriteLocations.get(i))) {
                    nextPlaceLocation = favoriteLocations.get(i);
                    nextPlaceLocationI = i;
                    nextPlace = placeList.get(i);
                }
            }
            distanceOnSortedPlaces.add(distance);
            currentPlaceLocation = nextPlaceLocation;
            placeListSorted.add(nextPlace);
            locationsSortedByDistance.add(currentPlaceLocation);
            placeList.remove(nextPlaceLocationI);
            favoriteLocations.remove(nextPlaceLocationI);
        } while (!favoriteLocations.isEmpty());
        int index = 0;
        for (GooglePlace googlePlace : placeListSorted) {
            setupMakeMyDay(googlePlace, reference, index);
            index++;
        }

        for (float dist : distanceOnSortedPlaces) {
            Log.i("DISTANCE", "" + dist);
        }

        //Log.i("CHECK ID TO PLACE", "" + favoriteLocations.size());

    }

    private void setupMakeMyDay(final GooglePlace myPlace, final DatabaseReference reference, final int finalK) {
        if (Objects.equals(isViewedList.get(finalK), "false")) {
            final RelativeLayout relativeLayout = new RelativeLayout(getActivity());
            relativeLayout.setId(finalK);
            ImageView imageView = new ImageView(getActivity());
            TextView favoriteNameText = new TextView(getActivity());
            if (!myPlace.getPhotos().isEmpty()) {
                Ion.with(imageView)
                        .placeholder(R.drawable.ic_postcard_new)
                        .load("https://maps.googleapis.com/maps/api/place/photo?maxwidth=500&photoreference=" + myPlace.getPhotos().get(0).getPhoto_reference() + "&sensor=false&key=AIzaSyD79S9Un0Ti8tDT_el4ko7ItRJz3KapOLA");
            } else {
                Ion.with(imageView)
                        .placeholder(R.drawable.ic_postcard_new)
                        .load("http://i.imgur.com/dQSoqN3.jpg");

            }
            imageView.setPadding(2, 2, 2, 2);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setBackgroundResource(R.drawable.shadow);
            imageView.setMinimumHeight(450);
            imageView.setMaxHeight(460);
            imageView.setMinimumWidth(600);
            imageView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getActivity(), PlaceDetailActivity.class);
                    i.putExtra("PLACE", myPlace);
                    startActivity(i);
                }
            });

            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.RECTANGLE);
            shape.setCornerRadii(new float[]{8, 8, 8, 8, 0, 0, 0, 0});
            if (distanceOnSortedPlaces.get(finalK) <= radius * 0.40) {
                shape.setColor(Color.GREEN);
            } else if (distanceOnSortedPlaces.get(finalK) > radius * 0.40 && distanceOnSortedPlaces.get(finalK) <= radius * 0.70) {
                shape.setColor(Color.YELLOW);
            } else {
                shape.setColor(Color.RED);
            }
            shape.setSize(50, 250);
            shape.setStroke(3, Color.GRAY);
            ImageView shapeImage = new ImageView(getActivity());
            shapeImage.setImageDrawable(shape);

            final Button deleteButton = new Button(getActivity());
            deleteButton.setBackgroundResource(R.drawable.ic_delete);
            final Button visitedButton = new Button(getActivity());
            visitedButton.setBackgroundResource(R.drawable.ic_not_visited);

            favoriteNameText.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getActivity(), PlaceDetailActivity.class);
                    i.putExtra("PLACE", myPlace);
                    startActivity(i);
                }
            });

            favoriteNameText.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            favoriteNameText.setTextColor(Color.WHITE);
            favoriteNameText.setTextSize(20);
            String upToNCharacters = myPlace.getName().substring(0, Math.min(myPlace.getName().length(), 33));
            favoriteNameText.setText(" " + upToNCharacters);


            //Layouts
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(10, 5, 10, 5);
            LinearLayout.LayoutParams layoutImageParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutImageParams.setMargins(0, 0, 400, 0);
            LinearLayout.LayoutParams layoutButtonParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutButtonParams.setMargins(0, 150, 0, 0);


            LinearLayout layoutImage = new LinearLayout(getActivity());
            layoutImage.addView(imageView, layoutImageParams);
            LinearLayout layoutButtons = new LinearLayout(getActivity());
            layoutButtons.setOrientation(LinearLayout.HORIZONTAL);
            layoutButtons.addView(visitedButton, layoutButtonParams);
            final LinearLayout layoutImageText = new LinearLayout(getActivity());
            layoutImageText.setOrientation(LinearLayout.VERTICAL);
            LinearLayout layoutImageButtons = new LinearLayout(getActivity());
            layoutImageButtons.setOrientation(LinearLayout.HORIZONTAL);
            layoutImageButtons.addView(layoutImage);
            layoutImageButtons.addView(layoutButtons);
            LinearLayout boxImageText = new LinearLayout(getActivity());
            boxImageText.setOrientation(LinearLayout.VERTICAL);
            boxImageText.addView(layoutImageButtons);
            boxImageText.addView(favoriteNameText);
            boxImageText.setBackgroundResource(R.drawable.bg_round_rect);
            layoutImageText.addView(shapeImage);
            layoutImageText.addView(boxImageText);

            visitedButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    reference.child("favorites").child(favoriteKeyList.get(finalK)).child("2").setValue("true");
                    linearLayout.removeView(layoutImageText);
                }
            });

            linearLayout.addView(layoutImageText, layoutParams);
        }
    }

    public void readData(DatabaseReference ref, final Makemydayfragment.OnGetDataListener listener) {
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
