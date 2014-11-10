package com.trihedraltutoring.quantumnote;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.trihedraltutoring.quantumnote.data.NoteItem;

public class NoteEditorActivity extends Activity {

    private NoteItem note;

    InkView inkView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);

        inkView = (InkView) findViewById(R.id.inkView);


        Intent intent = this.getIntent();
        note = new NoteItem();
        note.setKey(intent.getStringExtra("key"));
        note.setText(intent.getStringExtra("text"));

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

    @Override
    public void onBackPressed() {
        saveAndFinish();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("quantumNote");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.my_activity_drawer, menu);
            restoreActionBar();

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            showColorPickerDialogDemo();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showColorPickerDialogDemo() {

        int initialColor = Color.WHITE;

        ColorPickerDialog colorPickerDialog = new ColorPickerDialog(this, initialColor, new ColorPickerDialog.OnColorSelectedListener() {

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
        inkView.setColor(255, Color.red(color), Color.green(color),  Color.blue(color));
    }

}