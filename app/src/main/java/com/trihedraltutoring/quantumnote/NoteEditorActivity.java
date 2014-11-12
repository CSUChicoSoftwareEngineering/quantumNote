package com.trihedraltutoring.quantumnote;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Color;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceActivity;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.capricorn.ArcMenu;
import com.capricorn.RayMenu;
import com.trihedraltutoring.quantumnote.data.NoteItem;
import com.trihedraltutoring.quantumnote.data.NotesDataSource;
import com.trihedraltutoring.quantumnote.ColorPickerDialog;
import com.trihedraltutoring.quantumnote.ColorPickerDialog.OnColorSelectedListener;
import com.trihedraltutoring.quantumnote.R;

import java.io.File;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class NoteEditorActivity extends ListActivity implements Observer,
        NavigationDrawerFragment.NavigationDrawerCallbacks {

    private static final int[] ITEM_DRAWABLES = {R.drawable.ic_launcher, R.drawable.ic_launcher, R.drawable.ic_launcher, R.drawable.ic_launcher, R.drawable.ic_launcher};
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    InkView inkView;
    private NoteItem note;
    AudioRecorder audio;
    int playbackIndex = 0;
    boolean prevNavVisible = false;
    MotionEvent prevMotionEvent;
    private Button pieControl;
    ImageView iv;
    List<Sound> sounds;
    static final int REQUEST_IMAGE_CAPTURE = 1;


    public void openCamera(View view) {
//        Intent data = new Intent();
//        data.setAction(Intent.ACTION_GET_CONTENT);
        Intent intent = new Intent(this, OpenCamera.class);
        startActivityForResult(intent, 0);

//        Intent getintent = getIntent();
//
//        if(getintent.hasExtra("byteArray")) {
//            ImageView previewThumbnail = new ImageView(this);
//            Bitmap b = BitmapFactory.decodeByteArray(
//                    getIntent().getByteArrayExtra("byteArray"), 0, getIntent().getByteArrayExtra("byteArray").length);
//            previewThumbnail.setImageBitmap(b);
//            iv = (ImageView) findViewById(R.id.imageView1);
//            iv.setImageBitmap(b);
//        }

    }
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_note_editor);

        // Initializations //
        Intent intent = this.getIntent();
        note = new NoteItem();
        note.setKey(intent.getStringExtra("key"));
        note.setText(intent.getStringExtra("text"));
        audio = new AudioRecorder(this);
        audio.addObserver((Observer) this);
        sounds = new LinkedList();
        prevMotionEvent = MotionEvent.obtain(0,0,MotionEvent.ACTION_UP,0,0,0);
        inkView = (InkView) findViewById(R.id.inkView); // get inkView defined in xml
        ArcMenu arcMenu = (ArcMenu) findViewById(R.id.arc_menu);
        initArcMenu(arcMenu, ITEM_DRAWABLES);
        RayMenu rayMenu = (RayMenu) findViewById(R.id.ray_menu);

        EditText et = (EditText) findViewById(R.id.noteText);
        if(note.getText().length() == 0)
        {
            et.setText("New Note");
            et.setSelection(et.length());
        }
        else {
            et.setText(note.getText());
            et.setSelection(note.getText().length());
        }

        final int itemCount = ITEM_DRAWABLES.length;
        for(int i = 0; i < itemCount; i++) {
            ImageView item = new ImageView(this);
            item.setImageResource(ITEM_DRAWABLES[i]);

            final int position = i;
            rayMenu.addItem(item, new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (position == 0) {
                        Toast.makeText(NoteEditorActivity.this, "Back",
                                Toast.LENGTH_SHORT).show();
                    } else if (position == 1) {
                        Toast.makeText(NoteEditorActivity.this, "Forward",
                                Toast.LENGTH_SHORT).show();
                    } else if (position == 2) {
                        //Toast.makeText(NoteEditorActivity.this, "Camera",
                                //Toast.LENGTH_SHORT).show();
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                        }
                    } else if (position == 3) {
                        Toast.makeText(NoteEditorActivity.this, "Record",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(NoteEditorActivity.this, "Save",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        Log.d("DATA", "4!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        Log.d("DATA", "5!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));


        Log.d("DATA", "6!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        // Create onGlobalLayout to be called after inkView is drawn ///
        ViewTreeObserver vto = inkView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                inkView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    @Override
    public void onBackPressed() {
        saveAndFinish();
    }

    private void saveAndFinish() {
        EditText et = (EditText) findViewById(R.id.noteText);
        String noteText = et.getText().toString();

        Intent intent = new Intent();
        intent.putExtra("key", note.getKey());
        intent.putExtra("text", noteText);
        setResult(RESULT_OK, intent);
        finish();

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
                        Toast.makeText(NoteEditorActivity.this, "Back",
                                Toast.LENGTH_SHORT).show();
                    } else if (position == 1) {
                        Toast.makeText(NoteEditorActivity.this, "Forward",
                                Toast.LENGTH_SHORT).show();
                    } else if (position == 2) {
                        Toast.makeText(NoteEditorActivity.this, "Camera",
                                Toast.LENGTH_SHORT).show();
                    } else if (position == 3) {
                        Toast.makeText(NoteEditorActivity.this, "Record",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(NoteEditorActivity.this, "Save",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {

        // Temporary solution. position vales hard-coded //
        if(inkView != null) {
            switch (position) {
                case 0:
                    inkView.setWidth(4);
                    inkView.state = InkView.DRAWING;
                    break;
                case 1:
                    inkView.setWidth(4);
                    inkView.state = InkView.DRAWING_TRI;
                    break;
                case 2:
                    inkView.setWidth(4);
                    inkView.state = InkView.DRAWING_RECT;
                    break;
                case 3:
                    inkView.setWidth(4);
                    inkView.state = InkView.DRAWING_ELLI;
                    break;
                case 4:
                    inkView.setWidth(4);
                    inkView.state = InkView.DRAWING_LINE;
                    break;
                case 5:
                    inkView.setWidth(30);
                    inkView.state = InkView.ERASING_STROKE;
            }
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

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

            getMenuInflater().inflate(R.menu.my_activity_drawer, menu);
            restoreActionBar();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            showColorPickerDialogDemo();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showColorPickerDialogDemo() {

        int initialColor = Color.WHITE;

        ColorPickerDialog colorPickerDialog = new ColorPickerDialog(this, initialColor, new OnColorSelectedListener() {

            @Override
            public void onColorSelected(int color) {
                showToast(color);
            }

        });
        colorPickerDialog.show();

    }

    private void showToast(int color) {
        String rgbString = "R: " + Color.red(color) + " B: " + Color.blue(color) + " G: " + Color.green(color);
        Toast.makeText(this, rgbString, Toast.LENGTH_SHORT).show();
        inkView.setColor(255, Color.red(color), Color.green(color), Color.blue(color));
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
            audio.startPlaying(playbackIndex + ".mp3");
            inkView.dynamicHighlight(sounds.get(playbackIndex).startTime,
                    sounds.get(playbackIndex).endTime);
            playbackIndex++;
        }
        else
            playbackIndex = 0;
    }

    @Override
    /**
     * Called by AudioRecorder whenever state variables change.
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
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_MOVE){
            if(mNavigationDrawerFragment.isVisible() && !prevNavVisible
                    && inkView.isInking) {
                inkView.deleteLastStroke();
            }
        }
        prevNavVisible = mNavigationDrawerFragment.isVisible();
        return super.dispatchTouchEvent(motionEvent); // returns whether event was handled
    }

    private class Sound {
        public String filePath;
        public long startTime;
        public long endTime;
        public Sound(long t0){
            startTime = t0;
        }
    }
}
