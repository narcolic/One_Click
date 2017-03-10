package com.example.narco.one_click.Drawer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.narco.one_click.R;

public abstract class DrawerItemActivity extends AppCompatActivity {

    protected abstract Fragment createFragment();

    protected abstract void enableUpButton(ActionBar ab);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_fragment);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);


        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        enableUpButton(ab);

        FragmentManager fm =
                this.getSupportFragmentManager();
        Fragment fragment =
                fm.findFragmentById(R.id.fragmentContainer);
        if (fragment == null) {
            fragment = createFragment();
            fm.beginTransaction().
                    add(R.id.fragmentContainer, fragment).
                    commit();
        }

    }
}
