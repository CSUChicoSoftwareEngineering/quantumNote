package com.trihedraltutoring.quantumnote;

/**
 * Created by kyled_000 on 11/2/2014.
 */
//
import java.io.File;
import java.io.IOException;
import java.util.Observable;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;
import android.media.AudioManager;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.media.MediaPlayer;

public class AudioRecorder extends Observable  {

    public static final int RECORDING = 2;
    public static final int PLAYING = 1;
    public static final int STOPPED = 0;
    private MediaRecorder mRecorder;
    private MediaPlayer   mPlayer;
    private int prevState = 0;
    private int audioState = 0;
    Context context;

    @TargetApi(Build.VERSION_CODES.FROYO)
    public AudioRecorder(Context c){
        context = c;
        AudioManager btManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        context.registerReceiver(new btReceiver(), new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));
        Log.d("INFO", "starting bluetooth");
        btManager.startBluetoothSco();
    } //

    /**
     * Returns previous playing/recording state
     * @return 0: Stopped, 1: Playing, 2: Recording
     */
    public int getPrevState() {
        return prevState;
    }

    /**
     * Returns current playing/recording state
     * @return 0: Stopped, 1: Playing, 2: Recording
     */
    public int getState(){
        return audioState;
    }

    /**
     * Releases media resources in use
     */
    public void releaseAll() {
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    /**
     * Play audio file
     * @param audioFile File to play
     */
    public void startPlaying(File audioFile) {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setOnCompletionListener(new donePlayingListener());
        } catch(NullPointerException e){
            Log.e("ERROR", "setOnCompletionListener() failed: " + e.getMessage());
        }

        try {
            mPlayer.setDataSource(audioFile.getAbsolutePath());
            mPlayer.prepare();
            mPlayer.start();
            prevState = audioState;
            audioState = 1;
            setChanged();
            notifyObservers();
        } catch (IOException e) {
            Log.e("ERROR", "prepare() failed: " + e.getMessage());
        } catch (IllegalStateException e) {
            Log.e("ERROR", "prepare() failed: " + e.getMessage());
        }
    }

    /**
     * Stop audio playback
     */
    public void stopPlaying() {
        mPlayer.release();
        prevState = audioState;
        audioState = 0;
        setChanged();
        notifyObservers();
    }

    /**
     * Start recording audio to file. Input hierarcy: bluetooth, external mic, internal mic
     * @param audioFile File to record to
     */
    public void startRecording(File audioFile) {
        mRecorder = new MediaRecorder();
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
        prevState = audioState;
        audioState = 2;
        setChanged();
        notifyObservers();
    }

    /**
     * Stop audio recording
     */
    public void stopRecording() {
        mRecorder.stop();
        mRecorder.reset();
        prevState = audioState;
        audioState = 0;
        setChanged();
        notifyObservers();
    }

    /**
     * Broadcast Receiver for bluetooth hardware information
     */
    public class btReceiver extends BroadcastReceiver{
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
            if (AudioManager.SCO_AUDIO_STATE_CONNECTED == state) {
                context.unregisterReceiver(this);
            }
        }
    }

    /**
     * Listener called by system when audio file finishes playing.
     * Call stopPlaying function
     */
    class donePlayingListener implements OnCompletionListener{
        public void onCompletion(MediaPlayer m){
            stopPlaying();
        }
    }

}