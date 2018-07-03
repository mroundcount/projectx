package com.example.michael.recorder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import static android.content.ContentValues.TAG;


public class CreateAccountFragment extends Fragment {

    // View Items
    private View myFragmentView;
    private EditText usernameEdit;
    private EditText passwordEdit;
    private EditText repeatPasswordEdit;
    private EditText emailEdit;
    private Button createButton;

    // shared preferences
    private SharedPreferences sharedPreferences;
    private String jwt;
    private int userID;
    private int postStatus;

    public CreateAccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myFragmentView = inflater.inflate(R.layout.fragment_create_account, container, false);

        // initialize view items
        usernameEdit = myFragmentView.findViewById(R.id.createUsernameTxt);
        passwordEdit = myFragmentView.findViewById(R.id.createPasswordTxt);
        repeatPasswordEdit = myFragmentView.findViewById(R.id.repeatPasswordTxt);
        emailEdit = myFragmentView.findViewById(R.id.createEmailTxt);
        createButton = myFragmentView.findViewById(R.id.createAccountBtn);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(emailEdit.getText().toString().isEmpty() ||
                        usernameEdit.getText().toString().isEmpty() ||
                        passwordEdit.getText().toString().isEmpty() ||
                        repeatPasswordEdit.getText().toString().isEmpty()){
                    Toast.makeText(getActivity(), "Please fill out every field", Toast.LENGTH_LONG).show();
                } else {
                    String password1 = passwordEdit.getText().toString();
                    String password2 = repeatPasswordEdit.getText().toString();
                    if(!password1.equals(password2)){
                        Toast.makeText(getActivity(), "Passwords do not match", Toast.LENGTH_LONG).show();
                    } else{
                        Toast.makeText(getActivity(), "Creating account", Toast.LENGTH_LONG).show();

                        new MyTask().execute();
                    }
                }

            }
        });

        return myFragmentView;
    }



    private class MyTask extends AsyncTask<Void, Integer, Integer> {

        // Runs in UI before background thread is called
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // show loading panel
            myFragmentView.findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        }

        // This is run in a background thread
        @Override
        protected Integer doInBackground(Void... params) {

            try {
                fireOffHTTPRequest(new HttpRequest(getContext().getString(R.string.website_url)));
            } catch (IOException e){
                Log.e("Error", e.getMessage());
            }

            return postStatus;
        }

        // This is called from background thread but runs in UI
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            // Do things like update the progress bar
        }

        // This runs in UI when background thread finishes
        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            // hide loading panel
            myFragmentView.findViewById(R.id.loadingPanel).setVisibility(View.GONE);

            // go back to login if it works
            if(result == 201) {
                Toast.makeText(getActivity(), "Account creation successful! Please return to login", Toast.LENGTH_LONG).show();

                myFragmentView.findViewById(R.id.loadingPanel).setVisibility(View.GONE);

                Fragment fragment = new LoginFragment();
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, fragment, getString(R.string.tag_login));
                ft.addToBackStack(null);
                ft.commit();

            } else{
                Toast.makeText(getActivity(), "Create account failed", Toast.LENGTH_LONG).show();
                myFragmentView.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            }
        }
    }

    private void fireOffHTTPRequest(HttpRequest httpRequest) throws IOException {

        try {
            String response = httpRequest.registerPost("api/user",
                    createJSON(usernameEdit.getText().toString(), passwordEdit.getText().toString(),
                            emailEdit.getText().toString()));
            Log.i("RESPONSE", response);

            JSONObject jsonObject = new JSONObject(response);

            String string = jsonObject.getString("status");
            Log.i(TAG, "status (201 is a success response): " + string);
            postStatus = Integer.valueOf(string);

        } catch (JSONException e) {
            Log.e("ERROR", e.getMessage());
        }

    }

    public JSONObject createJSON(String username, String password, String email) throws JSONException {

        JSONObject postJSON = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        JSONObject a = new JSONObject();

        a.put("username", username);
        a.put("password", password);
        a.put("email", email);

        jsonArray.put(a);
        postJSON.put("User", jsonArray);
        Log.i("Json to post", postJSON.toString());

        return postJSON;
    }
}
