//package com.di.marinetracker.backendspringboot.services;
//
//import com.di.marinetracker.backendspringboot.repositories.VesselRepository;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//
//import java.util.Optional;
//
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class VesselPositionsConsumerTest {
//    @Mock
//    SimpMessagingTemplate messagingTemplate;
//
//    @Mock
//    VesselRepository vesselRepository;
//
//    @InjectMocks
//    VesselPositionsConsumer consumer;
//
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    @Test
//    void testConsume_validJson_sendsMessage() throws Exception {
//        String kafkaMessage = "{\"mmsi\":\"123456789\",\"type\":\"Cargo\"}";
//        String expectedMessage = objectMapper.readTree(kafkaMessage).toPrettyString();
//        when(vesselRepository.findById(any())).thenReturn(Optional.empty());
//
//        consumer.consume(kafkaMessage);
//
//        verify(messagingTemplate, times(1))
//                .convertAndSend("/topic/locations", expectedMessage);
//    }
//
//    @Test
//    void testConsume_invalidJson_doesNotSend() {
//        String invalidJson = "{invalid json}";
//
//        consumer.consume(invalidJson);
//
//        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
//    }
//}