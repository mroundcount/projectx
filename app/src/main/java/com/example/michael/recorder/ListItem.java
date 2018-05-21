package com.example.michael.recorder;

import android.app.Activity;
import android.content.Context;
import android.media.Image;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

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

public class ListItem implements Item {

    private static final String TAG = "ListItem";
    private final String title;
    private final String description;
    private final int timeCreated;
    private final int postID;
    private final Context context;
    private final Activity activity;
    //storing the output file
    private String OUTPUT_FILE;
    private MediaPlayer mediaPlayer;


    public ListItem(String title, String description, int timeCreated, int postID, Context context, Activity activity) {
        this.title = title;
        this.description = description;
        this.timeCreated = timeCreated;
        this.postID = postID;
        this.context = context;
        this.activity = activity;

        // Record to the external cache directory for visibility
        OUTPUT_FILE = activity.getExternalCacheDir().getAbsolutePath();
        OUTPUT_FILE += "/recorder.mp3";


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

        TextView titleText = view.findViewById(R.id.titleText);
        TextView descriptionText = view.findViewById(R.id.descriptionText);
        TextView timeCreatedText = view.findViewById(R.id.timeCreatedText);
        ImageButton playButton = view.findViewById(R.id.playButton);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get postID from s3 and play it

                Log.i("POST ID", postID + " ");

                downloadWithTransferUtility();

            }
        });


        titleText.setText(title);
        descriptionText.setText(description);
        try {
            timeCreatedText.setText(getDates(timeCreated));
        } catch (ParseException e){
            Log.e("Error", e.getMessage());
        }

        return view;
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
                        "s3Folder/" + postID + ".mp3",
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
        mediaPlayer = new MediaPlayer();
        //Have to add a throw declaration
        mediaPlayer.setDataSource(OUTPUT_FILE);
        try {
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            Log.e("ListItemTagLog", "prepare() failed");
        }
    }

}