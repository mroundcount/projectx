package com.example.michael.recorder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.R.*;
import android.widget.Toast;

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

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class ListItem implements Item {

    private static final String TAG = "ListItem";
    private final String description;
    private final int timeCreated;
    private final int postID;
    private final String username;
    private final Context context;
    private final Activity activity;
    private final String frag;
    private ImageButton playButton;
    private ImageButton deleteButton;
    //storing the output file
    private String OUTPUT_FILE;
    private MediaPlayer mediaPlayer;
    private OnClickDeleteButtonListener listener;
    private PopupWindow popupWindow;


    public ListItem(String description, int timeCreated, int postID, String username, Context context, Activity activity, OnClickDeleteButtonListener onClickDeleteButtonListener, String frag) {
        this.description = description;
        this.timeCreated = timeCreated;
        this.username = username;
        this.postID = postID;
        this.context = context;
        this.activity = activity;
        this.listener = onClickDeleteButtonListener;
        this.frag = frag;

        // Record to the external cache directory for visibility
        OUTPUT_FILE = activity.getExternalCacheDir().getAbsolutePath();
        OUTPUT_FILE += "/recorder.m4a";

    }

    @Override
    public int getViewType() {
        return TwoTextArrayAdapter.RowType.LIST_ITEM.ordinal();
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView) {
        View view;
        if (convertView == null) {
            view = (View) inflater.inflate(R.layout.my_list_item, null);
        } else {
            view = convertView;
        }

        TextView descriptionText = view.findViewById(R.id.descriptionText);
        TextView timeCreatedText = view.findViewById(R.id.timeCreatedText);
        playButton = view.findViewById(R.id.playButton);
        playButton.setImageResource(R.drawable.play);
        TextView usernameText = view.findViewById(R.id.usernameOfPoster);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get postID from s3 and play it

                Log.i("POST ID", postID + " ");

                downloadWithTransferUtility();

                // show the popup
                showPopup(view, "Playing " + description + " by " + username);

            }
        });

        deleteButton = view.findViewById(R.id.deleteButton);
        if (frag.contains("profile")){
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("CLIKCKEJD", "" + postID);
                    // delete

                    deletePost();

                }
            });
        } else{
            deleteButton.setVisibility(View.INVISIBLE);
        }

        descriptionText.setText(description);
        usernameText.setText("By: " + username);
        try {
            timeCreatedText.setText(getDates(timeCreated));
        } catch (ParseException e){
            Log.e("Error", e.getMessage());
        }

        return view;
    }

    private void deletePost(){
        AlertDialog.Builder adb=new AlertDialog.Builder(activity);
        adb.setTitle("Delete?");
        adb.setMessage("Are you sure you want to delete this post?");
        adb.setNegativeButton("Cancel", null);
        adb.setPositiveButton("Yes", new AlertDialog.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                // run delete post

                Log.i("CLIKCKEJD", "yes");


                listener.onBtnClick(postID);

            }});
        adb.show();
    }
    /*
    Gets the date of the workout and formats it
    to MM-DD-YYYY format
     */
    public String getDates(int epochDate) throws ParseException {
        DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");
        return formatter.format(new Date(((long) epochDate)*1000));
    }

    private void downloadWithTransferUtility() {

        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(context)
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance().getCredentialsProvider()))
                        .build();


        TransferObserver downloadObserver =
                transferUtility.download(
                        postID + ".m4a",
                        new File(OUTPUT_FILE));

        // Attach a listener to the observer to get state update and progress notifications
        downloadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload.

                    try {
                        playRecording();
                    } catch (IOException e){
                        Log.e("ERROR", e.getMessage());
                    }

                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float)bytesCurrent/(float)bytesTotal) * 100;
                int percentDone = (int)percentDonef;

                Log.d("MainActivity", "   ID:" + id + "   bytesCurrent: " + bytesCurrent + "   bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                // Handle errors
            }

        });

        // If you prefer to poll for the data, instead of attaching a
        // listener, check for the state and progress in the observer.
        if (TransferState.COMPLETED == downloadObserver.getState()) {
            // Handle a completed upload.
        }

        Log.d("YourActivity", "Bytes Transferrred: " + downloadObserver.getBytesTransferred());
        Log.d("YourActivity", "Bytes Total: " + downloadObserver.getBytesTotal());
    }

    private void playRecording() throws IOException {
        //make sure no media player is  that is already running

        AudioManager manager = (AudioManager)activity.getSystemService(Context.AUDIO_SERVICE);

        if(manager.isMusicActive())
        {
            // don't play new audio, create toast

            Toast.makeText(activity, "Sorry, can't play two posts at once!", Toast.LENGTH_LONG).show();


        } else{
            mediaPlayer = new MediaPlayer();
            //Have to add a throw declaration
            mediaPlayer.setDataSource(OUTPUT_FILE);
            try {
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                Log.e("ListItemTagLog", "prepare() failed");
            }

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    updateUI();
                }
            });
        }
    }

    private void updateUI() {
        // This can be executed on back thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                // do work on UI
                Log.i("MediaPlayer: ", "done playing post");
                dismissPopup();
            }
        });
    }

    private void showPopup(View view, String title){
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                activity.getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_playback, null);

        TextView textView = popupView.findViewById(R.id.desc);
        textView.setText(title);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                mediaPlayer.stop();
                return true;
            }
        });
    }

    private void dismissPopup(){
        popupWindow.dismiss();
    }
}