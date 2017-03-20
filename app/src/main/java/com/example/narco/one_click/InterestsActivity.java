package com.example.narco.one_click;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InterestsActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private List<String> interestsList;
    private List<String> finalInterestsList;
    private FirebaseUser user;

    private int checkCounter = 0;

    @BindView(R.id.btn_interest)
    Button _submitButton;
    @BindView(R.id.cb_animals)
    CheckBox _cb_animals;
    @BindView(R.id.cb_art)
    CheckBox _cb_art;
    @BindView(R.id.cb_books)
    CheckBox _cb_books;
    @BindView(R.id.cb_food)
    CheckBox _cb_food;
    @BindView(R.id.cb_history)
    CheckBox _cb_history;
    @BindView(R.id.cb_music)
    CheckBox _cb_music;
    @BindView(R.id.cb_nature)
    CheckBox _cb_nature;
    @BindView(R.id.cb_shopping)
    CheckBox _cb_shopping;
    @BindView(R.id.cb_sport)
    CheckBox _cb_sport;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interests);
        ButterKnife.bind(this);
        user = FirebaseAuth.getInstance().getCurrentUser();
        interestsList = new ArrayList<>();
        finalInterestsList = new ArrayList<>();

        databaseReference = FirebaseDatabase.getInstance().getReference();
        _submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
        fillLayout();

    }

    private void fillLayout() {

        if (user != null) {
            DatabaseReference ref1 = databaseReference.child(user.getUid());
            readData(ref1, new InterestsActivity.OnGetDataListener() {
                @Override
                public void onSuccess(DataSnapshot dataSnapshot) {
                    Log.d("ONSUCCESS", "Success");
                    for (DataSnapshot postSnapshot : dataSnapshot.child("interest").getChildren()) {
                        String interests = postSnapshot.getValue(String.class);
                        interestsList.add(interests);
                    }
                    if(!interestsList.isEmpty()){
                        for (String interest:interestsList){
                            switch (interest) {
                                case "Art":
                                    _cb_art.setChecked(true);
                                    break;
                                case "Animals":
                                    _cb_animals.setChecked(true);
                                    break;
                                case "Books":
                                    _cb_books.setChecked(true);
                                    break;
                                case "Food":
                                    _cb_food.setChecked(true);
                                    break;
                                case "History":
                                    _cb_history.setChecked(true);
                                    break;
                                case "Music":
                                    _cb_music.setChecked(true);
                                    break;
                                case "Nature":
                                    _cb_nature.setChecked(true);
                                    break;
                                case "Shopping":
                                    _cb_shopping.setChecked(true);
                                    break;
                                case "Sport":
                                    _cb_sport.setChecked(true);
                                    break;

                            }
                        }
                    }
                }

                @Override
                public void onStart() {
                    //Whatever you want to do on start
                    Log.d("ONSTART", "Started");
                }

                @Override
                public void onFailure() {
                    //Whatever you want to do in case of failure
                    Log.d("ONFAILURE", "Failure");
                }
            });
        }

    }

    private void saveUserInterests() {
        if (user != null) {
            databaseReference.child(user.getUid()).child("radius").setValue(1500);
            databaseReference.child(user.getUid()).child("interest").setValue(null);
            databaseReference.child(user.getUid()).child("interest").setValue(finalInterestsList);
        }
        Toast.makeText(this, "Selections saved...", Toast.LENGTH_LONG).show();

    }

    private void submit() {
        if (!validate()) {
            onSubmitFailed();
        } else {
            onSubmitSuccess();
        }
    }

    private void onSubmitSuccess() {
        saveUserInterests();
        _submitButton.setEnabled(false);
        finish();
    }

    private void onSubmitFailed() {
        Toast.makeText(getApplicationContext(), "Select at least 3 interests", Toast.LENGTH_LONG).show();
        _submitButton.setEnabled(true);
    }

    private boolean validate() {
        boolean valid = true;

        countChecks();
        if (checkCounter < 3) {
            valid = false;
        }


        return valid;
    }

    private void countChecks() {
        if (_cb_animals.isChecked()) {
            checkCounter++;
            finalInterestsList.add("Animals");
        }
        if (_cb_art.isChecked()) {
            checkCounter++;
            finalInterestsList.add("Art");
        }
        if (_cb_books.isChecked()) {
            checkCounter++;
            finalInterestsList.add("Books");
        }
        if (_cb_food.isChecked()) {
            checkCounter++;
            finalInterestsList.add("Food");
        }
        if (_cb_history.isChecked()) {
            checkCounter++;
            finalInterestsList.add("History");
        }
        if (_cb_music.isChecked()) {
            checkCounter++;
            finalInterestsList.add("Music");
        }
        if (_cb_nature.isChecked()) {
            checkCounter++;
            finalInterestsList.add("Nature");
        }
        if (_cb_shopping.isChecked()) {
            checkCounter++;
            finalInterestsList.add("Shopping");
        }
        if (_cb_sport.isChecked()) {
            checkCounter++;
            finalInterestsList.add("Sport");
        }
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }


    public void readData(DatabaseReference ref, final InterestsActivity.OnGetDataListener listener) {
        listener.onStart();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listener.onSuccess(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onFailure();
            }
        });

    }

    interface OnGetDataListener {
        //make new interface for call back
        void onSuccess(DataSnapshot dataSnapshot);

        void onStart();

        void onFailure();
    }

}