package com.di.marinetracker.backendspringboot.dto;

import java.util.List;

// DTO used to transfer ZoneOfInterest data between client and server
public class ZoneOfInterestDTO {
    // Polygon represented as WKT (Well-Known Text) which can be parsed into a JTS Polygon
    private String polygonWKT;

    // Zone of Interest restriction #1 (e.g. vessel types)
    private List<String> vesselTypes;

    // Zone of Interest restriction #2 (e.g. maximum allowed vessel speed)
    private Double maxVesselSpeed;

    public String getPolygonWKT() {
        return polygonWKT;
    }

    public void setPolygonWKT(String polygonWKT) {
        this.polygonWKT = polygonWKT;
    }

    public List<String> getVesselTypes() {
        return vesselTypes;
    }

    public void setVesselTypes(List<String> vesselTypes) {
        this.vesselTypes = vesselTypes;
    }

    public Double getMaxVesselSpeed() {
        return maxVesselSpeed;
    }

    public void setMaxVesselSpeed(Double maxVesselSpeed) {
        this.maxVesselSpeed = maxVesselSpeed;
    }
}
