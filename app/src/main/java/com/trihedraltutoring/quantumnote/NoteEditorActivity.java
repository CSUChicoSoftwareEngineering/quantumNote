package com.trihedraltutoring.quantumnote;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import com.capricorn.ArcMenu;
import com.capricorn.RayMenu;
import com.trihedraltutoring.quantumnote.data.NoteItem;
import com.trihedraltutoring.quantumnote.data.NotesDataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import it.gmariotti.android.example.colorpicker.Utils;
import it.gmariotti.android.example.colorpicker.calendarstock.ColorPickerDialog;
import it.gmariotti.android.example.colorpicker.calendarstock.ColorPickerSwatch;

public class NoteEditorActivity extends ListActivity implements Observer,
        NavigationDrawerFragment.NavigationDrawerCallbacks {

    private int mSelectedColorCal0 = 0;
    int mLastPosition;
    private static final int[] ITEM_DRAWABLES = { R.drawable.tri,
            R.drawable.sq,
            R.drawable.cir,
            R.drawable.line};
    private static final int[] RAY_DRAWABLES = {R.drawable.texticon,
            R.drawable.pencil,
            R.drawable.shapes,
            R.drawable.eraser,
            R.drawable.blank};
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;
    InkView inkView;
    private NoteItem note;
    AudioRecorder audio;
    int playbackIndex = 0;
    MotionEvent prevMotionEvent;
    ImageView iv;
    List<Sound> sounds;
    File noteRoot;
    File soundsDir;
    Handler dThandler;
    boolean blinkRecord = true;
    MenuItem recMenuItem;
    RayMenu rayMenu;
    ArcMenu arcMenu;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);

        // Initializations //
        dThandler = new Handler();
        Intent intent = this.getIntent();
        note = new NoteItem();
        note.setKey(intent.getStringExtra("key"));
        note.setText(intent.getStringExtra("text"));
        noteRoot = new File(this.getFilesDir(), note.getKey());
        noteRoot.mkdirs();
        soundsDir = new File(noteRoot, "Sounds");
        soundsDir.mkdirs();
        audio = new AudioRecorder(this);
        audio.addObserver((Observer) this);
        sounds = new LinkedList();
        prevMotionEvent = MotionEvent.obtain(0,0,MotionEvent.ACTION_UP,0,0,0);
        inkView = (InkView) findViewById(R.id.inkView); // get inkView defined in xml
        arcMenu = (ArcMenu) findViewById(R.id.arc_menu);
        rayMenu = (RayMenu) findViewById(R.id.ray_menu);
        iv = (ImageView) findViewById(R.id.imageView2);

        initArcMenu();
        initRayMenu();

        //inkView.requestFocus();
        //inkView.setFocusableInTouchMode(true);
        //inkView.setFocusable(true);

        loadNote();
        arcMenu.setVisibility(View.GONE);

        // CURRENTLY UNUSED NAVDRAWER STUFF
        /**
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        // Create onGlobalLayout to be called after inkView is drawn ///

        ViewTreeObserver vto = inkView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onGlobalLayout() {
                inkView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        }); **/
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveNote();
    }
    @Override
    public void onBackPressed() {
        saveNote();
        finish();
    }

    private void saveNote() {
        // Stop AudioRecorder //
        if (audio.getState() == AudioRecorder.PLAYING)
            audio.stopPlaying();
        else if (audio.getState() == AudioRecorder.RECORDING)
            audio.stopRecording();

        // Serialize sounds //
        try {
            File file = new File(soundsDir, "data");
            FileOutputStream fileOut = new FileOutputStream(file);
            ObjectOutputStream stream = new ObjectOutputStream(fileOut);
            stream.writeObject(sounds);
            stream.flush();
            stream.close();
        }
        catch(IOException e) {
            Log.e("ERROR", "Error saving sounds: " + e);
        }

        // Serialize inkview //
        inkView.serialize(new File(noteRoot, "inkView"));

        // Save note text and key //
        Intent intent = new Intent();
        String textStr = inkView.getText().toString();
        if (textStr.equals("")) textStr = "Untitled Note";
        intent.putExtra("text", textStr);
        intent.putExtra("key", note.getKey());
        setResult(RESULT_OK, intent);

    }

    private void loadNote(){
        // Deserialize inkView //
        inkView.deserialize(new File(noteRoot, "inkView"));
        inkView.setCursorVisible(false);

        // Deserialize sounds //
        try {
            File file = new File(soundsDir, "data");
            FileInputStream fileIn = new FileInputStream(file.getAbsolutePath());
            ObjectInputStream stream = new ObjectInputStream(fileIn);
            try {
                sounds = (List<Sound>)stream.readObject();
            } catch (ClassNotFoundException e) {
                Log.e("ERROR", e.getMessage());
            }
            stream.close();
        }
        catch(IOException e) {
            Log.e("ERROR", "Error loading sounds " + e);
        }

        inkView.setText(note.getText());
        inkView.setSelection(note.getText().length());

    }

    // Set up the PieMenu //
    private void initArcMenu() {
        final int itemCount = ITEM_DRAWABLES.length;
        for (int i = 0; i < itemCount; i++) {
            ImageView item = new ImageView(this);
            item.setImageResource(ITEM_DRAWABLES[i]);

            final int position = i;
            arcMenu.addItem(item, new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (position == 0) {
                        Toast.makeText(NoteEditorActivity.this, "Triangle",
                                Toast.LENGTH_SHORT).show();
                        inkView.state = InkView.DRAWING_TRI;
                        ImageView currentTool = (ImageView) findViewById(R.id.control_hint);
                        currentTool.setImageResource(R.drawable.tri);

                    } else if (position == 1) {
                        Toast.makeText(NoteEditorActivity.this, "Rectangle",
                                Toast.LENGTH_SHORT).show();
                        inkView.state = InkView.DRAWING_RECT;
                        ImageView currentTool = (ImageView) findViewById(R.id.control_hint);
                        currentTool.setImageResource(R.drawable.sq);

                    } else if (position == 2) {
                        Toast.makeText(NoteEditorActivity.this, "Ellipse",
                                Toast.LENGTH_SHORT).show();
                        inkView.state = InkView.DRAWING_ELLI;
                        ImageView currentTool = (ImageView) findViewById(R.id.control_hint);
                        currentTool.setImageResource(R.drawable.cir);

                    } else if (position == 3) {
                        Toast.makeText(NoteEditorActivity.this, "Line",
                                Toast.LENGTH_SHORT).show();
                        inkView.state = InkView.DRAWING_LINE;
                        ImageView currentTool = (ImageView) findViewById(R.id.control_hint);
                        currentTool.setImageResource(R.drawable.line);

                    } else if (position == 4) {

                    }
                }
            });
        }
    }

    // Set up the RayMenu //
    private void initRayMenu() {
        final int itemCount = RAY_DRAWABLES.length;
        for(int i = 0; i < itemCount; i++) {
            ImageView item = new ImageView(this);
            item.setImageResource(RAY_DRAWABLES[i]);

            final int position = i;
            rayMenu.addItem(item, new OnClickListener() {

                @TargetApi(Build.VERSION_CODES.CUPCAKE)
                @Override
                public void onClick(View v) {
                    arcMenu.setVisibility(View.GONE);
                    if (position == 0) {
                        inkView.setCursorVisible(true);
                        inkView.state = inkView.TYPING;
                        Toast.makeText(NoteEditorActivity.this, "Text",
                                Toast.LENGTH_SHORT).show();
                        ((InputMethodManager)getSystemService(
                                Context.INPUT_METHOD_SERVICE)).toggleSoftInput(
                                InputMethodManager.SHOW_FORCED,
                                InputMethodManager.HIDE_IMPLICIT_ONLY);
                        ImageView currentTool = (ImageView) findViewById(R.id.control_hint);
                        currentTool.setImageResource(R.drawable.texticon);

                    } else if (position == 1) {
                        ((InputMethodManager)getSystemService(
                                Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                                inkView.getWindowToken(), 0);

                        inkView.state = InkView.DRAWING;
                        Toast.makeText(NoteEditorActivity.this, "Ink",
                                Toast.LENGTH_SHORT).show();

                        inkView.setCursorVisible(false);
                        ImageView currentTool = (ImageView) findViewById(R.id.control_hint);
                        currentTool.setImageResource(R.drawable.pencil);

                    } else if (position == 2) {

                        Toast.makeText(NoteEditorActivity.this, "Shapes",
                                Toast.LENGTH_SHORT).show();
                        //openCam.onCreate(savedInstanceState);
                        arcMenu.setVisibility(View.VISIBLE);
                        arcMenu.requestFocus();
                        arcMenu.performClick();
                        inkView.setCursorVisible(false);
                    } else if(position == 3) {

                        Toast.makeText(NoteEditorActivity.this, "Erase",
                                Toast.LENGTH_SHORT).show();
                        inkView.state = InkView.ERASING_STROKE;
                        ImageView currentTool = (ImageView) findViewById(R.id.control_hint);
                        currentTool.setImageResource(R.drawable.eraser);
                    }
                }
            });
        }
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);

        if(note.getText().length() == 0)
        {
            actionBar.setTitle("Untitled Note");
        }
        else{
            actionBar.setTitle(note.getText());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.activity_note_editor, menu);
            restoreActionBar();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {

            int [] mColor = Utils.ColorUtils.colorChoice(this);

            ColorPickerDialog colorcalendar = ColorPickerDialog.newInstance(
                    R.string.color_picker_default_title, mColor,
                    mSelectedColorCal0, 5,
                    Utils.isTablet(this) ? ColorPickerDialog.SIZE_LARGE
                            : ColorPickerDialog.SIZE_SMALL);

            colorcalendar.setOnColorSelectedListener(colorcalendarListener);
            colorcalendar.show(getFragmentManager(), "cal");
            return true;
        }

        if (id == R.id.action_record) {
            recMenuItem = item;
            recClicked(inkView);
        }

        if (id == R.id.action_camera) {
            Toast.makeText(NoteEditorActivity.this, "Camera",
                    Toast.LENGTH_SHORT).show();

            Intent a = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(a, 0);
            //startActivityForResult(intent, 0);

//        Intent getintent = getIntent();
//
//        if(getintent.hasExtra("byteArray")) {
//            ImageView previewThumbnail = new ImageView(this);
//            Bitmap b = BitmapFactory.decodeByteArray(
//                    getIntent().getByteArrayExtra("byteArray"), 0, getIntent().getByteArrayExtra("byteArray").length);
//            previewThumbnail.setImageBitmap(b);
//            iv = (ImageView) findViewById(R.id.imageView2);
//            iv.setImageBitmap(b);
        }

        if (id == R.id.action_gallery) {
            Intent b = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "content://media/internal/images/media"));
            startActivity(b);
        }

        if(id == R.id.action_settings2) {
            if (audio.getState() == AudioRecorder.STOPPED)
                playNext();
        }

        return super.onOptionsItemSelected(item);
    }

    // Implement listener to get selected color value //
    ColorPickerSwatch.OnColorSelectedListener colorcalendarListener = new ColorPickerSwatch.OnColorSelectedListener(){

        @Override
        public void onColorSelected(int color) {
            mSelectedColorCal0 = color;
            Log.d("DATA", "Color: " + color);
            inkView.setColor(Color.alpha(color), Color.red(color),Color.green(color),Color.blue(color));
            // Increase to highlighting width for low alpha values //
            if (Color.alpha(color)<255){
                inkView.setWidth(40);
                inkView.setHighlightWidth(60);
                inkView.setHighlightColor(160, 255, 0, 0);
            }
            // Set pen width for alpha = 255 ///
            else {
                inkView.setWidth(4);
                inkView.setHighlightWidth(8);
                inkView.setHighlightColor(255, 255, 0, 0);
            }
        }
    };

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
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
            ((NoteEditorActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }

        @Override
        public void onClick(View view) {

        }
    }

    public void playNext(){
        if (playbackIndex < sounds.size()) {
            // play current audio file //
            File file = new File(soundsDir, playbackIndex + ".mp3");
            audio.startPlaying(file);
            // highlight stokes during current audio //
            Log.d("DATA", "Attempting to start highlight for this audio file");
            inkView.startDynamicHighlighting(sounds.get(playbackIndex).startTime,
                    sounds.get(playbackIndex).endTime);
            playbackIndex++;
        }
        else
            playbackIndex = 0;
    }

    /**
     * Called by AudioRecorder whenever state variables change.
     */
    @Override
    public void update(Observable observable, Object data) {
        ImageButton stopB = (ImageButton) findViewById(R.id.stopButton);
        if (audio.getState() == AudioRecorder.PLAYING){
            stopB.setVisibility(View.VISIBLE);
        }
        else {
            stopB.setVisibility(View.GONE);
            if (audio.getPrevState() == AudioRecorder.PLAYING){
                playNext(); // play next audio file
            }
        }
    }

    public void recClicked(View v){
        if (audio.getState() == AudioRecorder.RECORDING){
            sounds.get(sounds.size()-1).endTime = System.currentTimeMillis();
            audio.stopRecording();
        }
        else if (audio.getState() == AudioRecorder.STOPPED){
            File file = new File(soundsDir, sounds.size() + ".mp3");
            audio.startRecording(file);
            sounds.add(new Sound(System.currentTimeMillis()));
            dThandler.postDelayed(secPassed, 1); // start recursive calls
        }
    }

    public void stopClicked(View view){
        if (audio.getState() == AudioRecorder.PLAYING){
            playbackIndex = sounds.size(); // skip to end of sound list
            audio.stopPlaying();
            inkView.stopDynamicHighlighting();
        }
        else if (audio.getState() == AudioRecorder.STOPPED){
            playNext();
        }
    }

    private static class Sound implements Serializable{
        final long serialVersionUID = 1L;
        public long startTime;
        public long endTime;
        public Sound(long t0){
            startTime = t0;
        }
    }

    ////Recursive class called twice per second ////
    private Runnable secPassed = new Runnable() {
        @Override
        public void run() {
            if (blinkRecord)
                recMenuItem.setIcon(R.drawable.ic_action_record_blink);
            else
                recMenuItem.setIcon(R.drawable.ic_action_record);

            blinkRecord = !blinkRecord;

            if (audio.getState() != AudioRecorder.RECORDING)
                recMenuItem.setIcon(R.drawable.ic_action_record);
            else
                dThandler.postDelayed(secPassed, 500);
        }
    };



    // CURRENTLY UNUSED NAVDRAWER STUFF
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    //@Override
    public void onNavigationDrawerItemSelected(int position) {
        switch (position) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                break;
            case 5:
                break;
            case 6:
        }
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    // CURRENTLY UNUSED NAVDRAWER STUFF
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
            case 4:
                mTitle = getString(R.string.title_section4);
                break;
            case 5:
                mTitle = getString(R.string.title_section5);
                break;
        }
    }

    // CURRENTLY UNUSED NAVDRAWER STUFF
    /**
     @TargetApi(Build.VERSION_CODES.HONEYCOMB)
     @Override
     public boolean dispatchTouchEvent(MotionEvent motionEvent) {
     if (noteText.requestFocus() && inkView.state == inkView.INACTIVE){
     noteText.dispatchTouchEvent(motionEvent);
     }
     if (motionEvent.getAction() == MotionEvent.ACTION_MOVE){
     // hack to prevent drawing when opening Nav Frame //
     if(mNavigationDrawerFragment.isVisible() && !prevNavVisible
     && inkView.penIsDown) { // not using accessor method (recommended by Android)
     inkView.deleteLastStroke();
     inkView.invalidate();
     }
     }
     prevNavVisible = mNavigationDrawerFragment.isVisible();
     return super.dispatchTouchEvent(motionEvent); // returns whether event was handled
     }
     **/



}


