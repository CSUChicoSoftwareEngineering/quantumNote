package com.trihedraltutoring.quantumnote;

import android.util.Log;

/**
 * x,y coordinate floats
 */
public class Fpoint {
    public float x, y, a, b, c, dist;

    public Fpoint(float x1, float y1){
        x = x1;
        y = y1;
    }

    public Fpoint(Fpoint p){
        x = p.x;
        y = p.y;
    }

    public float distanceTo(Fpoint p){
        return (
                (float)Math.sqrt(
                        Math.pow(x-p.x,2)+
                                Math.pow(y-p.y,2) )
        );
    }

    public float cross2D(Fpoint p){
        return (x*p.y)-(y*p.x);
    }

    public Fpoint minus(Fpoint p){
        return new Fpoint(x-p.x, y-p.y);
    }

    public float magnitude(){
        return (
                (float)Math.sqrt(
                        Math.pow(x,2)+
                                Math.pow(y,2) )
        );
    }

    public float distanceToLine(Fpoint p1, Fpoint p2 ){
        c = p1.minus(p2).magnitude();
        if (c == 0)
            return Float.NaN;
        return Math.abs( this.minus(p1).cross2D(this.minus(p2)) )/c;
    }

    public boolean isBetween(Fpoint p1, Fpoint p2, float error ){
        dist = distanceToLine(p1, p2);
        if (dist == Float.NaN) return false;
        if (dist < error){ // if this point lies near the p1 p2 line
            // determine if this point is between p1 and p2 //
            a = this.minus(p1).magnitude();
            b = this.minus(p2).magnitude();
            c = p1.minus(p2).magnitude();
            if (c>a && c>b) {
                return true;
            }
        }
        return false;
    }
}