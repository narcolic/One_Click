package com.example.narco.one_click;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.narco.one_click.model.GooglePlace;
import com.example.narco.one_click.model.GooglePlaceList;
import com.example.narco.one_click.model.GooglePlacesUtility;
import com.google.gson.Gson;
import com.koushikdutta.ion.Ion;

import java.net.URLEncoder;

public class Suggestionsfragment extends Fragment implements AdapterView.OnItemClickListener {

    private GooglePlaceList nearby;
    private ListView list;
    private LinearLayout hilayout;
    private String placesKey;
    /* Location is Aston University */
    private double latitude = 52.485867;
    private double longitude = -1.890161;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.suggestions, container, false);
        list = (ListView) v.findViewById(R.id.the_list);
        hilayout = (LinearLayout) v.findViewById(R.id.linear);
        nearby = null;

        placesKey = this.getResources().getString(R.string.places_key);
        if (placesKey.equals("PUT YOUR KEY HERE"))
            Toast.makeText(getActivity(), "You haven't entered your Google Places Key into the strings file.  Dont forget to set a referer too.", Toast.LENGTH_LONG).show();
        else {
            String type = URLEncoder.encode("food");
            String placesRequest = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" +
                    latitude + "," + longitude + "&radius=500&key=" + placesKey;
            PlacesReadFeed process = new PlacesReadFeed();
            process.execute(placesRequest);
        }


        return v;
    }

    protected void reportBack(GooglePlaceList nearby) {
        if (this.nearby == null) {
            this.nearby = nearby;

        } else {
            this.nearby.getResults().addAll(nearby.getResults());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity().getApplicationContext(),
                android.R.layout.simple_list_item_1, this.nearby.getPlaceNames());
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);

        for (int i = 0; i < nearby.getResults().size(); i++) {
            ImageView imageView = new ImageView(getActivity());
            String ok = nearby.getPhotoUrl().get(i);
            Ion.with(imageView)
                    .placeholder(R.drawable.ic_postcard)
                    .load(ok);
            //.load(nearby.getResults().get(i).);
            imageView.setId(i);
            imageView.setPadding(2, 2, 2, 2);
//            imageView.setImageBitmap(BitmapFactory.decodeResource(
//                    getResources(), R.drawable.ic_favorites));
//            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            hilayout.addView(imageView);
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

    class PlacesReadFeed extends AsyncTask<String, Void, GooglePlaceList> {
        private final ProgressDialog dialog = new ProgressDialog(getActivity());

        @Override
        protected GooglePlaceList doInBackground(String... urls) {
            try {
                String referer = null;
                dialog.setMessage("Fetching Places Data");
                if (urls.length == 1) {
                    referer = null;
                } else {
                    referer = urls[1];
                }
                String input = GooglePlacesUtility.readGooglePlaces(urls[0], referer);
                Gson gson = new Gson();
                GooglePlaceList places = gson.fromJson(input, GooglePlaceList.class);
                Log.i("PLACES_EXAMPLE", "Number of places found is " + places.getResults().size());
                return places;
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
        protected void onPostExecute(GooglePlaceList places) {
            this.dialog.dismiss();
            reportBack(places);
        }


    }
}
