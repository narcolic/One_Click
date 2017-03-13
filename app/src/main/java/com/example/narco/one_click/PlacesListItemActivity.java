package com.example.narco.one_click;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.narco.one_click.model.GooglePlace;
import com.example.narco.one_click.model.GooglePlaceList;
import com.example.narco.one_click.model.GooglePlacesUtility;
import com.google.gson.Gson;

import java.net.URLEncoder;


public class PlacesListItemActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private GooglePlaceList nearby;
    private ListView list;
    private String placesKey;
    /* Location is Aston University */
    private double latitude = 52.485867;
    private double longitude = -1.890161;
     /* */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_places_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        list = (ListView) findViewById(R.id.the_list);
        nearby = null;

        placesKey = this.getResources().getString(R.string.places_key);
        if (placesKey.equals("PUT YOUR KEY HERE")) {

            Toast.makeText(this, "You haven't entered your Google Places Key into the strings file.  Dont forget to set a referer too.", Toast.LENGTH_LONG).show();

        } else {
            String type = URLEncoder.encode("food");
            String placesRequest = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" +
                    latitude + "," + longitude + "&radius=500&key=" + placesKey;
            PlacesReadFeed process = new PlacesReadFeed();
            process.execute(new String[] {placesRequest});
        }

        /*Button more = (Button) findViewById(R.id.more_button);
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String placesRequest = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?pagetoken=" +
                        nearby.getNext_page_token() + "&key=" + placesKey;
                PlacesReadFeed process = new PlacesReadFeed();
                process.execute(new String[] {placesRequest});
            }
        });*/
    }

    protected void reportBack(GooglePlaceList nearby) {
        if (this.nearby == null) {
            this.nearby = nearby;

        } else {
            this.nearby.getResults().addAll(nearby.getResults());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, this.nearby.getPlaceNames());
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_places_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.i("PLACE EXAMPLE", "Clicked " + parent.getItemAtPosition(position));
        GooglePlace place = nearby.getResults().get(position);
        Log.i("PLACE EXAMPLE", "Place is " + place.toString());
        Intent i = new Intent(this, PlaceDetailActivity.class);
        i.putExtra("PLACE",place);
        startActivity(i);
    }

    private class PlacesReadFeed extends AsyncTask<String, Void, GooglePlaceList> {
        private final ProgressDialog dialog = new ProgressDialog(PlacesListItemActivity.this);

        @Override
        protected GooglePlaceList doInBackground(String... urls) {
            try {
                String referer = null;
                //dialog.setMessage("Fetching Places Data");
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
