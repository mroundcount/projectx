package com.example.michael.recorder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.amazonaws.regions.Regions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.ContentValues.TAG;

public class ExploreProfileFragment extends Fragment {

    public ExploreProfileFragment() {
        // Required empty public constructor
    }

    //View items
    private View myFragmentView;
    private ListView listView;
    private TextView profileName;
    private Button followButton;

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
    private String response;
    private Integer id;
    private Integer loggedInUserID;
    private OnClickDeleteButtonListener listener;

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

        Bundle arguments = getArguments();
        username = arguments.getString("username");
        id = arguments.getInt("ID");

        listener = new OnClickDeleteButtonListener() {
            @Override
            public void onBtnClick(int position) {
                getPosts();
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myFragmentView = inflater.inflate(R.layout.fragment_explore_profile, container, false);

        sharedPreferences = getActivity().getSharedPreferences(
                "SharedPreferences", Context.MODE_PRIVATE);

        // get web token from shared pref
        jwt = sharedPreferences.getString("jwt", "jwt");
        loggedInUserID = sharedPreferences.getInt("userID", 0);

        getActivity().setTitle(username +"'s profile");

        listView = myFragmentView.findViewById(R.id.postsList);
        profileName = myFragmentView.findViewById(R.id.profileName);
        followButton = myFragmentView.findViewById(R.id.followButton);
        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("button", "i want to follow");

                try {
                    fireOffFollowRequest(new HttpRequest(getString(R.string.website_url)));
                } catch (IOException e){
                    Log.e("Error", e.getMessage());
                }
            }
        });

        profileName.setText(username + "'s profile");

        getPosts();

        return myFragmentView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    public void getPosts(){

        new MyTask().execute();

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
                    items.add(
                            new ListItem(
                                    currentObj.getString("description"),
                                    currentObj.getInt("time_created"),
                                    currentObj.getInt("post_i_d"),
                                    currentObj.getString("username"), getContext(), getActivity(), listener, "explore")
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

    private void fireOffFollowRequest(HttpRequest httpRequest) throws IOException {

        try {
            posts = httpRequest.dataPost("api/addFollower", jwt, createFollowJSON(loggedInUserID,id));
            Log.i("RESPONSE", posts);

            followButton.setText("Following!");

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

    public JSONObject createFollowJSON(Integer followerID, Integer followingID) throws JSONException {

        JSONObject postJSON = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        JSONObject a = new JSONObject();

        a.put("followerID", followerID);
        a.put("followingID", followingID);

        jsonArray.put(a);
        postJSON.put("Follower", jsonArray);
        Log.i("Json to post", postJSON.toString());

        return postJSON;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}

