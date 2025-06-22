package com.di.marinetracker.backendspringboot.controllers;

import com.di.marinetracker.backendspringboot.entities.Vessel;
import com.di.marinetracker.backendspringboot.entities.VesselPosition;
import com.di.marinetracker.backendspringboot.services.WebSocketService;
import com.fasterxml.jackson.databind.JsonNode;
import org.antlr.v4.runtime.misc.Triple;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

@Component
public class VesselCacheController {
    private final Map<String, JsonNode> mmsiToJsonRespose;
    private final Map<String, VesselPosition> mmsiToVesselPosition;

    VesselCacheController() {
        this.mmsiToJsonRespose = new ConcurrentHashMap<>();
        this.mmsiToVesselPosition = new ConcurrentHashMap<>();
    }

    public JsonNode put(String str, JsonNode data, VesselPosition vesselPosition) {

        mmsiToVesselPosition.put(str,  vesselPosition);
        return mmsiToJsonRespose.put(str,  data);
    }

    public void forEach(BiConsumer<? super String, ? super JsonNode> action) {
        mmsiToJsonRespose.forEach(action);
    }

    public void forEachVesselPosition(BiConsumer<? super String, ? super VesselPosition> action) {
        mmsiToVesselPosition.forEach(action);
    }

}
