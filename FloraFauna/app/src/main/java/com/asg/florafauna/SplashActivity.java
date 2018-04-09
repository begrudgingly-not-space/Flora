package com.asg.florafauna;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class SplashActivity extends AppCompatActivity {
    private String[] themeArray = new String[1];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(SplashActivity.this, SearchActivity.class);
        //Intent intent = new Intent(SplashActivity.this, SpeciesInfoActivity.class);

        startActivity(intent);
        finish();
    }
}
