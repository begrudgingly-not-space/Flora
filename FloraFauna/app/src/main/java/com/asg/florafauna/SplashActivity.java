package com.asg.florafauna;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Intent intent = new Intent(SplashActivity.this, SearchActivity.class);
        Intent intent = new Intent(SplashActivity.this, SearchActivity.class);

        //for use later, this is how to pass data between activities
        //Stolen from:
        //https://stackoverflow.com/questions/2091465/how-do-i-pass-data-between-activities-in-android-application#7325248
        //intent.putExtra("sName","Ursus arctos");

        startActivity(intent);
        finish();
    }
}
