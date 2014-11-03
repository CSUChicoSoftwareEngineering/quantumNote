package com.trihedraltutoring.quantumnote;

/**
 * Created by kyled_000 on 11/2/2014.
 */

import java.io.File;
import java.io.IOException;
import java.util.Observable;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.media.AudioManager;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.media.MediaPlayer;

public class AudioRecorder extends Observable  {

    private static String mFileName;
    private MediaRecorder mRecorder;
    private MediaPlayer   mPlayer;
    boolean isRecording = false;
    boolean isPlaying = false;
    Context context;

    public AudioRecorder(Context c){
        context = c;
        AudioManager btManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        context.registerReceiver(new btReceiver(), new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));
        Log.d("INFO", "starting bluetooth");
        btManager.startBluetoothSco();
    }

    public boolean isRecording(){
        return isRecording;
    }

    public boolean isPlaying(){
        return isPlaying;
    }

    public void pause() {
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    public void startPlaying() {
        mPlayer = new MediaPlayer();
        mFileName = "test.kyle";
        File audioFile = new File(context.getFilesDir(), mFileName);
        try {
            mPlayer.setOnCompletionListener(new donePlayingListener());
        } catch(NullPointerException e){
            Log.e("ERROR", "setOnCompletionListener() failed: " + e.getMessage());
        }

        try {
            mPlayer.setDataSource(audioFile.getAbsolutePath());
            mPlayer.prepare();
            mPlayer.start();
            isPlaying = true;
            setChanged();
            notifyObservers();
        } catch (IOException e) {
            Log.e("ERROR", "prepare() failed: " + e.getMessage());
        } catch (IllegalStateException e) {
            Log.e("ERROR", "prepare() failed: " + e.getMessage());
        }
    }

    public void stopPlaying() {
        mPlayer.release();
        isPlaying = false;
        setChanged();
        notifyObservers();
    }

    public void startRecording() {
        mRecorder = new MediaRecorder();
        mFileName = "test.kyle";
        File audioFile = new File(context.getFilesDir(), mFileName);
        mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT); // default to internal mic, unless external is present
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(audioFile.getAbsolutePath());
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e("ERROR", e.getMessage());
        }

        mRecorder.start();
        isRecording = true;
        setChanged();
        notifyObservers();
    }

    public void stopRecording() {
        mRecorder.stop();
        mRecorder.reset();
        isRecording = false;
        setChanged();
        notifyObservers();
    }

    public class btReceiver extends BroadcastReceiver{
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
            Log.d("INFO", "Audio SCO state: " + state);

            if (AudioManager.SCO_AUDIO_STATE_CONNECTED == state) {
                context.unregisterReceiver(this);
            }
        }
    }

    class donePlayingListener implements OnCompletionListener{
        public void onCompletion(MediaPlayer m){
            Log.d("INFO", "Play Completion Listener Called");
            stopPlaying();
        }
    }

}

//<uses-permission android:name="android.permission.RECORD_AUDIO"/>
