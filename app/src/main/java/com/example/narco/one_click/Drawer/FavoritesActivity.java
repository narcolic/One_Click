package com.example.narco.one_click.Drawer;


import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;

public class FavoritesActivity extends SinglePageWithUpBarActivity {

    @Override
    protected Fragment createFragment() {
        return new FavoritesFragment().newInstance();
    }

    @Override
    protected void enableUpButton(ActionBar ab) {
        ab.setDisplayHomeAsUpEnabled(true);
    }

}
