package com.example.narco.one_click.Drawer;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.narco.one_click.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class FavoritesFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = "Favorites Fragment";
    private FirebaseUser user;
    private List<String> favoriteList;
    private List<String> favoriteKeyList;
    private GoogleApiClient mGoogleApiClient;
    private List<String> favoriteNameList;
    LinearLayout linearLayout;

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
        user = FirebaseAuth.getInstance().getCurrentUser();
        favoriteList = new ArrayList<>();
        favoriteNameList = new ArrayList<>();
        favoriteKeyList = new ArrayList<>();

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Places.GEO_DATA_API)
                .addConnectionCallbacks(this).build();

        if (user != null) {
            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(user.getUid());
            readData(reference, new FavoritesFragment.OnGetDataListener() {
                @Override
                public void onSuccess(DataSnapshot dataSnapshot) {
                    Log.d("ONSUCCESS", "Success");
                    int favoritePositionOnList = 0;
                    for (DataSnapshot postSnapshot : dataSnapshot.child("favorites").getChildren()) {
                        String favorites = postSnapshot.getValue(String.class);
                        favoriteList.add(favorites);
                        String favoritesKey = postSnapshot.getKey();
                        favoriteKeyList.add(favoritesKey);
                    }

                    //GooglePlaceID to Place Name
                    for (String favoriteID : favoriteList) {
                        Places.GeoDataApi.getPlaceById(mGoogleApiClient, favoriteID)
                                .setResultCallback(new ResultCallback<PlaceBuffer>() {
                                    @Override
                                    public void onResult(PlaceBuffer places) {
                                        if (places.getStatus().isSuccess() && places.getCount() > 0) {
                                            final Place myPlace = places.get(0);
                                            Log.i(TAG, "Place found: " + myPlace.getName());
                                            //favoriteNameList.add(myPlace.getName().toString());
                                            LinearLayout ll = new LinearLayout(getActivity());
                                            Button deleteButton = new Button(getActivity());
                                            deleteButton.setText(R.string.delete_button);
                                            TextView favoriteNameText = new TextView(getActivity());
                                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                            layoutParams.setMargins(10, 20, 10, 20);
                                            favoriteNameText.setTextColor(Color.WHITE);
                                            favoriteNameText.setTextSize(20);
                                            favoriteNameText.setText(myPlace.getName().toString());
                                            ll.setOrientation(LinearLayout.HORIZONTAL);
                                            ll.addView(favoriteNameText);
                                            ll.addView(deleteButton);
                                            linearLayout.addView(ll, layoutParams);
                                        } else {
                                            Log.e(TAG, "Place not found");
                                        }
                                        places.release();
                                    }
                                });
                    }


                    /*final int finalFavoriteKey = favoritePositionOnList;
                    favoriteButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            if (!isFavorite) {
                                reference.child("favorites").push().setValue(place.getId());
                                favoriteButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fav_full, 0, 0, 0);
                                isFavorite = true;
                            } else {
                                reference.child("favorites").child(favoriteKeyList.get(finalFavoriteKey)).removeValue();
                                favoriteButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fav, 0, 0, 0);
                                isFavorite = false;
                            }
                        }
                    });*/
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
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    interface OnGetDataListener {
        //make new interface for call back
        void onSuccess(DataSnapshot dataSnapshot);

        void onStart();

        void onFailure();
    }

}
