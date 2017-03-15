package com.example.narco.one_click;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.example.narco.one_click.Drawer.FavoritesActivity;
import com.example.narco.one_click.Drawer.PostcardsActivity;
import com.example.narco.one_click.Drawer.ReviewsActivity;
import com.example.narco.one_click.Drawer.SettingsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

public class MainActivity extends AppCompatActivity {

    private Drawer result = null;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        checkLogin(user);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        if (user != null) {
            getSupportActionBar().setTitle(user.getEmail());
        }

        result = new DrawerBuilder(this)
                //this layout have to contain child layouts
                .withRootView(R.id.drawer_container)
                .withToolbar(toolbar)
                .withDisplayBelowStatusBar(false)
                .withActionBarDrawerToggleAnimated(true)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.drawer_favorites).withIcon(FontAwesome.Icon.faw_heart),
                        new PrimaryDrawerItem().withName(R.string.drawer_my_reviews).withIcon(FontAwesome.Icon.faw_comments),
                        new PrimaryDrawerItem().withName(R.string.drawer_postcards).withIcon(FontAwesome.Icon.faw_camera_retro),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_settings).withIcon(FontAwesome.Icon.faw_cog),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_about).withIcon(FontAwesome.Icon.faw_info_circle)
                )
                .addStickyDrawerItems(new SecondaryDrawerItem().withName(R.string.drawer_logout).withIcon(FontAwesome.Icon.faw_sign_out))
                .withSavedInstance(savedInstanceState)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        boolean flag;
                        if (drawerItem != null) {
                            flag = true;
                            switch (position) {
                                case 0:
                                    Intent intent1 = new Intent(MainActivity.this, FavoritesActivity.class);
                                    startActivity(intent1);
                                    result.closeDrawer();
                                    break;
                                case 1:
                                    Intent intent2 = new Intent(MainActivity.this, ReviewsActivity.class);
                                    startActivity(intent2);
                                    result.closeDrawer();
                                    break;
                                case 2:
                                    Intent intent3 = new Intent(MainActivity.this, PostcardsActivity.class);
                                    startActivity(intent3);
                                    result.closeDrawer();
                                    break;
                                case 4:
                                    Intent intent4 = new Intent(MainActivity.this, SettingsActivity.class);
                                    startActivity(intent4);
                                    result.closeDrawer();
                                    break;
                                case -1:
                                    mAuth.signOut();
                                    checkLogin(null);
                                    result.closeDrawer();
                                    break;
                            }
                        } else {
                            flag = false;
                        }
                        return flag;
                    }
                })
                .build();

        //BottomBar
        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if (tabId == R.id.tab_map) {
                    Mapfragment f = new Mapfragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame, f).commit();

                    result.closeDrawer();
                } else if (tabId == R.id.tab_suggestions) {
                    Makemydayfragment f = new Makemydayfragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame, f).commit();
                    result.closeDrawer();
                } else if (tabId == R.id.tab_makemyday) {
                    Makemydayfragment f = new Makemydayfragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame, f).commit();
                    result.closeDrawer();
                }
            }
        });
        bottomBar.setOnTabReselectListener(new OnTabReselectListener() {
            @Override
            public void onTabReSelected(@IdRes int tabId) {
                Toast.makeText(getApplicationContext(), TabMessage.get(tabId, true), Toast.LENGTH_LONG).show();
                result.closeDrawer();
            }
        });
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the drawer to the bundle
        outState = result.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (result != null && result.isDrawerOpen()) {
            result.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    boolean checkLogin(FirebaseUser user) {
        // Check login status
        if (user == null) {

            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(MainActivity.this, LoginActivity.class);

            // Closing all the Activities from stack
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            MainActivity.this.startActivity(i);

            return true;
        }
        return false;
    }

}