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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
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
import static com.asg.florafauna.FloraFaunaActionBar.createActionBar;
import static com.asg.florafauna.StateFinder.stateFinder;

public class SearchActivity extends AppCompatActivity implements LocationListener {
    private static final String TAG = "SearchActivity";
    private AutoCompleteTextView searchEditText;
    private ListView speciesListView;
    private InputMethodManager imm;
    private ProgressDialog dialog;
    private String locationPolygon;
    private double latitude, longitude, mileage;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_COARSE_LOCATION = 0;

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_WRITE_EXTERNAL_STORAGE = 0;

    public static final String INTENT_EXTRA_SPECIES_NAME = "speciesName";
    private int offset = 0, searchType = 0;
    private ArrayList<String> speciesNamesArray = new ArrayList<>(), filteredArrayList = new ArrayList<>(), history = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private String[] mileageArray = new String[1];
    private LinearLayout filterAndRadioButtons;
    private RadioButton Kingdom, Genus, MyLocation;
    private FrameLayout searchBox;
    private LinearLayout countyStateInput;
    private EditText countyInput, stateInput, filterEditText;
    private TextView myLocationBlackout;
    private Button btnLoadMore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeCreator.getTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // SSL Certification for webcalls
        if (BuildConfig.DEBUG) {
            SSLCertificates.trustAll();
        }

