package com.asg.florafauna;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(SplashActivity.this, SearchActivity.class);
        //Intent intent = new Intent(SplashActivity.this, SearchActivity.class);

        //for use later, this is how to pass data between activities
        //intent.putExtra("sName","Ursus arctos");

        startActivity(intent);
        finish();
    }
}
