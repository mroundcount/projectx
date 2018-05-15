package com.example.michael.recorder;

import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Class for making HttpRequests. Different from DatabaseManager in that DatabaseManager
 * uses HttpRequests with a given URL. This class requires a URL and makes either
 * a POST or GET request to the database.
 */
public class HttpRequest {

    private String line;
    private String baseURl;
    private HttpURLConnection urlConnection = null;

    // constructor
    public HttpRequest(String url) {
        this.baseURl = url;
        Log.i("url", url);
    }

    // makes login request
    public String loginPost(String endpoint, String username, String password) throws IOException {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String header = username + ":" + password;

        URL url = new URL(this.baseURl + endpoint);
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("POST");
        String Authorization = Base64.encodeToString(header.getBytes(), 0);
        Log.i("authorization", Authorization);
        urlConnection.setRequestProperty("Authorization", "Basic " + Authorization);
        urlConnection.connect();

        Log.i("connected", "Connected");
        int responseCode = urlConnection.getResponseCode();
        BufferedReader reader;
        InputStream inputStream;
        if (200 <= responseCode && responseCode <= 299) {
            // read the input stream into a string called "line"
            inputStream = urlConnection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));

        } else {
            inputStream = urlConnection.getErrorStream();
            Log.i("error stream", "stream");
            reader = new BufferedReader(new InputStreamReader(inputStream));
            Log.i("reader", reader.toString());
        }

        StringBuilder stringBuilder = new StringBuilder();
        while ((line = reader.readLine())  != null) {
            Log.i("LINE", line);
            // since JSON, newline is not necessary. it does make debugging a lot easier
            // if you print out the whole builder
            stringBuilder.append(line + "\n");
        }

        Log.i("String builder", stringBuilder.toString());

        return stringBuilder.toString();
    }

    // makes data post request
    public String dataPost(String endpoint, String token, JSONObject data) throws IOException {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        URL url = new URL(this.baseURl + endpoint);
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Authorization", "Bearer " + token);
        urlConnection.setRequestProperty("Content", "data");
        urlConnection.getOutputStream().write(data.toString().getBytes());
        urlConnection.connect();

        int responseCode = urlConnection.getResponseCode();
        BufferedReader reader;
        InputStream inputStream;
        if (200 <= responseCode && responseCode <= 299) {
            // read the input stream into a string called "line"
            inputStream = urlConnection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));

        } else {
            inputStream = urlConnection.getErrorStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));
        }

        StringBuilder stringBuilder = new StringBuilder();
        while ((line = reader.readLine())  != null) {
            Log.i("LINE", line);

            // since JSON, newline is not necessary. it does make debugging a lot easier
            // if you print out the whole builder
            stringBuilder.append(line + "\n");
        }

        return stringBuilder.toString();
    }

    public String getData(String endpoint, String token) throws IOException {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        URL url = new URL(this.baseURl + endpoint);
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("Authorization", "Bearer " + token);
        urlConnection.connect();

        int responseCode = urlConnection.getResponseCode();
        BufferedReader reader;
        InputStream inputStream;
        if (200 <= responseCode && responseCode <= 299) {
            // read the input stream into a string called "line"
            inputStream = urlConnection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));

        } else {
            inputStream = urlConnection.getErrorStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));
        }

        StringBuilder stringBuilder = new StringBuilder();
        while ((line = reader.readLine())  != null) {
            // since JSON, newline is not necessary. it does make debugging a lot easier
            // if you print out the whole builder
            stringBuilder.append(line + "\n");
        }

        return stringBuilder.toString();
    }

}