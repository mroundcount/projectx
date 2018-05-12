package com.example.michael.recorder;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
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
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class RecordFragment extends Fragment {


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
    private View myFragmentView;
    private Button recordButton;
    private Button playButton;
    private Button stopButton;
    private ImageView swipeArrow;

    // track if we are currently recording
    boolean currentlyRecording = false;
    boolean recordingPaused = false;

    // timer
    public CountDownTimer countDownTimer;
    public CountDownTimer resumedCountDownTimer;
    private long millisInFuture = 60000;
    private long newMillisInFuture = 0;

    // shared preferences
    private SharedPreferences sharedPreferences;

    public RecordFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myFragmentView = inflater.inflate(R.layout.fragment_record, container, false);
        //this audio format is 3rd gen partner project (supposed to be the most common
        //form for audio
        // Record to the external cache directory for visibility
        OUTPUT_FILE = getActivity().getExternalCacheDir().getAbsolutePath();
        OUTPUT_FILE += "/recorder.mp3";

        ActivityCompat.requestPermissions(getActivity(), permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        // shared pref
        sharedPreferences = myFragmentView.getContext().getSharedPreferences(
                "SharedPreferences", Context.MODE_PRIVATE);

        // initialize view items
        recordButton = myFragmentView.findViewById(R.id.startBtn);
        playButton = myFragmentView.findViewById(R.id.playBtn);
        stopButton = myFragmentView.findViewById(R.id.stopBtn);
        swipeArrow = myFragmentView.findViewById(R.id.swipeArrow);

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
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonTapped(view);
            }
        });

        // set swipe arrow listener
        swipeArrow.setOnTouchListener(new OnSwipeTouchListener(getActivity()) {
            public void onSwipeTop() {
                Toast.makeText(getActivity(), "YOU JUST PUBLISHED IT", Toast.LENGTH_SHORT).show();
            }
        });

        return myFragmentView;
    }

    //button clicking method
    //should have impoted view??
    public void buttonTapped(View view){
        //fuck... watch case sencitivity not ID

        switch(view.getId()){
            case R.id.startBtn:
                if (!currentlyRecording) {
                    if(recordingPaused){
                        resumeRecording();
                    }else {
                        try {
                            beginRecording();
                            currentlyRecording = true;
                            stopButton.setVisibility(View.VISIBLE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    pauseRecording();
                    currentlyRecording = false;
                    recordButton.setText("Continue recording");
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
            case R.id.stopBtn:
                try {
                    stopRecording();
                    Log.i(LOG_TAG, "Stop button pressed");
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
        currentlyRecording = false;
        countDownTimer.cancel();
        if(resumedCountDownTimer != null) {
            resumedCountDownTimer.cancel();
        }
        playButton.setVisibility(View.VISIBLE);
        recorder.stop();
        recorder.release();
        recorder = null;
        try {
            playRecording();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pauseRecording() {
        recorder.pause();
        recordingPaused = true;
        countDownTimer.cancel();
        if(resumedCountDownTimer!=null) {
            resumedCountDownTimer.cancel();
        }
    }

    private void resumeRecording() {
        currentlyRecording = true;
        recorder.resume();
        setResumedCountdownTimer();
        recordButton.setText(sharedPreferences.getString("timeElapsed", "Record"));
        resumedCountDownTimer.start();
    }

    private void beginRecording() throws IOException {
        //relsease the media recorder if it already open
        ditchMediaRecorder();

        // start timer, get time first
        setCountDownTimer();
        countDownTimer.start();

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
        if (!permissionToRecordAccepted )
            getActivity().finish();

    }

    public void setCountDownTimer(){
        countDownTimer = new CountDownTimer(millisInFuture, 1000){

            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = (millisInFuture - millisUntilFinished) / 1000;
                int hours = (int) seconds / 3600;
                int remainder = (int) seconds - hours * 3600;
                int mins = remainder / 60;
                remainder = remainder - mins * 60;
                int secs = remainder;

                Log.i("millis until finished", millisUntilFinished + "");
                Log.i("-----------", "------------");
                newMillisInFuture = millisUntilFinished;


                String string = String.format("%02d:%02d", mins, secs);

                sharedPreferences.edit().putString("timeElapsed", string).apply();

                recordButton.setText(string);
            }

            @Override
            public void onFinish() {
                stopRecording();
            }
        };
    }

    public void setResumedCountdownTimer(){
        resumedCountDownTimer = new CountDownTimer(newMillisInFuture, 1000){

            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = (millisInFuture - millisUntilFinished) / 1000;
                int hours = (int) seconds / 3600;
                int remainder = (int) seconds - hours * 3600;
                int mins = remainder / 60;
                remainder = remainder - mins * 60;
                int secs = remainder;

                String string = String.format("%02d:%02d", mins, secs);

                sharedPreferences.edit().putString("timeElapsed", string).apply();

                recordButton.setText(string);
            }

            @Override
            public void onFinish() {
                stopRecording();
            }
        };
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
