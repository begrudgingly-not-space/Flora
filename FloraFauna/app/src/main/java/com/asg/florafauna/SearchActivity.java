package com.asg.florafauna;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import static com.asg.florafauna.CountyFinder.countyFinder;
import static com.asg.florafauna.StateFinder.stateFinder;

public class SearchActivity extends AppCompatActivity implements LocationListener {
    private static final String TAG = "SearchActivity";
    private AutoCompleteTextView searchEditText;
    private ListView speciesListView;
    private InputMethodManager imm;
    private ProgressDialog dialog;
    private String locationPolygon;
    private double latitude;
    private double longitude;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_COARSE_LOCATION = 0;
    private int searchType = 0;
    public static final String INTENT_EXTRA_SPECIES_NAME = "speciesName";
    private int offset = 0;
    private ArrayList<String> speciesNamesArray = new ArrayList<>(), filteredArrayList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private ArrayList<String> history = new ArrayList<>();
    private String[] mileageArray = new String[1];
    private EditText filterEditText;
    private LinearLayout filterAndRadioButtons;
    private double mileage;
    private RadioButton Kingdom;
    private RadioButton Genus;
    private RadioButton MyLocation;
    private String[] themeArray = new String[1];

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Sets default theme if none found
        File themes = new File("theme");
        if(!themes.exists())
        {
            //sets default
            try{
                FileOutputStream fOut = openFileOutput("theme", MODE_PRIVATE); //open file 'theme'
                OutputStreamWriter osw = new OutputStreamWriter(fOut); // required to 'write' to file
                osw.write("Green");
                //clean up
                osw.flush();
                osw.close();
                fOut.close();
            }
            catch (FileNotFoundException x){
                x.printStackTrace();
            }
            catch (IOException x){
                Log.e("Exception", "Failed to save history: " + x.toString());
            }
        }

        //setTheme(R.style.AppTheme);
        try {
            //opens the file to read its contents
            FileInputStream fis = this.openFileInput("theme");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);

