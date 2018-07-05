package com.example.michael.recorder;
import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.amazonaws.regions.Regions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    // classes
    private SearchView searchView;
    private SharedPreferences sharedPreferences;

    // variables
    private String jwt;
    private String username;
    private String searchQuery;
    private String response;
    private String searchedUsername = "";


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            Fragment fragment = null;
            String FRAGMENT_TAG = "";

            switch (item.getItemId()) {
                case R.id.navigation_feed:
                    fragment = new FeedFragment();
                    FRAGMENT_TAG = getString(R.string.tag_feed);
                    break;
                case R.id.navigation_record:
                    fragment = new RecordFragment();
                    FRAGMENT_TAG = getString(R.string.tag_record);
                    break;
                case R.id.navigation_profile:
                    fragment = new ProfileFragment();
                    FRAGMENT_TAG = getString(R.string.tag_profile);
                    break;
            }
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, fragment, FRAGMENT_TAG);
            ft.addToBackStack(null);
            ft.commit();

            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FeedFragment(), getString(R.string.tag_feed)).commit();

        sharedPreferences = this.getSharedPreferences(
                "SharedPreferences", Context.MODE_PRIVATE);

        // get web token from shared pref
        jwt = sharedPreferences.getString("jwt", "jwt");
        username = sharedPreferences.getString("username", "user");

        searchView = findViewById(R.id.searchView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.i("Text submitted: ", query);

                searchQuery = query;

                new MyTask().execute();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.i("Text changed to: ", newText);

                return false;
            }
        });
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


    }

    private class MyTask extends AsyncTask<Void, Void, String> {

        private String databaseResult;
        // This is run in a background thread
        @Override
        protected String doInBackground(Void... params) {

            try {
                fireOffHTTPRequest(searchQuery, new HttpRequest(getString(R.string.website_url)));
            } catch (IOException e){
                Log.e("Error", e.getMessage());
            }


            return "complete";
        }
        // This runs in UI when background thread finishes
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // happens when background thread finishes
            // take user to profile page of person they just searched

            if(searchedUsername.contentEquals(searchQuery)){

                Fragment fragment = new ExploreProfileFragment();

                Bundle arguments = new Bundle();
                arguments.putString( "username" , searchedUsername);
                fragment.setArguments(arguments);

                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, fragment, getString(R.string.tag_exploreProfile));
                ft.addToBackStack(null);
                ft.commit();
            } else {
                Toast.makeText(getApplicationContext(), "Sorry, no user with that username found", Toast.LENGTH_LONG).show();
            }

        }

    }


    private void fireOffHTTPRequest(String query, HttpRequest httpRequest) throws IOException {

        try {
            response = httpRequest.dataPost("api/searchUser", jwt, createJSON(query));
            Log.i("RESPONSE", response);

            JSONObject jsonObject = new JSONObject(response);

            searchedUsername = jsonObject.getString("username");
            Log.i(TAG, "username" + username);

        } catch (JSONException e) {
            Log.e("ERROR", e.getMessage());
        }

    }

    public JSONObject createJSON(String username) throws JSONException {

        JSONObject postJSON = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        JSONObject a = new JSONObject();

        a.put("username", username);

        jsonArray.put(a);
        postJSON.put("Username", jsonArray);
        Log.i("Json to post", postJSON.toString());

        return postJSON;
    }
}

