package com.example.narco.one_click.model;

/**
 * Created by beaumoaj on 11/02/15.
 */
public class PlaceDetail {

    //@Key
    private GooglePlace result;

    public GooglePlace getResult() {
        return result;
    }

    public void setResult(GooglePlace result) {
        this.result = result;
    }

    @Override
    public String toString() {
        if (result!=null) {
            return result.toString();
        }
        return super.toString();
    }
}