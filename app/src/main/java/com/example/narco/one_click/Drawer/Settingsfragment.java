package com.example.narco.one_click.Drawer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.narco.one_click.R;


public class Settingsfragment extends Fragment {

    public static Settingsfragment newInstance() {
        return new Settingsfragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_settings, parent, false);
        getActivity().setTitle(R.string.settings_menu_title);

        return v;
    }
}
