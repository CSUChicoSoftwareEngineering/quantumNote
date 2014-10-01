package com.trihedraltutoring.quantumnote;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.util.AttributeSet;

import java.util.LinkedList;
import java.util.List;

/**
 * Main View object users interact with
 */
public class InkView extends View {
    Paint paint = new Paint();
    List<Point> pList;

    public InkView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        paint.setColor(Color.BLACK);
        pList = new LinkedList();
    }

    @Override
    public void onDraw(Canvas canvas) {
        float x0=0, y0=0;
        if (pList.size() > 0) {
            x0 = (pList.get(0)).x;
            y0 = (pList.get(0)).y;
        }
        for(Point p : pList){
            canvas.drawLine(x0, y0, p.x, p.y, paint);
            x0 = p.x;
            y0 = p.y;
        }
    }

    public void addPoint(float x, float y) {
        pList.add(new Point(x, y));
    }

    private class Point {
        public float x, y;
        public Point(float i, float j){
            x = i;
            y = j;
        }
    }

}

