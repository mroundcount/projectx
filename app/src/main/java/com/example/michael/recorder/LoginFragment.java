package com.example.michael.recorder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class LoginFragment extends Fragment {


    public LoginFragment() {
        // Required empty public constructor
    }

    //View items
    private View myFragmentView;
    private EditText userNameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button registerButton;

    // classes
    private SharedPreferences sharedPreferences;
    private DatabaseManager databaseManager = new DatabaseManager();


    // variables
    private String pass;
    private String user;
    private String token = "";
    private String loginString;
    boolean validUser = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myFragmentView = inflater.inflate(R.layout.fragment_login, container, false);

        sharedPreferences = getActivity().getSharedPreferences(
                "SharedPreferences", Context.MODE_PRIVATE);

        userNameEditText = myFragmentView.findViewById(R.id.username);
        passwordEditText = myFragmentView.findViewById(R.id.password);
        loginButton = myFragmentView.findViewById(R.id.login_button);
        registerButton = myFragmentView.findViewById(R.id.create_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Login();

            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new CreateAccountFragment();
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, fragment, getString(R.string.tag_create));
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        return myFragmentView;
    }


    /*
       When Login is clicked
    */
    public void Login(){

        myFragmentView.findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);

        // get username and password
        pass = passwordEditText.getText().toString();
        user = userNameEditText.getText().toString();

        Toast.makeText(getActivity(), "Logging in...", Toast.LENGTH_LONG).show();

        new MyTask().execute();
    }


    private class MyTask extends AsyncTask<Void, Integer, String> {

        // Runs in UI before background thread is called
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // show loading panel
            myFragmentView.findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        }

        // This is run in a background thread
        @Override
        protected String doInBackground(Void... params) {

            // get token
            token = databaseManager.getToken(user, pass);

            return token;
        }

        // This is called from background thread but runs in UI
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            // Do things like update the progress bar
        }

        // This runs in UI when background thread finishes
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // save token in shared pref
            sharedPreferences.edit().putString("jwt", token).apply();

            // valid token means validUser
            if(!token.isEmpty()) {
                validUser = true;
            }

            // hide loading panel
            myFragmentView.findViewById(R.id.loadingPanel).setVisibility(View.GONE);

            // show mainActivity if user is valid
            if(validUser) {
                Intent myIntent = new Intent(getActivity(), MainActivity.class);
                getActivity().startActivity(myIntent);
                getActivity().finish();
            } else{
                Toast.makeText(getActivity(), "Incorrect login information", Toast.LENGTH_LONG).show();
                myFragmentView.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

}
