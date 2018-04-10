package com.asg.florafauna;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

import static com.asg.florafauna.SpeciesInfoActivity.INTENT_EXTRA_IMAGELINK;


/**
 * Created by steven on 4/8/18.
 */


public class ImageActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        //String imageLink = getIntent().getStringExtra(INTENT_EXTRA_IMAGELINK);
        String imageLink="https://media.eol.org/content/2014/10/09/11/00594_580_360.jpg";
        //new DownloadImageTask((ImageView) findViewById(R.id.imageView1)).execute("https://media.eol.org/content/2014/10/09/11/00594_580_360.jpg");
        new DownloadImageTask((ImageView) findViewById(R.id.imageView1)).execute(imageLink);
    }

    //image viewer from https://stackoverflow.com/questions/5776851/load-image-from-url#10868126
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap>
    {
        ImageView bmImage;
        Bitmap image;
        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }
        @Override
        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                Log.i("place", "working on image "+urldisplay);
                InputStream in = new java.net.URL(urldisplay).openStream();
                image = BitmapFactory.decodeStream(in);
                mIcon11=image;



                Log.i("place", "claims to have decoded bitmap ");

                //bitmap is never getting assigned to image verified by log below crashes
                Log.i("place", "actually decoded bitmap "+image.getHeight());
            } catch (Exception e) {
                Log.e("Error in DownloadImage", e.getMessage());
                e.printStackTrace();
            }
            Log.i("place", "returning bitmap");
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            try {//this try/catch is not needed except to test with getHeight
                //bmImage.setImageBitmap(result);
                bmImage.setImageBitmap(image);

                Log.i("place ", "claims done With image");

                //crashes because image is null object reference
                Log.i("place ", "actually done With image"+image.getHeight());
            }
            catch(NullPointerException e)
            {
                Log.e("Error: ", "Done, but Bitmap never loaded");
            }
            catch(Exception e)
            {
                Log.e("error in display: ",e.toString());
            }
        }
    }
}
