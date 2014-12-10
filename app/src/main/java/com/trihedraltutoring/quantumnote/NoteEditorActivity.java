package com.trihedraltutoring.quantumnote;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceActivity;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.capricorn.ArcMenu;
import com.capricorn.RayMenu;
import com.trihedraltutoring.quantumnote.data.NoteItem;
import com.trihedraltutoring.quantumnote.data.NotesDataSource;
//import com.trihedraltutoring.quantumnote.ColorPickerDialog;
//import com.trihedraltutoring.quantumnote.ColorPickerDialog.OnColorSelectedListener;
import com.trihedraltutoring.quantumnote.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import it.gmariotti.android.example.colorpicker.internal.NsMenuAdapter;
import it.gmariotti.android.example.colorpicker.Utils;
import it.gmariotti.android.example.colorpicker.calendarstock.ColorPickerDialog;
import it.gmariotti.android.example.colorpicker.calendarstock.ColorPickerSwatch;
import it.gmariotti.android.example.colorpicker.internal.NsMenuItemModel;

public class NoteEditorActivity extends ListActivity implements Observer,
        NavigationDrawerFragment.NavigationDrawerCallbacks {

    private int mSelectedColorCal0 = 0;
    int mLastPosition;

    private static final int[] ITEM_DRAWABLES = { R.drawable.tri, R.drawable.sq, R.drawable.cir, R.drawable.line};
    private static final int[] RAY_DRAWABLES = {R.drawable.texticon, R.drawable.pencil, R.drawable.shapes, R.drawable.eraser, R.drawable.blank};

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    InkView inkView;
    OpenCamera openCam;
    NoteText noteText;
    private NoteItem note;
    AudioRecorder audio;
    int playbackIndex = 0;
    MotionEvent prevMotionEvent;
    ImageView iv;
    List<Sound> sounds;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    File noteRoot;
    File soundsDir;
    Handler dThandler;
    boolean blinkRecord = true;
    MenuItem recMenuItem;



    //public void openCamera(View view) {
//        Intent data = new Intent();
//        data.setAction(Intent.ACTION_GET_CONTENT);
        //Intent intent = new Intent(this, OpenCamera.class);
      //  startActivityForResult(intent, 0);

//        Intent getintent = getIntent();
//
//        if(getintent.hasExtra("byteArray")) {
//            ImageView previewThumbnail = new ImageView(this);
//            Bitmap b = BitmapFactory.decodeByteArray(
//                    getIntent().getByteArrayExtra("byteArray"), 0, getIntent().getByteArrayExtra("byteArray").length);
//            previewThumbnail.setImageBitmap(b);
//            iv = (ImageView) findViewById(R.id.imageView2);
//            iv.setImageBitmap(b);
//        }

    //}
    /**
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (getIntent().hasExtra("byteArray")) {
            Bitmap b = BitmapFactory.decodeByteArray(
                    getIntent().getByteArrayExtra("byteArray"), 0, getIntent().getByteArrayExtra("byteArray").length);

            iv = (ImageView) findViewById(R.id.imageView1);
            iv.setImageBitmap(b);
        }
    }
    **/
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
        noteText = (NoteText) findViewById(R.id.noteText);
        final ArcMenu arcMenu = (ArcMenu) findViewById(R.id.arc_menu);
        initArcMenu(arcMenu, ITEM_DRAWABLES);
        final RayMenu rayMenu = (RayMenu) findViewById(R.id.ray_menu);
        iv = (ImageView) findViewById(R.id.imageView2);

        // Deserialize inkView //
        inkView.requestFocus();
        inkView.deserialize(new File(noteRoot, "inkView"));
        noteText.setCursorVisible(false);
        arcMenu.setVisibility(View.GONE);

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

        noteText.setText(note.getText());
        noteText.setSelection(note.getText().length());

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
                        inkView.clearFocus();
                        noteText.setCursorVisible(true);
                        noteText.requestFocus();
                        inkView.state = inkView.INACTIVE;
                        Toast.makeText(NoteEditorActivity.this, "Text",
                                Toast.LENGTH_SHORT).show();
                        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                        ImageView currentTool = (ImageView) findViewById(R.id.control_hint);
                        currentTool.setImageResource(R.drawable.texticon);

                    } else if (position == 1) {
                        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(noteText.getWindowToken(), 0);
                        noteText.clearFocus();
                        inkView.requestFocus();
                        inkView.state = InkView.DRAWING;
                        Toast.makeText(NoteEditorActivity.this, "Ink",
                                Toast.LENGTH_SHORT).show();
                        noteText.setCursorVisible(false);
                        ImageView currentTool = (ImageView) findViewById(R.id.control_hint);
                        currentTool.setImageResource(R.drawable.pencil);

                    } else if (position == 2) {

                        Toast.makeText(NoteEditorActivity.this, "Shapes",
                                Toast.LENGTH_SHORT).show();
                        //openCam.onCreate(savedInstanceState);
                        arcMenu.setVisibility(View.VISIBLE);
                        arcMenu.performClick();
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

/** NAVDRAWER STUFF
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
**/
        // Create onGlobalLayout to be called after inkView is drawn ///
        ViewTreeObserver vto = inkView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onGlobalLayout() {
                inkView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
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
        String textStr = noteText.getText().toString();
        if (textStr.equals("")) textStr = "Untitled Note";

        // Serialize inkview //
        inkView.serialize(new File(noteRoot, "inkView"));

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

        Intent intent = new Intent();
        intent.putExtra("key", note.getKey());
        intent.putExtra("text", textStr);
        setResult(RESULT_OK, intent);

    }

    //This sets up the PieMenu
    private void initArcMenu(ArcMenu menu, int[] itemDrawables) {
        final int itemCount = itemDrawables.length;
        for (int i = 0; i < itemCount; i++) {
            ImageView item = new ImageView(this);
            item.setImageResource(itemDrawables[i]);

            final int position = i;
            menu.addItem(item, new OnClickListener() {

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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);

        actionBar.setTitle(note.getText());
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
            Intent a = new Intent(NoteEditorActivity.this, OpenCamera.class);
            startActivity(a);
        }

        return super.onOptionsItemSelected(item);
    }

    // Implement listener to get selected color value
    ColorPickerSwatch.OnColorSelectedListener colorcalendarListener = new ColorPickerSwatch.OnColorSelectedListener(){

        @Override
        public void onColorSelected(int color) {
            mSelectedColorCal0 = color;
            Log.d("DATA", "Color: " + color);
            inkView.setColor(Color.alpha(color), Color.red(color),Color.green(color),Color.blue(color));
            if (Color.alpha(color)<255){
                inkView.setWidth(40);
                inkView.setHighlightWidth(60);
                inkView.setHighlightColor(160, 255, 0, 0);
            }
            else {
                inkView.setWidth(4);
                inkView.setHighlightWidth(8);
                inkView.setHighlightColor(255, 255, 0, 0);
            }
        }
    };

    /**
     * A placeholder fragment containing a simple view.
     */
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

    public void playAll(){
        Log.d("INFO", "Playing new audio file");
        if (playbackIndex < sounds.size()) {
            File file = new File(soundsDir, playbackIndex + ".mp3");
            audio.startPlaying(file);
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
        ImageButton playB = (ImageButton) findViewById(R.id.playButton);
        Button recordB = (Button) findViewById(R.id.recButton);
        if (audio.getState() == AudioRecorder.PLAYING) playB.setImageResource(R.drawable.pause);
        else if (audio.getState() == AudioRecorder.RECORDING) recordB.setText("Stop");
        else {
            playB.setImageResource(R.drawable.play);
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
            File file = new File(soundsDir, sounds.size() + ".mp3");
            audio.startRecording(file);
            sounds.add(new Sound(System.currentTimeMillis()));
            dThandler.postDelayed(secPassed, 1); // start recursive calls
        }
    }

    public void playClicked(View v){
        if (audio.getState() == AudioRecorder.PLAYING){
            audio.stopPlaying();
            inkView.stopDynamicHighlighting();
        }
        else if (audio.getState() == AudioRecorder.STOPPED){
            playAll();
        }
    }


    /**
     * Called for all touch events //
     */
/** NAVDRAWER STUFF
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
    private static class Sound implements Serializable{
        final long serialVersionUID = 1L;
        public long startTime;
        public long endTime;
        public Sound(long t0){
            startTime = t0;
        }
    }


    ////Recursive class called once per second ////
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
                dThandler.postDelayed(secPassed, 1000);
        }
    };

}


