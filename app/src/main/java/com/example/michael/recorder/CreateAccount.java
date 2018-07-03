package com.example.michael.recorder;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.os.Vibrator;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import static android.content.ContentValues.TAG;


public class CreateAccount extends Fragment {

    // View Items
    private View myFragmentView;
    private Button createButton;
    private EditText usernameEdit;
    private EditText passwordEdit;
    private EditText repeatPasswordEdit;
    private EditText emailEdit;

    // shared preferences
    private SharedPreferences sharedPreferences;
    private String jwt;
    private int userID;
    private int postStatus;

    public CreateAccount() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myFragmentView = inflater.inflate(R.layout.fragment_record, container, false);

        // initialize view items
        createButton = myFragmentView.findViewById(R.id.createAccountBtn);
        usernameEdit = myFragmentView.findViewById(R.id.createUsernameTxt);
        passwordEdit = myFragmentView.findViewById(R.id.createPasswordTxt);
        repeatPasswordEdit = myFragmentView.findViewById(R.id.repeatPasswordTxt);
        emailEdit = myFragmentView.findViewById(R.id.createEmailTxt);

        return myFragmentView;
    }


    private void fireOffHTTPRequest(HttpRequest httpRequest) throws IOException {

        try {
            String response = httpRequest.dataPost("api/user", jwt,
                    createJSON("4", usernameEdit.getText().toString(), passwordEdit.getText().toString(),
                            emailEdit.getText().toString()));
            Log.i("RESPONSE", response);

            JSONObject jsonObject = new JSONObject(response);

            String string = jsonObject.getString("status");
            Log.i(TAG, "status (201 is a success response): " + string);
            postStatus = Integer.valueOf(string);

            String string2 = jsonObject.getString("postID");
            Log.i(TAG, "status (201 is a success response): " + string2);
            userID = Integer.valueOf(string2);

        } catch (JSONException e) {
            Log.e("ERROR", e.getMessage());
        }

    }

    public JSONObject createJSON(String id, String username, String password, String email) throws JSONException {

        JSONObject postJSON = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        JSONObject a = new JSONObject();

        a.put("username", username);
        a.put("password", password);
        a.put("email", email);

        jsonArray.put(a);
        postJSON.put("Post", jsonArray);
        Log.i("Json to post", postJSON.toString());

        return postJSON;
    }
}


    /*
    public static CreateAccount newInstance(String param1, String param2) {
        CreateAccount fragment = new CreateAccount();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
