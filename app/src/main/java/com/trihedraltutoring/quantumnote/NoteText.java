package com.trihedraltutoring.quantumnote;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

/**
 * Created by kyled_000 on 11/26/2014.
 */
public class NoteText extends EditText{

    public NoteText(Context c, AttributeSet attributeSet) {
        super(c, attributeSet);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        // Do fancy stuff here //

        return super.onTouchEvent(motionEvent);
    }

}
