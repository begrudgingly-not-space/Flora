package com.asg.florafauna;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        // Comment to test merge
    }

    public void sendMessage(View view){
        Intent intent = new Intent(SplashActivity.this, HelpActivity.class);
        startActivity(intent);

    }

    public void goToSettings(View view){
        Intent intent = new Intent(SplashActivity.this, SettingsActivity.class);
        startActivity(intent);

    }
}
