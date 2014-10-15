package com.trihedraltutoring.quantumnote;

import android.content.Context;
import android.view.View;
import android.widget.Button;

/**
 * Created by MacBookPro on 10/5/14.
 */
public class PieControl extends View{
    public PieControl(Context context) {
        super(context);
    }
    PieControl pieControl;

    public void buttonOnLongPress(View view){
        Button button = (Button) view;
        ((Button) view).setText("Pie Control");
    }
}
