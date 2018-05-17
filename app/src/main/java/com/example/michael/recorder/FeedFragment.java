package com.example.michael.recorder;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

public class FeedFragment extends Fragment {


    public FeedFragment() {
        // Required empty public constructor
    }



    //View items
    private View myFragmentView;
    private EditText titleEditText;
    private EditText descriptionEditText;
    private Button postButton;

    // classes
    private SharedPreferences sharedPreferences;

    // variables
    private String jwt;
    private String objectForPosting;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myFragmentView = inflater.inflate(R.layout.fragment_feed, container, false);

        sharedPreferences = getActivity().getSharedPreferences(
                "SharedPreferences", Context.MODE_PRIVATE);

        // get web token from shared pref
        jwt = sharedPreferences.getString("jwt", "jwt");

        descriptionEditText = myFragmentView.findViewById(R.id.descriptionEditText);
        titleEditText = myFragmentView.findViewById(R.id.titleEditText);
        postButton = myFragmentView.findViewById(R.id.postButton);

        // set button click listener
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    buttonClicked();
                } catch (IOException e){
                    Log.e("ERROR", e.getMessage());
                }
            }
        });

        return myFragmentView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    public void buttonClicked() throws IOException{

        HttpRequest httpRequest = new HttpRequest(getContext().getString(R.string.website_url));


        try {
            String response = httpRequest.dataPost("api/post", jwt,
                    createJSON("3", titleEditText.getText().toString(), descriptionEditText.getText().toString()));
            Log.i("RESPONSE", response);
        } catch (JSONException e){
            Log.e("ERROR", e.getMessage());
        }
    }


    public JSONObject createJSON(String id, String title, String description) throws JSONException{

        JSONObject postJSON = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        JSONObject a = new JSONObject();

        a.put("userID", 3);
        a.put("title", title);
        a.put("description",description);
        a.put("timeCreated", Long.toString(Calendar.getInstance().getTimeInMillis()/1000));
        a.put("likes",0);

        jsonArray.put(a);
        postJSON.put("Post",jsonArray);
        Log.i("Json to post", postJSON.toString());

        return postJSON;
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

}
