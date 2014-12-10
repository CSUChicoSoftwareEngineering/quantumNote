package com.trihedraltutoring.quantumnote;


import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
//import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.net.URI;


public class OpenCamera extends NoteEditorActivity {
    final int CAMERA_CAPTURE = 1;
    final int PIC_CROP = 2;
    private Uri picUri;
    ImageView iv;
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);
        //make sure these are linked correctly with the IDs
        iv = (ImageView) findViewById(R.id.imageView);
        btn = (Button) findViewById(R.id.action_camera);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //use standard intent to capture an image
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //we will handle the returned data in onActivityResult
                startActivityForResult(intent, CAMERA_CAPTURE);
            }

        });

        /*iv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                iv.setImageBitmap(null);
                return true;
            }
        });*/

    }

    @Override
    protected void onActivityResult(int requestcode, int resultcode, Intent data)
    {
        if (requestcode == CAMERA_CAPTURE)
        {
            if (resultcode == RESULT_OK)
            {
                Bitmap theImage = (Bitmap) data.getExtras().get("data");
                picUri = data.getData();
                iv.setImageBitmap(theImage);
            }
            else if (requestcode == PIC_CROP)
            {
                //get the returned data
                Bundle extras = data.getExtras();
                //get the cropped bitmap
                Bitmap thePic = extras.getParcelable("data");
                //retrieve a reference to the ImageView
                ImageView picView = (ImageView)findViewById(R.id.imageView);
                //performCrop();
                //display the returned cropped image
                picView.setImageBitmap(thePic);
            }
        }
    }

    /*private void performCrop()
    {
        try {
            //call the standard crop action intent (the user device may not support it)
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            //indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            //set crop properties
            cropIntent.putExtra("crop", "true");
            //indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            //indicate output X and Y
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 256);
            //retrieve data on return
            cropIntent.putExtra("return-data", true);
            //start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, PIC_CROP);
        }
        catch(ActivityNotFoundException anfe){
            //display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }*/


    float x, y = 0.0f;
    boolean moving = false;
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction())
        {
            //detecting when pressing down on screen
            case MotionEvent.ACTION_DOWN:
                moving = true;
                break;
            //detecting x y coordinates to move the image
            case MotionEvent.ACTION_MOVE:
                if (moving)
                {
                    x = event.getRawX()-iv.getWidth()/2;
                    y = event.getRawY()-iv.getHeight();
                    iv.setX(x);
                    iv.setY(y);
                }
                break;
            //detect when finger is lifted up
            case MotionEvent.ACTION_UP:
                moving = false;
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            default:
                break;

        }
        return true;
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.open_camera, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
}
