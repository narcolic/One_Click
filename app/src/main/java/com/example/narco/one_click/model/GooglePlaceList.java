package com.example.narco.one_click.model;

import java.util.ArrayList;
import java.util.List;

public class GooglePlaceList {
    private String status;
    private String next_page_token;
    private List<GooglePlace> results;

    public String getNext_page_token() {
        return next_page_token;
    }

    public void setNext_page_token(String next_page_token) {
        this.next_page_token = next_page_token;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<GooglePlace> getResults() {
        return results;
    }

    public void setResults(List<GooglePlace> results) {
        this.results = results;
    }

    public List<String> getPlaceNames() {
        List<String> result = new ArrayList<String>();
        for (GooglePlace place : results) {
            result.add(place.toString());
        }
        return result;
    }

    public List<String> getPhotoUrl() {
        List<String> presult = new ArrayList<String>();
        for (GooglePlace place : results) {
            if (!place.getPhotos().isEmpty()) {
                presult.add("https://maps.googleapis.com/maps/api/place/photo?maxwidth=500&photoreference=" + place.getPhotos().get(0).getPhoto_reference() + "&sensor=false&key=AIzaSyD79S9Un0Ti8tDT_el4ko7ItRJz3KapOLA");
            }
            else{
                presult.add("http://i.imgur.com/CAak2VE.png");
            }
        }
        return presult;
    }

}

