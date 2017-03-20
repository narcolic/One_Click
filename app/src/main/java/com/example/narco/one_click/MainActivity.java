package com.example.narco.one_click;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.narco.one_click.Drawer.FavoritesActivity;
import com.example.narco.one_click.Drawer.InfoActivity;
import com.example.narco.one_click.Drawer.PostcardsActivity;
import com.example.narco.one_click.Drawer.SettingsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

public class MainActivity extends AppCompatActivity {

    private Drawer result = null;
    private FirebaseAuth mAuth;
    FirebaseUser user;
    int radius;
    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        checkLogin(user);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("One Click");


        result = new DrawerBuilder(this)
                //this layout have to contain child layouts
                .withRootView(R.id.drawer_container)
                .withToolbar(toolbar)
                .withDisplayBelowStatusBar(false)
                .withActionBarDrawerToggleAnimated(true)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.drawer_favorites).withIcon(FontAwesome.Icon.faw_heart),
                        new PrimaryDrawerItem().withName(R.string.drawer_my_interests).withIcon(FontAwesome.Icon.faw_paper_plane_o),
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
                                    Intent intent2 = new Intent(MainActivity.this, InterestsActivity.class);
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
                                case 5:
                                    Intent intent5 = new Intent(MainActivity.this, InfoActivity.class);
                                    startActivity(intent5);
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
                    Suggestionsfragment f = new Suggestionsfragment();
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

    void checkLogin(FirebaseUser user) {
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
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_2:
                onRadiusSelected();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onRadiusSelected() {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.custom_pop, null);
        Button mLogin = (Button) mView.findViewById(R.id.btnLogin);
        DiscreteSeekBar discreteSeekBar = (DiscreteSeekBar) mView.findViewById(R.id.discrete1);
        databaseReference.child(user.getUid()).child("radius").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                radius = dataSnapshot.getValue(Integer.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
        discreteSeekBar.setProgress(radius / 100);
        discreteSeekBar.setNumericTransformer(new DiscreteSeekBar.NumericTransformer() {
            @Override
            public int transform(int value) {
                radius = value * 100;
                return radius;
            }
        });
        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("RADIUS", "" + radius);
                databaseReference.child(user.getUid()).child("radius").setValue(radius);
                dialog.dismiss();
            }
        });


    }
}