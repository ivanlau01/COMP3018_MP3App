package com.example.mp3;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class ServicePlayer extends Service {

    //Referenced from: "https://www.youtube.com/watch?v=G9M_HEdclTg"
    //Referenced from: "https://developer.android.com/guide/components/services"
    boolean running = true;

    //Allows other component to bind with the service.
    @Override
    public IBinder onBind(Intent arg0){
        Log.e("Bind","Method");
        return null;
    }

    //Create services.
    @Override
    public void onCreate(){
        Log.d("COMP3018","service is started");
        super.onCreate();
    }

    //Referenced from: Lab 5- Remote Services.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        String song = intent.getStringExtra("song");

        //If the song in the file path and the song loaded are not the same
        if(MainActivity.getInstance().music.getFilePath() != song){
            MainActivity.pausePlay.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
            //Set music to stop and load the newest music.
            MainActivity.getInstance().music.stop();
            MainActivity.getInstance().music.load(song);
            //Allows the seekBar, pausePlay button and stopSong button to work in the service.
            MainActivity.getInstance().sendTimeBar();
            MainActivity.getInstance().sendPausePlay();
            MainActivity.getInstance().sendStopSong();
        }
        return Service.START_STICKY;
    }

    //Destroy the service.
    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d("COMP3018", "service is destroyed");
        running = false;
    }
}
