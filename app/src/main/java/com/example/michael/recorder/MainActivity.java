package com.example.michael.recorder;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;

import java.io.File;
import java.io.IOException;

import static com.example.michael.recorder.R.id.finishBtn;

public class MainActivity extends AppCompatActivity {


    //Imported these libraries??
    private MediaPlayer mediaPlayer;
    private MediaRecorder recorder;
    //storing the output file
    private String OUTPUT_FILE;

    // View Items
    private Button button;
    private ToggleButton toggleRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //this audio format is 3rd gen partner project (supposed to be the most common
        //form for audio
        OUTPUT_FILE = Environment.getExternalStorageDirectory()+"/recorder.mp3";

        // initialize view items
        button = (Button)findViewById(R.id.startBtn);
        toggleRecord = (ToggleButton)findViewById(R.id.toggleRecord);

        toggleRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        // set button onclick listener
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonTapped(v);
            }
        });

    }

    //button clicking method
    //should have impoted view??
    public void buttonTapped(View view){
        //fuck... watch case sencitivity not ID
        int NumberOfClick = 0;

        switch(view.getId()){
            //I chaned the ID inside of the xml. WTF?
            case R.id.startBtn:
                ++NumberOfClick;
                switch (NumberOfClick) {
                    case 1:

                        button.setText("Click Me !");

                        try {
                            beginRecording();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    break;

                    case 2:
                        try {
                            stopRecording();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    break;

                }
                break;
            case R.id.finishBtn:
                try {
                    stopRecording();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.playBtn:
                try {
                    playRecording();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.stopBtn:
                try {
                    stopPlayback();
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
        ditchMediaPlayer();
        mediaPlayer = new MediaPlayer();
        //Have to add a throw declaration
        mediaPlayer.setDataSource(OUTPUT_FILE);
        mediaPlayer.prepare();
        mediaPlayer.start();
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
        if(recorder !=null)
            recorder.stop();
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
        recorder.prepare();
        recorder.start();
    }
    //method for ditching the media recorder
    private void ditchMediaRecorder() {
        //If the recorder is already create, release it
        //If we have a reocrd object, release it
        if(recorder !=null)
            recorder.release();
    }
}

