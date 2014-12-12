package com.trihedraltutoring.quantumnote;


import android.graphics.Paint;
import android.graphics.Path;

import java.util.LinkedList;
import java.util.List;

/**
 * Stroke made by a user while inking
 */
public class Stroke extends Path {
    public List<Fpoint> points;
    public int type;
    public Paint paintingBrush;
    public Paint highlightingBrush;
    public long time;
    public Paint brush;    // pointer to current Paint object

    public Stroke(float x, float y, long t, Paint pB, Paint hB) {
        super();
        points = new LinkedList();
        points.add( new Fpoint(x, y) );
        this.moveTo(x,y);
        time = t;
        highlightingBrush = new Paint(hB);
        paintingBrush = new Paint(pB);
        brush = paintingBrush;
    }

    public void setHighlightMode(boolean hi) {
        if (hi) brush = highlightingBrush;
        else brush = paintingBrush;
    }
//
    /**
     * Add point to stroke, add new cubic bezier every 3rd call
     * @param x x coordinate
     * @param y y coordinate
     */
    public void addDrawingPoint(float x, float y) {
        points.add(new Fpoint(x, y));
        int size = points.size();
        if (size > 2) {
            Fpoint sub1 = subPoint(points.get(size-2), points.get(size-1));
            Fpoint sub2 = subPoint(points.get(size-1), points.get(size-2));
            cubicTo( points.get(size-2).x, points.get(size-2).y,
                    sub1.x, sub1.y,
                    sub2.x, sub2.y  );
            moveTo(sub2.x, sub2.y);
        }
        else {
            Fpoint p = subPoint(points.get(1),points.get(0));
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
    private Fpoint subPoint(Fpoint p0, Fpoint p1) {
        return new Fpoint(
                (int)((p1.x-p0.x)/2+p0.x),
                (int)((p1.y-p0.y)/2+p0.y)   );
    }
}

