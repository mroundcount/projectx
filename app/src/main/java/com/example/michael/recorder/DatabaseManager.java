package com.example.michael.recorder;


import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;

/**
 * This file makes requests to the database.
 * Use this class to get information from the database.
 */

public class DatabaseManager {

    // reusable website base url
    private String website_url = "http://127.0.0.1:8081/";

    private static final String TAG = "DatabaseManager";

    public DatabaseManager(){
        // constructor
    }

    /*
        get Json Token
     */
    public String getToken(String use, String password) {

        String token = "";

        try {
            // initialize an HttpRequest
            HttpRequest httpRequest = new HttpRequest(website_url);

            // use method loginPost in HttpRequest, pass it endpoint apiToken
            // get response from API
            String response = httpRequest.loginPost("apiToken", use, password);

            Log.i(TAG, "getToken response: " + response);

            token = createJSON(response);

            Log.i(TAG, "jwt: " + token);

        } catch (Exception e) {
            Log.e("Exception", " " + e);
        }

        return token;
    }

    /*
        turns string response into a
            json object for future parsing
     */
    private String createJSON(String string) throws Exception{

        JSONObject obj = new JSONObject(string);

        String token = obj.getString("token");

        return token;
    }

    public String getPosts(String jwt) throws IOException{

        HttpRequest httpRequest = new HttpRequest(website_url);
        String response = httpRequest.getData("api/allPosts", jwt);

        return response;
    }

}

