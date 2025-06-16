package com.di.marinetracker.backendspringboot.websockets;

public class VisibleAreaOfSession {
    public double top = 0;
    public double bottom = 0;
    public double leftSide = 0;
    public double rightSide = 0;
    public boolean isWithin(double x, double y) {
        if (x < leftSide) return false;
        if (x > rightSide) return false;
        if (y < top) return false;   // WARNING  this could be the wrong way around
        if (y > bottom) return false;
        return true;
    }
}
