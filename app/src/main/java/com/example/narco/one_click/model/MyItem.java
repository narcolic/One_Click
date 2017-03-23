package com.example.narco.one_click.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by narco on 22-Mar-17.
 */

public class MyItem implements ClusterItem {
    private final LatLng mPosition;
    private String mTitle;
    private String mSnippet;

    public GooglePlace getmGooglePlace() {
        return mGooglePlace;
    }

    public void setmGooglePlace(GooglePlace mGooglePlace) {
        this.mGooglePlace = mGooglePlace;
    }

    private  GooglePlace mGooglePlace;

    public MyItem(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
        mTitle = null;
        mSnippet = null;
        mGooglePlace = null;
    }

    public MyItem(double lat, double lng, String title, String snippet, GooglePlace googlePlace) {
        mPosition = new LatLng(lat, lng);
        mTitle = title;
        mSnippet = snippet;
        mGooglePlace=googlePlace;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() { return mTitle; }

    @Override
    public String getSnippet() { return mSnippet; }

    /**
     * Set the title of the marker
     * @param title string to be set as title
     */
    public void setTitle(String title) {
        mTitle = title;
    }

    /**
     * Set the description of the marker
     * @param snippet string to be set as snippet
     */
    public void setSnippet(String snippet) {
        mSnippet = snippet;
    }
}
