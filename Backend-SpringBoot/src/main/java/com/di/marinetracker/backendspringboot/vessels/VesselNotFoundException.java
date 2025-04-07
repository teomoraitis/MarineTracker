package com.di.marinetracker.backendspringboot.vessels;

public class VesselNotFoundException extends RuntimeException {
    public VesselNotFoundException(String mmsi) {
        super("Vessel " + mmsi + " not found");
    }
}
