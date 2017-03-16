package com.example.narco.one_click;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InterestsActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private List<String> interestsList;

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

        interestsList = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        _submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
    }

    private void saveUserInterests() {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {
            databaseReference.child(user.getUid()).child("interest").setValue(interestsList);
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
            interestsList.add("Animals");
        }
        if (_cb_art.isChecked()) {
            checkCounter++;
            interestsList.add("Art");
        }
        if (_cb_books.isChecked()) {
            checkCounter++;
            interestsList.add("Books");
        }
        if (_cb_food.isChecked()) {
            checkCounter++;
            interestsList.add("Food");
        }
        if (_cb_history.isChecked()) {
            checkCounter++;
            interestsList.add("History");
        }
        if (_cb_music.isChecked()) {
            checkCounter++;
            interestsList.add("Music");
        }
        if (_cb_nature.isChecked()) {
            checkCounter++;
            interestsList.add("Nature");
        }
        if (_cb_shopping.isChecked()) {
            checkCounter++;
            interestsList.add("Shopping");
        }
        if (_cb_sport.isChecked()) {
            checkCounter++;
            interestsList.add("Sport");
        }
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

}