            themeArray[0] = reader.readLine(); //adds the line to the temp array
            reader.close();
            isr.close();
            fis.close();
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        if (themeArray[0].equals("Green")){
            setTheme(R.style.AppTheme);
        }
        else if (themeArray[0].equals("Blue")){
            setTheme(R.style.AppThemeBlue);
        }
        else if (themeArray[0].equals("Mono")){
            setTheme(R.style.AppThemeMono);
        }
        else if (themeArray[0].equals("Cherry")){
            setTheme(R.style.AppThemeCherry);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); // Enable hiding/showing keyboard
        FloraFaunaActionBar.createActionBar(getSupportActionBar(), R.layout.ab_search);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_COARSE_LOCATION);
        }
        getLocation();
        getMileage();

        searchEditText = findViewById(R.id.SearchEditText);
        speciesListView = findViewById(R.id.ListSpecies);
        filterEditText = findViewById(R.id.FilterEditText);
        filterAndRadioButtons = findViewById(R.id.FilterAndRadioButtons);
        Kingdom = findViewById(R.id.KingdomButton);
        Genus = findViewById(R.id.GenusButton);
        MyLocation = findViewById(R.id.MyLocationButton);


        // Setup for load more button
        Button btnLoadMore = new Button(this);
        btnLoadMore.setText("Load More");
        speciesListView.addFooterView(btnLoadMore);

        // Listener for load more button
        btnLoadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (searchType != 0) {
                    loadMore(searchType);
                }
            }
        });

        searchEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    search();
                }
                return false;
            }
        });

        filterEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    dialog = ProgressDialog.show(SearchActivity.this, "", "Loading. Please wait...", true);
                    filter();
                }
                return false;
            }
        });

        // Sets the history and sets dropdown list
        setHistory();

        RadioGroup radioGroup = findViewById(R.id.RadioButtons);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = findViewById(checkedId);
                String selection = (String) rb.getText();

                if (selection.equals("Scientific/Common Name")) {
                    searchEditText.setHint("Scientific/Common Name");
                }
                else if (selection.equals("County, State")) {
                    searchEditText.setHint("County, State");
                }
                else {
                    searchEditText.setHint("State");
                }
            }
        });
    }

    private void getMileage() {
        try {
            // Opens the file to read its contents
            FileInputStream fis = this.openFileInput("mileage");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);

            mileageArray[0] = reader.readLine(); // Adds the line to the mileageArray
            reader.close();
            isr.close();
            fis.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            mileageArray[0] = "1";
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        mileage = Double.parseDouble(mileageArray[0]);
    }

    public void getLocation() {
        try {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, this);
        }
        catch (SecurityException e) {
            Log.e("LocationSecurityExc", e.toString());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        Log.d (TAG, "Latitude = " + latitude + "\n Longitude = " + longitude);
    }

    @Override
    public void onProviderDisabled(String provider) {
        //Toast.makeText(SearchActivity.this, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settings_intent = new Intent(SearchActivity.this, SettingsActivity.class);
                startActivityForResult(settings_intent, 1);
                return true;
            case R.id.action_help:
                Intent help_intent = new Intent(SearchActivity.this, HelpActivity.class);
                startActivity(help_intent);
                return true;
            case R.id.action_map:
                Intent map_intent = new Intent(SearchActivity.this, MapActivity.class);
                startActivity(map_intent);
                return true;
            case R.id.action_recording:
                Intent recordings_intent = new Intent(SearchActivity.this, PersonalRecordingsActivity.class);
                startActivity(recordings_intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                }
                else {
                    // Permission denied, What's Around Me functionality disabled
                }
            }
        }
    }

    // Method to make sure search history is cleared
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            Intent refresh = new Intent(this, SearchActivity.class);
            startActivity(refresh);
            this.finish();
        }
    }

    @Override
    public void onBackPressed()
    {
        // Disables going back by manually pressing the back button
    }

    public void search(View view) {
        search();
    }

    public void filter(View view) {
        Log.d("Filter", "Click");
        dialog = ProgressDialog.show(this, "", "Loading. Please wait...", true);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                filter();
            }
        }, 1000);

    }

    // SEARCH FUNCTIONS
    public void search() {
        filterAndRadioButtons.setVisibility(View.VISIBLE);
        String searchInput = searchEditText.getText().toString();
        speciesNamesArray = new ArrayList<>();
        offset = 0;

        dialog = ProgressDialog.show(this, "", "Loading. Please wait...", true);

        // Create links to radio buttons
        RadioButton Species = findViewById(R.id.SpeciesButton);
        RadioButton County = findViewById(R.id.CountyButton);
        RadioButton State = findViewById(R.id.StateButton);

        //Checks which button (search type) is checked
        if (State.isChecked())
        {
            // Search by State
            searchRequestWithState(this, searchInput);
        }
        else if (Species.isChecked())
        {
            // Search by Species/common name
            searchRequestWithSpecies(this, searchInput);
        }
        else if (County.isChecked())
        {
            // Search by County
            searchRequestWithCounty(this, searchInput);
        }

        // Save history to file
        if(searchInput.length() != 0) {
            saveHistory(searchInput);
        }

        // Read search history
        setHistory();
    }

    private void filter() {
        //Checks which button (search type) is checked
        if (Kingdom.isChecked())
        {
        }
        else if (Genus.isChecked())
        {
            filterByGenus();
        }
        else if (MyLocation.isChecked())
        {
            filterByLocation();
        }
    }

    private void filterByGenus() {
        filteredArrayList.clear();
        for (int i = 0; i < speciesNamesArray.size(); i++) {
            String scientificName = speciesNamesArray.get(i);
            String genus = scientificName.substring(0, scientificName.indexOf(" "));
            if (genus.equals(filterEditText.getText().toString())) {
                filteredArrayList.add(speciesNamesArray.get(i));
            }
            Log.d("Genus", genus);
        }

        if (filteredArrayList.isEmpty()) {
            dialog.dismiss();
            AlertDialog alertDialog = new AlertDialog.Builder(SearchActivity.this).create();
            alertDialog.setTitle("No matches found.");
            alertDialog.setMessage("Please check the spelling of your entry.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface alertDialog, int which) {
                            alertDialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
        else {
            dialog.dismiss();
            adapter = new ArrayAdapter<String>(SearchActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, filteredArrayList);
            speciesListView.setAdapter(adapter);
        }
    }

    private void filterByLocation() {
        filteredArrayList.clear();
        String scientificName;
        String commonName;
        int speciesListCount = speciesNamesArray.size();
        for (int i = 0; i < speciesNamesArray.size(); i++) {
            speciesListCount--;
            if (speciesNamesArray.get(i).contains(",")) {
                scientificName = speciesNamesArray.get(i).substring(0, speciesNamesArray.get(i).indexOf(","));
                commonName = speciesNamesArray.get(i).substring(speciesNamesArray.get(i).indexOf(","), speciesNamesArray.get(i).length());
            }
            else {
                scientificName = speciesNamesArray.get(i).substring(0, speciesNamesArray.get(i).length());
                commonName = "";
            }

            Log.d("Check location", scientificName);
            locationRequest(this, scientificName, commonName, speciesListCount);
        }
    }

    private void locationRequest(Context context, final String scientificName, final String commonName, final int speciesListCount){
        String locationPolygon = setAOIBbox(latitude, longitude, mileage);
        final String url = "https://bison.usgs.gov/api/search.json?species=" + scientificName + "&type=scientific_name&aoibbox=" + locationPolygon + "&start=0&count=1";
        Log.d("url", url);

        // Initialize request queue
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest locationRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            final JSONArray speciesArray = response.getJSONArray("data");
                            Log.d("species Array", speciesArray.toString());
                            if (speciesArray.length() > 0) {
                                filteredArrayList.add(scientificName + commonName);
                            }
                        }
                        catch (JSONException error) {
                            Log.e("Species not in location", error.toString());
                        }

                        if (speciesListCount == 0) {
                            if (filteredArrayList.isEmpty()) {
                                dialog.dismiss();
                                AlertDialog alertDialog = new AlertDialog.Builder(SearchActivity.this).create();
                                alertDialog.setTitle("No matches found.");
                                alertDialog.setMessage("There are no species by that name near you.");
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface alertDialog, int which) {
                                                alertDialog.dismiss();
                                            }
                                        });
                                alertDialog.show();
                            }
                            else {
                                dialog.dismiss();
                                adapter = new ArrayAdapter<String>(SearchActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, filteredArrayList);
                                speciesListView.setAdapter(adapter);
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("onErrorResponse", error.toString());
                if (speciesListCount == 0) {
                    if (filteredArrayList.isEmpty()) {
                        dialog.dismiss();
                        AlertDialog alertDialog = new AlertDialog.Builder(SearchActivity.this).create();
                        alertDialog.setTitle("No matches found.");
                        alertDialog.setMessage("There are no species by that name near you.");
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface alertDialog, int which) {
                                        alertDialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                    }
                    else {
                        dialog.dismiss();
                        adapter = new ArrayAdapter<String>(SearchActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, filteredArrayList);
                        speciesListView.setAdapter(adapter);
                    }
                }
            }
        }
        );

        // Adds request to queue which is then sent
        requestQueue.add(locationRequest);
    }

    private void searchRequestWithState(final Context context, final String state) {
        // stateInput capitalizes the state
        // Bison produces an error if you input a state in all lowercase letters
        final int position = speciesNamesArray.size();
        String stateInput = "";

        if (state.length() > 2) {
            stateInput = state.substring(0, 1).toUpperCase() + state.substring(1);

            if (stateInput.contains(" ")){
                String[] stateParts = stateInput.split(" ");
                stateParts[1] = stateParts[1].substring(0,1).toUpperCase() + stateParts[1].substring(1);
                stateInput = stateParts[0] + " " + stateParts[1];
            }

        }
        else if (state.length() == 2) {
            stateInput = state.substring(0,2).toUpperCase();
            stateInput = stateFinder(context, stateInput);
        }

        stateInput = stateInput.replaceAll(" ", "%20");

        final String url = "https://bison.usgs.gov/api/search.json?state=" + stateInput + "&start=" + offset + "&count=50";
        Log.d("url", url);

        // Initialize request queue
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest searchRequestWithState = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dialog.dismiss();

                        try {
                            searchType = 2;
                            final JSONArray speciesArray = response.getJSONArray("data");

                            for(int i = 0; i < speciesArray.length(); i++) {
                                String currentScientificName = speciesArray.getJSONObject(i).getString("name");
                                String currentCommonName = speciesArray.getJSONObject(i).getString("common_name");
                                String fullName = currentScientificName;

                                if (!currentCommonName.equals("")) {
                                    String[] nameArray = currentCommonName.split(",");
                                    fullName = currentScientificName + ", " + nameArray[0];
                                }

                                if (!speciesNamesArray.contains(fullName)) {
                                    speciesNamesArray.add(fullName);
                                }
                            }

                            Log.d("searchResponse", speciesNamesArray.toString());

                           /* if (!selection.equals("None")){
                                speciesNamesArray = filter(selection);
                            }*/

                            adapter = new ArrayAdapter<>(SearchActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, speciesNamesArray);
                            speciesListView.setAdapter(adapter);
                            speciesListView.setVisibility(View.VISIBLE);
                            speciesListView.setSelection(position);

                            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                            speciesListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                            {
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                                {
                                    openSpeciesInfoPage(position);
                                }
                            });
                        }
                        catch (JSONException error) {
                            Log.e("searchResponseException", error.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("onErrorResponse", error.toString());
                dialog.dismiss();
                AlertDialog alertDialog = new AlertDialog.Builder(SearchActivity.this).create();
                alertDialog.setTitle("Invalid State");
                alertDialog.setMessage("Please input the full name of a state.");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface alertDialog, int which) {
                                alertDialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        }
        );

        // Adds request to queue which is then sent
        requestQueue.add(searchRequestWithState);
    }

    private void searchRequestWithCounty(final Context context, final String searchInput) {
        final int position = speciesNamesArray.size();

        String searchTerms[];
        String county = "";
        String state = "";

        if (searchInput.contains(",") && searchInput.length() >= 3) {
            searchTerms = searchInput.split(",");

            if (searchTerms.length == 2) {
                county = searchTerms[0];
                state = searchTerms[1];
                state = state.substring(1, state.length());
            }
        }

        if (state.length() > 2) {
            state = state.substring(0, 1).toUpperCase() + state.substring(1);

            if (state.contains(" ")){
                String[] stateParts = state.split(" ");
                stateParts[1] = stateParts[1].substring(0,1).toUpperCase() + stateParts[1].substring(1);
                state = stateParts[0] + " " + stateParts[1];
            }

        }
        else if (state.length() == 2) {
            state = state.substring(0,2).toUpperCase();
            state = stateFinder(context, state);
        }

        //capitalizes first char in county
        if (county.length() > 0){
            county = county.substring(0, 1).toUpperCase() + county.substring(1);
        }

        String countyFips = countyFinder(context, state, county);

        Log.d("searchterms", county);
        Log.d("searchterms", state);

        state = state.replaceAll(" ", "%20");

        final String url = "https://bison.usgs.gov/api/search.json?state=" + state + "&countyFips=" + countyFips + "&start=" + offset + "&count=50";
        Log.d("url", url);

        // Initialize request queue
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest searchRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dialog.dismiss();

                        try {
                            searchType = 1;
                            JSONArray speciesArray = response.getJSONArray("data");

                            for(int i = 0; i < speciesArray.length(); i++) {
                                String currentScientificName = speciesArray.getJSONObject(i).getString("name");
                                String currentCommonName = speciesArray.getJSONObject(i).getString("common_name");
                                String fullName = currentScientificName;

                                if (!currentCommonName.equals("")) {
                                    String[] nameArray = currentCommonName.split(",");
                                    fullName = currentScientificName + ", " + nameArray[0];
                                }

                                if (!speciesNamesArray.contains(fullName)) {
                                    speciesNamesArray.add(fullName);
                                }
                            }

                            Log.d("searchRespWithCounty", speciesNamesArray.toString());

                            adapter = new ArrayAdapter<String>(SearchActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, speciesNamesArray);

                            speciesListView.setAdapter(adapter);
                            speciesListView.setVisibility(View.VISIBLE);
                            speciesListView.setSelection(position);

                            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                            speciesListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                            {
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                                {
                                    openSpeciesInfoPage(position);
                                }
                            });
                        }
                        catch (JSONException error) {
                            Log.e("searchResponseException", error.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("onErrorResponse", error.toString());
                dialog.dismiss();
                AlertDialog alertDialog = new AlertDialog.Builder(SearchActivity.this).create();
                alertDialog.setTitle("Invalid County, State");
                alertDialog.setMessage("Please input the name of a county followed by a comma and then the name of the state");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface alertDialog, int which) {
                                alertDialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        }
        );

        // Adds request to queue which is then sent
        requestQueue.add(searchRequest);
    }

    private void searchRequestWithSpecies(final Context context, final String speciesName)
    {
        final SpeciesSearchHelper helper = new SpeciesSearchHelper();
        int length = helper.getNameLength(speciesName);

        // just a space entered
        if (length < 1)
        {
            // dialog -- please enter a species name
        }
        else if (length == 1)
        {
            // throw species name to partial search
            searchPartialName(this, speciesName);
            return;
        }

        Log.i("species input", speciesName);


        // base address for searching for a species
        String baseAddress = "https://www.itis.gov/ITISWebService/jsonservice/ITISService/searchForAnyMatch?srchKey=";
        // url for searching
        String formattedName = speciesName.replaceAll(" ", "%20");
        final String query = baseAddress + formattedName;

        Log.i("final url", query);

        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest searchRequest = new JsonObjectRequest(Request.Method.GET, query, null, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                String scientificName = null;
                String commonName = null;
                boolean match = false;

                // closes the loading, please wait dialog
                dialog.dismiss();

                try
                {
                    searchType = 4;
                    Log.i("response", response.toString());

                    // all of the possible results from the search
                    JSONArray arr = response.getJSONArray("anyMatchList");

                    // go through each index
                    for(int i = 0; i < arr.length(); i++)
                    {
                        JSONObject results = arr.getJSONObject(i);
                        JSONArray commonNames = results.getJSONObject("commonNameList").getJSONArray("commonNames");

                        // if the resulting scientific name is not two words long, then incorrect results
                        // could be a genus, family, or no results at all
                        String[] sciName = results.getString("sciName").split(" ");
                        if(sciName.length == 2)
                        {
                            scientificName = results.getString("sciName");
                        }

                        int j = 0;
                        while(!match && j < commonNames.length())
                        {
                            // each species result may have multiple common names for one or many languages
                            // go through all of the common names and find the English common name
                            JSONObject comm = commonNames.getJSONObject(j);
                            String[] commName = comm.getString("commonName").split(" ");

                            if(comm.getString("language").equalsIgnoreCase("English"))
                            {
                                // a common name can't be one word, scientific name must be two words
                                // and one of the names should match what the user entered
                                if((commName.length >= 2 && sciName.length == 2) && (speciesName.equalsIgnoreCase(results.getString("sciName")) || speciesName.equalsIgnoreCase(comm.getString("commonName"))))
                                {
                                    Log.i("Match", "MATCH!");
                                    match = true;
                                    commonName = comm.getString("commonName");
                                }
                            }

                            j++;
                        }
                        if (match)
                        {
                            break;
                        }
                    }

                    // if no results for at least one of the names, throw an exception
                    if(commonName == null || scientificName == null)
                    {
                        throw new Exception("No species results: Null name");
                    }

                    commonName = helper.capitalizeName(commonName);
                    speciesNamesArray.add(scientificName + ", " + commonName);
                    Log.i("Names", speciesNamesArray.get(0));

                    // throw species name in ListView and display
                    adapter = new ArrayAdapter<String>(SearchActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, speciesNamesArray);
                    speciesListView.setAdapter(adapter);
                    speciesListView.setVisibility(View.VISIBLE);

                    // hide the keyboard
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                    // on clicking species name listview, user should be sent to species info page
                    speciesListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                    {
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                        {
                            openSpeciesInfoPage(position);
                        }
                    });


                }
                catch(Exception exception)
                {
                    Log.e("Couldn't grab JSON data", exception.toString());

                    AlertDialog alertDialog = new AlertDialog.Builder(SearchActivity.this).create();
                    alertDialog.setTitle("Unknown Species Name");
                    alertDialog.setMessage("Please enter a common or scientific name");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface alertDialog, int which) {
                                    alertDialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
            }
        }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    Log.e("Error: ", error.toString());
                    dialog.dismiss();
                }
            });

        requestQueue.add(searchRequest);
    }

    private void searchPartialName(final Context context, final String speciesName)
    {
        Log.i("partial", "partial search");
        final SpeciesSearchHelper helper = new SpeciesSearchHelper();
        // base address for searching for a species/genus
        // this will grab any name that contains the text searched by user
        String baseAddress = "https://www.itis.gov/ITISWebService/jsonservice/ITISService/getITISTerms?srchKey=";
        //final ArrayList<String> speciesList = new ArrayList<>();

        // ensure no trailing whitespace for web call
        final String query = baseAddress + speciesName.trim();
        Log.i("final url:", query);

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JsonObjectRequest searchRequest = new JsonObjectRequest(Request.Method.GET, query, null, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                // closes the loading, please wait dialog
                dialog.dismiss();

                String scientificName = null;
                String commonName = null;

                try
                {
                    searchType = 4;
                    Log.i("response", response.toString());

                    // get the list
                    JSONArray arr = response.getJSONArray("itisTerms");
                    JSONArray commonNames = null;
                    //String[] speciesArr = new String[arr.length()];
                    /* the web call will produce all kinds of results, including the genus in
                     addition to specific species names
                     to ensure we only get species names, the resulting scientific name
                     must be two or more words in length
                    */
                    for(int i = 0; i < arr.length(); i++)
                    {
                        JSONObject results = arr.getJSONObject(i);
                        if(helper.getNameLength(results.getString("scientificName")) > 1)
                        {
                            scientificName = results.getString("scientificName");
                            commonNames = results.getJSONArray("commonNames");
                            if(commonNames.length() > 0)
                            {
                                commonName = commonNames.getString(0);
                            }

                            // get rid of null names
                            if(!commonName.equalsIgnoreCase("null") && !scientificName.equalsIgnoreCase("null"))
                            {
                                commonName = helper.capitalizeName(commonName);
                                speciesNamesArray.add(scientificName + ", " + commonName);
                            }

                        }

                        Log.i("common name", commonName);
                        Log.i("scientific name", scientificName);

                    }

                    // throw all names in ListView and display
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(SearchActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, speciesNamesArray);
                    speciesListView.setAdapter(adapter);
                    speciesListView.setVisibility(View.VISIBLE);

                    // hide the keyboard
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                    // on clicking species name listview, user should be sent to species info page
                    speciesListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                    {
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                        {
                            openSpeciesInfoPage(position);
                        }
                    });


                }
                catch(Exception exception)
                {
                    Log.e("Couldn't grab JSON data", exception.toString());

                    AlertDialog alertDialog = new AlertDialog.Builder(SearchActivity.this).create();
                    alertDialog.setTitle("Unknown Species Name");
                    alertDialog.setMessage("Please enter a common or scientific name");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface alertDialog, int which) {
                                    alertDialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e("Error: ", error.toString());
                dialog.dismiss();
            }
        });

        requestQueue.add(searchRequest);

    }

    public void whatsAroundMe(View view) {
        filterAndRadioButtons.setVisibility(View.VISIBLE);
        speciesNamesArray = new ArrayList<>();
        offset = 0;
        if (latitude != 0 && longitude != 0) {
            double mileage = Double.parseDouble(mileageArray[0]);
            locationPolygon = setAOIBbox(latitude, longitude, mileage);
            Log.d(TAG, locationPolygon);
            dialog = ProgressDialog.show(this, "","Loading. Please wait...", true);
            whatsAroundMeRequest(this, locationPolygon);
        }
        else {
            // Error message
            Log.e(TAG, "Location not found");
            AlertDialog alertDialog = new AlertDialog.Builder(SearchActivity.this).create();
            alertDialog.setTitle("Location not found");
            alertDialog.setMessage("The application may not be able to locate you.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface alertDialog, int which) {
                            alertDialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
    }

    private String setAOIBbox(double latitude, double longitude, double mileage) {
        double milesPerDegreeOfLatitude = 69;
        double milesPerDegreeOfLongitude = Math.cos(Math.toRadians(latitude)) * 69.172;

        double degreesOfLatitudePerMile = 1/milesPerDegreeOfLatitude;
        double degreesOfLongitudePerMile = 1/milesPerDegreeOfLongitude;

        Log.i(TAG, degreesOfLatitudePerMile + " " + degreesOfLongitudePerMile);
        String minLat = Double.toString(latitude - (degreesOfLatitudePerMile*mileage));
        String minLong = Double.toString(longitude - (degreesOfLongitudePerMile*mileage));
        String maxLat = Double.toString(latitude + (degreesOfLatitudePerMile*mileage));
        String maxLong = Double.toString(longitude + (degreesOfLongitudePerMile*mileage));

        return minLong + "," + minLat + "," + maxLong + "," + maxLat;
    }

    // What's Around Me? webcall
    private void whatsAroundMeRequest(final Context context, final String polygon)
    {
        final String url = "https://bison.usgs.gov/api/search.json?aoibbox=" + polygon + "&start=" + offset + "&count=50";;
        Log.d("url", url);
        final int position = speciesNamesArray.size();

        // Initialize request queue
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest whatsAroundMeRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dialog.dismiss();

                        try {
                            searchType = 3;
                            JSONArray speciesArray = response.getJSONArray("data");

                            for(int i = 0; i < speciesArray.length(); i++) {
                                String currentScientificName = speciesArray.getJSONObject(i).getString("name");
                                String currentCommonName = speciesArray.getJSONObject(i).getString("common_name");
                                String fullName = currentScientificName;

                                if (!currentCommonName.equals("")) {
                                    String[] nameArray = currentCommonName.split(",");
                                    fullName = currentScientificName + ", " + nameArray[0];
                                }

                                if (!speciesNamesArray.contains(fullName)) {
                                    speciesNamesArray.add(fullName);
                                }
                            }

                            Log.d("whatsAroundMeResponse", speciesNamesArray.toString());

                            adapter = new ArrayAdapter<String>(SearchActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, speciesNamesArray);

                            speciesListView.setAdapter(adapter);
                            speciesListView.setVisibility(View.VISIBLE);
                            speciesListView.setSelection(position);

                            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                            speciesListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                            {
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                                {
                                    openSpeciesInfoPage(position);
                                }
                            });
                        }
                        catch (JSONException error) {
                            Log.e("whatsAroundMeRespExcept", error.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("onErrorResponse", error.toString());
                dialog.dismiss();
                AlertDialog alertDialog = new AlertDialog.Builder(SearchActivity.this).create();
                alertDialog.setTitle("What's Around Me Error Description");
                alertDialog.setMessage(error.toString());
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface alertDialog, int which) {
                                alertDialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        }
        );

        // Adds request to queue which is then sent
        requestQueue.add(whatsAroundMeRequest);
    }

    // Changes offset and reruns search
    public void loadMore(int searchType){
        dialog = ProgressDialog.show(this, "",
                "Loading. Please wait...", true);
        offset += 500;
        String searchInput = searchEditText.getText().toString();
        if (searchType == 1) {
            searchRequestWithCounty(this, searchInput);
        }
        else if (searchType == 2) {
            searchRequestWithState(this, searchInput);
        }
        else if (searchType == 3) {
            whatsAroundMeRequest(this, locationPolygon);
        }
        else if (searchType == 4) {
            searchRequestWithSpecies(this, searchInput);
        }

    }

    // Save history method
    private void saveHistory(String data){
        try{
            FileOutputStream fOut = openFileOutput("history", MODE_APPEND);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);
            osw.append(data);
            osw.append("\n");
            osw.flush();
            osw.close();
            fOut.close();
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
        catch (IOException e){
            Log.e("Exception", "Failed to save history: " + e.toString());
        }
    }

    // Sets the history for a dropdown
    public void setHistory(){
        history.clear();
        try {
            FileInputStream fis = this.openFileInput("history");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);
            String line;

            while ((line = reader.readLine()) != null)
            {
                history.add(line);
            }
            reader.close();
            isr.close();
            fis.close();
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_selectable_list_item, history);
        searchEditText.setThreshold(0);
        searchEditText.setAdapter(adapter);
    }

    private void openSpeciesInfoPage(int position) {
        String speciesName = speciesListView.getItemAtPosition(position).toString();
        String scientificName = speciesName.substring(0, speciesName.indexOf(","));
        Intent intent = new Intent(SearchActivity.this, SpeciesInfoActivity.class);
        intent.putExtra(INTENT_EXTRA_SPECIES_NAME, scientificName);
        startActivity(intent);
    }
}