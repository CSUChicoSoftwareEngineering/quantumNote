package com.trihedraltutoring.quantumnote;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.util.AttributeSet;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import java.lang.Math;

/**
 * Main View object users interact with
 */
public class InkView extends EditText {
    private List<Stroke> strokes;
    private Paint currentPaint;
    private Paint dynamichighlightPaint;
    private float xOffset, yOffset;
    private float oldWidth = 1;
    private float oldHeight = 1;
    private long markTf;
    private int litStroke;
    private int impactDist = 20;  // distance in pixels that qualifies as an impact
    private Fpoint prevPoint;
    private Fpoint newPoint;
    private boolean newNote = true;
    private Handler highLightHandler;
    public boolean penIsDown = false; // public for frequent external access (recommended by Android)
    private boolean dynamicHighlighting = false;
    public int state = 1;
    private Context context;
    private Configuration config;
    private static final int STROKE_ATTRIBUTES = 3; // for serialization extensibility
    public static final int TYPING = 0;
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
        config = context.getResources().getConfiguration();
        //setSelection(0);
        strokes = new LinkedList();
        highLightHandler = new Handler();
        // setup default paint objects //
        currentPaint = new Paint();
        currentPaint.setColor(Color.BLACK);
        currentPaint.setStyle(Paint.Style.STROKE);
        currentPaint.setStrokeWidth(4);
        dynamichighlightPaint = new Paint();
        dynamichighlightPaint.setColor(Color.RED);
        dynamichighlightPaint.setStyle(Paint.Style.STROKE);
        dynamichighlightPaint.setStrokeWidth(8);
    }

    /**
     * Called by system to draw all shapes
     */
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for(Stroke s : strokes) {
           canvas.drawPath(s, s.brush);
        }
    }

    /**
     * Determine and save location of this InkView on the screen
     */
    public void determineOffset() {
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
        dynamichighlightPaint.setARGB(a, r, g, b);
    }

    public void setHighlightWidth(int w){
        dynamichighlightPaint.setStrokeWidth(w);
    }

    /**
     * Create new stroke and add it to strokes LinkedList
     * @param x x coordinate of first point in stroke
     * @param y y coordinate of first point in stroke
     */
    private void startStroke(float x, float y, long t){
        penIsDown = true;
        Stroke s = new Stroke(x, y, t, currentPaint, dynamichighlightPaint);
        s.type = state;
        strokes.add(s);
    }

    /**
     * Add point to current stroke
     * @param x x coordinate
     * @param y y coordinate
     */
    private void midStroke(float x, float y) {
        if (!penIsDown) return;
        Stroke s = strokes.get( strokes.size()-1 );
        if (state == DRAWING) {
            s.addDrawingPoint(x, y);
        }
        else drawShape(x, y);
        invalidate(); // force re-draw
    }

    private void endStroke(float x, float y){
        if (state == DRAWING)
            strokes.get( strokes.size()-1 ).addDrawingPoint(x, y);
        else
            drawShape(x, y);
        drawShape(x, y);
        saveShape(x, y);
        penIsDown = false;
    }

    private void drawShape(float x, float y) {
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
    }

    private void saveShape(float x, float y){
        if (!penIsDown) return;
        // for most recent stroke //
        Stroke s = strokes.get( strokes.size()-1 );
        // set final point //
        s.points.add( new Fpoint(x, y) );
    }

    private void midErase(float x, float y){
        Fpoint touchPoint = new Fpoint(x, y);
        if (strokes.size()>0) prevPoint = strokes.get(0).points.get(0);
        else return;
        int s = 0;
        Iterator<Stroke> i = strokes.iterator();
        //Log.d("DATA", "Number of strokes: " + strokes.size());
        while (i.hasNext()) {
            //Log.d("DATA", "Checking stroke " + s);
            Stroke str = i.next();
            for (Fpoint pnt : str.points) {
                if (touchPoint.isBetween(pnt, prevPoint, impactDist)) {
                    //Log.d("DATA", "Distance to point: " + touchPoint.distanceToLine(pnt, prevPoint));
                    i.remove();
                    invalidate();
                    return;
                }
                prevPoint = pnt;
            }
            s++;
        }
    }

    public void stopDynamicHighlighting(){
        dynamicHighlighting = false;
    }

    public String getTitle() {
        String title;
        String text = this.getText().toString();
        if (text.equals("")) title = "Untitled Note";
        else{
            int i = 0;
            for ( ; i<text.length(); i++) {
                if (text.charAt(i) == '\n') break;
            }
        title = text.substring(0,i);
        }
        return title;
    }

    public void serialize(File file){
        try {
            FileOutputStream fileOut = new FileOutputStream(file);
            ObjectOutputStream stream = new ObjectOutputStream(fileOut);
            stream.writeInt(STROKE_ATTRIBUTES);
            stream.writeFloat(oldWidth);
            stream.writeFloat(oldHeight);
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
                for (Fpoint p : s.points){
                    stream.writeFloat(p.x);
                    stream.writeFloat(p.y);
                }
            }
            stream.writeInt(state);
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
            newNote = false;

            int numAttrib  = stream.readInt();
            oldWidth = stream.readFloat();
            oldHeight = stream.readFloat();
            int numStrokes = stream.readInt();
            for (int s = 0; s < numStrokes; s++) {
                int numPoints = stream.readInt();
                state = stream.readInt(); // set drawing state
                currentPaint.setAlpha(stream.readInt());
                currentPaint.setColor(stream.readInt());
                currentPaint.setStrokeWidth(stream.readFloat());
                dynamichighlightPaint.setAlpha(stream.readInt());
                dynamichighlightPaint.setColor(stream.readInt());
                dynamichighlightPaint.setStrokeWidth(stream.readFloat());
                long t = stream.readLong();

                startStroke(stream.readFloat(), stream.readFloat(),t);
                // from second point, to second-to-last point //
                for (int p = 1; p < numPoints-1; p++) {
                    midStroke(stream.readFloat(), stream.readFloat());
                }
                endStroke(stream.readFloat(), stream.readFloat());
            }
            state = stream.readInt();
            stream.close();
        }
        catch(IOException e) {
            Log.e("ERROR", "Error loading strokes: " + e);

        }
    }

    // part of a hack to allow app to use a navDrawer //
    public void deleteLastStroke(){
        int size = strokes.size();
        Stroke stroke = strokes.get( strokes.size()-1 );
        if (size > 0 && stroke.points.get(0).x < 50 && state != TYPING) {
            strokes.remove( strokes.size()-1 );
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        if (newNote) {
            int newLines = 500;
            String textStr = "Untitled Note";
            for (int i=0; i<newLines; i++){
                textStr += '\n';
            }
            setText(textStr.toCharArray(), 0, newLines+1);
        }
        newNote = false;
        if (ow == 0){ // if note was just opened or created
            Matrix transMatrix = new Matrix();
            float scale = (float)w/oldWidth;
            transMatrix.setScale(scale, scale, 0, 0);
            for (Stroke s : strokes) {
                s.transform(transMatrix);
                for( Fpoint p : s.points ) {
                    p.x *= scale;
                    p.y *= scale;
                }
            }
            oldWidth = (float)w;
            oldHeight = (float)h;
        }
        super.onSizeChanged(w,h,ow,oh);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (state == TYPING) {
            super.onTouchEvent(motionEvent);
        }

        float x = motionEvent.getX();
        float y = motionEvent.getY();
        switch (motionEvent.getAction()){
            case(MotionEvent.ACTION_DOWN):
                penIsDown = true;
                if (state == ERASING_STROKE){

                }
                else if (state == SELECTING){

                }
                else {
                    startStroke(x, y, System.currentTimeMillis());
                }

                String textStr = getText().toString();
                int newLines = 0;
                for (int i=0; i< textStr.length(); i++) {
                    if (textStr.charAt(i) == '\n')
                        newLines ++;
                }
                if (newLines < 100){
                    CharSequence newChars;
                    //for
                    //append
                }
                Log.d("DATA", "New Lines: " + newLines);
                Log.d("DATA", "ScrollY: " + getScrollY());
                Log.d("DATA", "maxHeight: " + getHeight());


                break;

            case(MotionEvent.ACTION_MOVE):
                if (!penIsDown) break; // hack to prevent invalid ACTION_MOVE events
                if (state == ERASING_STROKE){
                    midErase(x, y);
                }
                else if (state == SELECTING){

                }
                else {
                    midStroke(x, y);
                }
                break;

            case(MotionEvent.ACTION_UP):
                if (state == ERASING_STROKE){

                }
                else if (state == SELECTING){

                }
                else {
                    endStroke(x, y);

                }
                penIsDown = false;
        }
        return true; // return that touch event was handled
    }

    /**
     * Sets time range for and begins dynamic highlighting
     * @param t0 time to start dynamic highlighting
     * @param t time to end dynamic highlighting
     */
    public void startDynamicHighlighting(long t0, long t){
        if (strokes.size() == 0) return;
        dynamicHighlighting = true;
        markTf = t;
        // locate index of first stroke in t-t0 //
        for(int i=0; i<strokes.size(); i++){
            if (strokes.get(i).time >= t0 && strokes.get(i).time < t){
                Log.d("DATA", "Found first stroke to highlight");
                litStroke = i;
                // Schedule 1st call to recursive highlight function //
                long dt = strokes.get(litStroke).time - t0;
                highLightHandler.postDelayed(highLight, dt);
                break;
            }
        }
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
                Log.d("DATA", "highlight time: " + dt);
                litStroke++;
                highLightHandler.postDelayed(highLight, dt);
            }
            invalidate();
        }
    };
}