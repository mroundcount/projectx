package com.example.michael.recorder;



import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

//roundcount add
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;


public class LoginActivity extends AppCompatActivity {

    //roundcount add
    public interface OnFragmentInteractionListener {
        public void changeFragment(int id);
    }
    private OnFragmentInteractionListener mListener;
    //end roundcount add


    // view items
    private EditText userNameEditText;
    private EditText passwordEditText;
    private EditText emailEditText;
    private Button registerButton;
    //roundcount add
    private Button createButton;

    // Classes
    private SharedPreferences sharedPreferences;
    private DatabaseManager databaseManager = new DatabaseManager();

    // Variables
    private String pass;
    private String user;
    private String token = "";
    private String loginString;
    boolean validUser = false;

    // View items
    private Activity activity = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userNameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        emailEditText = findViewById(R.id.email);
        registerButton = findViewById(R.id.register_button);

        //roundcount add
        createButton = findViewById(R.id.create_button);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // any code you write below will execute when user taps the "createButton"

            }
        });

        sharedPreferences = this.getSharedPreferences(
                "SharedPreferences", Context.MODE_PRIVATE);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Login();

            }
        });




    }
    //roundcount add
    //@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_account, container, false);
        Button btn = (Button) view.findViewById(R.id.create_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, fragment, FRAGMENT_TAG);
                ft.addToBackStack(null);
                ft.commit();
            }
        });
        return view;
    }



    //end roundcount add



    @Override
    public void onBackPressed(){
        finish();
    }

    /*
        When Login is clicked
     */
    public void Login(){

        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);

        // get username and password
        pass = passwordEditText.getText().toString();
        user = userNameEditText.getText().toString();

        Toast.makeText(this, "logging in...", Toast.LENGTH_SHORT).show();

        new MyTask().execute();
    }

    private class MyTask extends AsyncTask<Void, Integer, String> {

        // Runs in UI before background thread is called
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // show loading panel
            findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
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
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);

            // show mainActivity if user is valid
            if(validUser) {
                Intent myIntent = new Intent(activity, MainActivity.class);
                activity.startActivity(myIntent);
                finish();
            } else{
                Toast.makeText(activity, "Incorrect login information", Toast.LENGTH_LONG).show();
                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            }
        }
    }

}