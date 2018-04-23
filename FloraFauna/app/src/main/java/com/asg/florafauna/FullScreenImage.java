package com.asg.florafauna;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class FullScreenImage extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeCreator.getTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_screen_image);

        Intent i = getIntent();

        // Get the position
        int position = i.getExtras().getInt("position");

        // Get String arrays FilePathStrings
        ArrayList<String> filepath = i.getStringArrayListExtra("filepath");
        Log.i("filepath", filepath.toString());

        // Get String arrays FileNameStrings
        ArrayList<String> filename = i.getStringArrayListExtra("filename");

        // Locate the ImageView in full_screen_image.xml
        ImageView imageview = (ImageView) findViewById(R.id.fullImage);

        TextView textView = (TextView) findViewById(R.id.desc_Full_Screen);

        // Decode the filepath with BitmapFactory followed by the position
        Bitmap bmp = BitmapFactory.decodeFile(filepath.get(position));

        // Set the decoded bitmap into ImageView
        imageview.setImageBitmap(bmp);

        String[] name = filename.get(position).split("!");

        if (name[1] != null) {
            textView.setText(name[1]);
        }

        // Sets the title of the page to the name of the image (with capital first letter)
        setTitle(name[0].substring(0,1).toUpperCase()
                + name[0].substring(1).toLowerCase());
    }
}
