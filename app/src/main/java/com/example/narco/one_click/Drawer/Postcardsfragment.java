package com.example.narco.one_click.Drawer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.narco.one_click.R;


public class Postcardsfragment extends Fragment {

    public static Postcardsfragment newInstance() {
        return new Postcardsfragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_postcards, parent, false);
        getActivity().setTitle(R.string.postcards_menu_title);

        return v;
    }
}
