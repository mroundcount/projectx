package com.example.michael.recorder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.amazonaws.regions.Regions;
import com.google.gson.JsonIOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        // Required empty public constructor
    }

    //View items
    private View myFragmentView;
    private ListView listView;

    // classes
    private SharedPreferences sharedPreferences;
    DatabaseManager databaseManager = new DatabaseManager();

    // variables
    private String jwt;
    private String objectForPosting;
    private String posts;
    private String username;
    private JSONObject postsObject;
    private JSONArray postsArray;
    private JSONObject currentObj;
    private List<Item> items = new ArrayList<Item>();
    private OnClickDeleteButtonListener listener;
    private OnLikeButtonListener likeListener;
    private ArrayList likedPosts = new ArrayList();
    private JSONArray likedPostsJSON;
    private boolean isPostLiked = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        AWSMobileClient.getInstance().initialize(getActivity(), new AWSStartupHandler() {
            @Override
            public void onComplete(AWSStartupResult awsStartupResult) {
                Log.d("YourMainActivity", "AWSMobileClient is instantiated and you are connected to AWS!");
            }
        }).execute();


        // Initialize the Amazon Cognito credentials provider
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getActivity(),
                "us-east-1:f15853d2-bfd1-42b6-b0f0-25e2bb49a81b", // Identity pool ID
                Regions.US_EAST_1 // Region
        );

        listener = new OnClickDeleteButtonListener() {
            @Override
            public void onBtnClick(int position) {
                Log.i("TIME TO DELETE AND ", "" + position);

                JSONObject postJSON = new JSONObject();

                try{
                    postJSON.put("postID",position);
                    Log.i("Json to post", postJSON.toString());
                } catch (JSONException e){
                    // error
                }

                try {
                    databaseManager.deletePost(jwt, postJSON);
                } catch (IOException e){

                }

                getPosts();
            }
        };

        likeListener = new OnLikeButtonListener() {
            @Override
            public void onBtnClick(int position) {
                Log.i("liked my own post lol", position + "");
                likePost(position);
            }
        };

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myFragmentView = inflater.inflate(R.layout.fragment_profile, container, false);

        sharedPreferences = getActivity().getSharedPreferences(
                "SharedPreferences", Context.MODE_PRIVATE);

        // get web token from shared pref
        jwt = sharedPreferences.getString("jwt", "jwt");
        username = sharedPreferences.getString("username", "user");

        getActivity().setTitle("My profile");

        listView = myFragmentView.findViewById(R.id.postsList);

        getPosts();
        getLikesForUser();

        return myFragmentView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    public void getPosts(){

        items.clear();
        new MyTask().execute();

    }

    public void likePost(int postID){
        JSONObject postJSON = new JSONObject();

        try{
            postJSON.put("postID",postID);
            Log.i("Json to post", postJSON.toString());
        } catch (JSONException e){
            // error
        }

        try {
            databaseManager.likePost(jwt, postJSON);
        } catch (IOException e){

        }

    }

    private class MyTask extends AsyncTask<Void, Void, String> {

        private String databaseResult;
        // This is run in a background thread
        @Override
        protected String doInBackground(Void... params) {

            try {
                fireOffHTTPRequest(username, new HttpRequest(getString(R.string.website_url)));
            } catch (IOException e){
                Log.e("Error", e.getMessage());
            }


            return "complete";
        }
        // This runs in UI when background thread finishes
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                postsArray = new JSONArray(posts);
            } catch (JSONException e){
                Log.e("Error", e.getMessage());
            }

            // Do things like hide the progress bar or update ListView
            for(int i=0; i< postsArray.length(); i++){

                try {
                    currentObj = postsArray.getJSONObject(i);
                } catch (JSONException e){
                    Log.e("Error", e.getMessage());
                }

                Log.i("Current obj", currentObj.toString());

                try {

                    if(likedPosts.contains(currentObj.getInt("post_i_d"))){
                        isPostLiked = true;
                    } else{
                        isPostLiked = false;
                    }

                    items.add(
                            new ListItem(
                                    currentObj.getString("description"),
                                    currentObj.getInt("time_created"),
                                    currentObj.getInt("post_i_d"),
                                    currentObj.getString("username"),
                                    currentObj.getInt("likes"),
                                    getContext(),
                                    getActivity(),
                                    listener,
                                    "profile",
                                    likeListener,
                                    isPostLiked)

                    );
                } catch (JSONException e){
                    Log.e("Error", e.getMessage());
                }
            }

            // make newest exercise the first item in listview
            Collections.reverse(items);

            // set Adapter and set click listener
            TwoTextArrayAdapter adapter =
                    new TwoTextArrayAdapter(getActivity(), items);
            listView.setAdapter(adapter);

        }

    }

    private void fireOffHTTPRequest(String query, HttpRequest httpRequest) throws IOException {

        try {
            posts = httpRequest.dataPost("api/getPostsForUser", jwt, createJSON(query));
            Log.i("RESPONSE", posts);

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

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void getLikesForUser(){
        Log.i("getting likes", "gjlakdj");

        JSONObject likeObj;
        try {
            try {
                likedPostsJSON = new JSONArray(databaseManager.getLikesForUser(jwt));
                for(int i=0; i < likedPostsJSON.length(); i++) {
                    try {
                        likedPosts.add(likedPostsJSON.getJSONObject(i).getInt("post_i_d"));
                    } catch (JSONException e) {
                        Log.e("Error", e.getMessage());
                    }
                }
            } catch (JSONException e){
                Log.e("Error", e.getMessage());
            }
        } catch (IOException e){ }




        Log.i("array to string:", likedPosts.toString());
        sharedPreferences.edit().putString("likedPosts", likedPosts.toString()).apply();
    }


}
