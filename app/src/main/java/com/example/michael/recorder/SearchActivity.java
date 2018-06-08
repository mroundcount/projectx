package com.example.michael.recorder;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

}

/*
<?php
//Check if the form has been submitted
        if (isset($_POST['searchbox'])) {

        //explode the string
        //explode for spaces
        $tosearch = explode(" ", $_POST['searchbox']);

        //since a 1 word search is likely a username....
        //this way you do not have to have the username completely correct
        if (count($tosearch) == 1) {

        //we're going to on every two chacters to get more results returned i.e. Mi ch ae l
        $tosearch = str_split($tosearch[0], 2);
        }

        $whereclause = "";
        $paramsarray = array(':username'=>'%'.$_POST['searchbox'].'%');
        for ($i = 0; $i < count($tosearch); $i++) {
        //comparing sing the LIKE operator
        $whereclause .= " OR username LIKE :u$i ";
        $paramsarray[":u$i"] = $tosearch[$i];
        }

        //The query for searching for usernames
        //We only want to return usernames here
        $users = DB::query('SELECT users.username FROM users WHERE users.username LIKE :username '.$whereclause.'', $paramsarray);
        print_r($users);

        //This will allow you to search for posts
        $whereclause = "";

        //switching the body of the post instead of the username
        $paramsarray = array(':body'=>'%'.$_POST['searchbox'].'%');

        //search by every second word of the description. This was we don't get millions of results
        for ($i = 0; $i < count($tosearch); $i++) {
        if ($i % 2) {

        //comparing sing the LIKE operator
        $whereclause .= " OR body LIKE :p$i ";
        $paramsarray[":p$i"] = $tosearch[$i];
        }
        }


        //This query will search for post descriptions
        //Will need to be modified to match table naming conventions
        $posts = DB::query('SELECT posts.body FROM posts WHERE posts.body LIKE :body '.$whereclause.'', $paramsarray);
        echo '<pre>';
        print_r($posts);
        echo '</pre>';
        }

        ?>
*/
