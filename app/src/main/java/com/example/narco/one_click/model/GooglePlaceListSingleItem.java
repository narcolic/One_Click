package com.example.narco.one_click.model;

import java.util.ArrayList;
import java.util.List;

public class GooglePlaceListSingleItem {
    private String status;

    private GooglePlace result;


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public GooglePlace getResult() {
        return result;
    }

    public void setResult(GooglePlace result) {
        this.result = result;
    }


    public List<String> getPhotoUrl() {
        List<String> presult = new ArrayList<String>();

            if (!result.getPhotos().isEmpty()) {
                presult.add("https://maps.googleapis.com/maps/api/place/photo?maxwidth=500&photoreference=" + result.getPhotos().get(0).getPhoto_reference() + "&sensor=false&key=AIzaSyD79S9Un0Ti8tDT_el4ko7ItRJz3KapOLA");
            }
            else{
                presult.add("http://i.imgur.com/dQSoqN3.jpg");
            }

        return presult;
    }

}

