package com.trihedraltutoring.quantumnote;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.Toast;
import android.widget.TextView;
import android.graphics.Color;
import android.widget.Toast;

import java.io.File;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;


public class MyActivityDrawer extends Activity implements Observer,
        NavigationDrawerFragment.NavigationDrawerCallbacks {
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    InkView inkView;
    AudioRecorder audio;
    int playbackIndex = 0;
    private Button pieControl;
    ImageView iv;
    Handler playbackHandler;
    List<Sound> sounds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_activity_drawer);
        audio = new AudioRecorder(this);
        audio.addObserver(this);
        sounds = new LinkedList();

        iv = (ImageView) findViewById(R.id.imageView1);
        Button b = (Button) findViewById(R.id.camera);
        b.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 0);
            }
        });


        pieControl = (Button) findViewById(R.id.pieControlbtn);


        pieControl.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                PopupMenu popup = new PopupMenu(MyActivityDrawer.this, pieControl);
                popup.getMenuInflater().inflate(R.menu.pie_control, popup.getMenu());


                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        Toast.makeText(
                                MyActivityDrawer.this,
                                "You Clicked : " + item.getTitle(),
                                Toast.LENGTH_SHORT
                        ).show();
                        return true;
                    }
                });
                popup.show();
                return false;
        }
        });


        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        inkView = (InkView) findViewById(R.id.inkView); // get inkView defined in xml


        // Create onGlobalLayout to be called after inkView is drawn ///
        ViewTreeObserver vto = inkView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                inkView.setOffset();
                inkView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
           audio.releaseAll();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bm = (Bitmap) data.getExtras().get("data");
        iv.setImageBitmap(bm);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.my_activity_drawer, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements View.OnClickListener {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_my_activity_drawer, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MyActivityDrawer) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }

        @Override
        public void onClick(View view) {

        }
    }

    public void pieClicked(View v){

    }

    public void playAll(){
        Log.d("INFO", "Playing new audio file");
        if (playbackIndex < sounds.size()) {
            audio.startPlaying(playbackIndex + ".mp3");
            inkView.trace(sounds.get(playbackIndex).startTime,
                    sounds.get(playbackIndex).endTime); // start and length of highlighted section
            playbackIndex++;
        }
        else
            playbackIndex = 0;
    }

    @Override
    /**
     * Called by AudioRecorder Whenever state variables change.
     */
    public void update(Observable observable, Object data) {
        Button playB = (Button) findViewById(R.id.playButton);
        Button recordB = (Button) findViewById(R.id.recButton);
        if (audio.getState() == AudioRecorder.PLAYING) playB.setText("Stop");
        else if (audio.getState() == AudioRecorder.RECORDING) recordB.setText("Stop");
        else {
            playB.setText("Play");
            recordB.setText("Rec");
            if (audio.getPrevState() == AudioRecorder.PLAYING){
                playAll(); // play next audio file
            }
        }
    }

    public void recClicked(View v){
        if (audio.getState() == AudioRecorder.RECORDING){
            sounds.get(sounds.size()-1).endTime = System.currentTimeMillis();
            audio.stopRecording();
        }
        else if (audio.getState() == AudioRecorder.STOPPED){
            audio.startRecording(sounds.size() + ".mp3");
            sounds.add(new Sound(System.currentTimeMillis()));
        }
    }

    /**
     *
     * Plays back all audio files associated with note while highlighting text
     */
    public void playClicked(View v){
        if (audio.getState() == AudioRecorder.PLAYING){
            audio.stopPlaying();
        }
        else if (audio.getState() == AudioRecorder.STOPPED){
            playAll();
        }
    }

    @Override
    /**
     * Called for all touch events
     */
    public boolean dispatchTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();
        if (findViewById(R.id.navigation_drawer).getVisibility() != View.VISIBLE) {
        //if (!mNavigationDrawerFragment.isDrawerOpen()){
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                Log.d("INFO", "adding stroke");
                inkView.addStroke(x, y);
            }
            else if (e.getAction() == MotionEvent.ACTION_MOVE) {
                Log.d("INFO", "adding point");
                inkView.addPoint(x, y);
                inkView.invalidate();
            }
            /**
            else if (e.getAction() == MotionEvent.ACTION_UP) {

                LayoutInflater inflater = (LayoutInflater)
                        this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                PopupWindow pw = new PopupWindow(
                        inflater.inflate(R.layout.pie, null, false),
                        500,
                        500,
                        true);
                // The code below assumes that the root container has an id called 'main'
                pw.showAtLocation(this.findViewById(R.id.inkView), Gravity.CENTER, 0, 0);
            }
             **/

        }
        //Log.d("INFO", "getVisibility: " + findViewById(R.id.navigation_drawer).getVisibility());
        return super.dispatchTouchEvent(e); // returns whether event was handled
    }

    private class Sound{
        public String filePath;
        public long startTime;
        public long endTime;
        public Sound(long t0){
            startTime = t0;
        }
    }
}
