package com.di.marinetracker.backendspringboot.services;

import com.di.marinetracker.backendspringboot.entities.Vessel;
import com.di.marinetracker.backendspringboot.repositories.VesselPositionRepository;
import com.di.marinetracker.backendspringboot.repositories.VesselRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

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
        String expectedMessage = objectMapper.readTree(kafkaMessage).toPrettyString();
        when(vesselRepository.findById(any())).thenReturn(Optional.of(new Vessel("123456789", "Cargo")));

        consumer.consume(kafkaMessage);

        verify(webSocketService).broadcastVesselPosition(eq(kafkaMessage), any(), any());
    }
}