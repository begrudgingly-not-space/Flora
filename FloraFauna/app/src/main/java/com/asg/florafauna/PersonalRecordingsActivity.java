package com.asg.florafauna;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static android.widget.Toast.LENGTH_LONG;

public class PersonalRecordingsActivity extends AppCompatActivity {
    private Bitmap currentImage;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_WRITE_EXTERNAL_STORAGE = 0;
    private Intent PR_Directory;
    private String dirName; // = Environment.getExternalStorageDirectory().toString() + "/FloraFauna/Recordings/";
    private File recordings; // = new File(dirName);

    // List Files
    GridView imagegrid;
    ArrayList<String> FilePathStrings = new ArrayList<String>(); // List of file paths
    File[] listFile;
    ArrayList<String> FileNameStrings = new ArrayList<String>();
    AlertDialog imageDialog;
    AlertDialog folderDialog;
    String folderName;
    boolean nameGiven = true;
    ArrayAdapter<String> spinAdapter;
    ArrayList<File> folderAL = new ArrayList<File>();
    File imageLocation;
    // Using an array list to create entries for the save location spinner
    final ArrayList<String> defaultDirs = new ArrayList<>();
    File newFolder;

    //Loading screen
    private ProgressDialog dialog;

    Spinner spinner_test;




    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i("here", "onCreate");
        setTheme(ThemeCreator.getTheme(this));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_recordings);
        if (getSupportActionBar() != null) {
            FloraFaunaActionBar.createActionBar(getSupportActionBar(), R.layout.ab_recordings);
        }

        // SSL Certification for webcalls
        if (BuildConfig.DEBUG) {
            SSLCertificates.trustAll();
        }

        // Traverse directories
        PR_Directory = getIntent();

        //Toast.makeText(this, PR_Directory.getStringExtra("RDIR"), Toast.LENGTH_LONG).show();
        if (PR_Directory.getStringExtra("RDIR") == null) {
            dirName = Environment.getExternalStorageDirectory().toString() + "/FloraFauna/Recordings/";
        }
        else {
            dirName = PR_Directory.getStringExtra("RDIR");

            // Split the directory path into an array
            String[] name = dirName.split("/");

            // Call the textView on the action bar .xml file
            TextView textView = findViewById(R.id.recordings_ab_text);

            // Set the actionbar title text to current directory
            textView.setText(name[name.length - 1].substring(0,1).toUpperCase()
                    + name[name.length - 1].substring(1).toLowerCase());
        }

        // Gallery button
        FloatingActionButton openGallery = findViewById(R.id.floatingUpload);
        openGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 1);
            }
        });

        // Open the default camera app to take a picture
//        ImageButton openCamera = findViewById(R.id.floatingCameraButton);
//        openCamera.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                File tempFile = null;
//                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//                    startActivityForResult(takePictureIntent, 2);
//                }
//            }
//        });

        // Create directory for recordings
