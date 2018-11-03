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

public class RecordFragment extends Fragment {


    //Imported these libraries??
    private MediaPlayer mediaPlayer;
    private MediaRecorder recorder;
    private File outFile;

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
    private EditText descriptionEdit;

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
    private String jwt;
    private int postID;
    private int postStatus;
    private String username;
    private int userID;

    //Good vibrations
    private Vibrator mVibrator;

    public RecordFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AWSMobileClient.getInstance().initialize(getActivity(), new AWSStartupHandler() {
            @Override
            public void onComplete(AWSStartupResult awsStartupResult) {
                Log.d("YourMainActivity", "AWSMobileClient is instantiated and you are connected to AWS!");
            }
        }).execute();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myFragmentView = inflater.inflate(R.layout.fragment_record, container, false);
        //this audio format is 3rd gen partner project (supposed to be the most common
        //form for audio
        // Record to the external cache directory for visibility
        OUTPUT_FILE = getActivity().getExternalCacheDir().getAbsolutePath();
        OUTPUT_FILE += "/recorder.m4a";

        ActivityCompat.requestPermissions(getActivity(), permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        // shared pref
        sharedPreferences = myFragmentView.getContext().getSharedPreferences(
                "SharedPreferences", Context.MODE_PRIVATE);
        // get web token from shared pref
        jwt = sharedPreferences.getString("jwt", "jwt");
        username = sharedPreferences.getString("username", "user");
        userID = sharedPreferences.getInt("userID", 0);

        getActivity().setTitle(username);

        // initialize view items
        recordButton = myFragmentView.findViewById(R.id.startBtn);
        playButton = myFragmentView.findViewById(R.id.playBtn);
        stopButton = myFragmentView.findViewById(R.id.stopBtn);
        swipeArrow = myFragmentView.findViewById(R.id.swipeArrow);
        descriptionEdit = myFragmentView.findViewById(R.id.description);

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
            public void onSwipeTop(){
                new MyTask().execute();
            }
        });

        return myFragmentView;
    }

    private class MyTask extends AsyncTask<Void, Integer, String> {

        // Runs in UI before background thread is called
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // show loading panel
        }

        // This is run in a background thread
        @Override
        protected String doInBackground(Void... params) {
            HttpRequest httpRequest = new HttpRequest(getContext().getString(R.string.website_url));

            try {
                fireOffHTTPRequest(httpRequest);
            } catch (IOException e){
                Log.e("Error", e.getMessage());
            }

            return "result";
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


            Toast.makeText(getActivity(), "Published!", Toast.LENGTH_SHORT).show();


            uploadWithTransferUtility();

        }
    }

    private void fireOffHTTPRequest(HttpRequest httpRequest) throws IOException{

        try {
            String response = httpRequest.dataPost("api/post", jwt,
                    createJSON(username, descriptionEdit.getText().toString(), userID));
            Log.i("RESPONSE", response);

            JSONObject jsonObject = new JSONObject(response);

            String string = jsonObject.getString("status");
            Log.i(TAG, "status (201 is a success response): " + string);
            postStatus = Integer.valueOf(string);

            String string2 = jsonObject.getString("postID");
            Log.i(TAG, "status (201 is a success response): " + string2);
            postID = Integer.valueOf(string2);

        } catch (JSONException e){
            Log.e("ERROR", e.getMessage());
        }

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

        //Good vibrations
        //mVibrator.vibrate(5000);

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

        //Good vibrations
        //mVibrator.vibrate(5000);

        //reference the output file for recoding storage
        outFile = new File(OUTPUT_FILE);

        //if there is a file already recordered, destroy it and overrite it.
        if(outFile.exists())
            outFile.delete();

        //media objects
        recorder = new MediaRecorder();
        //We are using the mic to record it. Look at uplink for the future for incomming calls
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //3rd generation partnership project
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
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

    public void uploadWithTransferUtility() {

        // Initialize the Amazon Cognito credentials provider
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getActivity(),
                "us-east-1:f15853d2-bfd1-42b6-b0f0-25e2bb49a81b", // Identity pool ID
                Regions.US_EAST_1 // Region
        );


        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(getActivity())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(new AmazonS3Client(credentialsProvider))
                        .build();

        TransferObserver uploadObserver =
                transferUtility.upload(
                        postID + ".m4a",
                        outFile);

        // Attach a listener to the observer to get state update and progress notifications
        uploadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload.

                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int)percentDonef;

                Log.d("YourActivity", "ID:" + id + " bytesCurrent: " + bytesCurrent
                        + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                // Handle errors
            }

        });

        // If you prefer to poll for the data, instead of attaching a
        // listener, check for the state and progress in the observer.
        if (TransferState.COMPLETED == uploadObserver.getState()) {
            // Handle a completed upload.
        }

        Log.d("YourActivity", "Bytes Transferrred: " + uploadObserver.getBytesTransferred());
        Log.d("YourActivity", "Bytes Total: " + uploadObserver.getBytesTotal());
    }



    public JSONObject createJSON(String username, String description, Integer userID) throws JSONException {

        JSONObject postJSON = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        JSONObject a = new JSONObject();

        a.put("username", username);
        a.put("description",description);
        a.put("timeCreated", Long.toString(Calendar.getInstance().getTimeInMillis()/1000));
        a.put("likes",0);

        jsonArray.put(a);
        postJSON.put("Post",jsonArray);
        Log.i("Json to post", postJSON.toString());

        return postJSON;
    }

}
