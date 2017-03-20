package com.example.narco.one_click.Drawer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.example.narco.one_click.PlaceDetailActivity;
import com.example.narco.one_click.R;
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


public class FavoritesFragment extends Fragment {

    private GooglePlaceListSingleItem nearby;
    private static final String TAG = "Favorites Fragment";
    private List<String> favoriteRefList;
    private List<String> favoriteKeyList;
    private List<String> isViewedList;
    LinearLayout linearLayout;
    DatabaseReference reference;

    public static FavoritesFragment newInstance() {
        return new FavoritesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_favorites, parent, false);
        getActivity().setTitle(R.string.favorites_menu_title);
        linearLayout = (LinearLayout) v.findViewById(R.id.main_layout);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        favoriteRefList = new ArrayList<>();
        favoriteKeyList = new ArrayList<>();
        isViewedList = new ArrayList<>();
        nearby = null;


        if (user != null) {
            reference = FirebaseDatabase.getInstance().getReference().child(user.getUid());
            readData(reference, new FavoritesFragment.OnGetDataListener() {
                @Override
                public void onSuccess(DataSnapshot dataSnapshot) {
                    Log.d("ONSUCCESS", "Success");
                    for (DataSnapshot postSnapshot : dataSnapshot.child("favorites").getChildren()) {
                        String favoritesID = postSnapshot.child("0").getValue(String.class);
                        favoriteRefList.add(favoritesID);
                        String isViewed = postSnapshot.child("2").getValue(String.class);
                        isViewedList.add(isViewed);
                        String favoritesKey = postSnapshot.getKey();
                        favoriteKeyList.add(favoritesKey);
                    }
                    //GooglePlaceUrl to Place Name
                    PlacesReadFeed process = new PlacesReadFeed();
                    process.execute(favoriteRefList);
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

    private void SetupFavorites(final GooglePlace myPlace, final DatabaseReference reference, final int finalK) {
        Log.i(TAG, "Place found: " + myPlace.getName());
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

        final Button deleteButton = new Button(getActivity());
        deleteButton.setBackgroundResource(R.drawable.ic_delete);
        final Button visitedButton = new Button(getActivity());
        if (Objects.equals(isViewedList.get(finalK), "true")){
            visitedButton.setBackgroundResource(R.drawable.ic_visited);
        }else{
            visitedButton.setBackgroundResource(R.drawable.ic_not_visited);
        }



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
        String upToNCharacters = myPlace.getName().substring(0, Math.min(myPlace.getName().length(), 35));
        favoriteNameText.setText(" " + upToNCharacters);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(10, 20, 10, 20);
        LinearLayout.LayoutParams layoutImageParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutImageParams.setMargins(0,0,200,0);
        LinearLayout.LayoutParams layoutButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutButtonParams.setMargins(0,150,0,0);


        LinearLayout layoutImage = new LinearLayout(getActivity());
        layoutImage.addView(imageView,layoutImageParams);
        LinearLayout layoutButtons = new LinearLayout(getActivity());
        layoutButtons.setOrientation(LinearLayout.HORIZONTAL);
        layoutButtons.addView(visitedButton,layoutButtonParams);
        layoutButtons.addView(deleteButton,layoutButtonParams);
        final LinearLayout layoutImageText = new LinearLayout(getActivity());
        layoutImageText.setOrientation(LinearLayout.VERTICAL);
        LinearLayout layoutImageButtons = new LinearLayout(getActivity());
        layoutImageButtons.setOrientation(LinearLayout.HORIZONTAL);
        layoutImageButtons.addView(layoutImage);
        layoutImageButtons.addView(layoutButtons);
        layoutImageText.addView(layoutImageButtons);
        layoutImageText.addView(favoriteNameText);


        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                reference.child("favorites").child(favoriteKeyList.get(finalK)).removeValue();
                linearLayout.removeView(layoutImageText);
            }
        });
        visitedButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (Objects.equals(isViewedList.get(finalK), "false")) {
                    reference.child("favorites").child(favoriteKeyList.get(finalK)).child("2").setValue("true");
                    visitedButton.setBackgroundResource(R.drawable.ic_visited);
                } else {
                    reference.child("favorites").child(favoriteKeyList.get(finalK)).child("2").setValue("false");
                    visitedButton.setBackgroundResource(R.drawable.ic_not_visited);
                }

            }
        });
        linearLayout.addView(layoutImageText, layoutParams);
    }

    public void readData(DatabaseReference ref, final FavoritesFragment.OnGetDataListener listener) {
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

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    interface OnGetDataListener {
        //make new interface for call back
        void onSuccess(DataSnapshot dataSnapshot);

        void onStart();

        void onFailure();
    }

    protected void reportBack(List<GooglePlaceListSingleItem> nearby) {
        int index = 0;
        for (GooglePlaceListSingleItem gpl : nearby) {
            if (this.nearby == null) {
                this.nearby = gpl;
            }
            //Log.i("PLACES_EXAMPLE", gpl.getResult().get(0).getName());
            GooglePlace place = gpl.getResult();
            SetupFavorites(place, reference, index);
            index++;
        }
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

}
