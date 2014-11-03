package com.trihedraltutoring.quantumnote;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;


public class OpenCamera extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent capture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(capture, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bm = (Bitmap) data.getExtras().get("data");

        Intent i = new Intent(this, MyActivityDrawer.class);
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 50, bs);
        i.putExtra("byteArray", bs.toByteArray());
        setResult(1);
        finish();
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
