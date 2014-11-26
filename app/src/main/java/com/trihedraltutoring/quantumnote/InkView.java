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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
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
    private int litStroke;
    private Handler highLightHandler;
    public boolean isInking = false; // public for frequent external access (recommended by Android)
    private boolean dynamicHighlighting = false;
    public int state = 1;
    transient Context context;
    private static final int STROKE_ATTRIBUTES = 3; // for serialization extensibility
    public static final int INACTIVE = 0;
    public static final int DRAWING = 1;
    public static final int DRAWING_RECT = 2;
    public static final int DRAWING_TRI = 3;
    public static final int DRAWING_ELLI = 4;
    public static final int DRAWING_LINE = 5;
    public static final int ERASING_STROKE = 6;
    public static final int SELECTING = 7;

    public InkView(Context c, AttributeSet attributeSet) {
        super(c, attributeSet);
        context = c;
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

    /**
     * Called by system to draw all shapes
     */
    @Override
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
    private void startStroke(float x, float y, long t){
        isInking = true;
        Stroke s = new Stroke(x, y, t, currentPaint, highlightPaint);
        s.type = state;
        strokes.add(s);
    }

    private void endStroke(float x, float y){
        if (state == DRAWING)
            strokes.get( strokes.size()-1 ).addPoint(x, y);
        else
            drawShape(x, y);
            saveShape(x, y);
        isInking = false;
    }

    /**
     * Add point to current stroke
     * @param x x coordinate
     * @param y y coordinate
     */
    private void addPoint(float x, float y) {
        if (!isInking) return;
        Stroke s = strokes.get( strokes.size()-1 );
        if (state == DRAWING) {
            s.addPoint(x, y);
        }
        else drawShape(x, y);
        invalidate(); // force re-draw
    }

    private void drawShape(float x, float y){
        Stroke s = strokes.get( strokes.size()-1 );
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

    private void saveShape(float x, float y){
        if (!isInking) return;
        // for most recent stroke //
        Stroke s = strokes.get( strokes.size()-1 );
        // set final point //
        s.points.add( new Point(x, y) );
    }

    /**
     * Sets time range for and begins dynamic highlighting
     * @param t0 time to start dynamic highlighting
     * @param t time to end dynamic highlighting
     */
    public void startDynamicHighlighting(long t0, long t){
        dynamicHighlighting = true;
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

    public void stopDynamicHighlighting(){
        dynamicHighlighting = false;
    }

    /**
     * x,y coordinate floats
     */
    private static class Point {
        public float x, y;
        public Point(float x1, float y1){
            x = x1;
            y = y1;
        }
    }

    public void serialize(File file){
        try {
            FileOutputStream fileOut = new FileOutputStream(file);
            ObjectOutputStream stream = new ObjectOutputStream(fileOut);
            stream.writeInt(STROKE_ATTRIBUTES);
            stream.writeInt(strokes.size());
            for (Stroke s : strokes){
                stream.writeInt(s.points.size());
                stream.writeInt(s.type);
                stream.writeInt(s.paintingBrush.getAlpha());
                stream.writeInt(s.paintingBrush.getColor());
                stream.writeFloat(s.paintingBrush.getStrokeWidth());
                stream.writeInt(s.highlightingBrush.getAlpha());
                stream.writeInt(s.highlightingBrush.getColor());
                stream.writeFloat(s.highlightingBrush.getStrokeWidth());
                stream.writeLong(s.time);
                for (Point p : s.points){
                    stream.writeFloat(p.x);
                    stream.writeFloat(p.y);
                }
            }
            stream.flush();
            stream.close();
        }
        catch(IOException e) {
            Log.e("ERROR", "Error saving strokes: " + e);
        }
    }

    public void deserialize(File file){
        try {
            FileInputStream fileIn = new FileInputStream(file.getAbsolutePath());
            ObjectInputStream stream = new ObjectInputStream(fileIn);

            int numAttrib  = stream.readInt();
            Log.d("INFO", "numAttrib: " + numAttrib);
            int numStrokes = stream.readInt();
            Log.d("INFO", "numStrokes: " + numStrokes);
            for (int s = 0; s < numStrokes; s++) {
                int numPoints = stream.readInt();
                Log.d("INFO", "   numPoints: " + numPoints);
                state = stream.readInt(); // set drawing state
                currentPaint.setAlpha(stream.readInt());
                currentPaint.setColor(stream.readInt());
                currentPaint.setStrokeWidth(stream.readFloat());
                highlightPaint.setAlpha(stream.readInt());
                highlightPaint.setColor(stream.readInt());
                highlightPaint.setStrokeWidth(stream.readFloat());
                long t = stream.readLong();

                startStroke(stream.readFloat(), stream.readFloat(),t);
                // from second stroke, to second-to-last stroke //
                for (int p = 1; p < numPoints-1; p++) {
                    addPoint(stream.readFloat(), stream.readFloat());
                }
                endStroke(stream.readFloat(), stream.readFloat());
                Log.d("INFO", "Done with stroke");
            }

            stream.close();
        }
        catch(IOException e) {
            Log.e("ERROR", "Error loading strokes: " + e);
        }
    }

    private float distance(float x, float y, Point p) {
        return ( (float)Math.sqrt(
                        Math.pow(x-p.x,2)+
                        Math.pow(y-p.y,2) )
        );
    }

    public void deleteLastStroke(){
        int size = strokes.size();
        Stroke stroke = strokes.get( strokes.size()-1 );
        if (size > 0 && stroke.points.get(0).x < 50 && state != INACTIVE) {
            strokes.remove( strokes.size()-1 );
        }
    }

    /**
     * Stroke made by a user while inking
     */
    private class Stroke extends Path {
        final long serialVersionUID = 0L;
        private List<Point> points;
        private int type;
        private Paint paintingBrush;
        private Paint highlightingBrush;
        private long time;
        private Paint brush;    // pointer to current Paint object

        public Stroke(float x, float y, long t, Paint pB, Paint hB) {
            super();
            points = new LinkedList();
            points.add( new Point(x, y) );
            this.moveTo(x,y);
            time = t;
            highlightingBrush = new Paint(hB);
            paintingBrush = new Paint(pB);
            brush = paintingBrush;
        }

        private void setHighlightMode(boolean hi) {
            if (hi) brush = highlightingBrush;
            else brush = paintingBrush;
        }
//
        /**
         * Add point to stroke, add new cubic bezier every 3rd call
         * @param x x coordinate
         * @param y y coordinate
         */
        private void addPoint(float x, float y) {
            points.add(new Point(x, y));
            int size = points.size();
            if (size > 2) {
                Point sub1 = subPoint(points.get(size-2), points.get(size-1));
                Point sub2 = subPoint(points.get(size-1), points.get(size-2));
                cubicTo( points.get(size-2).x, points.get(size-2).y,
                         sub1.x, sub1.y,
                         sub2.x, sub2.y  );
                moveTo(sub2.x, sub2.y);
            }
            else {
                Point p = subPoint(points.get(1),points.get(0));
                lineTo(p.x, p.y);
                moveTo(p.x, p.y);
            }
        }

        /**
         * Calculate new point bisecting line between 2 existing points
         * @param p0 first point
         * @param p1 second point
         * @return new point directly between p0 and p1
         */
        private Point subPoint(Point p0, Point p1) {
            return new Point(
                    (int)((p1.x-p0.x)/2+p0.x),
                    (int)((p1.y-p0.y)/2+p0.y)   );
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (state == INACTIVE) return true;

            float x = motionEvent.getX();
            float y = motionEvent.getY();
            switch (motionEvent.getAction()){
                case(MotionEvent.ACTION_DOWN):
                        startStroke(x, y, System.currentTimeMillis());
                    break;

                case(MotionEvent.ACTION_UP):
                    endStroke(x, y);
                    break;

                case(MotionEvent.ACTION_MOVE):
                    addPoint(x, y);
                    break;

            }
        //return super.onTouchEvent(motionEvent);
        return true;
    }

    /**
     * Recursive object calls itself to highlight each stroke in sequence
     */
    private Runnable highLight = new Runnable() {
        @Override
        public void run() {
            // un-highlight previous stroke //
            if (litStroke > 0) {
                strokes.get(litStroke - 1).setHighlightMode(false);
            }
            // end highlighting if no strokes remain //
            if (litStroke >= strokes.size())
                dynamicHighlighting = false;
            // schedule next highlight //
            if (dynamicHighlighting){
                strokes.get(litStroke).setHighlightMode(true); // highlight current stroke
                invalidate();
                long dt;
                // calculate time to highlight current stroke //
                if (litStroke < strokes.size()-1 && strokes.get(litStroke+1).time < markTf) {
                    dt = strokes.get(litStroke + 1).time - strokes.get(litStroke).time;
                }
                else{
                    dt = markTf - strokes.get(litStroke).time;
                    // end highlighting if this stroke goes until end of highlight period //
                    dynamicHighlighting = false;
                }
                litStroke++;
                highLightHandler.postDelayed(highLight, dt);
            }
            invalidate();
        }
    };
}