//        // Request for permission to write to storage
//        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                    MY_PERMISSIONS_REQUEST_ACCESS_WRITE_EXTERNAL_STORAGE);
//        }
        //Test for permission granted
        PackageManager pm = this.getPackageManager();
        int hasPerm = pm.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, this.getPackageName());

        //check for permissions, if false tell user
        if(!(hasPerm == PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(this, "Permission not granted",LENGTH_LONG).show();
        }
        else {
            imagegrid = findViewById(R.id.FileList);
            // Set the directory to be read
            recordings = new File(dirName);

            // Checks if the recordings directory exists
            if (!recordings.exists()) {
                // If directory creation fails, tell the user
                if (!recordings.mkdirs()) {
                    Log.d("error", "failed to make dir");
                    Toast.makeText(this, "Failed to create directory", LENGTH_LONG).show();
                }
            }
            // If the directory exists, log error
            else {
                Log.d("Error", "Directory already exists");
            }

            GetFiles();
            imagegrid.setAdapter(new ImageAdapter());
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
                Intent search_intent = new Intent(PersonalRecordingsActivity.this, SearchActivity.class);
                PR_Directory.removeExtra("RDIR");
                startActivity(search_intent);
                return true;
            case R.id.action_settings:
                Intent settings_intent = new Intent(PersonalRecordingsActivity.this, SettingsActivity.class);
                PR_Directory.removeExtra("RDIR");
                startActivity(settings_intent);
                return true;
            case R.id.action_help:
                Intent help_intent = new Intent(PersonalRecordingsActivity.this, HelpActivity.class);
                PR_Directory.removeExtra("RDIR");
                startActivity(help_intent);
                return true;
            case R.id.action_map:
                Intent intent = new Intent(PersonalRecordingsActivity.this, MapActivity.class);
                PR_Directory.removeExtra("RDIR");
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 2 && resultCode == RESULT_OK)
        {
            Log.i("using camera", "img");
            createImageDialog(data);
        }
        else if (requestCode == 1 && resultCode == RESULT_OK) {
            Log.i("uploading",  "img");
            createImageDialog(data);
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
        // Closes the activity
        PR_Directory.removeExtra("RDIR");
        setResult(RESULT_OK, null);
        finish();
    }

    //List Files
    public void GetFiles()
    {
        if (recordings.isDirectory())
        {
            listFile = recordings.listFiles();

            for (int i = 0; i < listFile.length; i++)
            {
                // Get the path of the image file
                FilePathStrings.add(listFile[i].getAbsolutePath());
                // Get the name image file
                FileNameStrings.add(listFile[i].getName());
            }
        }
    }

    public class ImageAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public ImageAdapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return FilePathStrings.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            File testfile = new File(FilePathStrings.get(position));
            if (convertView == null) {
                holder = new ViewHolder(); //create new ViewHolder (custom class)

                // Inflates the galleryitem.xml layout to be used and populate the gridview
                convertView = mInflater.inflate(R.layout.galleryitem, null);
                holder.imageview = convertView.findViewById(R.id.thumbImage); //thumbnail
                holder.fileName = convertView.findViewById(R.id.fileName); //name of text
                holder.delete = convertView.findViewById(R.id.delete); //delete button
                holder.imgDescription = convertView.findViewById(R.id.description); // image description

                convertView.setTag(holder);

                // Set onclicklistener for delete for each item
                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //code to delete file here
                        //on click, confirm with pop-up,
                        //if true, delete
                        ConfirmDelete(position);
                    }
                });

                //set onclicklistener for each item
                holder.imageview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //if image, do this
                        File testFileClicked = new File(FilePathStrings.get(position));
                        if(!testFileClicked.isDirectory()) {
                            Intent i = new Intent(getApplicationContext(), FullScreenImage.class);
                            // Pass String arrays FilePathStrings
                            i.putExtra("filepath", FilePathStrings);
                            // Pass String arrays FileNameStrings
                            i.putExtra("filename", FileNameStrings);
                            // Pass click position
                            i.putExtra("position", position);
                            startActivity(i);
                        }

                        //if folder, set new directory and open new instance
                        else if(testFileClicked.isDirectory()){
                            //do stuff
                            PR_Directory = new Intent(getApplicationContext(), PersonalRecordingsActivity.class);
                            PR_Directory.putExtra("RDIR", FilePathStrings.get(position));
                            startActivity(PR_Directory);
                        }

                    }
                });
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            //set thumbnail
            final Bitmap myBitmap = BitmapFactory.decodeFile(FilePathStrings.get(position));
            if(!testfile.isDirectory()) {
                holder.imageview.setImageBitmap(myBitmap);
            }
            else if(testfile.isDirectory()){
                holder.imageview.setImageResource(R.drawable.folder);
            }
            //breakdown file path to get only file name
            String filepath = FilePathStrings.get(position);
            ArrayList<String> list = new ArrayList<String>(Arrays.asList(filepath.split("/")));

            // set text name
            // both file name and description come as one string, split by '!'
            // name is first, description is second
            String[] nameDescr = list.get(list.size() - 1).split("!<>!");
            if(nameDescr.length > 0) {
                holder.fileName.setText(nameDescr[0]);
            }


            //set description
            //if not folder
            if(!testfile.isDirectory()) {
                if(nameDescr.length > 1)
                {
                    holder.imgDescription.setText(nameDescr[1]);
                }
            }
            //if folder
            else if(testfile.isDirectory()){
                    holder.imgDescription.setText("");
            }

            return convertView;
        }
    }
    class ViewHolder {
        ImageView imageview;
        TextView fileName;
        CheckBox checkBox;
        ImageButton delete;
        TextView imgDescription;
    }



    // function to create the custom alert dialog
    protected void createImageDialog(final Intent data)
    {
        // create a builder to add custom settings to
        // an alert dialog
        AlertDialog.Builder builder;

        // create alert dialog in personal recordings context
        builder = new AlertDialog.Builder(this);

        // create a view associated with the alert dialog xml file
        View dView = getLayoutInflater().inflate(R.layout.dialog_addimage, null);

        // connect all of the components
        final EditText description = (EditText) dView.findViewById(R.id.description);
        final EditText imageName = (EditText) dView.findViewById(R.id.nameImage);

        // this should result in an image being placed in a directory or on the page
        Button okayButton = (Button) dView.findViewById(R.id.setImageData);
        okayButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                //loading dialog
                dialog = ProgressDialog.show(PersonalRecordingsActivity.this, "", "Loading. Please wait...", true);


                //if name is empty, tell user
                if(imageName.getText().toString().equalsIgnoreCase("")){
                    Toast.makeText(getBaseContext(), "Please Input a File Name..", LENGTH_LONG).show();
                    nameGiven = false;
                    imageDialog.dismiss();
                    createImageDialog(data);
                }
                else {
                    nameGiven = true;
                }


                //test if previously name was given
                if(nameGiven) {
                    Log.i("data gathered", "camera");

                    Uri photoUri = data.getData();
                       if (photoUri != null) {
                            //code to mess with images will be here
                            ContentResolver cr = getContentResolver();
                            try {
                                currentImage = MediaStore.Images.Media.getBitmap(cr, photoUri);
                                Log.i("have an image", currentImage.toString());
                                //selectedImage.setImageBitmap(currentImage); //set the image view to the current image
                                FileOutputStream output = new FileOutputStream(getSaveFolder() + "/" + imageName.getText() + "!<>!" + description.getText());
                                currentImage.compress(Bitmap.CompressFormat.PNG, 100, output); //save file
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            refresh();
                        }
                        else
                       {
                           // taking an image with the camera doesn't return a URI
                           // so getting the thumbnail through extras
                           Bundle extras = data.getExtras();
                           try
                           {
                               Bitmap imageBitmap = (Bitmap) extras.get("data");
                               currentImage = imageBitmap;
                               FileOutputStream output = new FileOutputStream(getSaveFolder() + "/" + imageName.getText() + "!<>!" + description.getText());
                               currentImage.compress(Bitmap.CompressFormat.PNG, 100, output);
                           }
                           catch (Exception e)
                           {
                               e.printStackTrace();
                           }

                           refresh();
                       }

                    imageDialog.dismiss();
                }

            }
        });

        // this should result in nothing added to either the page
        // or any of the directories
        Button cancelButton = (Button) dView.findViewById(R.id.cancelImage);
        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                imageDialog.dismiss();
                if(!(folderName == null)) {
                    newFolder.delete();
                }
            }
        });
        if(nameGiven) {
            defaultDirs.add("Current Folder");
            defaultDirs.add("Create New");
        }

        if (listFile != null) {
            // If there are existing folders, populate those in the imageDialog spinner
            for (int i = 0; i < listFile.length; i++)
            {
                if(listFile[i].isDirectory() && !defaultDirs.contains(listFile[i]))
                {
                    defaultDirs.add(listFile[i].getName());
                }
            }
        }
        if(nameGiven) {

            spinAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, defaultDirs);
            spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }
        // just a TextView to display words, "Save Location"
        TextView saveLoc = (TextView) dView.findViewById(R.id.saveLocation);

        // the spinner's entries should be all existing directories in the F&F folder
        // the user should also have the ability to create a new folder
        // lastly, the user should be able to save a picture in the 'root' part of the page
        // "On Page" for now
        final Spinner dirSelector = (Spinner) dView.findViewById(R.id.dirSpinner);
        spinner_test = dirSelector;
        dirSelector.setAdapter(spinAdapter);

        dirSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // if "Create New" is selected, then open a prompt for the user to type in
                // the name of the new folder
                // if "On Page" is selected, place image on the personal recording page
                // else, place image in selected folder
                String selectedDir = dirSelector.getSelectedItem().toString();
                if(selectedDir.equals("Create New"))
                {
                    createFolderDialog();

                }
                // root personal recordings page
                else if(selectedDir.equals("Current Folder"))
                {
                    Log.i("Save on page", "image");
                    imageLocation = recordings;

                }
                else
                {
                    // create new file for image
                    String folderPath = dirName + "/" + selectedDir;
                    Log.i("root directory", folderPath);
                    File newFolder = new File(folderPath);
                    imageLocation = newFolder;
                    Log.i("PATH", imageLocation.getName());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        builder.setView(dView);

        imageDialog = builder.create();

        if(!nameGiven){
            imageName.setTextColor(Color.RED);
            imageName.setHintTextColor(Color.RED);
        }

        imageDialog.setTitle("Save Image");
        imageDialog.show();
    }

    protected void createFolderDialog()
    {
        Log.i("selected new", "folder");
        // create a builder to add custom settings to
        // an alert dialog
        AlertDialog.Builder dirBuilder;

        // create alert dialog in personal recordings context
        dirBuilder = new AlertDialog.Builder(PersonalRecordingsActivity.this);

        // create a view associated with the alert dialog xml file
        View dirView = getLayoutInflater().inflate(R.layout.dialog_createdir, null);

        // three components of this dialog
        // dirText, cancelButton, saveButton
        final EditText newFolder = (EditText) dirView.findViewById(R.id.dirText);

        // don't create folder, just close dialog
        Button noSave = (Button) dirView.findViewById(R.id.cancelButton);
        noSave.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                folderDialog.dismiss();
            }
        });

        Button saveFolder = (Button) dirView.findViewById(R.id.saveButton);
        saveFolder.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                // get the folder name and create the folder
                if(!newFolder.getText().toString().equalsIgnoreCase("")) {
                    folderName = newFolder.getText().toString();
                    // if folder doesn't already exist, create it
                    if(!folderAL.contains(folderName))
                    {
                        createNewFolder(folderName);

                        spinner_test.setSelection(spinner_test.getAdapter().getCount()-1);

                        folderDialog.dismiss();
                    }
                }
                // alert that a name hasn't been entered
                else
                {
                    Toast.makeText(getBaseContext(), "Enter a folder name", LENGTH_LONG).show();
                }


            }
        });

        dirBuilder.setView(dirView);

        folderDialog = dirBuilder.create();
        folderDialog.show();

    }

    // function to create the custom alert dialog
    public void ConfirmDelete(final int position)
    {
        // create a builder to add custom settings to
        // an alert dialog
        AlertDialog.Builder builder;

        // create alert dialog in personal recordings context
        builder = new AlertDialog.Builder(this);

        // create a view associated with the alert dialog xml file
        View dView = getLayoutInflater().inflate(R.layout.dialog_confirmdelete, null);


        // this should result in an image or directory being removed from storage and page
        Button okayButton = (Button) dView.findViewById(R.id.yesButton);
        okayButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                //loading dialog
                dialog = ProgressDialog.show(PersonalRecordingsActivity.this, "", "Deleting. Please wait...", true);

                File deleteThis = new File(FilePathStrings.get(position));
                //if directory, recursively delete
                if(deleteThis.isDirectory()){
                    String[] children = deleteThis.list();
                    for (int i = 0; i < children.length; i++)
                    {
                        new File(deleteThis, children[i]).delete();
                    }
                    deleteThis.delete();

                }
                else {
                    deleteThis.delete();
                }
                //refresh the activity
                refresh();
                imageDialog.dismiss();
            }
        });

        // this should result in nothing added to either the page
        // or any of the directories
        Button cancelButton = (Button) dView.findViewById(R.id.noButton);
        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                imageDialog.dismiss();
            }
        });

        builder.setView(dView);

        imageDialog = builder.create();

        imageDialog.setTitle("Confirm Delete?");
        imageDialog.show();
    }

    protected void refresh(){
        Intent refresh = new Intent(PersonalRecordingsActivity.this, PersonalRecordingsActivity.class);
        refresh.putExtra("RDIR", dirName);
        finish();
        startActivity(refresh);

    }

    // Create a folder in the root path /FloraFauna/Recordings
    protected void createNewFolder(String pathname)
    {
        String folderPath = dirName + "/" + pathname;
        Log.i("root directory", folderPath);
        newFolder = new File(folderPath);

        // create new folder and store it in the folder array list
        if(newFolder.mkdir())
        {

            folderAL.add(newFolder);
            defaultDirs.add(newFolder.getName());
            spinAdapter.notifyDataSetChanged();
        }
        else
        {
            Toast.makeText(getBaseContext(), "Couldn't create folder", LENGTH_LONG).show();
        }
    }

    // Returns the folder that the user is intending to save an image in
    protected File getSaveFolder()
    {
        return imageLocation;
    }

}

