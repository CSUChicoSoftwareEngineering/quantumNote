package com.trihedraltutoring.quantumnote;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.View;
import android.util.AttributeSet;

import java.util.LinkedList;
import java.util.List;

/**
 * Main View object users interact with
 */
public class InkView extends View {
    Paint paint = new Paint();
    //List< List<Point> > pDLL;
    List<Stroke> strokes;
    int yOffset;

    public InkView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        strokes = new LinkedList();
    }

    @Override
    public void onDraw(Canvas canvas) {
        for(Stroke s : strokes) {
            canvas.drawPath(s.path, paint);
            float x0 = s.point.get(0).x;
            float y0 = s.point.get(0).y;
            for(Point p : s.point) {
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
        strokes.get(strokes.size()-1).addPoint(x, y - yOffset);
    }

    public void addStroke(float x, float y) {
        strokes.add( new Stroke(x, y - yOffset, System.currentTimeMillis()) );
    }

    public void finishStroke(){

    }

    private class Point {
        public float x, y;
        public long t;
        public Point(float x1, float y1){
            x = x1;
            y = y1;
        }
    }

    private class Stroke {
        long time;
        List<Point> point;
        Path path;
        public Stroke(float x0, float y0, long t0){
            point = new LinkedList();
            path = new Path();
            path.moveTo(x0,y0);
            time = t0;
        }

        public void addPoint(float x, float y) {
            point.add(new Point(x, y));
            if (point.size() == 4){
                point.remove(0);
                // add curve to path //
                Point p1 = point.remove(0);
                Point p2 = point.remove(0);
                path.cubicTo(p1.x, p1.y, p2.x, p2.y, x, y);
                // point now contains only x,y //
                path.moveTo(x, y);

            }
        }

    }

}