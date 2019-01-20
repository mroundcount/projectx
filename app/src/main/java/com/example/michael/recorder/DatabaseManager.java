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
    // local host for testing
//    private String website_url = "http://127.0.0.1:8081/";
    // live for aws
    private String website_url = "http://totem-env.qqkpcqqjfi.us-east-1.elasticbeanstalk.com/";

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

    public String deletePost(String jwt, JSONObject data) throws IOException{

        HttpRequest httpRequest = new HttpRequest(website_url);
        String response = httpRequest.dataPost("api/deletePost", jwt, data);

        return response;
    }

    public String likePost(String jwt, JSONObject data) throws IOException{

        HttpRequest httpRequest = new HttpRequest(website_url);
        String response = httpRequest.dataPost("api/like", jwt, data);

        return response;
    }

    public String getPostsForUser(String jwt) throws IOException{

        HttpRequest httpRequest = new HttpRequest(website_url);
        String response = httpRequest.getData("api/postsForUser", jwt);

        return response;
    }

    public String getLikesForUser(String jwt) throws IOException{

        HttpRequest httpRequest = new HttpRequest(website_url);
        String response = httpRequest.getData("api/getLikesForUser", jwt);

        return response;
    }

    public String searchUser(String jwt) throws IOException{
        HttpRequest httpRequest = new HttpRequest(website_url);
        String response = httpRequest.getData("api/searchUser", jwt);

        return response;
    }

}


