package com.example.michael.recorder;
import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {


    //Imported these libraries??
    private MediaPlayer mediaPlayer;
    private MediaRecorder recorder;

    // Tag for logging to terminal (only for debugging purposes)
    private static final String LOG_TAG = "AudioRecordTest";

    // Must ask for permission at runtime
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    //storing the output file
    private String OUTPUT_FILE;

    // View Items
    private Button recordButton;
    private Button playButton;

    // track if we are currently recording
    boolean currentlyRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //this audio format is 3rd gen partner project (supposed to be the most common
        //form for audio
        // Record to the external cache directory for visibility
        OUTPUT_FILE = getExternalCacheDir().getAbsolutePath();
        OUTPUT_FILE += "/recorder.mp3";

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        // initialize view items
        recordButton = (Button)findViewById(R.id.startBtn);
        playButton = (Button)findViewById(R.id.playBtn);

        // set button onclick listener
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonTapped(v);
            }
        });
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonTapped(view);
            }
        });

    }

    //button clicking method
    //should have impoted view??
    public void buttonTapped(View view){
        //fuck... watch case sencitivity not ID

        switch(view.getId()){
            case R.id.startBtn:
                if (!currentlyRecording) {
                    try {
                        beginRecording();
                        currentlyRecording = true;
                        recordButton.setText("Stop recording");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    stopRecording();
                    currentlyRecording = false;
                    recordButton.setText("Start recording");
                }
                break;
            case R.id.playBtn:
                try {
                    playRecording();
                    Log.i(LOG_TAG, "Play button pressed");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void stopPlayback() {
        if(mediaPlayer !=null)
            mediaPlayer.stop();
    }

    private void playRecording() throws IOException {
        //make sure no media player is  that is already running
        mediaPlayer = new MediaPlayer();
        //Have to add a throw declaration
        mediaPlayer.setDataSource(OUTPUT_FILE);
        try {
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    //ditchMediaPlayer method
    private void ditchMediaPlayer() {
        if(mediaPlayer !=null){
            //catch excpetions
            try {
                mediaPlayer.release();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    //stop recording method
    private void stopRecording() {
        //If the reocrder is running, stop it
        recorder.stop();
        recorder.release();
        recorder = null;

    }

    private void beginRecording() throws IOException {
        //relsease the media recorder if it already open
        ditchMediaRecorder();
        //reference the output file for recoding storage
        File outFile = new File(OUTPUT_FILE);

        //if there is a file already recordered, destroy it and overrite it.
        if(outFile.exists())
            outFile.delete();

        //media objects
        recorder = new MediaRecorder();
        //We are using the mic to record it. Look at uplink for the future for incomming calls
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //3rd generation partnership project
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        //Adaptive multiprate narrow bans, we can use Y Band or AA for later version
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(OUTPUT_FILE);
        //Add exception to throwback.
        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        recorder.start();
    }

    //method for ditching the media recorder
    private void ditchMediaRecorder() {
        //If the recorder is already create, release it
        //If we have a reocrd object, release it
        if(recorder !=null)
            recorder.release();
    }

    // Request permission to record audio at runtime
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }

}

