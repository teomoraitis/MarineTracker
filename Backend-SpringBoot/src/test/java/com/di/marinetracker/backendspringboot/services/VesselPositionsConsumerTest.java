package com.di.marinetracker.backendspringboot.services;

import com.di.marinetracker.backendspringboot.entities.Vessel;
import com.di.marinetracker.backendspringboot.entities.VesselPosition;
import com.di.marinetracker.backendspringboot.repositories.VesselPositionRepository;
import com.di.marinetracker.backendspringboot.repositories.VesselRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VesselPositionsConsumerTest {
    @Mock
    VesselRepository vesselRepository;

    @Mock
    VesselPositionRepository vesselPositionRepository;

    @Mock
    WebSocketService webSocketService;

    @InjectMocks
    VesselPositionsConsumer consumer;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testInvalidJson_silentlyIgnored() {
        consumer.consume("{invalid json}");
        verifyNoInteractions(webSocketService);
    }

    @Test
    void testValidJson_sendsMessage() throws Exception {
        String kafkaMessage = "{\"mmsi\":\"123456789\",\"type\":\"Cargo\",\"lat\":0,\"lon\":0,\"speed\":0,\"course\":0,\"status\":0,\"turn\":0,\"heading\":0,\"timestamp\":0}";
        when(vesselRepository.findById(any())).thenReturn(Optional.of(new Vessel("123456789", "Cargo")));

        consumer.consume(kafkaMessage);

        verify(webSocketService).broadcastVesselPosition(eq(kafkaMessage), any(), any());
    }

    @Test
    void testUpdates9MinutesApart_arentSavedTogether() {
        when(vesselRepository.findById(any())).thenReturn(Optional.of(new Vessel("123456789", "Cargo")));
        VesselPosition[] lastOne = new VesselPosition[3];
        // Times: 0, 1 second, 1 minute.
        // lastOne[0] is the latest saved. lastOne[1] is only there because the first update of a vessel never changes.
        // We expect the saved 1-second one to go away and the 1-minute one to be saved.
        lastOne[0] = new VesselPosition(null, 0.0, 0.0, 0.0, 0.0, 0, 0.0, 0, Instant.ofEpochSecond(1));
        lastOne[1] = new VesselPosition(null, 0.0, 0.0, 0.0, 0.0, 0, 0.0, 0, Instant.ofEpochSecond(0));
        Mockito.when(vesselPositionRepository.find2LatestByVesselMmsi("123456789")).thenReturn(Optional.of(lastOne));
        consumer.consume("{\"mmsi\":\"123456789\",\"type\":\"Cargo\",\"lat\":0,\"lon\":0,\"speed\":0,\"course\":0,\"status\":0,\"turn\":0,\"heading\":0,\"timestamp\":600}");
        verify(vesselPositionRepository, times(1)).delete(any());
    }

    @Test
    void testUpdates10MinutesApart_areSavedTogether() {
        when(vesselRepository.findById(any())).thenReturn(Optional.of(new Vessel("123456789", "Cargo")));
        VesselPosition[] lastOne = new VesselPosition[3];
        // 0, 1 second, 1 second + 1 minute + 1 second = 602 seconds
        lastOne[0] = new VesselPosition(null, 0.0, 0.0, 0.0, 0.0, 0, 0.0, 0, Instant.ofEpochSecond(1));
        lastOne[1] = new VesselPosition(null, 0.0, 0.0, 0.0, 0.0, 0, 0.0, 0, Instant.ofEpochSecond(0));
        Mockito.when(vesselPositionRepository.find2LatestByVesselMmsi("123456789")).thenReturn(Optional.of(lastOne));
        consumer.consume("{\"mmsi\":\"123456789\",\"type\":\"Cargo\",\"lat\":0,\"lon\":0,\"speed\":0,\"course\":0,\"status\":0,\"turn\":0,\"heading\":0,\"timestamp\":602}");
        verify(vesselPositionRepository, times(0)).delete(any());
    }
}