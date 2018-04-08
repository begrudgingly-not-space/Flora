package com.asg.florafauna;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;

public class PersonalRecordings extends AppCompatActivity {

    private ImageView selectedImage;
    private Bitmap currentImage;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_WRITE_EXTERNAL_STORAGE= 0;
    private final File recordings = new File(Environment.getExternalStorageDirectory().toString(), "/FloraFauna/Recordings");



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_recordings);

        FloraFaunaActionBar.createActionBar(getSupportActionBar(), R.layout.ab_recordings);


        selectedImage = (ImageView) findViewById(R.id.imageView1);
        FloatingActionButton openGallery = (FloatingActionButton) findViewById(R.id.floatingUpload);

        openGallery.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 1);
            }
        });



        //Create dir for recordings
        //request for permission to write to storage
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_ACCESS_WRITE_EXTERNAL_STORAGE);
        }

        //checks if the recordings dir exists
        if (!recordings.exists()) {
            //if directory creation fails, tell the user
            if (!recordings.mkdirs()) {
                Log.d("error", "failed to make dir");
                Toast.makeText(this, "Failed to create directory", Toast.LENGTH_LONG).show();
            }
        }
        //if the directory exists, make a log
        else {
            Log.d("error", "dir. already exists");
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.recordings_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_home:
                Intent search_intent = new Intent(PersonalRecordings.this, SearchActivity.class);
                startActivity(search_intent);
                return true;
            case R.id.action_settings:
                Intent settings_intent = new Intent(PersonalRecordings.this, SettingsActivity.class);
                startActivity(settings_intent);
                return true;
            case R.id.action_help:
                Intent help_intent = new Intent(PersonalRecordings.this, HelpActivity.class);
                startActivity(help_intent);
                return true;
            case R.id.action_map:
                Intent intent = new Intent(PersonalRecordings.this, MapActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Uri photoUri = data.getData();
            if (photoUri != null) {
                //code to mess with images will be here
                try {
                    currentImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                    selectedImage.setImageBitmap(currentImage); //set the image view to the current image
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                }
                else {
                    // Permission denied, Unable to create directory
                }
            }
        }
    }

    public void goBack(View view){
        /* closes the activity */
        setResult(RESULT_OK, null);
        finish();
    }

}

