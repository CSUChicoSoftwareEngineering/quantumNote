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
    List< List<Point> > pDLL;
    int yOffset;

    public InkView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        paint.setColor(Color.BLACK);
        pDLL = new LinkedList();

    }

    @Override
    public void onDraw(Canvas canvas) {
        float x0=0, y0=0;
        for(List<Point> L : pDLL){
            if (L.size() > 0) {
                x0 = (L.get(0)).x;
                y0 = (L.get(0)).y;
            }
            for(Point p : L) {
                canvas.drawLine(x0, y0, p.x, p.y, paint);
                x0 = p.x;
                y0 = p.y;
            }
        }
    }

    public void setOffset() {
        int[] v = new int[2];
        getLocationOnScreen(v);
        yOffset = v[1];
    }

    public void addPoint(float x, float y) {
        pDLL.get(pDLL.size()-1).add(new Point(x, y - yOffset, System.currentTimeMillis()));
    }

    public void addStroke(float x, float y){
        List<Point> stroke = new LinkedList();
        stroke.add( new Point( x, y-yOffset, System.currentTimeMillis() ) );
        pDLL.add(stroke);
    }

    private class Point {
        public float x, y;
        public long t;
        public Point(float x1, float y1, long t1){
            x = x1;
            y = y1;
            t = t1;
        }
    }

}

