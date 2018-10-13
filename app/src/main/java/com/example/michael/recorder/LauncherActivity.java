package com.example.michael.recorder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class LauncherActivity extends AppCompatActivity {


    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_launcher);

        sharedPreferences = this.getSharedPreferences(
                "SharedPreferences", Context.MODE_PRIVATE);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 2000ms
                checkForToken();
            }
        }, 2100);


    }

    private void checkForToken() {

        if((sharedPreferences.getString("jwt", " ").length() > 1)) {
            Log.i("Launcher act", "there is a token, proceed to MAIN activity");
            Intent myIntent = new Intent(this, MainActivity.class);
            this.startActivity(myIntent);
            this.finish();
        } else {
            Log.i("Launcher act", "there is NOT a token, proceed to LOGIN activity");
            Intent myIntent = new Intent(this, LoginActivity.class);
            this.startActivity(myIntent);
            this.finish();
        }
    }
}
