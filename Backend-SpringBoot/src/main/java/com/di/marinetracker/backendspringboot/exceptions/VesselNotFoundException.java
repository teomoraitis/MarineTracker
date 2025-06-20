package com.di.marinetracker.backendspringboot.exceptions;

// Maybe not needed as separate exception file/class (for now used a little)
public class VesselNotFoundException extends RuntimeException {
    public VesselNotFoundException(String mmsi) {
        super("Vessel " + mmsi + " not found");
    }
}
