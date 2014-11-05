package com.trihedraltutoring.quantumnote;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
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
    List<Stroke> strokes;
    int yOffset;
    long markT0, markTf;
    Handler highLightHandler;
    int litStroke;

    public InkView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        strokes = new LinkedList();
        highLightHandler = new Handler();
    }

    @Override
    /**
     * Called by system to draw all shapes
     */
    public void onDraw(Canvas canvas) {
        for(Stroke s : strokes) {
            canvas.drawPath(s.path, s.brush);
            float x0 = s.point.get(0).x;
            float y0 = s.point.get(0).y;
            for(Point p : s.point) {
                canvas.drawLine(x0, y0, p.x, p.y, s.brush);
                x0 = p.x;
                y0 = p.y;
            }
        }
    }

    /**
     * Determine and save location of this InkView
     */
    public void setOffset() {
        int[] v = new int[2];
        getLocationOnScreen(v);
        yOffset = v[1];
    }

    /**
     * Add point to current stroke
     * @param x
     * @param y
     */
    public void addPoint(float x, float y) {
        strokes.get(strokes.size()-1).addPoint(x, y - yOffset);
    }

    /**
     * Create new stroke and add it to strokes LinkedList
     * @param x
     * @param y
     */
    public void addStroke(float x, float y) {
        strokes.add( new Stroke(x, y - yOffset, System.currentTimeMillis()) );
    }

    /**
     * Sets time range for tracing
     * @param t0
     * @param t
     */
    public void trace(long t0, long t){
        markT0 = t0;
        markTf = t;
        // locate index of first stroke in t-t0 //
        for(int i=0; i<strokes.size(); i++){
            if (strokes.get(i).time >= markT0){
                litStroke = i;
                break;
            }
        }
        // 1st call to recursive highlighte function //
        long dt = strokes.get(litStroke).time - markT0;
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
     * Stroke made by a user while inking
     */
    private class Stroke {
        public long time;
        public List<Point> point;
        public Path path;
        public Paint brush;

        public Stroke(float x0, float y0, long t0){
            point = new LinkedList();
            path = new Path();
            path.moveTo(x0,y0);
            time = t0;
            // setup default paint object //
            brush = new Paint();
            brush.setColor(Color.BLACK);
            brush.setStyle(Paint.Style.STROKE);
            brush.setStrokeWidth(4);
        }

        /**
         * Add point to stroke, add new bezier every 3rd call
         * @param x
         * @param y
         */
        private void addPoint(float x, float y) {
            point.add(new Point(x, y));
            if (point.size() == 4){
                point.remove(0);
                // add curve to path //
                Point p1 = point.remove(0);
                Point p2 = point.remove(0);
                path.cubicTo(p1.x, p1.y, p2.x, p2.y, x, y);
                /// point now contains only x,y ///
                path.moveTo(x, y);

            }
        }

    }

    /**
     * Recursive function calls itself to highlight each stroke in sequence
     */
    private Runnable highLight = new Runnable() {
        @Override
        public void run() {
            if (litStroke > 0) {
                strokes.get(litStroke-1).brush.setColor(Color.BLACK);
                strokes.get(litStroke-1).brush.setStrokeWidth(4);
            }
            strokes.get(litStroke).brush.setColor(Color.RED);
            strokes.get(litStroke).brush.setStrokeWidth(8);
            invalidate();
            if (litStroke < strokes.size()-1 && strokes.get(litStroke+1).time < markTf) {
                long dt = strokes.get(litStroke + 1).time - strokes.get(litStroke).time;
                litStroke++;
                highLightHandler.postDelayed(highLight, dt);
            }
        }
    };

}