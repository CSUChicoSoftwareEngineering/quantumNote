package com.trihedraltutoring.quantumnote;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.util.AttributeSet;

import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import java.lang.Math;

/**
 * Main View object users interact with
 */
public class InkView extends View {
    private List<Stroke> strokes;
    private Paint currentPaint;
    private Paint highlightPaint;
    private float xOffset, yOffset;
    private long markTf;
    private Handler highLightHandler;
    private int litStroke;
    private float curvature = 2;
    public boolean isInking = false;
    public int state = 1;
    public static final int DRAWING = 1;
    public static final int DRAWING_RECT = 2;
    public static final int DRAWING_TRI = 3;
    public static final int DRAWING_ELLI = 4;
    public static final int DRAWING_LINE = 5;
    public static final int ERASING_STROKE = 6;
    public static final int SELECTING = 7;

    public InkView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        strokes = new LinkedList();
        highLightHandler = new Handler();
        // setup default paint objects //
        currentPaint = new Paint();
        currentPaint.setColor(Color.BLACK);
        currentPaint.setStyle(Paint.Style.STROKE);
        currentPaint.setStrokeWidth(4);
        highlightPaint = new Paint();
        highlightPaint.setColor(Color.RED);
        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setStrokeWidth(8);
    }

    @Override
    /**
     * Called by system to draw all shapes
     */
    public void onDraw(Canvas canvas) {
        for(Stroke s : strokes) {
            canvas.drawPath(s, s.brush);
        }
    }

    /**
     * Determine and save location of this InkView
     */
    public void setOffset() {
        int[] v = new int[2];
        getLocationOnScreen(v);
        xOffset = v[0];
        yOffset = v[1];
    }

    public void setColor(int a, int r, int g, int b) {
        currentPaint.setARGB(a, r, g, b);
    }

    public void setWidth(int w){
        currentPaint.setStrokeWidth(w);
    }

    public void setHighlightColor(int a, int r, int g, int b) {
        highlightPaint.setARGB(a, r, g, b);
    }

    public void setHighlightWidth(int w){
        highlightPaint.setStrokeWidth(w);
    }

    /**
     * Create new stroke and add it to strokes LinkedList
     * @param x x coordinate of first point in stroke
     * @param y y coordinate of first point in stroke
     */
    public void addStroke(float x, float y) {
        isInking = true;
        startStroke(x, y);
    }

    /**
     * Add point to current stroke
     * @param x x coordinate
     * @param y y coordinate
     */
    public void addPoint(float x, float y) {
        if (!isInking) return;
        Stroke s = strokes.get(strokes.size() - 1);
        if (state == DRAWING) {
            s.addPoint(x, y);
        }
        else {
            float x0 = s.points.get(0).x;
            float y0 = s.points.get(0).y;
            if (state == DRAWING_RECT) {
                s.rewind();
                s.moveTo(x0, y0);
                s.lineTo(x0, y);
                s.lineTo(x, y);
                s.lineTo(x, y0);
                s.lineTo(x0, y0);
            }
            else if (state == DRAWING_TRI) {
                s.rewind();
                s.moveTo(x0, y0);
                s.lineTo(x0, y);
                s.lineTo(x, y);
                s.lineTo(x0, y0);
            }
            else if (state == DRAWING_ELLI) {
                s.rewind();
                s.moveTo(x0, y0);
                int x1, y1, x2, y2;
                if(x0>x){ x1 = (int)x; x2 = (int)x0; }
                else    { x2 = (int)x; x1 = (int)x0; }
                if(y0>y){ y1 = (int)y; y2 = (int)y0; }
                else    { y2 = (int)y; y1 = (int)y0; }
                s.addOval(new RectF(x1, y1, x2, y2), Path.Direction.CW);
            }
            else if (state == DRAWING_LINE) {
                s.rewind();
                s.moveTo(x0, y0);
                s.lineTo(x, y);
            }
            else if (state == ERASING_STROKE) {

                Iterator<Stroke> i = strokes.iterator();
                while (i.hasNext()) {
                    Stroke str = i.next();
                    for (Point pnt : str.points) {
                        if (distance(x, y, pnt) < currentPaint.getStrokeWidth()) {
                            i.remove();
                            break;
                        }
                    }
                }
            }
        }
        invalidate(); // force re-draw
    }

    public void startStroke(float x, float y){
        Stroke s = new Stroke(x, y,
                System.currentTimeMillis(), currentPaint);
        s.points.add(new Point(x, y));
        strokes.add(s);
    }

    public void endStroke(){
        isInking = false;
    }

    /**
     * Sets time range for and begins dynamic highlighting
     * @param t0 time to start dynamic highlighting
     * @param t time to end dynamic highlighting
     */
    public void dynamicHighlight(long t0, long t){
        markTf = t;
        // locate index of first stroke in t-t0 //
        for(int i=0; i<strokes.size(); i++){
            if (strokes.get(i).time >= t0){
                litStroke = i;
                break;
            }
        }
        // Schedule 1st call to recursive highlight function //
        long dt = strokes.get(litStroke).time - t0;
        highLightHandler.postDelayed(highLight, dt);
    }

    /**
     * x,y coordinate
     */
    private class Point {
        public float x, y;
        public Point(float x1, float y1){
            x = x1;
            y = y1;
        }
    }

    /**
     * Calculate new point along line between 2 existing points
     * @param p0 first point
     * @param p1 second point
     * @param c New point will be 1/c from p0 to p1
     * @return new point between p0 and p1
     */
    private Point subPoint(Point p0, Point p1, float c) {
        return new Point(
                (int)((p1.x-p0.x)/c+p0.x),
                (int)((p1.y-p0.y)/c+p0.y)   );
    }

    private float distance(float x, float y, Point p) {
        return ( (float)Math.sqrt(
                        Math.pow(x-p.x,2)+
                        Math.pow(y-p.y,2) )
        );
    }

    public void deleteLastStroke(){
        if (strokes.size()>0) {
            strokes.remove(strokes.size() - 1);
            invalidate();
        }
    }

    /**
     * Stroke made by a user while inking
     */
    private class Stroke extends Path {
        private long time;
        private List<Point> points;
        private Paint brush;
        private Paint paintingBrush;

        public Stroke(float x, float y, long t, Paint p){
            super();
            points = new LinkedList();
            this.moveTo(x,y);
            time = t;
            brush = new Paint(p);
            paintingBrush = new Paint(p);
        }

        private void highlightMode(boolean hi){
            if (hi) brush = highlightPaint;
            else brush = paintingBrush;
        }
//
        /**
         * Add point to stroke, add new bezier every 3rd call
         * @param x x coordinate
         * @param y y coordinate
         */
        private void addPoint(float x, float y) {
            points.add(new Point(x, y));
            int size = points.size();
            if (size > 2) {
                Point sub1 = subPoint(points.get(size-2), points.get(size-1), curvature);
                Point sub2 = subPoint(points.get(size-1), points.get(size-2), curvature);
                cubicTo( points.get(size-2).x, points.get(size-2).y,
                         sub1.x, sub1.y,
                         sub2.x, sub2.y  );
                moveTo(sub2.x, sub2.y);
            }
            else {
                Point p = subPoint(points.get(1),points.get(0),curvature);
                lineTo(p.x, p.y);
                moveTo(p.x, p.y);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        Log.d("DATA", "InkView Registered touch");
            float x = motionEvent.getX();
            float y = motionEvent.getY();
            switch (motionEvent.getAction()){
                case(MotionEvent.ACTION_DOWN):
                        addStroke(x, y);
                    break;

                case(MotionEvent.ACTION_UP):
                    endStroke();
                    break;

                case(MotionEvent.ACTION_MOVE):
                    addPoint(x, y);
                    break;

            }
            //prevNavVisible = mNavigationDrawerFragment.isVisible();
        //return super.onTouchEvent(motionEvent);
        return true;
    }

    /**
     * Recursive function calls itself to highlight each stroke in sequence
     */
    private Runnable highLight = new Runnable() {
        @Override
        public void run() {
            if (litStroke > 0) {
                strokes.get(litStroke-1).highlightMode(false);
            }
            strokes.get(litStroke).highlightMode(true);
            invalidate();
            if (litStroke < strokes.size()-1 && strokes.get(litStroke+1).time < markTf) {
                long dt = strokes.get(litStroke + 1).time - strokes.get(litStroke).time;
                litStroke++;
                highLightHandler.postDelayed(highLight, dt);
            }
        }
    };

}