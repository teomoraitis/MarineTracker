package com.di.marinetracker.backendspringboot.dto;


public class VesselDTO {
    private String mmsi;
    private String name;
    private String type;
    private VesselPositionDTO vesselPosition;

    private Boolean inFleet;

    public VesselDTO(String mmsi, String name, String type, VesselPositionDTO vesselPositionDTO, Boolean inFleet) {
        this.mmsi = mmsi;
        this.name = name;
        this.type = type;
        this.vesselPosition = vesselPositionDTO;
        this.inFleet = inFleet;
    }
    public String getMmsi() {
        return mmsi;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public VesselPositionDTO getVesselPosition() {
        return vesselPosition;
    }

    public Boolean getInFleet() {
        return inFleet;
    }
}
