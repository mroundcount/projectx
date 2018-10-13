package com.example.michael.recorder;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        getSupportFragmentManager().beginTransaction().
                replace(R.id.fragment_container, new LoginFragment(), getString(R.string.tag_login))
                .commit();

    }

    @Override
    public void onBackPressed(){
        finish();
    }


}