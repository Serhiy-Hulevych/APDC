package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class Morada extends AppCompatActivity {

    private Context mContext;
    private String mName;
    private String mEmail;
    private String mLatLon;
    private String mStreet;
    private String mPlace;
    private String mCountry;

    private TextView mNameView;
    private TextView mStreetView;
    private TextView mPlaceView;
    private TextView mCountryView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_morada);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mContext = this;

        Intent intent = getIntent();

        mName = intent.getStringExtra("name");
        mEmail = intent.getStringExtra("email");
        mLatLon = intent.getStringExtra("latlon");
        mStreet = intent.getStringExtra("street");
        mPlace = intent.getStringExtra("place");
        mCountry = intent.getStringExtra("country");

        mNameView = (TextView) findViewById(R.id.name);
        mStreetView = (TextView) findViewById(R.id.street);
        mPlaceView = (TextView) findViewById(R.id.place);
        mCountryView = (TextView) findViewById(R.id.country);

        mNameView.setText(mName);
        mStreetView.setText(mStreet);
        mPlaceView.setText(mPlace);
        mCountryView.setText(mCountry);

        /*FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
    }

}
