package com.example.mp3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ListView;
import android.database.Cursor;
import android.provider.MediaStore;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    //Referenced from: "https://www.youtube.com/watch?v=1D1Jo1sLBMo"
    //Initializing variable
    public TextView currentTime, totalTime;
    public SeekBar timeBar;
    @SuppressLint("StaticFieldLeak")
    public static ImageView pausePlay;
    public ImageView stopSong;
    @SuppressLint("StaticFieldLeak")
    private static MainActivity instance;

    public MP3Player music = new MP3Player();

    //To obtain service data from main.
    public static MainActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;

        //Assign variable for currentTime, totalTime, seekBar, pausePlay button and stopSong button.
        currentTime = findViewById(R.id.current_time);
        totalTime = findViewById(R.id.total_time);
        timeBar = findViewById(R.id.seekBar);
        pausePlay = findViewById(R.id.pause_play);
        stopSong = findViewById(R.id.stop_song);

        //Assign Handler to allow send and process every Message and Runnable objects which are associated with a thread's MessageQueue.
        Handler mHandler = new Handler();

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) final ListView lv = findViewById(R.id.listView);
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                MediaStore.Audio.Media.IS_MUSIC + "!= 0",
                null,
                null);
        lv.setAdapter(new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1,
                cursor,
                new String[]{MediaStore.Audio.Media.DATA},
                new int[]{android.R.id.text1}));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {
                Cursor c = (Cursor) lv.getItemAtPosition(myItemInt);
                @SuppressLint("Range")
                String uri = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATA));
                Log.d("comp3018", uri);

                //Initializing an intent to start service from ServicePlayer class.
                Intent service = new Intent(MainActivity.this, ServicePlayer.class);
                service.putExtra("song", uri);
                startService(service);

                //Referenced from: Lab 4- Simple Services
                //Create a channel for notification if the device is running the app that has Android SDK 26 or up
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel("MP3 Notification", "MP3 Notification", NotificationManager.IMPORTANCE_DEFAULT);
                    NotificationManager manager = getSystemService(NotificationManager.class);
                    manager.createNotificationChannel(channel);
                }

                //Referenced from: "https://www.youtube.com/watch?v=4BuRMScaaI4"
                //To allow easier control over all the flags.
                NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, "MP3 Notification");
                //Set title for notification.
                builder.setContentTitle("My MP3");
                //Set message send to the user in the notification.
                builder.setContentText(uri + " is playing");
                //Set icon for the notification.
                builder.setSmallIcon(R.drawable.ic_baseline_library_music_24);

                //Allows user to swipe the notification.
                builder.setAutoCancel(true);
                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(MainActivity.this);
                //Will notify the user with the help of ID.
                notificationManagerCompat.notify(1, builder.build());

                //To execute the specified action on the UI thread.
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //If mediaPlayer from MP3Player has some value
                        if (music.mediaPlayer != null) {
                            int currentPos = music.getProgress();
                            //Set the seekBar to the current position.
                            timeBar.setProgress(currentPos);
                            //Set the progress time of the song in the currentTime textView.
                            currentTime.setText(convertToMinSecs(currentPos + ""));
                            int total = music.getDuration();
                            //Set the max value for seekBar by using the total duration.
                            timeBar.setMax(total);
                            //Set the total time of the song in the totalTime textView.
                            totalTime.setText(convertToMinSecs(total + ""));
                        }
                        //It will update the seekbar according to the time position by grabbing the information from MP3Player.
                        mHandler.postDelayed(this, 1000);
                    }
                });
                // do something with the selected uri string...
            }
        });
    }

    @SuppressLint("DefaultLocale")
    public static String convertToMinSecs(String duration) {
        //The duration will be converted to milliseconds.
        long millis = Long.parseLong(duration);
        //To convert the duration into minutes and seconds.
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
    }

    public void sendPausePlay() {
        //Link a pause image to a button.
        pausePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //If the music is still playing
                if (music.mediaPlayer != null && music.mediaPlayer.isPlaying()) {
                    //Set the music to pause after clicking the button
                    music.pause();
                    //Set the image of button to play after clicking
                    pausePlay.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
                } else {
                    //Set the music to play after clicking the button
                    music.play();
                    //Set the image of button to pause after clicking
                    pausePlay.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
                }
            }
        });
    }

    public void sendTimeBar() {
        timeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            //Keep track the changes of value made from the seekbar
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //Set the progress of song after the user has changed the progress in the seekBar.
                if (music.mediaPlayer != null && fromUser) {
                    music.mediaPlayer.seekTo(progress);
                }
            }

            @Override
            //To track the start point
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            //To track the stop point
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void sendStopSong() {
        //Link a stop image to a button.
        stopSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Set the music to stop and reset to 0 after clicking the button.
                music.stop();
                pausePlay.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
                //Set the progress from the seekBar to 0 after clicking stop.
                int currentPos2 = music.getProgress();
                timeBar.setProgress(currentPos2);
                //Set the current time of the song in the currentTime textView.
                currentTime.setText(convertToMinSecs(currentPos2 + ""));
            }
        });
    }
}

