package com.trihedraltutoring.quantumnote;

import android.util.Log;

/**
 * x,y coordinate floats
 */
public class Fpoint {
    public float x, y;

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
        //Log.d("DATA", "("+x+"-"+p.y+")*("+y+"-"+p.x+")");
        return (x-p.y)*(y-p.x);
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
        Fpoint A = this.minus(p1);
        //Log.d("DATA", "Finger to point 1: " + A.magnitude());
        Fpoint B = this.minus(p2);
        //Log.d("DATA", "Finger to point 2: " + B.magnitude());
        float c = ( p1.minus(p2) ).magnitude();
        //Log.d("DATA", "Point 1 to point 2: " + c);
        if (c == 0) return Float.NaN;
        return(Math.abs( A.cross2D(B) )/c);
    }
}