        if (getSupportActionBar() != null) {
            createActionBar(getSupportActionBar(), R.layout.ab_search);
        }

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_COARSE_LOCATION);
        }
        else {
            getLocation();
            getMileage();
        }
        // Request for permission to write to storage
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_ACCESS_WRITE_EXTERNAL_STORAGE);
        }

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); // Enable hiding/showing keyboard
        searchBox = findViewById(R.id.SearchFrame);
        searchEditText = findViewById(R.id.SearchEditText);
        searchEditText.requestFocus();
        filterEditText = findViewById(R.id.FilterEditText);
        speciesListView = findViewById(R.id.ListSpecies);

        filterAndRadioButtons = findViewById(R.id.FilterAndRadioButtons);
        Kingdom = findViewById(R.id.KingdomButton);
        Genus = findViewById(R.id.GenusButton);
        MyLocation = findViewById(R.id.MyLocationButton);

        countyStateInput = findViewById(R.id.locationInput);
        countyInput = findViewById(R.id.countyInput);
        stateInput = findViewById(R.id.stateInput);

        // Setup for load more button
        btnLoadMore = new Button(this);
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

        stateInput.setOnEditorActionListener(new EditText.OnEditorActionListener() {
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

                if (selection.equals("Species")) {
                    searchEditText.setHint("Scientific OR Common Name");
                    searchBox.setVisibility(View.VISIBLE);
                    countyStateInput.setVisibility(View.INVISIBLE);
                }
                else if (selection.equals("County")) {
                    countyInput.requestFocus();
                    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
                    countyInput.setHint("County");
                    stateInput.setHint("State");
                    searchBox.setVisibility(View.INVISIBLE);
                    countyStateInput.setVisibility(View.VISIBLE);
                }
                else {
                    searchEditText.setHint("State");
                    searchBox.setVisibility(View.VISIBLE);
                    countyStateInput.setVisibility(View.INVISIBLE);
                }
            }
        });

        RadioGroup filterRadioButtons = findViewById(R.id.FilterRadioButtons);
        myLocationBlackout = findViewById(R.id.MyLocationBlackout);
        filterRadioButtons.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = findViewById(checkedId);
                String selection = (String) rb.getText();

                if (selection.equals("Kingdom")) {
                    filterEditText.setHint("Kingdom");
                    myLocationBlackout.setVisibility(View.INVISIBLE);
                }
                else if (selection.equals("Genus")) {
                    filterEditText.setHint("Genus");
                    myLocationBlackout.setVisibility(View.INVISIBLE);
                }
                else {
                    filterEditText.setHint("My Location");
                    myLocationBlackout.setVisibility(View.VISIBLE);
                }
            }
        });

        speciesListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                openSpeciesInfoPage(position);
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
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    getLocation();
                    getMileage();
                }
        }
        }
    }

    // Method to make sure search history is cleared
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            Intent refresh = new Intent(this, SearchActivity.class);
            startActivity(refresh);
            this.finish();
        }
    }

    // OnClick method for search
    public void search(View view) {
        search();
    }

    // SEARCH FUNCTIONS
    public void search() {
        dialog = ProgressDialog.show(this, "", "Loading. Please wait...", true);
        filterAndRadioButtons.setVisibility(View.VISIBLE);
        String searchInput = searchEditText.getText().toString();

        String county = countyInput.getText().toString();
        String state = stateInput.getText().toString();

        speciesNamesArray = new ArrayList<>();
        offset = 0;

        // Create links to radio buttons
        RadioButton Species = findViewById(R.id.SpeciesButton);
        RadioButton County = findViewById(R.id.CountyButton);
        RadioButton State = findViewById(R.id.StateButton);

        // Checks which button (search type) is checked
        if (State.isChecked())
        {
            // Search by State
            searchRequestWithState(this, searchInput);
        }
        else if (Species.isChecked())
        {
            // Search by Scientific/Common Name
            searchRequestWithSpecies(this, searchInput);
        }
        else if (County.isChecked())
        {
            // Search by County
            searchRequestWithCounty(this, state, county);
        }

        // Save history to file
        if(searchInput.length() != 0) {
            saveHistory(searchInput);
        }

        // Read search history
        setHistory();
    }

    private void searchRequestWithState(final Context context, final String state) {
        btnLoadMore.setVisibility(View.VISIBLE);

        AlertDialog alertDialog = new AlertDialog.Builder(SearchActivity.this).create();

        if (state.equals("")){
            alertDialog.setTitle("No state entered.");
            alertDialog.setMessage("Please enter a state in the search box.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface alertDialog, int which) {
                            alertDialog.dismiss();
                        }
                    });
            alertDialog.show();
            dialog.dismiss();
            return;
        }

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

        final String url = "https://bison.usgs.gov/api/search.json?state=" + stateInput + "&start=" + offset + "&count=500";
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

                            if (getCurrentFocus() != null) {
                                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                            }
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

                if (error instanceof NoConnectionError){
                    alertDialog.setTitle("No internet connection");
                    alertDialog.setMessage("Device must be connected to internet to retrieve results.");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface alertDialog, int which) {
                                    alertDialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
                else if (error instanceof TimeoutError) {
                    alertDialog.setTitle("Request timed out");
                    alertDialog.setMessage("BISON servers may be down at this time. Please retry search.");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface alertDialog, int which) {
                                    alertDialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
                else {
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
        }
        );

        // Adds request to queue which is then sent
        requestQueue.add(searchRequestWithState);
    }

    private void searchRequestWithCounty(final Context context, String state, String county) {
        btnLoadMore.setVisibility(View.VISIBLE);

        AlertDialog alertDialog = new AlertDialog.Builder(SearchActivity.this).create();

        if (state.equals("") && county.equals("")){
            alertDialog.setTitle("No county nor state entered");
            alertDialog.setMessage("Please enter a county in the first search box and a state in the second search box.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface alertDialog, int which) {
                            alertDialog.dismiss();
                        }
                    });
            alertDialog.show();
            dialog.dismiss();
            return;
        }
        else if (state.equals("")){
            alertDialog.setTitle("No state entered");
            alertDialog.setMessage("Please enter a state in the second search box.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface alertDialog, int which) {
                            alertDialog.dismiss();
                        }
                    });
            alertDialog.show();
            dialog.dismiss();
            return;
        }
        else if (county.equals("")){
            alertDialog.setTitle("No county entered");
            alertDialog.setMessage("Please enter a county in the first search box.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface alertDialog, int which) {
                            alertDialog.dismiss();
                        }
                    });
            alertDialog.show();
            dialog.dismiss();
            return;
        }

        final int position = speciesNamesArray.size();

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

        // Capitalizes first char in county
        if (county.length() > 0){
            county = county.substring(0, 1).toUpperCase() + county.substring(1);

            if (county.contains(" ") && county.length() > 2){
                String[] countyParts = county.split(" ");
                countyParts[1] = countyParts[1].substring(0,1).toUpperCase() + countyParts[1].substring(1);
                county = countyParts[0] + " " + countyParts[1];
            }
            
        }

        String countyFips = countyFinder(context, state, county);

        Log.d("searchterms", county);
        Log.d("searchterms", state);

        state = state.replaceAll(" ", "%20");

        final String url = "https://bison.usgs.gov/api/search.json?state=" + state + "&countyFips=" + countyFips + "&start=" + offset + "&count=500";
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

                            if (getCurrentFocus() != null) {
                                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                            }

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

                if (error instanceof NoConnectionError){
                    alertDialog.setTitle("No internet connection");
                    alertDialog.setMessage("Device must be connected to internet to retrieve results.");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface alertDialog, int which) {
                                    alertDialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
                else if (error instanceof TimeoutError) {
                    alertDialog.setTitle("Request timed out");
                    alertDialog.setMessage("BISON servers may be down at this time. Please retry search.");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface alertDialog, int which) {
                                    alertDialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
                else {
                    alertDialog.setTitle("Invalid County, State");
                    alertDialog.setMessage("Please input a county in the first search box and a state in the second search box.");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface alertDialog, int which) {
                                    alertDialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
            }
        }
        );

        // Adds request to queue which is then sent
        requestQueue.add(searchRequest);
    }

    private void searchRequestWithSpecies(final Context context, final String speciesName)
    {
        btnLoadMore.setVisibility(View.GONE);

        AlertDialog alertDialog = new AlertDialog.Builder(SearchActivity.this).create();

        if (speciesName.equals("")){
            alertDialog.setTitle("No species entered");
            alertDialog.setMessage("Please enter a species name in the search box.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface alertDialog, int which) {
                            alertDialog.dismiss();
                        }
                    });
            alertDialog.show();
            dialog.dismiss();
            return;
        }

        final SpeciesSearchHelper helper = new SpeciesSearchHelper();
        int length = helper.getNameLength(speciesName);

        // Just a space entered
        if (length < 1)
        {
            // Dialog -- please enter a species name
        }
        else if (length == 1)
        {
            // Throw species name to partial search
            searchPartialName(this, speciesName);
            return;
        }

        Log.i("species input", speciesName);

        // Base address for searching for a species
        String baseAddress = "https://www.itis.gov/ITISWebService/jsonservice/ITISService/searchForAnyMatch?srchKey=";
        // Url for searching
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

                // Closes the loading, please wait dialog
                dialog.dismiss();

                try
                {
                    //searchType = 4;
                    Log.i("response", response.toString());

                    // All of the possible results from the search
                    JSONArray arr = response.getJSONArray("anyMatchList");

                    // Go through each index
                    for(int i = 0; i < arr.length(); i++)
                    {
                        JSONObject results = arr.getJSONObject(i);
                        JSONArray commonNames = results.getJSONObject("commonNameList").getJSONArray("commonNames");

                        // If the resulting scientific name is not two words long, then incorrect results
                        // Could be a genus, family, or no results at all
                        String[] sciName = results.getString("sciName").split(" ");
                        if(sciName.length == 2)
                        {
                            scientificName = results.getString("sciName");
                        }

                        int j = 0;
                        while(!match && j < commonNames.length())
                        {
                            // Each species result may have multiple common names for one or many languages
                            // Go through all of the common names and find the English common name
                            JSONObject comm = commonNames.getJSONObject(j);
                            String[] commName = comm.getString("commonName").split(" ");

                            if (comm.getString("language").equalsIgnoreCase("English"))
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

                    // If no results for at least one of the names, throw an exception
                    if (commonName == null || scientificName == null)
                    {
                        throw new Exception("No species results: Null name");
                    }

                    commonName = helper.capitalizeName(commonName);
                    speciesNamesArray.add(scientificName + ", " + commonName);
                    Log.i("Names", speciesNamesArray.get(0));

                    // Throw species name in ListView and display
                    adapter = new ArrayAdapter<String>(SearchActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, speciesNamesArray);
                    speciesListView.setAdapter(adapter);
                    speciesListView.setVisibility(View.VISIBLE);

                    // Hide the keyboard
                    if (getCurrentFocus() != null) {
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    }
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
                    Log.e("SearchError", error.toString());
                    dialog.dismiss();
                    AlertDialog alertDialog = new AlertDialog.Builder(SearchActivity.this).create();

                    if (error instanceof NoConnectionError){
                        alertDialog.setTitle("No internet connection");
                        alertDialog.setMessage("Device must be connected to internet to retrieve results.");
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface alertDialog, int which) {
                                        alertDialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                    }
                    else if (error instanceof TimeoutError) {
                        alertDialog.setTitle("Request timed out");
                        alertDialog.setMessage("ITIS servers may be down at this time. Please retry search.");
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface alertDialog, int which) {
                                        alertDialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                    }
                }
            });

        requestQueue.add(searchRequest);
    }

    private void searchPartialName(final Context context, final String speciesName)
    {
        Log.i("partial", "partial search");
        final SpeciesSearchHelper helper = new SpeciesSearchHelper();
        // Base address for searching for a species/genus
        // This will grab any name that contains the text searched by user
        String baseAddress = "https://www.itis.gov/ITISWebService/jsonservice/ITISService/getITISTerms?srchKey=";

        // Ensure no trailing whitespace for web call
        final String query = baseAddress + speciesName.trim();
        Log.i("final url:", query);

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JsonObjectRequest partialSearchRequest = new JsonObjectRequest(Request.Method.GET, query, null, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                // Closes the loading, please wait dialog
                dialog.dismiss();

                String scientificName = null;
                String commonName = null;

                try
                {
                    searchType = 4;
                    Log.i("response", response.toString());

                    // Get the list
                    JSONArray arr = response.getJSONArray("itisTerms");
                    JSONArray commonNames = null;
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

                    // Throw all names in ListView and display
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(SearchActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, speciesNamesArray);
                    speciesListView.setAdapter(adapter);
                    speciesListView.setVisibility(View.VISIBLE);

                    // Hide the keyboard
                    if (getCurrentFocus() != null) {
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    }
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
                Log.e("PartialSearchError", error.toString());
                dialog.dismiss();

                AlertDialog alertDialog = new AlertDialog.Builder(SearchActivity.this).create();

                if (error instanceof NoConnectionError){
                    alertDialog.setTitle("No internet connection");
                    alertDialog.setMessage("Device must be connected to internet to retrieve results.");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface alertDialog, int which) {
                                    alertDialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
                else if (error instanceof TimeoutError) {
                    alertDialog.setTitle("Request timed out");
                    alertDialog.setMessage("ITIS servers may be down at this time. Please retry search.");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface alertDialog, int which) {
                                    alertDialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
                else if (error instanceof ParseError) {
                    alertDialog.setTitle("Cannot connect to ITIS servers at this time.");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface alertDialog, int which) {
                                    alertDialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
            }
        });

        requestQueue.add(partialSearchRequest);
    }

    // OnClick method for whatsAroundMe
    public void whatsAroundMe(View view) {
        filterAndRadioButtons.setVisibility(View.VISIBLE);
        speciesNamesArray = new ArrayList<>();
        offset = 0;
        latitude = 32.5261848;
        longitude = -92.6447334;
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

    public static String setAOIBbox(double latitude, double longitude, double mileage) {
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

    // What's Around Me webcall
    private void whatsAroundMeRequest(final Context context, final String polygon)
    {
        btnLoadMore.setVisibility(View.VISIBLE);

        final String url = "https://bison.usgs.gov/api/search.json?aoibbox=" + polygon + "&start=" + offset + "&count=500";
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

                            if (getCurrentFocus() != null) {
                                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                            }
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

                if (error instanceof NoConnectionError){
                    alertDialog.setTitle("No internet connection");
                    alertDialog.setMessage("Device must be connected to internet to retrieve results.");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface alertDialog, int which) {
                                    alertDialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
                else if (error instanceof TimeoutError) {
                    alertDialog.setTitle("Request timed out");
                    alertDialog.setMessage("BISON servers may be down at this time. Please retry search.");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface alertDialog, int which) {
                                    alertDialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
                else {
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

        String county = countyInput.getText().toString();
        String state = stateInput.getText().toString();

        if (searchType == 1) {
            searchRequestWithCounty(this, state, county);
        }
        else if (searchType == 2) {
            searchRequestWithState(this, searchInput);
        }
        else if (searchType == 3) {
            whatsAroundMeRequest(this, locationPolygon);
        }
        /*else if (searchType == 4) {
            searchRequestWithSpecies(this, searchInput);
        }*/
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

    // OnClick method for filter
    public void filter(View view) {
        dialog = ProgressDialog.show(this, "", "Loading. Please wait...", true);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                filter();
            }
        }, 1000);
    }

    private void filter() {
        int kingdom = 0;
        int location = 1;

        // Checks which button (search type) is checked
        if (Kingdom.isChecked())
        {
            if (filterEditText.getText().toString().equals("")) {
                AlertDialog alertDialog = new AlertDialog.Builder(SearchActivity.this).create();
                alertDialog.setTitle("No filter entered.");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface alertDialog, int which) {
                                alertDialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
            filterByKingdomOrLocation(kingdom);
        }
        else if (Genus.isChecked())
        {
            if (filterEditText.getText().toString().equals("")) {
                AlertDialog alertDialog = new AlertDialog.Builder(SearchActivity.this).create();
                alertDialog.setTitle("No filter entered.");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface alertDialog, int which) {
                                alertDialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
            filterByGenus();
        }
        else if (MyLocation.isChecked())
        {
            filterByKingdomOrLocation(location);
        }
    }

    private void filterByKingdomOrLocation(int filterType) {
        filteredArrayList.clear();
        String scientificName;
        String commonName;
        boolean isLastItem;
        adapter = new ArrayAdapter<String>(SearchActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, filteredArrayList);
        speciesListView.setAdapter(adapter);

        if (speciesNamesArray.size() != 0) {
            for (int i = 0; i < speciesNamesArray.size(); i++) {
                if (speciesNamesArray.get(i).contains(",")) {
                    scientificName = speciesNamesArray.get(i).substring(0, speciesNamesArray.get(i).indexOf(","));
                    commonName = speciesNamesArray.get(i).substring(speciesNamesArray.get(i).indexOf(","), speciesNamesArray.get(i).length());
                }
                else {
                    scientificName = speciesNamesArray.get(i).substring(0, speciesNamesArray.get(i).length());
                    commonName = "";
                }

                Log.d("Check location", scientificName);

                isLastItem = (i == speciesNamesArray.size() - 1);

                if (filterType == 0) {
                    kingdomRequest(this, scientificName, commonName, isLastItem);
                }
                else {
                    locationRequest(this, scientificName, commonName, isLastItem);
                }
            }
        }
        else {
            AlertDialog alertDialog = new AlertDialog.Builder(SearchActivity.this).create();
            alertDialog.setTitle("No results to filter by.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface alertDialog, int which) {
                            alertDialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
    }

    private void kingdomRequest(Context context, final String scientificName, final String commonName, final boolean lastItem){
        String formattedName = scientificName.replaceAll(" ", "%20");
        final String url = "http://eol.org/api/search/1.0.json?q=" + formattedName + "&page=1&exact=true&filter_by_string=" + filterEditText.getText().toString();
        Log.d("url", url);

        // Initialize request queue
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest kingdomRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            final JSONArray speciesArray = response.getJSONArray("results");
                            Log.d("response", speciesArray.toString());
                            if (speciesArray.length() > 0) {
                                filteredArrayList.add(scientificName + commonName);
                                Log.d("filteredArrayList", filteredArrayList.toString());
                                Log.d("response", "Add to list");
                            }
                        }
                        catch (JSONException error) {
                            Log.e("No kingdom data.", error.toString());
                        }

                        if (lastItem) {
                            Log.d("filteredArrayList", filteredArrayList.toString());
                            if (filteredArrayList.isEmpty()) {
                                dialog.dismiss();
                                AlertDialog alertDialog = new AlertDialog.Builder(SearchActivity.this).create();
                                alertDialog.setTitle("No matches found.");
                                alertDialog.setMessage("There are no species with the input name in that kingdom.");
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
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("onErrorResponseKingdom", error.toString());
                dialog.dismiss();
                AlertDialog alertDialog = new AlertDialog.Builder(SearchActivity.this).create();
                alertDialog.setTitle("Error with input/response.");
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
        kingdomRequest.setRetryPolicy(/*new DefaultRetryPolicy(50000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)*/new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {}
        });

        // Adds request to queue which is then sent
        requestQueue.add(kingdomRequest);
    }

    private void locationRequest(Context context, final String scientificName, final String commonName, final boolean lastItem){
        latitude = 32.5261848;
        longitude = -92.6447334;
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

                        if (lastItem) {
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
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("onErrorResponse", error.toString());
                dialog.dismiss();
                AlertDialog alertDialog = new AlertDialog.Builder(SearchActivity.this).create();
                alertDialog.setTitle("Error with input/response.");
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
        /*locationRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {}
        });*/

        // Adds request to queue which is then sent
        requestQueue.add(locationRequest);
    }

    private void filterByGenus() {
        String genus;
        filteredArrayList.clear();
        for (int i = 0; i < speciesNamesArray.size(); i++) {
            String speciesName = speciesNamesArray.get(i);
            Log.d("scientificName", speciesName);

            if (speciesName.contains(" ")) {
                genus = speciesName.substring(0, speciesName.indexOf(" "));
            }
            else {
                genus = speciesName;
            }

            String inputGenus = filterEditText.getText().toString().replaceAll("\\s+","");
            if (genus.equalsIgnoreCase(inputGenus)) {
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
            adapter.notifyDataSetChanged();
        }
    }

    private void openSpeciesInfoPage(int position) {
        String speciesName = speciesListView.getItemAtPosition(position).toString();
        String scientificName;
        if (speciesName.contains(",")) {
            scientificName = speciesName.substring(0, speciesName.indexOf(","));
        }
        else {
            scientificName = speciesName;
        }
        Intent intent = new Intent(SearchActivity.this, SpeciesInfoActivity.class);
        intent.putExtra(INTENT_EXTRA_SPECIES_NAME, scientificName);
        startActivity(intent);
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
    public void onBackPressed() {
        // Disables going back by manually pressing the back button
    }
}