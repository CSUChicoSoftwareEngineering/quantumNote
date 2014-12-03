package com.trihedraltutoring.quantumnote;

/**
 * x,y coordinate floats
 */
public class Point {
    public float x, y;
    public Point(float x1, float y1){
        x = x1;
        y = y1;
    }

    public float distanceTo(Point p){
        return (
                (float)Math.sqrt(
                        Math.pow(x-p.x,2)+
                                Math.pow(y-p.y,2) )
        );
    }
}
