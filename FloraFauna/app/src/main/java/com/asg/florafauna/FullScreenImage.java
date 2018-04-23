package com.asg.florafauna;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class FullScreenImage extends AppCompatActivity {
    //for theme
    private String[] themeArray = new String[1];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //set theme---------------------------------------------------------------------------
        try {
            //opens the file to read its contents
            FileInputStream fis = this.openFileInput("theme");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);

            themeArray[0] = reader.readLine(); //adds the line to the temp array
            reader.close();
            isr.close();
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (themeArray[0].equals("Green")) {
            setTheme(R.style.AppTheme);
        } else if (themeArray[0].equals("Blue")) {
            setTheme(R.style.AppThemeBlue);
        } else if (themeArray[0].equals("Mono")) {
            setTheme(R.style.AppThemeMono);
        } else if (themeArray[0].equals("Cherry")) {
            setTheme(R.style.AppThemeCherry);
        }
        //--------------------------------------------------------------------------------------
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

        // Decode the filepath with BitmapFactory followed by the position
        Bitmap bmp = BitmapFactory.decodeFile(filepath.get(position));

        // Set the decoded bitmap into ImageView
        imageview.setImageBitmap(bmp);

        String[] name = filename.get(position).split("!");


        //sets the title of the page to the name of the image (with capital first letter)
        setTitle(name[0].substring(0,1).toUpperCase()
                + name[0].substring(1).toLowerCase());

    }

}
