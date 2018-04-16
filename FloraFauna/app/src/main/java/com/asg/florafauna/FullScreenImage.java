package com.asg.florafauna;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import java.util.ArrayList;

public class FullScreenImage extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_screen_image);



        Intent i = getIntent();

        // Get the position
        int position = i.getExtras().getInt("position");

        // Get String arrays FilePathStrings
        ArrayList<String> filepath = i.getStringArrayListExtra("filepath");
        Log.i("filepath", filepath.toString());

        // Get String arrays FileNameStrings
        String[] filename = i.getStringArrayExtra("filename");

        // Locate the ImageView in view_image.xml
        ImageView imageview = (ImageView) findViewById(R.id.fullImage);

        // Decode the filepath with BitmapFactory followed by the position
        Bitmap bmp = BitmapFactory.decodeFile(filepath.get(position));

        // Set the decoded bitmap into ImageView
        imageview.setImageBitmap(bmp);

    }

}
