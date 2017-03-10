package com.example.narco.one_click.Drawer;


import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;

public class SettingsActivity extends DrawerItemActivity {

    @Override
    protected Fragment createFragment() {
        return new Settingsfragment().newInstance();
    }

    @Override
    protected void enableUpButton(ActionBar ab) {
        ab.setDisplayHomeAsUpEnabled(true);
    }

}
